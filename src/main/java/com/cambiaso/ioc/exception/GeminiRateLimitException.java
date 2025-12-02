package com.cambiaso.ioc.exception;

/**
 * Exception thrown when Gemini API returns 429 (Too Many Requests).
 * Indicates that the free tier quota has been exceeded.
 */
public class GeminiRateLimitException extends GeminiApiException {

    public GeminiRateLimitException(String message) {
        super(message);
    }
}
