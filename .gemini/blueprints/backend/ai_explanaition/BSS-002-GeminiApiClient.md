# Backend Service Specification: GeminiApiClient

## Metadata
- **BSS ID**: BSS-002
- **Technical Design**: TD-001A (Dashboard AI Explanation)
- **Feature Plan**: FP-001A
- **Clase**: `GeminiApiClient`
- **Tipo**: Service (External API Gateway)
- **Package**: `com.cambiaso.ioc.service.ai`
- **Sprint**: Sprint 2
- **Fecha Creación**: 2025-11-12
- **Estado**: DRAFT

---

## 1. Propósito y Responsabilidades

### 1.1. Propósito
Gateway dedicado para comunicación con la API de Google Gemini. Encapsula toda la lógica de invocación HTTP, manejo de timeouts, retries y parsing de respuestas de IA.

### 1.2. Responsabilidades
- Construir requests HTTP a Gemini API con formato correcto
- Gestionar timeout total de 90 segundos (connect 5s + read 85s)
- Implementar política de retries (máx 2 intentos) con backoff exponencial
- Extraer texto de respuesta Gemini (JSON parsing)
- Manejar errores transitorios (503, network errors)
- Logging detallado de interacciones con IA
- Sanitización/escape de prompts para JSON

### 1.3. Ubicación en Arquitectura
```
DashboardExplanationService
         ↓
    [GeminiApiClient]
         ↓ (HTTP/HTTPS)
   Google Gemini API
   (generativelanguage.googleapis.com)
```

---

## 2. Interfaz Pública

### 2.1. Signature Completa

```java
package com.cambiaso.ioc.service.ai;

/**
 * Cliente para interactuar con Google Gemini API.
 * Maneja timeouts, retries y parsing de respuestas.
 */
@Service
public class GeminiApiClient {

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
    public String callGemini(String prompt);
    
    /**
     * Estima tokens usados en el prompt (aproximación).
     * 
     * @param text Texto a estimar
     * @return Estimación de tokens (length / 4)
     */
    public int estimateTokens(String text);
}
```

---

## 3. Dependencias Inyectadas

### 3.1. Constructor Injection

```java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiApiClient {

    private final WebClient.Builder webClientBuilder;
    
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
    
    // Métodos...
}
```

### 3.2. Grafo de Dependencias
```
GeminiApiClient
    ↓
WebClient.Builder (Spring Bean)
    ↓
Reactor Netty (HttpClient)
```

---

## 4. Reglas de Negocio

### 4.1. Políticas de Retry

| Condición | Acción | Backoff |
|-----------|--------|---------|
| 503 Service Unavailable | Retry hasta max-attempts | Exponencial (500ms, 1500ms) |
| ConnectException | Retry hasta max-attempts | Exponencial |
| SocketTimeoutException | Retry 1 vez | Exponencial |
| 429 Too Many Requests | NO retry, lanzar excepción inmediata | N/A |
| 401/403 Auth Error | NO retry, lanzar excepción inmediata | N/A |
| 4xx otros | NO retry | N/A |

### 4.2. Timeouts

| Fase | Timeout |
|------|---------|
| Connection | 5 segundos |
| Read | 85 segundos |
| Total (Resilience4j) | 90 segundos |

### 4.3. Modelo de Generación

```java
{
  "temperature": 0.2,           // Baja variabilidad para respuestas consistentes
  "maxOutputTokens": 2048,      // Suficiente para análisis ejecutivo
  "topP": 0.95,                 // Nucleus sampling
  "topK": 40                    // Top-K sampling
}
```

---

## 5. Implementación Detallada

### 5.1. Método Principal: callGemini

