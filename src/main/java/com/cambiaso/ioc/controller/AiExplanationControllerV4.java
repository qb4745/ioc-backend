package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.service.DashboardAccessService;
import com.cambiaso.ioc.service.ai.SpringAiDashboardExplanationService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Controller v4 para explicación de dashboards usando Spring AI.
 * Proporciona streaming real de chunks con soporte nativo de Spring AI.
 */
@RestController
@RequestMapping("/api/v4/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Explanation v4", description = "Streaming con Spring AI - Chunks reales incrementales")
public class AiExplanationControllerV4 {

    private final SpringAiDashboardExplanationService explanationService;
    private final DashboardAccessService dashboardAccessService;

    @PostMapping(
            path = {"/explain-dashboard-stream", "/explain-stream"},
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    @RateLimiter(name = "aiExplanation")
    @Operation(
            summary = "Generar explicación de dashboard con streaming (Spring AI)",
            description = "Genera análisis ejecutivo usando Spring AI con streaming real de chunks. " +
                    "Retorna Server-Sent Events con fragmentos de texto conforme se generan.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Stream iniciado exitosamente",
                            content = @Content(
                                    mediaType = "text/event-stream",
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado al dashboard"),
                    @ApiResponse(responseCode = "404", description = "Dashboard no encontrado"),
                    @ApiResponse(responseCode = "429", description = "Rate limit excedido"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            }
    )
    public Flux<ServerSentEvent<String>> explainDashboardStream(
            @Valid @RequestBody DashboardExplanationRequest request,
            Authentication authentication) {

        log.info("V4 AI streaming explanation requested - User: {}, Dashboard: {}",
                authentication != null ? authentication.getName() : "anonymous",
                request.dashboardId());

        // Verificar acceso al dashboard
        dashboardAccessService.checkAccessOrThrow(authentication, request.dashboardId());

        // Retornar stream de Spring AI
        return explanationService.explainDashboardStream(request);
    }
}

