package com.cambiaso.ioc.service.ai;

import com.cambiaso.ioc.exception.GeminiApiException;
import com.cambiaso.ioc.exception.GeminiRateLimitException;
import com.cambiaso.ioc.exception.GeminiTimeoutException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Cliente para interactuar con Google Gemini API en modo streaming.
 * Usa streamGenerateContent para recibir respuestas incrementales.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StreamingGeminiApiClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    @Value("${gemini.timeout.seconds:90}")
    private int timeoutSeconds;

    @Value("${gemini.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;

    @Value("${gemini.max-output-tokens:8192}")
    private int maxOutputTokens;

    // New configurable thinking budget: 0 = disabled
    @Value("${gemini.thinking-budget:0}")
    private int thinkingBudget;

    /**
     * Invoca la API de Gemini en modo streaming.
     *
     * @param prompt Prompt completo (system + context + data + instructions)
     * @return Flux de fragmentos de texto conforme se generan
     * @throws GeminiApiException       si falla la llamada
     * @throws GeminiTimeoutException   si excede el timeout
     * @throws GeminiRateLimitException si API retorna 429
     */
    public Flux<String> callGeminiStream(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return Flux.error(new IllegalArgumentException("Prompt cannot be null or empty"));
        }

        log.info("Calling Gemini Streaming API - Prompt length: {} chars, model: {}",
                prompt.length(), model);

        WebClient client = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String requestBody = buildRequestBody(prompt);

        return client.post()
                .uri("/v1beta/models/{model}:streamGenerateContent?key={apiKey}", model, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        clientResponse -> {
                            int status = clientResponse.statusCode().value();
                            if (status == 429) {
                                return reactor.core.publisher.Mono.error(
                                        new GeminiRateLimitException("API rate limit exceeded"));
                            }
                            return reactor.core.publisher.Mono.error(
                                    new GeminiApiException("Client error: " + status));
                        })
                .bodyToFlux(DataBuffer.class)
                .doOnNext(buffer -> log.debug("Received DataBuffer: {} bytes", buffer.readableByteCount()))
                .map(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .doOnNext(chunk -> log.debug("Converted to String: {} chars", chunk.length()))
                .transformDeferred(this::accumulateAndExtractJsonObjects)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnNext(text -> log.info("Emitting text chunk to client: {} chars", text.length()))
                .doOnError(throwable -> {
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        log.error("Gemini Streaming API timeout after {}s", timeoutSeconds);
                    } else {
                        log.error("Gemini Streaming API error: {}", throwable.getMessage(), throwable);
                    }
                })
                .onErrorMap(java.util.concurrent.TimeoutException.class,
                        e -> new GeminiTimeoutException("Gemini streaming request timed out", e))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GeminiApiException ||
                        throwable instanceof GeminiRateLimitException ||
                        throwable instanceof GeminiTimeoutException) {
                        return throwable;
                    }
                    return new GeminiApiException("Failed to get streaming response from Gemini API", throwable);
                });
    }

    /**
     * Acumula chunks, extrae objetos JSON completos y extrae el texto.
     */
    private Flux<String> accumulateAndExtractJsonObjects(Flux<String> chunks) {
        final StringBuilder jsonBuffer = new StringBuilder();

        return chunks.concatMap(chunk -> {
            log.debug("Processing chunk in buffer: {} chars", chunk.length());

            // Accumulate chunks in the buffer
            jsonBuffer.append(chunk);

            // Try to extract complete JSON objects
            String accumulated = jsonBuffer.toString();

            // Look for complete JSON objects: starts with { or [ and ends with } or ]
            java.util.List<String> extractedTexts = new java.util.ArrayList<>();
            int depth = 0;
            int objectStart = -1;

            for (int i = 0; i < accumulated.length(); i++) {
                char c = accumulated.charAt(i);

                if (c == '{' || c == '[') {
                    if (depth == 0) {
                        objectStart = i;
                    }
                    depth++;
                } else if (c == '}' || c == ']') {
                    depth--;
                    if (depth == 0 && objectStart != -1) {
                        // Found a complete JSON object
                        String jsonObject = accumulated.substring(objectStart, i + 1);
                        log.info("Extracted complete JSON object: {} chars", jsonObject.length());

                        // Extract text from JSON immediately
                        String text = extractTextFromJson(jsonObject);
                        if (text != null && !text.isEmpty()) {
                            extractedTexts.add(text);
                            log.info("Added text to emit: {} chars", text.length());
                        }

                        objectStart = -1;
                    }
                }
            }

            // Clear processed JSON objects from buffer
            if (!extractedTexts.isEmpty()) {
                // Find how much we can safely delete
                int lastProcessedPos = accumulated.lastIndexOf("}");
                if (lastProcessedPos != -1) {
                    jsonBuffer.delete(0, lastProcessedPos + 1);
                    log.debug("Buffer cleared, remaining: {} chars", jsonBuffer.length());
                }
            }

            return Flux.fromIterable(extractedTexts);
        });
    }

    /**
     * Extrae el texto de un objeto JSON completo de Gemini (version síncrona para usar dentro de concatMap).
     */
    private String extractTextFromJson(String jsonObject) {
        try {
            log.info("Parsing complete JSON object: {} chars", jsonObject.length());
            JsonNode root = objectMapper.readTree(jsonObject);

            // Log the entire JSON structure for debugging
            log.debug("Full JSON received from Gemini: {}", root.toPrettyString().substring(0, Math.min(1000, root.toPrettyString().length())));

            // Gemini streaming returns an array with a single object, not a direct object
            JsonNode responseObject;
            if (root.isArray() && !root.isEmpty()) {
                log.debug("Root is an array, extracting first element");
                responseObject = root.get(0);
            } else {
                responseObject = root;
            }

            JsonNode candidates = responseObject.path("candidates");

            if (candidates.isEmpty() || !candidates.isArray()) {
                log.warn("No candidates array found in JSON. Response object keys: {}",
                        responseObject.fieldNames().hasNext() ?
                        java.util.stream.StreamSupport.stream(
                            java.util.Spliterators.spliteratorUnknownSize(responseObject.fieldNames(), 0), false)
                            .toList() : "empty");
                return null;
            }

            JsonNode firstCandidate = candidates.get(0);
            log.debug("First candidate found with {} fields", firstCandidate.size());

            // Check for finishReason
            String finishReason = firstCandidate.path("finishReason").asText("");
            if (!finishReason.isEmpty()) {
                log.info("Gemini stream ended with finishReason: {}", finishReason);
                return null;
            }

            JsonNode content = firstCandidate.path("content");
            if (content.isMissingNode()) {
                log.warn("No content node found in candidate. Candidate keys: {}",
                        firstCandidate.fieldNames().hasNext() ?
                        java.util.stream.StreamSupport.stream(
                            java.util.Spliterators.spliteratorUnknownSize(firstCandidate.fieldNames(), 0), false)
                            .toList() : "empty");
                return null;
            }

            JsonNode parts = content.path("parts");

            if (parts.isEmpty() || !parts.isArray()) {
                log.warn("No parts array found in content. Content keys: {}",
                        content.fieldNames().hasNext() ?
                        java.util.stream.StreamSupport.stream(
                            java.util.Spliterators.spliteratorUnknownSize(content.fieldNames(), 0), false)
                            .toList() : "empty");
                return null;
            }

            JsonNode firstPart = parts.get(0);
            String text = firstPart.path("text").asText("");

            if (text.isEmpty()) {
                log.warn("Empty text in part. Part keys: {}",
                        firstPart.fieldNames().hasNext() ?
                        java.util.stream.StreamSupport.stream(
                            java.util.Spliterators.spliteratorUnknownSize(firstPart.fieldNames(), 0), false)
                            .toList() : "empty");
                return null;
            }

            log.info("✓ Successfully extracted text chunk: {} chars", text.length());

            // The text is the actual streaming chunk - return it directly
            // It's a fragment of the final JSON response being built incrementally
            return text;

        } catch (Exception e) {
            log.error("Error parsing JSON: {} - Error: {} - Stack trace: ",
                    jsonObject.length() > 200 ? jsonObject.substring(0, 200) + "..." : jsonObject,
                    e.getMessage(), e);
            return null;
        }
    }

    private String buildRequestBody(String prompt) {
        // Escape prompt for JSON
        String escapedPrompt = escapeJson(prompt);

        // Include thinkingConfig only when thinkingBudget != 0
        String thinkingConfig = "";
        if (thinkingBudget != 0) {
            thinkingConfig = String.format("\n              \"thinkingConfig\": {\n                \"thinkingBudget\": %d\n              },\n            ", thinkingBudget);
        }

        return String.format("""
                {
                  "contents": [{
                    "role": "user",
                    "parts": [{"text": "%s"}]
                  }],
                  "generationConfig": {%s
                    "temperature": 0.2,
                    "maxOutputTokens": %d,
                    "topP": 0.95,
                    "topK": 40
                  },
                  "safetySettings": [
                    {
                      "category": "HARM_CATEGORY_HARASSMENT",
                      "threshold": "BLOCK_NONE"
                    },
                    {
                      "category": "HARM_CATEGORY_HATE_SPEECH",
                      "threshold": "BLOCK_NONE"
                    },
                    {
                      "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                      "threshold": "BLOCK_NONE"
                    },
                    {
                      "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                      "threshold": "BLOCK_NONE"
                    }
                  ]
                }
                """, escapedPrompt, thinkingConfig, maxOutputTokens);
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
}
