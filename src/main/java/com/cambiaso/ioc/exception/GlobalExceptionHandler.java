package com.cambiaso.ioc.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.OffsetDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JobConflictException.class)
    public ResponseEntity<Map<String, Object>> handleJobConflict(JobConflictException ex) {
        log.warn("Job conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<Map<String, Object>> handleFileValidation(FileValidationException ex) {
        log.error("File validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(createErrorResponse("VALIDATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(createErrorResponse("INVALID_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("File size exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(createErrorResponse("FILE_TOO_LARGE", "File size exceeds maximum allowed limit"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        return Map.of(
                "error", errorCode,
                "message", message,
                "timestamp", OffsetDateTime.now()
        );
    }
}
