package com.cambiaso.ioc.service.ai;

import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.service.metabase.MetabaseApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * Servicio v4 que usa Spring AI Google GenAI para generar explicaciones con streaming real.
 * Spring AI maneja automáticamente el streaming de chunks desde Gemini.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SpringAiDashboardExplanationService {

    private final GoogleGenAiChatModel chatModel;
    private final MetabaseApiClient metabaseClient;

    @Value("classpath:prompts/system-prompt.txt")
    private Resource systemPromptResource;

    /**
     * Genera explicación del dashboard con streaming real usando Spring AI.
     *
     * @param request Request con dashboardId, fechas y filtros
     * @return Flux de Server-Sent Events con chunks de texto reales
     */
    public Flux<ServerSentEvent<String>> explainDashboardStream(DashboardExplanationRequest request) {
        log.info("Generating Spring AI streaming explanation for dashboard {}", request.dashboardId());

        try {
            // 1. Fetch Dashboard Metadata
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
            String promptText = buildDynamicPrompt(request, dashboardName, dataSummary.toString());
            Prompt prompt = new Prompt(promptText);

            log.info("Calling Spring AI Gemini with streaming - Prompt length: {} chars", promptText.length());

            // 4. Stream response from Spring AI - esto retorna chunks REALES!
            return chatModel.stream(prompt)
                    .doOnNext(chatResponse -> {
                        String content = extractContent(chatResponse);
                        if (!content.isEmpty()) {
                            log.info("Received chunk from Spring AI: {} chars", content.length());
                        }
                    })
                    .map(chatResponse -> {
                        String content = extractContent(chatResponse);

                        // Convertir cada chunk a SSE
                        return ServerSentEvent.<String>builder()
                                .event("message")
                                .data(content)
                                .build();
                    })
                    .filter(sse -> !sse.data().isEmpty()) // Filtrar chunks vacíos
                    .concatWith(Flux.just(
                            ServerSentEvent.<String>builder()
                                    .event("done")
                                    .data("[DONE]")
                                    .build()
                    ))
                    .doOnComplete(() -> log.info("Spring AI streaming completed for dashboard {}", request.dashboardId()))
                    .doOnError(e -> log.error("Spring AI streaming failed for dashboard {}: {}",
                            request.dashboardId(), e.getMessage(), e));

        } catch (Exception e) {
            log.error("Failed to initialize Spring AI streaming for dashboard {}: {}",
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

    /**
     * Extrae el contenido de texto del ChatResponse de Spring AI
     */
    private String extractContent(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getResults() == null || chatResponse.getResults().isEmpty()) {
            return "";
        }

        var result = chatResponse.getResults().get(0);
        if (result == null || result.getOutput() == null) {
            return "";
        }

        String content = result.getOutput().getText();
        return content != null ? content : "";
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
