package com.cambiaso.ioc.service.metabase;

import com.cambiaso.ioc.config.MetabaseProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cliente para interactuar con la API REST de Metabase.
 * Maneja autenticación y ejecución de consultas.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MetabaseApiClient {

    private final MetabaseProperties properties;
    private final RestClient.Builder restClientBuilder;

    private String sessionToken;

    /**
     * Obtiene el token de sesión, autenticando si es necesario.
     */
    private synchronized String getSessionToken() {
        if (sessionToken == null) {
            authenticate();
        }
        return sessionToken;
    }

    /**
     * Autentica con Metabase y guarda el token de sesión.
     */
    public void authenticate() {
        log.info("Authenticating with Metabase API at {}", properties.getSiteUrl());

        try {
            Map<String, String> credentials = Map.of(
                    "username", properties.getUsername(),
                    "password", properties.getPassword());

            JsonNode response = restClientBuilder.build()
                    .post()
                    .uri(properties.getSiteUrl() + "/api/session")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(credentials)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("id")) {
                this.sessionToken = response.get("id").asText();
                log.info("Metabase authentication successful");
            } else {
                throw new RuntimeException("Metabase authentication failed: No session ID in response");
            }
        } catch (Exception e) {
            log.error("Failed to authenticate with Metabase", e);
            throw new RuntimeException("Failed to authenticate with Metabase: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene la metadata de un dashboard y sus tarjetas (cards).
     */
    public JsonNode getDashboard(int dashboardId) {
        // Simple in-memory cache for dashboard structure (could be improved with
        // Caffeine)
        // if (dashboardCache.containsKey(dashboardId)) {
        // return dashboardCache.get(dashboardId);
        // }

        return executeWithRetry(() -> {
            log.debug("Fetching dashboard {} metadata", dashboardId);
            JsonNode response = restClientBuilder.build()
                    .get()
                    .uri(properties.getSiteUrl() + "/api/dashboard/" + dashboardId)
                    .header("X-Metabase-Session", getSessionToken())
                    .retrieve()
                    .body(JsonNode.class);
            log.info("Metabase Dashboard Response: {}", response);
            return response;
        });
    }

    /**
     * Ejecuta la consulta de una tarjeta específica.
     */
    public JsonNode runCardQuery(int cardId, Map<String, Object> parameters) {
        return executeWithRetry(() -> {
            log.debug("Running query for card {}", cardId);

            // Metabase API expects parameters in a specific format depending on the card
            // type
            // This is a simplified implementation assuming standard query execution
            // For native queries, parameters might need to be passed differently

            return restClientBuilder.build()
                    .post()
                    .uri(properties.getSiteUrl() + "/api/card/" + cardId + "/query")
                    .header("X-Metabase-Session", getSessionToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    // .body(Map.of("parameters", parameters)) // TODO: Map parameters correctly to
                    // Metabase format
                    .retrieve()
                    .body(JsonNode.class);
        });
    }

    /**
     * Ejecuta una operación con reintento automático si falla por 401 (Token
     * expirado).
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> operation) {
        try {
            return operation.get();
        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
            log.warn("Metabase API 401 Unauthorized - Refreshing token and retrying");
            authenticate();
            return operation.get();
        }
    }
}
