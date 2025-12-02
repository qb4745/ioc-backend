package com.cambiaso.ioc.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Map;

/**
 * Manejador global de excepciones para el controlador de explicaciones AI.
 *
 * R4: Mapea DashboardAccessDeniedException a HTTP 403 Forbidden
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de acceso denegado a dashboards.
     * Devuelve HTTP 403 con mensaje estructurado.
     */
    @ExceptionHandler(DashboardAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleDashboardAccessDenied(
            DashboardAccessDeniedException ex
    ) {
        log.warn("Dashboard access denied: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of(
                "timestamp", Instant.now(),
                "error", "FORBIDDEN",
                "message", "You don't have permission to access this dashboard",
                "details", ex.getMessage()
            ));
    }

    /**
     * Map ConstraintViolationException (bean/method validation) to HTTP 400 Bad Request
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
            "timestamp", Instant.now(),
            "error", "Bad Request",
            "message", "Invalid parameters",
            "details", ex.getMessage()
        ));
    }

    /**
     * Map DashboardNotFoundException to HTTP 404 Not Found
     */
    @ExceptionHandler(DashboardNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDashboardNotFound(DashboardNotFoundException ex) {
        log.warn("Dashboard not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "timestamp", Instant.now(),
            "error", "Not Found",
            "message", ex.getMessage()
        ));
    }

    /**
     * Fallback handler: unexpected exceptions → 500 with structured body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "timestamp", Instant.now(),
            "error", "Internal Server Error",
            "message", ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()
        ));
    }
}
