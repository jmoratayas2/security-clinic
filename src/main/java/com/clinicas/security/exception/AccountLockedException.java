package com.clinicas.security.exception;

public class AccountLockedException extends RuntimeException {
    private final long minutesRemaining;

    public AccountLockedException(String message, long minutesRemaining) {
        super(message);
        this.minutesRemaining = minutesRemaining;
    }

    public long getMinutesRemaining() {
        return minutesRemaining;
    }
}