```java
public String callGemini(String prompt) {
    if (prompt == null || prompt.isBlank()) {
        throw new IllegalArgumentException("Prompt cannot be null or empty");
    }
    
    log.debug("Calling Gemini API - Prompt length: {} chars, estimated tokens: {}", 
        prompt.length(), estimateTokens(prompt));
    
    WebClient client = webClientBuilder
        .baseUrl("https://generativelanguage.googleapis.com")
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
                    || (throwable instanceof TimeoutException && maxRetryAttempts > 0)
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
        
    } catch (TimeoutException e) {
        log.error("Gemini API timeout after {}s", timeoutSeconds, e);
        throw new GeminiTimeoutException("Gemini API request timed out", e);
    } catch (GeminiRateLimitException | GeminiApiException e) {
        throw e; // Re-throw custom exceptions
    } catch (Exception e) {
        log.error("Gemini API call failed after retries: {}", e.getMessage(), e);
        throw new GeminiApiException("Failed to get response from Gemini API", e);
    }
}
```

### 5.2. Construcción del Request Body

```java
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
```

### 5.3. Extracción de Texto de Respuesta

```java
private String extractTextFromGeminiResponse(String response) {
    if (response == null || response.isBlank()) {
        throw new GeminiApiException("Empty response from Gemini API");
    }
    
    try {
        // Parse JSON structure: response.candidates[0].content.parts[0].text
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        
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
```

### 5.4. Fallback Regex Extraction

```java
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
```

### 5.5. JSON Escaping

```java
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
```

### 5.6. Estimación de Tokens

```java
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
```

---

## 6. Excepciones Personalizadas

### 6.1. GeminiApiException (Base)

```java
package com.cambiaso.ioc.exception;

public class GeminiApiException extends RuntimeException {
    public GeminiApiException(String message) {
        super(message);
    }
    
    public GeminiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 6.2. GeminiTimeoutException

```java
package com.cambiaso.ioc.exception;

