package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.service.MetabaseEmbeddingService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboards")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DashboardController {

    private final MetabaseEmbeddingService embeddingService;

    /**
     * Obtiene una URL firmada para incrustar un dashboard de Metabase.
     * 
     * @param dashboardId ID del dashboard (1-999999)
     * @param authentication Información del usuario autenticado (inyectada por Spring Security)
     * @return Objeto JSON con la URL firmada y tiempo de expiración
     */
    @GetMapping("/{dashboardId}")
    @RateLimiter(name = "dashboardAccess")
    public ResponseEntity<Map<String, Object>> getDashboardUrl(
            @PathVariable 
            @Min(value = 1, message = "Dashboard ID must be at least 1") 
            @Max(value = 999999, message = "Dashboard ID must be at most 999999") 
            int dashboardId,
            Authentication authentication
    ) {
        log.info("Dashboard URL requested - User: {}, Dashboard ID: {}", 
            authentication.getName(), dashboardId);
        
        String signedUrl = embeddingService.getSignedDashboardUrl(dashboardId, authentication);
        
        return ResponseEntity.ok(Map.of(
            "signedUrl", signedUrl,
            "expiresInMinutes", 10, // Debería coincidir con la config
            "dashboardId", dashboardId
        ));
    }
}
