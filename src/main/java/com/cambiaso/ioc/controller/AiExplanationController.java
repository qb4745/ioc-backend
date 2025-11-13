package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.dto.ai.DashboardExplanationResponse;
import com.cambiaso.ioc.exception.GeminiApiException;
import com.cambiaso.ioc.exception.GeminiRateLimitException;
import com.cambiaso.ioc.exception.GeminiTimeoutException;
import com.cambiaso.ioc.service.ai.DashboardExplanationService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Controlador REST para explicaciones de dashboard generadas por IA.
 *
 * BSS-004: AiExplanationController
 * Feature: FP-001A - Dashboard AI Explanation
 *
 * Endpoints:
 * - POST /api/v1/ai/explain - Genera explicación para un dashboard (con body JSON)
 * - GET /api/v1/ai/explain/{dashboardId} - Genera explicación con query params
 *
 * Seguridad:
 * - Requiere autenticación (Spring Security)
 * - Rate limiting: 10 requests/min por usuario
 * - Timeout: 90 segundos
 *
 * Manejo de errores:
 * - 400 Bad Request: Validaciones fallidas (rango de fechas, dashboard inválido)
 * - 408 Request Timeout: Gemini API timeout (>90s)
 * - 429 Too Many Requests: Rate limit de Gemini API
 * - 500 Internal Server Error: Errores inesperados
 * - 503 Service Unavailable: Gemini API no disponible
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AiExplanationController {

    private final DashboardExplanationService explanationService;

    /**
     * Genera explicación ejecutiva de un dashboard usando IA (POST con body JSON).
     *
     * Request body ejemplo:
     * {
     *   "dashboardId": 5,
     *   "fechaInicio": "2025-06-01",
     *   "fechaFin": "2025-06-30",
     *   "filtros": {"turno": "DIA"}
     * }
     *
     * @param request DTO con dashboardId, rango de fechas y filtros opcionales
     * @param authentication Usuario autenticado (inyectado por Spring Security)
     * @return Respuesta con resumen ejecutivo, key points, insights y alertas
     */
    @PostMapping("/explain")
    @RateLimiter(name = "aiExplanation")
    public ResponseEntity<DashboardExplanationResponse> explainDashboard(
            @Valid @RequestBody DashboardExplanationRequest request,
            Authentication authentication
    ) {
        log.info("AI explanation requested via POST - User: {}, Dashboard: {}, Range: {} to {}",
            authentication.getName(),
            request.dashboardId(),
            request.fechaInicio(),
            request.fechaFin());

        try {
            DashboardExplanationResponse response = explanationService.explainDashboard(request);

            log.info("AI explanation generated successfully - Dashboard: {}, FromCache: {}, Tokens: {}",
                request.dashboardId(),
                response.fromCache(),
                response.tokensUsados());

            return ResponseEntity.ok(response);

        } catch (GeminiTimeoutException e) {
            log.error("Gemini API timeout for dashboard {} - User: {}",
                request.dashboardId(), authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .body(createErrorResponse(request, "Request timeout - try again later"));

        } catch (GeminiRateLimitException e) {
            log.warn("Gemini API rate limit hit for dashboard {} - User: {}",
                request.dashboardId(), authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(createErrorResponse(request, "Rate limit exceeded - please wait"));

        } catch (GeminiApiException e) {
            log.error("Gemini API error for dashboard {} - User: {}",
                request.dashboardId(), authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createErrorResponse(request, "AI service temporarily unavailable"));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for dashboard {} - User: {} - Error: {}",
                request.dashboardId(), authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(request, e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error generating explanation for dashboard {} - User: {}",
                request.dashboardId(), authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(request, "Internal server error"));
        }
    }

    /**
     * Genera explicación ejecutiva de un dashboard usando query parameters (GET).
     *
     * Útil para integraciones simples sin body JSON.
     * Los filtros se pasan como query params adicionales y se agrupan en un Map.
     *
     * Ejemplo:
     * GET /api/v1/ai/explain/5?fechaInicio=2025-06-01&fechaFin=2025-06-30&turno=DIA
     *
     * @param dashboardId ID del dashboard (1-999999)
     * @param fechaInicio Fecha inicial del rango (formato ISO: yyyy-MM-dd)
     * @param fechaFin Fecha final del rango (formato ISO: yyyy-MM-dd)
     * @param allParams Todos los query params (Spring los captura automáticamente)
     * @param authentication Usuario autenticado
     * @return Respuesta con explicación del dashboard
     */
    @GetMapping("/explain/{dashboardId}")
    @RateLimiter(name = "aiExplanation")
    public ResponseEntity<DashboardExplanationResponse> explainDashboardGet(
            @PathVariable
            @Min(value = 1, message = "Dashboard ID must be at least 1")
            @Max(value = 999999, message = "Dashboard ID must be at most 999999")
            Integer dashboardId,

            @RequestParam
            @NotNull(message = "fechaInicio is required")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam
            @NotNull(message = "fechaFin is required")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin,

            @RequestParam(required = false) Map<String, String> allParams,

            Authentication authentication
    ) {
        log.info("AI explanation requested via GET - User: {}, Dashboard: {}, Range: {} to {}",
            authentication.getName(), dashboardId, fechaInicio, fechaFin);

        // Extraer filtros adicionales (todos los params excepto los conocidos)
        Map<String, String> filtros = extractFilters(allParams);

        // Construir request DTO
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            dashboardId,
            fechaInicio,
            fechaFin,
            filtros
        );

        // Reutilizar la lógica del POST
        return explainDashboard(request, authentication);
    }

    /**
     * Health check endpoint para verificar disponibilidad del servicio de IA.
     *
     * @return Status 200 si el servicio está disponible
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "service", "AI Explanation Service",
            "status", "UP",
            "timestamp", LocalDate.now()
        ));
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Extrae filtros adicionales de los query parameters.
     * Excluye parámetros conocidos (dashboardId, fechaInicio, fechaFin).
     *
     * @param allParams Todos los query parameters
     * @return Map con solo los filtros personalizados
     */
    private Map<String, String> extractFilters(Map<String, String> allParams) {
        if (allParams == null) {
            return Map.of();
        }

        return allParams.entrySet().stream()
            .filter(entry -> !entry.getKey().equals("fechaInicio"))
            .filter(entry -> !entry.getKey().equals("fechaFin"))
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }

    /**
     * Crea una respuesta de error estructurada.
     *
     * @param request Request original
     * @param errorMessage Mensaje de error
     * @return Response con estructura de error
     */
    private DashboardExplanationResponse createErrorResponse(
            DashboardExplanationRequest request,
            String errorMessage
    ) {
        return new DashboardExplanationResponse(
            errorMessage,
            java.util.List.of(),
            java.util.List.of(),
            java.util.List.of("Error: " + errorMessage),
            request.dashboardId(),
            "Dashboard " + request.dashboardId(), // dashboardName
            request.fechaInicio(),
            request.fechaFin(),
            request.filtros(),
            java.time.Instant.now(),
            false, // fromCache
            0, // tokensUsados
            300 // cacheTTL mínimo para errores
        );
    }

    /**
     * Maneja excepciones de validación a nivel de controlador.
     *
     * @param ex Excepción de validación
     * @return Respuesta 400 con detalles del error
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new java.util.HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation errors: {}", errors);

        return ResponseEntity.badRequest().body(Map.of(
            "error", "Validation failed",
            "details", errors,
            "timestamp", java.time.Instant.now()
        ));
    }

    /**
     * Maneja excepciones de validación de constraints.
     *
     * @param ex Excepción de constraint
     * @return Respuesta 400 con mensaje de error
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex
    ) {
        log.warn("Constraint violation: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(Map.of(
            "error", "Invalid parameters",
            "message", ex.getMessage(),
            "timestamp", java.time.Instant.now()
        ));
    }

    /**
     * Maneja excepciones de parámetros faltantes en requests.
     *
     * @param ex Excepción de parámetro faltante
     * @return Respuesta 400 con mensaje de error
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParams(
            org.springframework.web.bind.MissingServletRequestParameterException ex
    ) {
        log.warn("Missing required parameter: {}", ex.getParameterName());

        return ResponseEntity.badRequest().body(Map.of(
            "error", "Invalid parameters",
            "message", "Required parameter '" + ex.getParameterName() + "' is missing",
            "timestamp", java.time.Instant.now()
        ));
    }
}