public class GeminiTimeoutException extends GeminiApiException {
    public GeminiTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 6.3. GeminiRateLimitException

```java
package com.cambiaso.ioc.exception;

public class GeminiRateLimitException extends GeminiApiException {
    public GeminiRateLimitException(String message) {
        super(message);
    }
}
```

---

## 7. Manejo de Errores

### 7.1. Matriz de Errores

| Error HTTP | Retry | Excepción Lanzada | Acción Caller |
|------------|-------|-------------------|---------------|
| 200 OK | N/A | Ninguna | Procesar respuesta |
| 400 Bad Request | No | GeminiApiException | 500 al cliente |
| 401 Unauthorized | No | GeminiApiException | Alerta urgente (API key inválida) |
| 403 Forbidden | No | GeminiApiException | Alerta urgente |
| 429 Too Many Requests | No | GeminiRateLimitException | 503 al cliente + retry header |
| 500 Internal Error | Sí | GeminiApiException (si agota retries) | 503 al cliente |
| 503 Service Unavailable | Sí | GeminiApiException (si agota retries) | 503 al cliente |
| Timeout (90s) | Sí (1 vez) | GeminiTimeoutException | 504 al cliente |
| Network Error | Sí | GeminiApiException | 503 al cliente |

### 7.2. Logging Estructurado

```java
// Success
log.info("Gemini API call successful - Duration: {}ms, Tokens (est): {}", duration, tokens);

// Retry
log.warn("Retrying Gemini API call (attempt {}/{}): {}", attempt, maxAttempts, errorMsg);

// Error
log.error("Gemini API call failed after {} retries: {}", maxAttempts, e.getMessage(), e);

// Rate Limit
log.error("Gemini API rate limit exceeded - Check free tier quota");
```

---

## 8. Performance

### 8.1. Latencia Esperada

| Escenario | P50 | P95 | P99 |
|-----------|-----|-----|-----|
| Prompt corto (500 tokens) | 2s | 5s | 8s |
| Prompt medio (1000 tokens) | 3s | 6s | 10s |
| Prompt largo (1500 tokens) | 4s | 8s | 15s |
| Error + retry | 10s | 20s | 30s |

### 8.2. Optimizaciones
- Connection pooling (Reactor Netty default)
- Reutilización de WebClient.Builder bean
- Timeout agresivo para evitar bloqueos
- Backoff exponencial para espaciar retries

---

## 9. Testing

### 9.1. Tests Unitarios con WireMock

```java
@SpringBootTest
class GeminiApiClientTest {

    @Autowired
    private GeminiApiClient geminiClient;
    
    private WireMockServer wireMockServer;
    
    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        configureFor("localhost", 8089);
    }
    
    @AfterEach
    void teardown() {
        wireMockServer.stop();
    }
    
    @Test
    void callGemini_withValidPrompt_returnsText() {
        // Given
        String mockResponse = """
            {
              "candidates": [{
                "content": {
                  "parts": [{"text": "Análisis ejecutivo..."}]
                }
              }]
            }
            """;
        
        stubFor(post(urlPathMatching("/v1/models/.*/generateContent"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(mockResponse)
                .withFixedDelay(100)));
        
        // When
        String result = geminiClient.callGemini("Test prompt");
        
        // Then
        assertEquals("Análisis ejecutivo...", result);
    }
    
    @Test
    void callGemini_with503_retriesAndSucceeds() {
        // Given
        String mockResponse = """
            {
              "candidates": [{
                "content": {
                  "parts": [{"text": "Success after retry"}]
                }
              }]
            }
            """;
        
        stubFor(post(urlPathMatching("/v1/models/.*/generateContent"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse().withStatus(503))
            .willSetStateTo("First Retry"));
        
        stubFor(post(urlPathMatching("/v1/models/.*/generateContent"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("First Retry")
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(mockResponse)));
        
        // When
        String result = geminiClient.callGemini("Test prompt");
        
        // Then
        assertEquals("Success after retry", result);
        verify(exactly(2), postRequestedFor(urlPathMatching("/v1/models/.*/generateContent")));
    }
    
    @Test
    void callGemini_with429_throwsRateLimitException() {
        // Given
        stubFor(post(urlPathMatching("/v1/models/.*/generateContent"))
            .willReturn(aResponse().withStatus(429)));
        
        // When/Then
        assertThrows(GeminiRateLimitException.class, 
            () -> geminiClient.callGemini("Test prompt"));
    }
    
    @Test
    void callGemini_withTimeout_throwsTimeoutException() {
        // Given
        stubFor(post(urlPathMatching("/v1/models/.*/generateContent"))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(95000))); // > 90s timeout
        
        // When/Then
        assertThrows(GeminiTimeoutException.class, 
            () -> geminiClient.callGemini("Test prompt"));
    }
    
    @Test
    void estimateTokens_withText_returnsApproximation() {
        // Given
        String text = "a".repeat(400); // 400 chars
        
        // When
        int tokens = geminiClient.estimateTokens(text);
        
        // Then
        assertEquals(100, tokens); // 400 / 4 = 100
    }
}
```

### 9.2. Cobertura Objetivo
- **Líneas**: 95%
- **Branches**: 90%
- **Métodos**: 100%

---

## 10. Observabilidad

### 10.1. Métricas Micrometer

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiApiClient {

    private final MeterRegistry meterRegistry;
    
    public String callGemini(String prompt) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            String result = // ... llamada API
            
            sample.stop(Timer.builder("gemini.api.call")
                .tag("outcome", "success")
                .register(meterRegistry));
            
            meterRegistry.counter("gemini.api.requests.total", 
                "outcome", "success").increment();
            
            return result;
            
        } catch (GeminiRateLimitException e) {
            sample.stop(Timer.builder("gemini.api.call")
                .tag("outcome", "rate_limited")
                .register(meterRegistry));
            
            meterRegistry.counter("gemini.api.requests.total", 
                "outcome", "rate_limited").increment();
            throw e;
            
        } catch (GeminiTimeoutException e) {
            sample.stop(Timer.builder("gemini.api.call")
                .tag("outcome", "timeout")
                .register(meterRegistry));
            
            meterRegistry.counter("gemini.api.requests.total", 
                "outcome", "timeout").increment();
            throw e;
            
        } catch (Exception e) {
            sample.stop(Timer.builder("gemini.api.call")
                .tag("outcome", "error")
                .register(meterRegistry));
            
            meterRegistry.counter("gemini.api.requests.total", 
                "outcome", "error").increment();
            throw e;
        }
    }
}
```

### 10.2. Métricas Propuestas

| Métrica | Tipo | Tags | Descripción |
|---------|------|------|-------------|
| `gemini.api.call` | Timer | outcome=success\|error\|timeout\|rate_limited | Duración llamadas |
| `gemini.api.requests.total` | Counter | outcome | Total de requests |
| `gemini.api.tokens.estimated` | Distribution Summary | - | Tokens estimados por request |
| `gemini.api.retries.total` | Counter | - | Total de retries |

---

## 11. Seguridad

### 11.1. API Key Management
- ✅ Nunca hardcodear API key en código
- ✅ Usar variable de entorno `GEMINI_API_KEY`
- ✅ No loguear API key (parcial: `***${apiKey.substring(apiKey.length()-4)}`)
- ✅ Rotar API key periódicamente (cada 90 días)

### 11.2. Prompt Injection Prevention
- ✅ Escape completo de caracteres especiales JSON
- ✅ Validación de longitud máxima de prompt (prevenir DoS)
- ✅ Backend controla estructura del prompt (no inputs arbitrarios del usuario)

### 11.3. Rate Limiting
- Implementado en capa superior (Controller)
- Cliente respeta headers `Retry-After` de Gemini API

---

## 12. Configuración

### 12.1. application.properties

```properties
# Gemini API Configuration
gemini.api-key=${GEMINI_API_KEY}
gemini.model=gemini-1.5-flash
gemini.timeout.seconds=90
gemini.retry.max-attempts=2
gemini.retry.backoff.initial=500
gemini.retry.backoff.max=1500

