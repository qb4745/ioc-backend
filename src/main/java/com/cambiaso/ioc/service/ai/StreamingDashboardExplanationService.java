package com.cambiaso.ioc.service.ai;

import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.service.metabase.MetabaseApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * Servicio para generar explicaciones de dashboards en modo streaming.
 * Procesa los datos del dashboard y envía el prompt a Gemini,
 * retornando un Flux de SSE con los fragmentos de respuesta.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StreamingDashboardExplanationService {

    private final MetabaseApiClient metabaseClient;
    private final StreamingGeminiApiClient streamingGeminiClient;
    private final ObjectMapper objectMapper;

    @Value("classpath:prompts/system-prompt.txt")
    private Resource systemPromptResource;

    /**
     * Genera explicación del dashboard en modo streaming.
     *
     * @param request Request con dashboardId, fechas y filtros
     * @return Flux de Server-Sent Events con fragmentos de texto
     */
    public Flux<ServerSentEvent<String>> explainDashboardStream(DashboardExplanationRequest request) {
        log.info("Generating streaming explanation for dashboard {}", request.dashboardId());

        try {
            // 1. Fetch Dashboard Metadata (blocking, pero se hace una sola vez al inicio)
            JsonNode dashboard = metabaseClient.getDashboard(request.dashboardId());
            String dashboardName = dashboard.path("name").asText("Dashboard " + request.dashboardId());

            // 2. Fetch Data for each Card
            StringBuilder dataSummary = new StringBuilder();
            JsonNode dashcards = dashboard.path("dashcards");

            if (dashcards.isArray()) {
                log.info("Found {} cards in dashboard {}", dashcards.size(), request.dashboardId());
                for (JsonNode cardRef : dashcards) {
                    JsonNode card = cardRef.path("card");
                    if (card.isMissingNode()) {
                        continue;
                    }

                    int cardId = card.path("id").asInt();
                    String cardName = card.path("name").asText("Untitled Card");
                    String display = card.path("display").asText();

                    // Skip invalid or text cards
                    if (cardId <= 0 || "text".equals(display)) {
                        continue;
                    }

                    try {
                        JsonNode queryResult = metabaseClient.runCardQuery(cardId, Map.of());
                        dataSummary.append("## ").append(cardName).append("\n");
                        String formattedData = formatCardData(queryResult);
                        dataSummary.append(formattedData).append("\n\n");
                    } catch (Exception e) {
                        log.warn("Failed to fetch data for card {}: {}", cardId, e.getMessage());
                        dataSummary.append("## ").append(cardName).append("\n");
                        dataSummary.append("Error fetching data: ").append(e.getMessage()).append("\n\n");
                    }
                }
            }

            // 3. Build Prompt
            String prompt = buildDynamicPrompt(request, dashboardName, dataSummary.toString());

            // 4. Use collect() to accumulate all chunks, then emit complete response
            return streamingGeminiClient.callGeminiStream(prompt)
                    .doOnNext(textChunk -> log.info("Received chunk from Gemini: {} chars", textChunk.length()))
                    .collect(StringBuilder::new, StringBuilder::append)
                    .flux()
                    .flatMap(accumulated -> {
                        String completeText = accumulated.toString();
                        log.info("Accumulated complete response: {} chars", completeText.length());

                        // Send the complete JSON as a single SSE event
                        return Flux.just(
                                ServerSentEvent.<String>builder()
                                        .event("message")
                                        .data(completeText)
                                        .build(),
                                ServerSentEvent.<String>builder()
                                        .event("done")
                                        .data("[DONE]")
                                        .build()
                        );
                    })
                    .doOnComplete(() -> log.info("Streaming completed for dashboard {}", request.dashboardId()))
                    .doOnError(e -> log.error("Streaming failed for dashboard {}: {}",
                            request.dashboardId(), e.getMessage()));

        } catch (Exception e) {
            log.error("Failed to initialize streaming for dashboard {}: {}",
                    request.dashboardId(), e.getMessage(), e);

            // Return error as SSE
            return Flux.just(
                    ServerSentEvent.<String>builder()
                            .event("error")
                            .data("Error al procesar dashboard: " + e.getMessage())
                            .build()
            );
        }
    }

    private String formatCardData(JsonNode queryResult) {
        if (queryResult.has("data") && queryResult.path("data").has("rows")) {
            JsonNode rows = queryResult.path("data").path("rows");
            if (rows.size() > 10) {
                return "Top 10 rows: " + rows.toString().substring(0, Math.min(rows.toString().length(), 1000)) + "...";
            }
            return rows.toString();
        }
        return "No data found";
    }

    private String buildDynamicPrompt(DashboardExplanationRequest request, String dashboardName, String dataSummary) {
        String systemPrompt = "";
        try (java.io.InputStream is = systemPromptResource.getInputStream()) {
            systemPrompt = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to load system prompt", e);
            systemPrompt = "Actúa como un experto analista de datos. Responde solo en JSON.";
        }

        return String.format("""
                %s

                DASHBOARD: %s
                RANGO: %s a %s

                DATOS:
                %s
                """,
                systemPrompt,
                dashboardName,
                request.fechaInicio(),
                request.fechaFin(),
                dataSummary);
    }
}
