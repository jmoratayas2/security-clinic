package com.clinicas.security.service;

import com.clinicas.security.dto.auth.LoginRequest;
import com.clinicas.security.dto.auth.LoginResponse;
import com.clinicas.security.exception.AccountLockedException;
import com.clinicas.security.security.JwtService;
import com.clinicas.security.security.LoginAttemptService;
import com.clinicas.security.security.TurnstileVerificationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final TurnstileVerificationService turnstileVerificationService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            LoginAttemptService loginAttemptService,
            TurnstileVerificationService turnstileVerificationService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.turnstileVerificationService = turnstileVerificationService;
    }

    public LoginResponse login(LoginRequest request, String clientIp) {
        // 1. Validar CAPTCHA / Cloudflare Turnstile antes de procesar credenciales
        turnstileVerificationService.verify(request.turnstileToken(), clientIp);

        String username = request.username() != null ? request.username().trim() : "";

        // 2. Control de Bloqueo Temporal: Verificar si la cuenta superó los 5 intentos y está en período de 15 min
        if (loginAttemptService.isBlocked(username)) {
            long remainingMinutes = loginAttemptService.getMinutesRemaining(username);
            throw new AccountLockedException(
                    "La cuenta está temporalmente bloqueada por 15 minutos debido a 5 intentos fallidos consecutivos. " +
                    "Tiempo restante: " + remainingMinutes + " minuto(s).",
                    remainingMinutes
            );
        }

        // 3. Intento de autenticación
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password()));

            // Autenticación exitosa: reiniciar contador de fallos
            loginAttemptService.recordSuccess(username);

            return new LoginResponse(jwtService.generate(authentication), "Bearer", jwtService.expirationSeconds());
        } catch (AuthenticationException ex) {
            // Contraseña incorrecta o credenciales no válidas
            int currentFails = loginAttemptService.recordFailedAttempt(username);

            if (currentFails >= loginAttemptService.getMaxFailedAttempts()) {
                throw new AccountLockedException(
                        "Ha alcanzado el límite de 5 intentos fallidos consecutivos. " +
                        "La cuenta ha sido bloqueada temporalmente durante 15 minutos.",
                        loginAttemptService.getLockDurationMinutes()
                );
            }

            int remaining = loginAttemptService.getMaxFailedAttempts() - currentFails;
            throw new BadCredentialsException(
                    "Credenciales invalidas. Intento fallido " + currentFails + " de " +
                    loginAttemptService.getMaxFailedAttempts() + " (quedan " + remaining + " intentos antes del bloqueo temporal)."
            );
        }
    }
}
