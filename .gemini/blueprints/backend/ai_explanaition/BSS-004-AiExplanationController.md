# Backend Service Specification: AiExplanationController

## Metadata
- **BSS ID**: BSS-004
- **Technical Design**: TD-001A (Dashboard AI Explanation)
- **Feature Plan**: FP-001A
- **Clase**: `AiExplanationController`
- **Tipo**: REST Controller
- **Package**: `com.cambiaso.ioc.controller`
- **Sprint**: Sprint 2
- **Fecha Creación**: 2025-11-12
- **Estado**: DRAFT

---

## 1. Propósito y Responsabilidades

### 1.1. Propósito
Controller REST que expone el endpoint público para generar explicaciones de dashboards usando IA. Maneja autenticación, autorización, validación de inputs, rate limiting y mapeo de errores a respuestas HTTP apropiadas.

### 1.2. Responsabilidades
- Exponer endpoint `POST /api/v1/ai/explain-dashboard`
- Validar JWT token y extraer usuario autenticado
- Verificar autorización (acceso al dashboard solicitado)
- Validar request DTO con Bean Validation
- Aplicar rate limiting (5 requests por minuto por usuario)
- Delegar lógica de negocio a `DashboardExplanationService`
- Mapear excepciones a códigos HTTP apropiados
- Retornar respuestas JSON estandarizadas
- Logging de requests y errores

### 1.3. Ubicación en Arquitectura
```
Frontend (React)
      ↓ HTTP POST
[AiExplanationController] ← JWT Filter, Rate Limiter
      ↓
DashboardExplanationService
```

---

## 2. Interfaz Pública (API Contract)

### 2.1. Endpoint Specification

**URL**: `POST /api/v1/ai/explain-dashboard`

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "dashboardId": 5,
  "fechaInicio": "2025-06-01",
  "fechaFin": "2025-06-30",
  "filtros": {
    "turno": "Día"
  }
}
```

**Response 200 OK**:
```json
{
  "resumenEjecutivo": "La producción en junio 2025 mostró un desempeño sólido...",
  "keyPoints": [
    "Producción total: 45,234 unidades",
    "Eficiencia promedio: 94.5%",
    "Top operario: Juan Pérez con 5,432 unidades"
  ],
  "insightsAccionables": [
    "Considerar reforzar turno noche que muestra 8% menos eficiencia",
    "Máquina M-003 requiere mantenimiento preventivo"
  ],
  "alertas": [
    "Incremento de defectos en últimos 7 días (+12%)"
  ],
  "metadata": {
    "dashboardId": 5,
    "titulo": "Producción por Operario - Mensual",
    "fechaInicio": "2025-06-01",
    "fechaFin": "2025-06-30",
    "filtrosAplicados": {
      "turno": "Día"
    }
  },
  "generadoAt": "2025-11-12T10:30:45Z",
  "fromCache": false,
  "tokensUsados": 1245,
  "cacheTTLSeconds": 1800
}
```

**Error Responses**:

| Status | Código Error | Mensaje Ejemplo |
|--------|--------------|-----------------|
| 400 | VALIDATION_ERROR | "fechaFin debe ser mayor o igual a fechaInicio" |
| 401 | UNAUTHORIZED | "Token JWT inválido o expirado" |
| 403 | FORBIDDEN | "No tienes permisos para acceder al dashboard 5" |
| 429 | RATE_LIMIT_EXCEEDED | "Máximo 5 explicaciones por minuto. Intenta en 45 segundos." |
| 500 | INTERNAL_ERROR | "Error interno del servidor" |
| 503 | AI_SERVICE_UNAVAILABLE | "Servicio de IA temporalmente no disponible" |
| 504 | AI_TIMEOUT | "La generación de explicación excedió el tiempo límite" |

---

## 3. Implementación Completa

### 3.1. Controller Class

```java
package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.dto.ai.DashboardExplanationResponse;
import com.cambiaso.ioc.dto.ErrorResponse;
import com.cambiaso.ioc.exception.*;
import com.cambiaso.ioc.service.ai.DashboardExplanationService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para endpoints de explicaciones de dashboards con IA.
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Explanations", description = "Endpoints para generar explicaciones de dashboards usando IA")
public class AiExplanationController {

    private final DashboardExplanationService explanationService;

