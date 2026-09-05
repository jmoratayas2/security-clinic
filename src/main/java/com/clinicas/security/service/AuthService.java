package com.clinicas.security.service;

import com.clinicas.security.dto.auth.LoginRequest;
import com.clinicas.security.dto.auth.LoginResponse;
import com.clinicas.security.security.JwtService;
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

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            return new LoginResponse(jwtService.generate(authentication), "Bearer", jwtService.expirationSeconds());
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Credenciales invalidas");
        }
    }
}
