package com.clinicas.security.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para registrar intentos fallidos de autenticación y gestionar el bloqueo temporal de cuentas.
 * - Máximo de intentos fallidos permitidos: 5.
 * - Duración del bloqueo temporal: 15 minutos.
 */
@Service
public class LoginAttemptService {

    private final int maxFailedAttempts;
    private final int lockDurationMinutes;

    private final Map<String, AttemptData> attemptsCache = new ConcurrentHashMap<>();

    public LoginAttemptService(
            @Value("${security.login.max-failed-attempts:5}") int maxFailedAttempts,
            @Value("${security.login.lock-duration-minutes:15}") int lockDurationMinutes) {
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockDurationMinutes = lockDurationMinutes;
    }

    private static class AttemptData {
        int failedAttempts = 0;
        Instant lockedUntil = null;
        Instant lastAttempt = Instant.now();
    }

    /**
     * Verifica si la cuenta asociada al usuario se encuentra actualmente bloqueada.
     * Si el tiempo de bloqueo ya expiró, reinicia el estado y permite reintentar.
     */
    public synchronized boolean isBlocked(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        String key = username.trim().toLowerCase();
        AttemptData data = attemptsCache.get(key);
        if (data == null || data.lockedUntil == null) {
            return false;
        }

        Instant now = Instant.now();
        if (now.isBefore(data.lockedUntil)) {
            return true;
        }

        // El período de bloqueo expiró, limpiar estado
        attemptsCache.remove(key);
        return false;
    }

    /**
     * Registra un intento fallido de autenticación para el usuario.
     * Si alcanza el límite de 5 fallos consecutivos, bloquea la cuenta por 15 minutos.
     * @return El número actual de intentos fallidos consecutivos
     */
    public synchronized int recordFailedAttempt(String username) {
        if (username == null || username.isBlank()) {
            return 1;
        }
        String key = username.trim().toLowerCase();
        AttemptData data = attemptsCache.computeIfAbsent(key, k -> new AttemptData());

        // Si ya había expirado un bloqueo anterior
        if (data.lockedUntil != null && Instant.now().isAfter(data.lockedUntil)) {
            data.failedAttempts = 0;
            data.lockedUntil = null;
        }

        data.failedAttempts++;
        data.lastAttempt = Instant.now();

        if (data.failedAttempts >= maxFailedAttempts) {
            data.lockedUntil = Instant.now().plus(lockDurationMinutes, ChronoUnit.MINUTES);
        }

        return data.failedAttempts;
    }

    /**
     * Registra una autenticación exitosa, reseteando los contadores para este usuario.
     */
    public synchronized void recordSuccess(String username) {
        if (username != null) {
            attemptsCache.remove(username.trim().toLowerCase());
        }
    }

    /**
     * Retorna los minutos restantes del bloqueo temporal.
     */
    public synchronized long getMinutesRemaining(String username) {
        if (username == null) return 0L;
        AttemptData data = attemptsCache.get(username.trim().toLowerCase());
        if (data == null || data.lockedUntil == null) return 0L;
        Instant now = Instant.now();
        if (now.isAfter(data.lockedUntil)) return 0L;
        long seconds = Duration.between(now, data.lockedUntil).getSeconds();
        return Math.max(1L, (seconds + 59L) / 60L);
    }

    /**
     * Retorna cuántos intentos fallidos lleva acumulados el usuario.
     */
    public synchronized int getFailedAttempts(String username) {
        if (username == null) return 0;
        AttemptData data = attemptsCache.get(username.trim().toLowerCase());
        return data != null ? data.failedAttempts : 0;
    }

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public int getLockDurationMinutes() {
        return lockDurationMinutes;
    }
}