    /**
     * Genera una explicación ejecutiva del dashboard usando IA (Gemini).
     * 
     * Rate Limit: 5 requests por minuto por usuario.
     * Timeout: Máximo 90 segundos para generación.
     * Cache: Respuestas cacheadas con TTL dinámico (30min - 24h).
     * 
     * @param request DTO con dashboardId, rango de fechas y filtros opcionales
     * @param authentication Usuario autenticado (inyectado por Spring Security)
     * @return Explicación generada con resumen, insights y alertas
     */
    @PostMapping("/explain-dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @RateLimiter(name = "aiExplanation", fallbackMethod = "rateLimitFallback")
    @Operation(
        summary = "Generar explicación de dashboard con IA",
        description = "Genera un análisis ejecutivo del dashboard usando datos agregados y Gemini AI",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Explicación generada exitosamente",
                content = @Content(schema = @Schema(implementation = DashboardExplanationResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validación de request fallida",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "No autenticado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Sin permisos para acceder al dashboard",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "429",
                description = "Rate limit excedido",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "503",
                description = "Servicio de IA no disponible",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "504",
                description = "Timeout generando explicación",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
        }
    )
    public ResponseEntity<DashboardExplanationResponse> explainDashboard(
            @Valid @RequestBody DashboardExplanationRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Received AI explanation request from user: {}, dashboard: {}, range: {} to {}", 
            username, request.dashboardId(), request.fechaInicio(), request.fechaFin());
        
        // Validar acceso al dashboard (opcional: verificar que el usuario tenga permisos)
        // validateDashboardAccess(request.dashboardId(), authentication);
        
        // Delegar a servicio
        DashboardExplanationResponse response = explanationService.explainDashboard(request);
        
        log.info("AI explanation generated successfully for user: {}, dashboard: {}, fromCache: {}", 
            username, request.dashboardId(), response.fromCache());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Fallback cuando se excede el rate limit.
     */
    public ResponseEntity<ErrorResponse> rateLimitFallback(
            DashboardExplanationRequest request,
            Authentication authentication,
            Exception e) {
        
        log.warn("Rate limit exceeded for user: {}", authentication.getName());
        
        ErrorResponse error = new ErrorResponse(
            "RATE_LIMIT_EXCEEDED",
            "Has excedido el límite de 5 explicaciones por minuto. Por favor intenta nuevamente en unos segundos.",
            Instant.now(),
            "/api/v1/ai/explain-dashboard",
            Map.of("retryAfter", "60")
        );
        
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .header("Retry-After", "60")
            .body(error);
    }
    
    /**
     * Valida que el usuario tenga acceso al dashboard solicitado.
     * (Implementación específica según reglas de negocio)
     */
    private void validateDashboardAccess(int dashboardId, Authentication authentication) {
        // TODO: Implementar lógica de autorización granular
        // Opciones:
        // 1. Verificar en tabla de permisos dashboard_access
        // 2. Reutilizar lógica de MetabaseEmbeddingService
        // 3. Validar contra roles permitidos por dashboard
        
        // Ejemplo simplificado:
        // if (!dashboardAccessService.canAccess(dashboardId, authentication.getName())) {
        //     throw new ForbiddenException("No tienes permisos para acceder al dashboard " + dashboardId);
        // }
    }
}
```

---

## 4. Exception Handling

### 4.1. Global Exception Handler

```java
package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.dto.ErrorResponse;
import com.cambiaso.ioc.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para el controller de AI.
 */
@RestControllerAdvice(assignableTypes = AiExplanationController.class)
@Slf4j
public class AiExplanationExceptionHandler {

    /**
     * Maneja errores de validación de Bean Validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation error: {}", errors);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Errores de validación en el request",
            Instant.now(),
            request.getDescription(false).replace("uri=", ""),
            errors
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * Maneja errores de validación de negocio (rango de fechas, etc.).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {
        
        log.warn("Business validation error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            Instant.now(),
            request.getDescription(false).replace("uri=", ""),
            null
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * Maneja errores de timeout de Gemini.
     */
    @ExceptionHandler(GeminiTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleGeminiTimeout(
            GeminiTimeoutException ex,
            WebRequest request) {
        
        log.error("Gemini timeout: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "AI_TIMEOUT",
            "La generación de la explicación tardó demasiado tiempo. Por favor intenta con un rango de fechas menor.",
            Instant.now(),
            request.getDescription(false).replace("uri=", ""),
            Map.of("suggestion", "Reduce el rango de fechas o intenta nuevamente")
        );
        
        return ResponseEntity
            .status(HttpStatus.GATEWAY_TIMEOUT)
            .body(errorResponse);
    }
    
    /**
     * Maneja errores de rate limit de Gemini API.
     */
    @ExceptionHandler(GeminiRateLimitException.class)
    public ResponseEntity<ErrorResponse> handleGeminiRateLimit(
            GeminiRateLimitException ex,
            WebRequest request) {
        
        log.error("Gemini rate limit exceeded: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "AI_SERVICE_UNAVAILABLE",
            "El servicio de IA ha alcanzado su límite de capacidad. Por favor intenta nuevamente en unos minutos.",
            Instant.now(),
            request.getDescription(false).replace("uri=", ""),
            Map.of("retryAfter", "300")
        );
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .header("Retry-After", "300")
            .body(errorResponse);
    }
    
    /**
     * Maneja otros errores de Gemini API.
     */
    @ExceptionHandler(GeminiApiException.class)
    public ResponseEntity<ErrorResponse> handleGeminiApiError(
            GeminiApiException ex,
            WebRequest request) {
        
        log.error("Gemini API error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "AI_SERVICE_UNAVAILABLE",
            "El servicio de IA está temporalmente no disponible. Por favor intenta nuevamente.",
            Instant.now(),
            request.getDescription(false).replace("uri=", ""),
            null
        );
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(errorResponse);
    }
    
    /**
     * Maneja errores de autorización.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request) {
        
        log.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "FORBIDDEN",
            "No tienes permisos para acceder a este recurso",
            Instant.now(),
            request.getDescription(false).replace("uri=", ""),
            null
        );
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(errorResponse);
    }
    
    /**
     * Maneja errores genéricos no capturados.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(
            Exception ex,
            WebRequest request) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_ERROR",
            "Error interno del servidor. Por favor contacta soporte si el problema persiste.",
            Instant.now(),
            request.getDescription(false).replace("uri=", ""),
            null
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }
}
```

---

## 5. DTOs

### 5.1. DashboardExplanationRequest

```java
package com.cambiaso.ioc.dto.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Request para generar explicación de dashboard.
 */
@Schema(description = "Request para generar explicación de dashboard con IA")
public record DashboardExplanationRequest(
    
    @NotNull(message = "dashboardId es requerido")
    @Min(value = 1, message = "dashboardId debe ser mayor a 0")
    @Schema(description = "ID del dashboard de Metabase", example = "5")
    Integer dashboardId,
    
    @NotNull(message = "fechaInicio es requerida")
    @PastOrPresent(message = "fechaInicio no puede ser futura")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha inicial del rango a analizar", example = "2025-06-01")
    LocalDate fechaInicio,
    
    @NotNull(message = "fechaFin es requerida")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha final del rango a analizar", example = "2025-06-30")
    LocalDate fechaFin,
    
    @Schema(description = "Filtros opcionales del dashboard", example = "{\"turno\": \"Día\"}")
    Map<String, String> filtros
) {
    /**
     * Constructor con validaciones adicionales.
     */
    public DashboardExplanationRequest {
        // Validación: fechaFin >= fechaInicio
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("fechaFin debe ser mayor o igual a fechaInicio");
        }
        
        // Inicializar filtros vacíos si null
        if (filtros == null) {
            filtros = Map.of();
        }
    }
}
```

### 5.2. DashboardExplanationResponse

```java
package com.cambiaso.ioc.dto.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Respuesta con explicación generada por IA.
 */
@Schema(description = "Respuesta con explicación del dashboard generada por IA")
public record DashboardExplanationResponse(
    
    @Schema(description = "Resumen ejecutivo del análisis", 
        example = "La producción en junio 2025 mostró un desempeño sólido...")
    String resumenEjecutivo,
    
    @Schema(description = "Puntos clave del análisis (3-5 items)")
    List<String> keyPoints,
    
    @Schema(description = "Insights accionables para mejorar producción")
    List<String> insightsAccionables,
    
    @Schema(description = "Alertas o puntos de atención")
    List<String> alertas,
    
    @Schema(description = "ID del dashboard analizado", example = "5")
    Integer dashboardId,
    
    @Schema(description = "Título del dashboard", example = "Producción por Operario - Mensual")
    String dashboardTitulo,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha inicio del rango analizado")
    LocalDate fechaInicio,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha fin del rango analizado")
    LocalDate fechaFin,
    
    @Schema(description = "Filtros aplicados en el análisis")
    Map<String, String> filtrosAplicados,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Timestamp de generación")
    Instant generadoAt,
    
    @Schema(description = "Indica si la respuesta vino de cache", example = "false")
    Boolean fromCache,
    
    @Schema(description = "Tokens estimados usados en la generación", example = "1245")
    Integer tokensUsados,
    
    @Schema(description = "TTL del cache en segundos", example = "1800")
    Integer cacheTTLSeconds
) {
    /**
     * Crea una copia con fromCache actualizado.
     */
    public DashboardExplanationResponse withFromCache(boolean fromCache) {
        return new DashboardExplanationResponse(
            resumenEjecutivo, keyPoints, insightsAccionables, alertas,
            dashboardId, dashboardTitulo, fechaInicio, fechaFin, filtrosAplicados,
            generadoAt, fromCache, tokensUsados, cacheTTLSeconds
        );
    }
}
```

### 5.3. ErrorResponse

```java
package com.cambiaso.ioc.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/**
 * Respuesta estándar de error.
 */
@Schema(description = "Respuesta de error estándar")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    
    @Schema(description = "Código de error", example = "VALIDATION_ERROR")
    String error,
    
    @Schema(description = "Mensaje descriptivo del error")
    String message,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Timestamp del error")
    Instant timestamp,
    
    @Schema(description = "Path del endpoint", example = "/api/v1/ai/explain-dashboard")
    String path,
    
    @Schema(description = "Detalles adicionales del error (opcional)")
    Map<String, String> details
) {}
```

---

## 6. Configuración de Rate Limiting

### 6.1. application.properties

```properties
# Rate Limiting para AI Explanation
resilience4j.ratelimiter.instances.aiExplanation.limit-for-period=5
resilience4j.ratelimiter.instances.aiExplanation.limit-refresh-period=60s
resilience4j.ratelimiter.instances.aiExplanation.timeout-duration=0s
resilience4j.ratelimiter.instances.aiExplanation.allow-health-indicator-to-fail=true
resilience4j.ratelimiter.instances.aiExplanation.subscribe-for-events=true
resilience4j.ratelimiter.instances.aiExplanation.event-consumer-buffer-size=100
```

### 6.2. Configuración Bean (Alternativa)

```java
package com.cambiaso.ioc.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfiguration {

    @Bean
    public RateLimiter aiExplanationRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
            .limitForPeriod(5)
            .limitRefreshPeriod(Duration.ofSeconds(60))
            .timeoutDuration(Duration.ofSeconds(0))
            .build();
        
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        return registry.rateLimiter("aiExplanation");
    }
}
```

---

## 7. Testing

### 7.1. Tests de Controller

```java
package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.dto.ai.DashboardExplanationResponse;
import com.cambiaso.ioc.service.ai.DashboardExplanationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiExplanationController.class)
class AiExplanationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private DashboardExplanationService explanationService;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void explainDashboard_withValidRequest_returns200() throws Exception {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of()
        );
        
        DashboardExplanationResponse mockResponse = new DashboardExplanationResponse(
            "Resumen ejecutivo...",
            List.of("Punto 1", "Punto 2"),
            List.of("Insight 1"),
            List.of(),
            5,
            "Dashboard Producción",
            request.fechaInicio(),
            request.fechaFin(),
            Map.of(),
            Instant.now(),
            false,
            1200,
            1800
        );
        
        when(explanationService.explainDashboard(any())).thenReturn(mockResponse);
        
        // When/Then
        mockMvc.perform(post("/api/v1/ai/explain-dashboard")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resumenEjecutivo").value("Resumen ejecutivo..."))
            .andExpect(jsonPath("$.keyPoints").isArray())
            .andExpect(jsonPath("$.keyPoints.length()").value(2))
            .andExpect(jsonPath("$.fromCache").value(false));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void explainDashboard_withInvalidDateRange_returns400() throws Exception {
        // Given: fechaFin < fechaInicio
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 30),
            LocalDate.of(2025, 6, 1),
            Map.of()
        );
        
        // When/Then
        mockMvc.perform(post("/api/v1/ai/explain-dashboard")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
    
    @Test
    void explainDashboard_withoutAuthentication_returns401() throws Exception {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of()
        );
        
        // When/Then
        mockMvc.perform(post("/api/v1/ai/explain-dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void explainDashboard_withUserRole_returns200() throws Exception {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of()
        );
        
        DashboardExplanationResponse mockResponse = new DashboardExplanationResponse(
            "Resumen...",
            List.of("Punto 1"),
            List.of(),
            List.of(),
            5,
            "Dashboard",
            request.fechaInicio(),
            request.fechaFin(),
            Map.of(),
            Instant.now(),
            false,
            1000,
            1800
        );
        
        when(explanationService.explainDashboard(any())).thenReturn(mockResponse);
        
        // When/Then
        mockMvc.perform(post("/api/v1/ai/explain-dashboard")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }
}
```

### 7.2. Cobertura Objetivo
- **Líneas**: 90%
- **Branches**: 85%
- **Métodos**: 100%

---

## 8. Seguridad

### 8.1. Autenticación y Autorización

| Aspecto | Implementación |
|---------|----------------|
| Autenticación | JWT Bearer token (filtro Spring Security) |
| Autorización | `@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")` |
| CSRF | Protección habilitada para métodos POST |
| Rate Limiting | Resilience4j - 5 req/min por usuario |
| Input Validation | Bean Validation + validaciones custom en DTO |

### 8.2. Headers de Seguridad

```java
// Configuración Spring Security (ejemplo)
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .headers()
            .contentSecurityPolicy("default-src 'self'")
            .and()
            .xssProtection()
            .and()
            .frameOptions().deny();
    
    return http.build();
}
```

---

## 9. Observabilidad

### 9.1. Logging

```java
// Request log
log.info("Received AI explanation request from user: {}, dashboard: {}, range: {} to {}", 
    username, dashboardId, fechaInicio, fechaFin);

// Success log
log.info("AI explanation generated successfully for user: {}, dashboard: {}, fromCache: {}", 
    username, dashboardId, fromCache);

// Error log
log.error("Error generating AI explanation for user: {}: {}", username, e.getMessage(), e);
```

### 9.2. Métricas (Micrometer)

Métricas automáticas de Spring Boot:
- `http.server.requests` (tags: uri, method, status, outcome)

Métricas custom (en Service):
- `ai.explanation.requests` (tags: outcome)
- `ai.explanation.duration` (tags: phase, cache)

---

## 10. Documentación OpenAPI

### 10.1. Configuración Swagger

```java
package com.cambiaso.ioc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("IOC Backend API")
                .version("1.0")
                .description("API REST para aplicación IOC incluyendo endpoints de IA"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

**Swagger UI**: `http://localhost:8080/swagger-ui.html`

---

## 11. Checklist de Implementación

- [ ] Crear clase `AiExplanationController` en package `com.cambiaso.ioc.controller`
- [ ] Crear `AiExplanationExceptionHandler` (RestControllerAdvice)
- [ ] Crear DTOs: `DashboardExplanationRequest`, `DashboardExplanationResponse`, `ErrorResponse`
- [ ] Configurar Rate Limiting (Resilience4j)
- [ ] Añadir anotaciones OpenAPI/Swagger
- [ ] Implementar método `explainDashboard` con validaciones
- [ ] Implementar fallback de rate limiting
- [ ] Implementar validación de acceso a dashboard (opcional)
- [ ] Crear exception handlers para todos los casos (timeout, rate limit, validation)
- [ ] Configurar CORS si necesario
- [ ] Tests unitarios con MockMvc
- [ ] Tests de autorización (@WithMockUser)
- [ ] Tests de validación (400 Bad Request)
- [ ] Verificar cobertura >= 85%
- [ ] Probar endpoint con Postman/Insomnia
- [ ] Verificar documentación Swagger generada
- [ ] Code review

---

## 12. Referencias

- **Technical Design**: TD-001A-dashboard-ai-explanation-A.md
- **Spring Security**: https://spring.io/projects/spring-security
- **Resilience4j**: https://resilience4j.readme.io/docs/ratelimiter
- **OpenAPI**: https://springdoc.org/

---

## 13. Ejemplo de Request/Response (cURL)

### 13.1. Request Exitoso

```bash
curl -X POST http://localhost:8080/api/v1/ai/explain-dashboard \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "dashboardId": 5,
    "fechaInicio": "2025-06-01",
    "fechaFin": "2025-06-30",
    "filtros": {"turno": "Día"}
  }'
```

### 13.2. Response 200 OK

```json
{
  "resumenEjecutivo": "La producción en junio 2025 mostró...",
  "keyPoints": [
    "Producción total: 45,234 unidades",
    "Eficiencia promedio: 94.5%"
  ],
  "insightsAccionables": [
    "Considerar reforzar turno noche..."
  ],
  "alertas": [],
  "metadata": {
    "dashboardId": 5,
    "titulo": "Producción por Operario - Mensual",
    "fechaInicio": "2025-06-01",
    "fechaFin": "2025-06-30",
    "filtrosAplicados": {"turno": "Día"}
  },
  "generadoAt": "2025-11-12T10:30:45Z",
  "fromCache": false,
  "tokensUsados": 1245,
  "cacheTTLSeconds": 1800
}
```

---

**Fin BSS-004**

