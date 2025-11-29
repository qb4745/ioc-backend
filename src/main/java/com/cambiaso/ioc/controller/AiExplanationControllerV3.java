package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.service.ai.StreamingDashboardExplanationService;
import com.cambiaso.ioc.service.DashboardAccessService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Controller V3 para explicaciones de dashboards con AI usando Server-Sent Events (SSE).
 * Permite streaming de respuestas para mejor UX - el usuario ve la respuesta mientras se genera.
 */
@RestController
@RequestMapping("/api/v3/ai")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AiExplanationControllerV3 {

    private final StreamingDashboardExplanationService streamingExplanationService;
    private final DashboardAccessService dashboardAccessService;

    /**
     * Endpoint de streaming para explicaciones de dashboard.
     * Retorna Server-Sent Events (SSE) con fragmentos de texto conforme se generan.
     *
     * El cliente debe conectarse con EventSource o fetch con stream:
     *
     * JavaScript ejemplo:
     * <pre>
     * const eventSource = new EventSource('/api/v3/ai/explain-dashboard-stream');
     * eventSource.addEventListener('message', (e) => {
     *   console.log('Chunk:', e.data);
     *   // Append to UI
     * });
     * eventSource.addEventListener('done', () => {
     *   console.log('Stream completed');
     *   eventSource.close();
     * });
     * </pre>
     *
     * @param request Request con dashboardId, fechas y filtros
     * @param authentication Usuario autenticado
     * @return Flux de SSE con fragmentos de texto
     */
    @PostMapping(path = {"/explain-dashboard-stream", "/explain-stream"},
                 produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimiter(name = "aiExplanation")
    public Flux<ServerSentEvent<String>> explainDashboardStream(
            @Valid @RequestBody DashboardExplanationRequest request,
            Authentication authentication) {

        log.info("V3 AI streaming explanation requested - User: {}, Dashboard: {}",
                authentication.getName(), request.dashboardId());

        // Verify access (throws exception if unauthorized)
        dashboardAccessService.checkAccessOrThrow(authentication, request.dashboardId());

        // Return streaming response
        return streamingExplanationService.explainDashboardStream(request);
    }
}

