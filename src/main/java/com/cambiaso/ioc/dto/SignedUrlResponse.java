package com.cambiaso.ioc.dto;

/**
 * Simple DTO representing a signed URL response for embedding dashboards.
 */
public record SignedUrlResponse(String signedUrl) {
    public SignedUrlResponse {
        if (signedUrl == null || signedUrl.isBlank()) {
            throw new IllegalArgumentException("Signed URL cannot be null or empty");
        }
    }
}

