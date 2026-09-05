package com.clinicas.security.dto.auth;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
