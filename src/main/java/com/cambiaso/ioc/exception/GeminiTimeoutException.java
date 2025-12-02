package com.cambiaso.ioc.exception;

/**
 * Exception thrown when Gemini API request exceeds the configured timeout.
 */
public class GeminiTimeoutException extends GeminiApiException {

    public GeminiTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

