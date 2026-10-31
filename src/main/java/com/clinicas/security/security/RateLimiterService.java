package com.clinicas.security.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de Rate Limiting en memoria usando Sliding Window Log por IP.
 * Controla un máximo de peticiones por minuto (por defecto 10 req/min).
 */
@Service
public class RateLimiterService {

    private final int maxRequestsPerMinute;
    private final Map<String, Deque<Long>> requestLogs = new ConcurrentHashMap<>();

    public RateLimiterService(@Value("${security.ratelimit.requests-per-minute:10}") int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }

    /**
     * Intenta registrar una solicitud para la IP dada.
     * @param clientIp IP del cliente
     * @return true si la solicitud es aceptada; false si excede el límite
     */
    public synchronized boolean tryAcquire(String clientIp) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - 60_000L; // 60 segundos atrás

        Deque<Long> timestamps = requestLogs.computeIfAbsent(clientIp, k -> new ArrayDeque<>());

        // Remover solicitudes fuera de la ventana de 60 segundos
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxRequestsPerMinute) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    /**
     * Retorna cuántas peticiones le restan en la ventana actual.
     */
    public synchronized int getRemainingRequests(String clientIp) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - 60_000L;
        Deque<Long> timestamps = requestLogs.get(clientIp);
        if (timestamps == null) {
            return maxRequestsPerMinute;
        }
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
            timestamps.pollFirst();
        }
        return Math.max(0, maxRequestsPerMinute - timestamps.size());
    }

    /**
     * Retorna cuántos segundos debe esperar el cliente antes de que expire la solicitud más vieja.
     */
    public synchronized long getSecondsUntilNextAvailable(String clientIp) {
        Deque<Long> timestamps = requestLogs.get(clientIp);
        if (timestamps == null || timestamps.isEmpty()) {
            return 0L;
        }
        long oldest = timestamps.peekFirst();
        long expireTime = oldest + 60_000L;
        long diff = expireTime - Instant.now().toEpochMilli();
        return Math.max(1L, (diff + 999L) / 1000L);
    }

    public int getMaxRequestsPerMinute() {
        return maxRequestsPerMinute;
    }
}