# WebClient Configuration
spring.codec.max-in-memory-size=10MB
```

### 12.2. Validación de Configuración

```java
@PostConstruct
public void validateConfiguration() {
    if (apiKey == null || apiKey.isBlank()) {
        log.error("GEMINI_API_KEY not configured");
        throw new IllegalStateException("GEMINI_API_KEY environment variable is required");
    }
    
    if (timeoutSeconds < 30) {
        log.warn("Gemini timeout is very low: {}s (recommended: 90s)", timeoutSeconds);
    }
    
    log.info("GeminiApiClient configured - Model: {}, Timeout: {}s, Max Retries: {}", 
        model, timeoutSeconds, maxRetryAttempts);
}
```

---

## 13. Checklist de Implementación

- [ ] Crear clase `GeminiApiClient` en package `com.cambiaso.ioc.service.ai`
- [ ] Crear excepciones personalizadas (GeminiApiException, GeminiTimeoutException, GeminiRateLimitException)
- [ ] Implementar método `callGemini` con timeout y retries
- [ ] Implementar métodos auxiliares (buildRequestBody, extractText, escapeJson)
- [ ] Implementar método `estimateTokens`
- [ ] Añadir validación @PostConstruct de API key
- [ ] Configurar properties en application.properties
- [ ] Añadir métricas Micrometer
- [ ] Crear tests unitarios con WireMock
- [ ] Test de timeout (simulado con delay)
- [ ] Test de retry con escenarios
- [ ] Test de rate limit (429)
- [ ] Verificar cobertura >= 90%
- [ ] Documentar JavaDoc
- [ ] Code review por Tech Lead

---

## 14. Referencias

- **Technical Design**: TD-001A-dashboard-ai-explanation-A.md
- **Gemini API Docs**: https://ai.google.dev/docs
- **Spring WebClient**: https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html
- **WireMock**: http://wiremock.org/docs/

---

## 15. Notas de Implementación

### 15.1. Alternativas Consideradas
- **RestTemplate**: Descartado (bloqueante, deprecated para nuevos proyectos)
- **Feign Client**: Descartado (no necesitamos abstracción de alto nivel)
- **Apache HttpClient**: Descartado (más verboso, menos integrado con Spring)

### 15.2. Deuda Técnica
- Estimación de tokens es aproximada (mejorar con tiktoken library o API oficial)
- Parsing JSON podría usar deserialización automática (ObjectMapper a POJO)

### 15.3. Extensibilidad Futura
- Soporte para streaming de respuestas (Server-Sent Events)
- Soporte para múltiples modelos (strategy pattern)
- Circuit breaker si errores recurrentes (Resilience4j)
- Cache de respuestas idénticas (nivel cliente)

---

**Fin BSS-002**

