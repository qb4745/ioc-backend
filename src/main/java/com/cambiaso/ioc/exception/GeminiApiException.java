package com.cambiaso.ioc.exception;

/**
 * Base exception for Gemini API-related errors.
 */
public class GeminiApiException extends RuntimeException {
    public GeminiApiException(String message) {
        super(message);
    }

    public GeminiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

