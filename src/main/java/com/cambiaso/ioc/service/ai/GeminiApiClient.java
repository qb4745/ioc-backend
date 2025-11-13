package com.cambiaso.ioc.service.ai;

import com.cambiaso.ioc.exception.GeminiApiException;
import com.cambiaso.ioc.exception.GeminiRateLimitException;
import com.cambiaso.ioc.exception.GeminiTimeoutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cliente para interactuar con Google Gemini API.
 * Maneja timeouts, retries y parsing de respuestas.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiApiClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${gemini.timeout.seconds:90}")
    private int timeoutSeconds;

    @Value("${gemini.retry.max-attempts:2}")
    private int maxRetryAttempts;

    @Value("${gemini.retry.backoff.initial:500}")
    private long initialBackoffMs;

    @Value("${gemini.retry.backoff.max:1500}")
    private long maxBackoffMs;

    @Value("${gemini.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;

    /**
     * Invoca la API de Gemini con el prompt completo.
     *
     * Timeout total: 90 segundos (connect 5s + read 85s)
     * Retries: Máximo 2 intentos con backoff exponencial (500ms inicial, 1500ms máximo)
     *
     * @param prompt Prompt completo (system + context + data + instructions)
     * @return Texto de respuesta extraído del JSON de Gemini
     * @throws GeminiApiException si falla después de todos los retries
     * @throws GeminiTimeoutException si excede el timeout de 90s
     * @throws GeminiRateLimitException si API retorna 429
     */
    public String callGemini(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }

        log.debug("Calling Gemini API - Prompt length: {} chars, estimated tokens: {}",
                prompt.length(), estimateTokens(prompt));

        WebClient client = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String requestBody = buildRequestBody(prompt);

        try {
            long startTime = System.currentTimeMillis();

            String response = client.post()
                    .uri("/v1/models/{model}:generateContent?key={apiKey}", model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            clientResponse -> {
                                int status = clientResponse.statusCode().value();
                                if (status == 429) {
                                    return Mono.error(new GeminiRateLimitException("API rate limit exceeded"));
                                }
                                return Mono.error(new GeminiApiException("Client error: " + status));
                            }
                    )
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofMillis(initialBackoffMs))
                            .maxBackoff(Duration.ofMillis(maxBackoffMs))
                            .filter(throwable ->
                                    throwable instanceof WebClientResponseException.ServiceUnavailable
                                            || throwable instanceof java.net.ConnectException
                            )
                            .doBeforeRetry(retrySignal ->
                                    log.warn("Retrying Gemini API call (attempt {}/{}): {}",
                                            retrySignal.totalRetries() + 1,
                                            maxRetryAttempts,
                                            retrySignal.failure().getMessage())
                            )
                    )
                    .block();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Gemini API call successful - Duration: {}ms, Response length: {} chars",
                    duration, response != null ? response.length() : 0);

            return extractTextFromGeminiResponse(response);

        } catch (GeminiRateLimitException e) {
            throw e; // Re-throw rate limit exception
        } catch (GeminiApiException e) {
            throw e; // Re-throw custom exceptions
        } catch (RuntimeException e) {
            // Reactor / WebClient timeouts and related issues surface as runtime exceptions.
            // Detect timeout-like failures and wrap them in GeminiTimeoutException.
            String className = e.getClass().getName();
            String message = e.getMessage() != null ? e.getMessage() : "";

            boolean isTimeout = className.toLowerCase().contains("timeout")
                    || message.toLowerCase().contains("timeout")
                    || (e.getCause() != null && e.getCause().getClass().getName().toLowerCase().contains("timeout"));

            if (isTimeout) {
                log.error("Gemini API timeout after {}s (detected runtime)", timeoutSeconds, e);
                throw new GeminiTimeoutException("Gemini API request timed out", e);
            }

            log.error("Gemini API call failed after retries: {}", e.getMessage(), e);
            throw new GeminiApiException("Failed to get response from Gemini API", e);
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            throw new GeminiApiException("Failed to get response from Gemini API", e);
        }
    }

    /**
     * Estima tokens usados en el prompt (aproximación).
     *
     * @param text Texto a estimar
     * @return Estimación de tokens (length / 4)
     */
    public int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        // Aproximación simple: 1 token ≈ 4 caracteres
        // En producción, usar tiktoken o API oficial de conteo
        int estimate = text.length() / 4;

        log.debug("Token estimation for {} chars: ~{} tokens", text.length(), estimate);
        return estimate;
    }

    private String buildRequestBody(String prompt) {
        // Escape prompt for JSON
        String escapedPrompt = escapeJson(prompt);

        return String.format("""
                {
                  "contents": [{
                    "parts": [{"text": "%s"}]
                  }],
                  "generationConfig": {
                    "temperature": 0.2,
                    "maxOutputTokens": 2048,
                    "topP": 0.95,
                    "topK": 40
                  },
                  "safetySettings": [
                    {
                      "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                      "threshold": "BLOCK_NONE"
                    }
                  ]
                }
                """, escapedPrompt);
    }

    private String extractTextFromGeminiResponse(String response) {
        if (response == null || response.isBlank()) {
            throw new GeminiApiException("Empty response from Gemini API");
        }

        try {
            // Parse JSON structure: response.candidates[0].content.parts[0].text
            JsonNode root = objectMapper.readTree(response);

            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty()) {
                log.error("No candidates in Gemini response");
                throw new GeminiApiException("No candidates in response");
            }

            JsonNode firstCandidate = candidates.get(0);
            JsonNode content = firstCandidate.path("content");
            JsonNode parts = content.path("parts");

            if (parts.isEmpty()) {
                log.error("No parts in Gemini response");
                throw new GeminiApiException("No parts in response");
            }

            String text = parts.get(0).path("text").asText();

            if (text == null || text.isBlank()) {
                log.error("Empty text in Gemini response");
                throw new GeminiApiException("Empty text in response");
            }

            log.debug("Extracted text from Gemini response: {} chars", text.length());
            return text;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini response as JSON", e);
            // Fallback: try to extract text with regex (less reliable)
            return extractTextWithRegexFallback(response);
        }
    }

    private String extractTextWithRegexFallback(String response) {
        log.warn("Using regex fallback to extract text from Gemini response");

        // Pattern: "text": "..."
        Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            String text = matcher.group(1);
            // Unescape JSON
            return text.replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\t", "\t");
        }

        log.error("Could not extract text from Gemini response with regex fallback");
        throw new GeminiApiException("Failed to extract text from response");
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\\", "\\\\")    // Backslash primero
                .replace("\"", "\\\"")     // Comillas
                .replace("\n", "\\n")      // Newline
                .replace("\r", "\\r")      // Carriage return
                .replace("\t", "\\t")      // Tab
                .replace("\b", "\\b")      // Backspace
                .replace("\f", "\\f");     // Form feed
    }
}
