package com.cambiaso.ioc.service.ai;

import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.dto.ai.DashboardExplanationResponse;
import com.cambiaso.ioc.dto.ai.GeminiJsonResponse;
import com.cambiaso.ioc.service.metabase.MetabaseApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

@Service
@Slf4j
@RequiredArgsConstructor
public class DynamicDashboardExplanationService {

    private final MetabaseApiClient metabaseClient;
    private final GeminiApiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Value("classpath:prompts/system-prompt.txt")
    private Resource systemPromptResource;

    public DashboardExplanationResponse explainDashboard(DashboardExplanationRequest request) {
        log.info("Generating dynamic explanation for dashboard {}", request.dashboardId());

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
                    log.warn("Skipping missing card node");
                    continue;
                }

                int cardId = card.path("id").asInt();
                String cardName = card.path("name").asText("Untitled Card");
                String display = card.path("display").asText();

                log.info("Processing card ID: {}, Name: {}, Display: {}", cardId, cardName, display);

                // Skip text cards or non-data cards if necessary
                if ("text".equals(display)) {
                    log.info("Skipping text card: {}", cardName);
                    continue;
                }

                try {
                    // Execute Query
                    JsonNode queryResult = metabaseClient.runCardQuery(cardId, Map.of()); // TODO: Pass date filters
                    log.info("Query result for card {}: {}", cardId, queryResult != null ? "Success" : "Null");

                    // Format Data
                    dataSummary.append("## ").append(cardName).append("\n");
                    String formattedData = formatCardData(queryResult);
                    log.debug("Formatted data for card {}: {}", cardId, formattedData);
                    dataSummary.append(formattedData).append("\n\n");

                } catch (Exception e) {
                    log.warn("Failed to fetch data for card {}: {}", cardId, e.getMessage());
                    dataSummary.append("## ").append(cardName).append("\n");
                    dataSummary.append("Error fetching data: ").append(e.getMessage()).append("\n\n");
                }
            }
        } else {
            log.warn("No dashcards found in dashboard response");
        }

        // 3. Build Prompt
        String prompt = buildDynamicPrompt(request, dashboardName, dataSummary.toString());

        // 4. Call Gemini
        String geminiResponse = geminiClient.callGemini(prompt);

        // 5. Parse Response (Reuse v1 logic or duplicate)
        // For simplicity, we'll assume a similar parsing logic or reuse v1's private
        // methods if made public/protected
        // Here we will do a manual parse for now to demonstrate the flow

        return parseResponse(geminiResponse, request, dashboardName);
    }

    private String formatCardData(JsonNode queryResult) {
        // Extract rows and columns from Metabase JSON response
        // This is a simplified formatter
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
            // Fallback if file load fails
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

    private DashboardExplanationResponse parseResponse(String jsonText, DashboardExplanationRequest request,
            String dashboardName) {
        try {
            // Robust JSON extraction: find first { and last }
            int startIndex = jsonText.indexOf("{");
            int endIndex = jsonText.lastIndexOf("}");

            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                jsonText = jsonText.substring(startIndex, endIndex + 1);
            } else {
                // Fallback: try to clean markdown if indices not found (unlikely if valid JSON
                // exists)
                if (jsonText.contains("```json")) {
                    jsonText = jsonText.substring(jsonText.indexOf("```json") + 7);
                    if (jsonText.contains("```")) {
                        jsonText = jsonText.substring(0, jsonText.indexOf("```"));
                    }
                }
            }

            GeminiJsonResponse response = objectMapper.readValue(jsonText, GeminiJsonResponse.class);

            return new DashboardExplanationResponse(
                    response.resumenEjecutivo(),
                    response.keyPoints(),
                    response.insightsAccionables(),
                    response.alertas(),
                    request.dashboardId(),
                    dashboardName,
                    request.fechaInicio(),
                    request.fechaFin(),
                    request.filtros(),
                    Instant.now(),
                    false,
                    0, // tokens unknown
                    300);
        } catch (Exception e) {
            log.error("Failed to parse AI response", e);
            return new DashboardExplanationResponse(
                    "Error parsing AI response",
                    List.of(),
                    List.of(),
                    List.of("Error: " + e.getMessage()),
                    request.dashboardId(),
                    dashboardName,
                    request.fechaInicio(),
                    request.fechaFin(),
                    request.filtros(),
                    Instant.now(),
                    false,
                    0,
                    300);
        }
    }
}
