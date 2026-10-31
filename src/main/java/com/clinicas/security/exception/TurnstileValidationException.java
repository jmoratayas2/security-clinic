package com.clinicas.security.exception;

public class TurnstileValidationException extends RuntimeException {
    public TurnstileValidationException(String message) {
        super(message);
    }
}
