package com.clinicas.security.service;

import com.clinicas.security.dto.auth.LoginRequest;
import com.clinicas.security.dto.auth.LoginResponse;
import com.clinicas.security.entity.IntentoLogin;
import com.clinicas.security.entity.Usuario;
import com.clinicas.security.exception.AccountLockedException;
import com.clinicas.security.repository.IntentoLoginRepository;
import com.clinicas.security.repository.UsuarioRepository;
import com.clinicas.security.security.JwtService;
import com.clinicas.security.security.LoginAttemptService;
import com.clinicas.security.security.TurnstileVerificationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final TurnstileVerificationService turnstileVerificationService;
    private final IntentoLoginRepository intentoLoginRepository;
    private final UsuarioRepository usuarioRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            LoginAttemptService loginAttemptService,
            TurnstileVerificationService turnstileVerificationService,
            IntentoLoginRepository intentoLoginRepository,
            UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.turnstileVerificationService = turnstileVerificationService;
        this.intentoLoginRepository = intentoLoginRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public LoginResponse login(LoginRequest request, String clientIp) {
        String username = request.username() != null ? request.username().trim() : "";

        // 1. Validar CAPTCHA / Cloudflare Turnstile antes de procesar credenciales
        try {
            turnstileVerificationService.verify(request.turnstileToken(), clientIp);
        } catch (Exception ex) {
            registrarIntentoEnBd(username, clientIp, false, "CAPTCHA_FAILED: " + ex.getMessage(), null, null);
            throw ex;
        }

        // 2. Control de Bloqueo Temporal: Verificar si la cuenta superó los 5 intentos y está en período de 15 min
        if (loginAttemptService.isBlocked(username)) {
            long remainingMinutes = loginAttemptService.getMinutesRemaining(username);
            LocalDateTime bloqueadoHasta = LocalDateTime.now().plusMinutes(remainingMinutes);
            registrarIntentoEnBd(username, clientIp, false, "CUENTA_BLOQUEADA_15MIN", 5, bloqueadoHasta);

            throw new AccountLockedException(
                    "La cuenta está temporalmente bloqueada por 15 minutos debido a 5 intentos fallidos consecutivos. " +
                    "Tiempo restante: " + remainingMinutes + " minuto(s).",
                    remainingMinutes
            );
        }

        // 3. Intento de autenticación de credenciales
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password()));

            // Autenticación exitosa: reiniciar contador de fallos
            loginAttemptService.recordSuccess(username);

            // Persistir éxito en auditoría y limpiar bloqueo en entidad Usuario
            registrarIntentoEnBd(username, clientIp, true, "LOGIN_EXITOSO", 0, null);
            usuarioRepository.findByUsername(username).ifPresent(u -> {
                u.setIntentosFallidos(0);
                u.setBloqueadoHasta(null);
                usuarioRepository.save(u);
            });

            return new LoginResponse(jwtService.generate(authentication), "Bearer", jwtService.expirationSeconds());
        } catch (AuthenticationException ex) {
            // Contraseña incorrecta o credenciales no válidas
            int currentFails = loginAttemptService.recordFailedAttempt(username);
            LocalDateTime bloqueadoHasta = currentFails >= loginAttemptService.getMaxFailedAttempts()
                    ? LocalDateTime.now().plusMinutes(loginAttemptService.getLockDurationMinutes())
                    : null;

            // Actualizar contador y bloqueo en la entidad Usuario en base de datos
            usuarioRepository.findByUsername(username).ifPresent(u -> {
                u.setIntentosFallidos(currentFails);
                u.setBloqueadoHasta(bloqueadoHasta);
                usuarioRepository.save(u);
            });

            if (currentFails >= loginAttemptService.getMaxFailedAttempts()) {
                registrarIntentoEnBd(username, clientIp, false, "BLOQUEO_ACTIVADO_5_FALLOS", currentFails, bloqueadoHasta);
                throw new AccountLockedException(
                        "Ha alcanzado el límite de 5 intentos fallidos consecutivos. " +
                        "La cuenta ha sido bloqueada temporalmente durante 15 minutos.",
                        loginAttemptService.getLockDurationMinutes()
                );
            }

            int remaining = loginAttemptService.getMaxFailedAttempts() - currentFails;
            registrarIntentoEnBd(username, clientIp, false, "PASSWORD_INCORRECTA (Intento " + currentFails + "/5)", currentFails, null);

            throw new BadCredentialsException(
                    "Credenciales invalidas. Intento fallido " + currentFails + " de " +
                    loginAttemptService.getMaxFailedAttempts() + " (quedan " + remaining + " intentos antes del bloqueo temporal)."
            );
        }
    }

    private void registrarIntentoEnBd(String username, String ip, boolean exitoso, String resultado,
                                      Integer intentos, LocalDateTime bloqueadoHasta) {
        try {
            IntentoLogin intento = new IntentoLogin(username, ip, exitoso, resultado, intentos, bloqueadoHasta);
            intentoLoginRepository.save(intento);
        } catch (Exception e) {
            // No interrumpir el flujo si la base de datos de auditoría falla
        }
    }
}
