package com.clinicas.security.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Filtro que aplica Rate Limiting al endpoint de autenticación (/api/auth/login).
 * Responde con HTTP 429 - Too Many Requests al exceder 10 peticiones por minuto.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    public RateLimitFilter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        // Aplicar al endpoint de login (tanto POST como preflight OPTIONS se dejan pasar)
        if (path.endsWith("/api/auth/login") && "POST".equalsIgnoreCase(request.getMethod())) {
            String clientIp = extractClientIp(request);

            if (!rateLimiterService.tryAcquire(clientIp)) {
                long retryAfter = rateLimiterService.getSecondsUntilNextAvailable(clientIp);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Retry-After", String.valueOf(retryAfter));
                response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimiterService.getMaxRequestsPerMinute()));
                response.setHeader("X-RateLimit-Remaining", "0");

                String json = String.format(
                        "{\"status\":429,\"error\":\"TOO_MANY_REQUESTS\"," +
                        "\"message\":\"Demasiadas solicitudes. Limite excedido: %d peticiones por minuto.\"," +
                        "\"timestamp\":\"%s\"," +
                        "\"details\":[\"Espere %d segundos antes de intentar nuevamente.\"]}",
                        rateLimiterService.getMaxRequestsPerMinute(),
                        LocalDateTime.now(),
                        retryAfter
                );
                response.getWriter().write(json);
                return;
            }

            int remaining = rateLimiterService.getRemainingRequests(clientIp);
            response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimiterService.getMaxRequestsPerMinute()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isBlank()) {
            return cfConnectingIp.trim();
        }
        return request.getRemoteAddr();
    }
}
