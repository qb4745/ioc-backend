
---

## 📈 Métricas y Performance

### v2 (Gemini 2.0 Flash)
- **Tiempo promedio:** 8-12 segundos
- **Tokens input:** ~3,500-5,500 (dependiendo del dashboard)
- **Tokens output:** ~1,500-3,000
- **Total tokens:** ~5,000-8,500

### v2 (Gemini 2.5 Flash con Thinking)
- **Tiempo promedio:** 12-20 segundos
- **Tokens input:** ~3,500-5,500
- **Tokens thinking:** ~1,000-2,000 (interno)
- **Tokens output:** ~2,000-4,000
- **Total tokens:** ~6,500-11,500

### v3 (Streaming)
- **Tiempo primer chunk:** ~500ms-2s
- **Tiempo total:** Similar a v2
- **Ventaja:** Percepción de velocidad mucho mejor
- **Tokens:** Mismos que v2

---

## 🔒 Seguridad

### Autenticación
- Requiere token JWT válido
- Verificación de acceso al dashboard antes de generar explicación

### Rate Limiting
- 5 requests por minuto por usuario
- Protección contra abuso del endpoint
- Implementado con Resilience4j

### Validación de Input
```java
@Valid @RequestBody DashboardExplanationRequest request
```

**Validaciones:**
- `dashboardId` requerido (integer)
- `fechaInicio` y `fechaFin` en formato ISO 8601
- `filtros` opcional (Map)

---

## 📝 Logging

### Niveles de Log

**INFO:**
- Inicio de request: modelo, longitud de prompt
- Éxito de llamada: duración, longitud de respuesta

**DEBUG:**
- Prompt completo enviado a Gemini
- Respuesta completa de Gemini
- JSON limpio después de `cleanGeminiJsonErrors()`

**WARN:**
- Fallos en obtención de datos de cards
- Retries de llamadas a Gemini

**ERROR:**
- Bloqueos por safety filters con `finishReason`
- Errores de parsing JSON
- Timeouts de Gemini
- Rate limits excedidos

**TRACE:**
- Chunks incompletos en streaming (normal)

### Ejemplo de Logs

```
INFO: Calling Gemini API - Prompt length: 12450 chars, model: gemini-2.5-flash
INFO: FULL PROMPT: [contenido del prompt]
INFO: Gemini API call successful - Duration: 15234ms, Response length: 3611 chars
DEBUG: Full Gemini response: {"candidates":[...]}
DEBUG: Cleaned JSON for parsing: {"resumenEjecutivo":"..."}
```

---

## 🚀 Deployment

### Variables de Entorno Requeridas

```bash
# Gemini API
GEMINI_API_KEY=your_api_key_here

# Opcional - Configuración avanzada
GEMINI_MODEL=gemini-2.5-flash
GEMINI_MAX_OUTPUT_TOKENS=8192
GEMINI_TIMEOUT_SECONDS=90
```

### Docker

```dockerfile
ENV GEMINI_API_KEY=${GEMINI_API_KEY}
ENV GEMINI_MODEL=gemini-2.5-flash
ENV GEMINI_MAX_OUTPUT_TOKENS=8192
```

### Configuración Producción

```properties
# application-prod.properties
gemini.model=gemini-2.5-flash
gemini.max-output-tokens=8192
gemini.timeout.seconds=120

# Rate limiting más estricto
resilience4j.ratelimiter.instances.aiExplanation.limit-for-period=3
resilience4j.ratelimiter.instances.aiExplanation.limit-refresh-period=60s
```

---

## 🐛 Troubleshooting

### Error: MAX_TOKENS

**Síntoma:** `Content blocked by Gemini safety filters. Reason: MAX_TOKENS`

**Solución:**
```properties
gemini.max-output-tokens=16384  # Aumentar a 16K
```

### Error: No parts in response

**Síntoma:** `No parts in Gemini response. FinishReason: SAFETY`

**Solución:** Verificar que las 4 categorías de safety estén en `BLOCK_NONE`

### Error: Timeout

**Síntoma:** `Gemini API request timed out`

**Solución:**
```properties
gemini.timeout.seconds=120  # Aumentar timeout
```

### Warnings en Streaming

**Síntoma:** `WARN: Failed to parse stream chunk`

**Solución:** Esto fue resuelto cambiando a `log.trace()` - ya no deberías ver estos warnings

### JSON Mal Formado

**Síntoma:** `JsonMappingException: Unexpected close marker`

**Solución:** El método `cleanGeminiJsonErrors()` lo repara automáticamente

---

## 📚 Recursos Adicionales

### Documentación Creada

- **AI_STREAMING_V3_GUIDE.md** - Guía completa del endpoint v3 con ejemplos
- **GEMINI_SAFETY_FILTERS_FIX.md** - Fix para bloqueos de contenido
- **GEMINI_MAX_TOKENS_FIX.md** - Solución para error MAX_TOKENS

### APIs Externas Utilizadas

- **Google Gemini API:** https://ai.google.dev/api
- **Metabase API:** Documentación interna del proyecto

### Librerías Frontend Recomendadas

- `@microsoft/fetch-event-source` - Cliente SSE para fetch API
- `eventsource` - Polyfill para navegadores antiguos

---

## ✅ Checklist de Implementación Completa

### Backend
- ✅ Endpoint v2 con respuesta JSON completa
- ✅ Endpoint v3 con Server-Sent Events (streaming)
- ✅ Soporte para Gemini 2.0 y 2.5 Flash
- ✅ ThinkingConfig automático para Gemini 2.5
- ✅ Filtros de seguridad completos (4 categorías)
- ✅ Limpieza automática de JSON mal formado (v2)
- ✅ Manejo robusto de chunks fragmentados (v3)
- ✅ Rate limiting (5 req/min)
- ✅ Logging detallado con niveles apropiados
- ✅ Manejo de errores con finishReason
- ✅ Configuración externalizada en properties
- ✅ Tests de compilación exitosos

### Configuración
- ✅ `application.properties` con valores por defecto
- ✅ Variables de entorno documentadas
- ✅ Soporte para múltiples modelos
- ✅ Timeouts configurables
- ✅ Tokens de salida configurables

### Documentación
- ✅ Guía de uso v3 (AI_STREAMING_V3_GUIDE.md)
- ✅ Fix para safety filters (GEMINI_SAFETY_FILTERS_FIX.md)
- ✅ Fix para MAX_TOKENS (GEMINI_MAX_TOKENS_FIX.md)
- ✅ Informe completo para agente IA (este documento)

### Pendiente (Frontend)
- ⏳ Implementar consumo de v2 en React
- ⏳ Implementar consumo de v3 con SSE en React
- ⏳ UI para mostrar streaming de texto
- ⏳ Manejo de errores en frontend
- ⏳ Indicadores de carga

---

## 🎯 Próximos Pasos Recomendados

1. **Frontend v3 (Alta Prioridad)**
   - Implementar componente React para SSE
   - Agregar animación de "typing" para mejor UX
   - Botón de cancelar stream

2. **Optimizaciones Backend**
   - Cache de respuestas recientes (Redis/Caffeine)
   - Compresión de prompts largos
   - Métricas con Prometheus

3. **Features Adicionales**
   - Comparación entre periodos
   - Alertas automáticas basadas en IA
   - Exportación de explicaciones a PDF

4. **Testing**
   - Tests unitarios para `GeminiApiClient`
   - Tests de integración para endpoints
   - Tests E2E con frontend

---

## 📞 Contacto y Soporte

**Equipo:** IOC Backend Development  
**Proyecto:** Inteligencia Operacional Cambiaso  
**Versión:** 1.0.0  
**Última actualización:** 27 de Noviembre de 2025

---

**Fin del Informe**

Este documento sirve como referencia completa para cualquier agente de IA o desarrollador que necesite entender la implementación de los endpoints v2 y v3 de explicación de dashboards con IA.
# Informe Completo: Endpoints AI v2 y v3 - Dashboard Explanation

**Fecha:** 27 de Noviembre de 2025  
**Proyecto:** IOC Backend - Inteligencia Operacional Cambiaso  
**Feature:** Explicación de Dashboards con IA (Gemini)

---

## 📋 Resumen Ejecutivo

Se implementaron **dos versiones** del endpoint de explicación de dashboards con IA:

- **v2**: Endpoint tradicional que retorna respuesta JSON completa
- **v3**: Endpoint moderno con **Server-Sent Events (SSE)** para streaming en tiempo real

Ambas versiones utilizan **Google Gemini API** (modelos 2.0 y 2.5 Flash) para generar análisis ejecutivos de dashboards de Metabase.

---

## 🎯 Endpoints Implementados

### Endpoint v2 (Respuesta Completa)

```
POST /api/v2/ai/explain-dashboard
POST /api/v2/ai/explain        (alias)
Content-Type: application/json
```

**Request:**
```json
{
  "dashboardId": 5,
  "fechaInicio": "2025-01-01",
  "fechaFin": "2025-01-31",
  "filtros": {}
}
```

**Response:**
```json
{
  "resumenEjecutivo": "Texto de 2-3 líneas con estado general...",
  "keyPoints": [
    "Punto clave 1 con métricas",
    "Punto clave 2 con métricas",
    "Punto clave 3 con métricas"
  ],
  "insightsAccionables": [
    "Recomendación 1",
    "Recomendación 2"
  ],
  "alertas": [
    "Alerta crítica si existe"
  ],
  "dashboardId": 5,
  "dashboardName": "Dashboard Analítico IOC",
  "fechaInicio": "2025-01-01",
  "fechaFin": "2025-01-31",
  "filtros": {},
  "generatedAt": "2025-11-27T22:44:01.219Z",
  "cached": false,
  "tokensUsed": 0,
  "durationSeconds": 20
}
```

**Características:**
- ✅ Tiempo de respuesta: ~8-20 segundos
- ✅ Respuesta JSON completa en un solo request
- ✅ Más simple de implementar en frontend
- ✅ Compatible con Gemini 2.0 y 2.5 Flash

---

### Endpoint v3 (Streaming SSE)

```
POST /api/v3/ai/explain-dashboard-stream
POST /api/v3/ai/explain-stream              (alias)
Content-Type: application/json
Produces: text/event-stream
```

**Request:**
```json
{
  "dashboardId": 5,
  "fechaInicio": "2025-01-01",
  "fechaFin": "2025-01-31",
  "filtros": {}
}
```

**Response (Server-Sent Events):**
```
event: message
data: {"resumenEjecutivo":"En el periodo analizado

event: message
data:  se observa un crecimiento del 15%

event: message
data:  en las métricas clave...

event: done
data: [DONE]
```

**Características:**
- ✅ Respuesta **incremental** en tiempo real
- ✅ Mejor UX - el usuario ve texto aparecer mientras se genera
- ✅ Usa `streamGenerateContent` de Gemini
- ✅ Menor sensación de tiempo de espera
- ✅ Compatible con cancelación de stream

---

## 🏗️ Arquitectura Implementada

### Componentes v2

```
AiExplanationControllerV2
    ↓
DynamicDashboardExplanationService
    ↓ (obtiene datos)
MetabaseApiClient
    ↓ (genera explicación)
GeminiApiClient (generateContent)
    ↓
parseResponse() → cleanGeminiJsonErrors()
    ↓
DashboardExplanationResponse
```

### Componentes v3

```
AiExplanationControllerV3
    ↓
StreamingDashboardExplanationService
    ↓ (obtiene datos)
MetabaseApiClient
    ↓ (genera explicación streaming)
StreamingGeminiApiClient (streamGenerateContent)
    ↓
Flux<ServerSentEvent<String>>
    ↓
Frontend (recibe chunks en tiempo real)
```

---

## 📁 Archivos Implementados

### Controllers

**`AiExplanationControllerV2.java`**
```java
@RestController
@RequestMapping("/api/v2/ai")
public class AiExplanationControllerV2 {
    
    @PostMapping(path = { "/explain", "/explain-dashboard" })
    @RateLimiter(name = "aiExplanation")
    public ResponseEntity<DashboardExplanationResponse> explainDashboard(
            @Valid @RequestBody DashboardExplanationRequest request,
            Authentication authentication) {
        // Verificar acceso
        dashboardAccessService.checkAccessOrThrow(authentication, request.dashboardId());
        
        // Generar explicación
        DashboardExplanationResponse response = explanationService.explainDashboard(request);
        
        return ResponseEntity.ok(response);
    }
}
```

**`AiExplanationControllerV3.java`**
```java
@RestController
@RequestMapping("/api/v3/ai")
public class AiExplanationControllerV3 {
    
    @PostMapping(path = {"/explain-dashboard-stream", "/explain-stream"}, 
                 produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimiter(name = "aiExplanation")
    public Flux<ServerSentEvent<String>> explainDashboardStream(
            @Valid @RequestBody DashboardExplanationRequest request,
            Authentication authentication) {
        // Verificar acceso
        dashboardAccessService.checkAccessOrThrow(authentication, request.dashboardId());
        
        // Retornar stream
        return streamingExplanationService.explainDashboardStream(request);
    }
}
```

### Services

**`DynamicDashboardExplanationService.java`** (v2)
- Orquesta obtención de datos de Metabase
- Construye prompt con system prompt + datos
- Llama a `GeminiApiClient.callGemini()`
- Parsea respuesta JSON con `cleanGeminiJsonErrors()`

**`StreamingDashboardExplanationService.java`** (v3)
- Misma lógica de obtención de datos
- Llama a `StreamingGeminiApiClient.callGeminiStream()`
- Retorna `Flux<ServerSentEvent<String>>` con chunks

### Clientes Gemini

**`GeminiApiClient.java`** (v2)
- Método: `callGemini(String prompt)`
- Endpoint: `/v1beta/models/{model}:generateContent`
- Retorna: `String` (respuesta completa)
- Características:
  - ✅ Timeout: 90 segundos configurable
  - ✅ Retries: 2 intentos con backoff exponencial
  - ✅ Manejo de errores: Rate limit, timeout, safety filters
  - ✅ Parsing robusto con `extractTextFromGeminiResponse()`

**`StreamingGeminiApiClient.java`** (v3)
- Método: `callGeminiStream(String prompt)`
- Endpoint: `/v1beta/models/{model}:streamGenerateContent`
- Retorna: `Flux<String>` (chunks incrementales)
- Características:
  - ✅ Timeout: 90 segundos configurable
  - ✅ Chunks procesados línea por línea
  - ✅ Logging con `trace` (no `warn`) para chunks incompletos
  - ✅ Buffer management para fragmentos JSON

---

## ⚙️ Configuración (application.properties)

```properties
# Modelo Gemini
gemini.model=gemini-2.5-flash
gemini.api-key=${GEMINI_API_KEY}
gemini.base-url=https://generativelanguage.googleapis.com

# Configuración de Tokens
gemini.max-output-tokens=8192

# Timeouts
gemini.timeout.seconds=90

# Retries
gemini.retry.max-attempts=2
gemini.retry.backoff.initial=500
gemini.retry.backoff.max=1500

# Rate Limiting
resilience4j.ratelimiter.instances.aiExplanation.limit-for-period=5
resilience4j.ratelimiter.instances.aiExplanation.limit-refresh-period=60s
```

---

## 🎨 Características Implementadas

### 1. Soporte Multi-Modelo

| Modelo | Configuración | Output Limit | Input Limit | Thinking |
|--------|---------------|--------------|-------------|----------|
| **gemini-2.0-flash** | `gemini.model=gemini-2.0-flash` | 8,192 tokens | 1M tokens | ❌ |
| **gemini-2.5-flash** | `gemini.model=gemini-2.5-flash` | 65,536 tokens | 1M tokens | ✅ |

**Código de detección automática:**
```java
boolean supportsThinking = model.contains("2.5");

String thinkingConfig = supportsThinking 
    ? """
      "thinkingConfig": {
        "thinkingBudget": -1
      },
      """
    : "";
```

### 2. Filtros de Seguridad Completos

**Problema resuelto:** Gemini bloqueaba contenido técnico/industrial por filtros de seguridad.

**Solución:** Configurar todas las 4 categorías con `BLOCK_NONE`

```json
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
```

### 3. Limpieza Automática de JSON Mal Formado (v2)

**Problema:** Gemini a veces genera JSON inválido (arrays cerrados con `}` en lugar de `]`)

**Solución:** Método `cleanGeminiJsonErrors()` que repara automáticamente:

```java
private String cleanGeminiJsonErrors(String json) {
    // Fix: "item"} → "item"]
    json = json.replaceAll(",\\s*\"([^\"]+)\"\\s*\\}", ",\"$1\"]");
    
    // Fix: trailing commas
    json = json.replaceAll(",\\s*\\]", "]");
    json = json.replaceAll(",\\s*\\}", "}");
    
    // Remove markdown
    json = json.replace("```json", "").replace("```", "");
    
    return json.trim();
}
```

### 4. Manejo de Chunks Fragmentados (v3)

**Problema:** Streaming envía JSON fragmentado que no se puede parsear chunk por chunk

**Solución:** Cambio de `log.warn()` a `log.trace()` y procesamiento línea por línea:

```java
private Flux<String> extractTextFromStreamChunk(String chunk, StringBuilder buffer) {
    String[] lines = chunk.split("\n");
    
    return Flux.fromArray(lines)
            .filter(line -> !line.trim().isEmpty())
            .flatMap(line -> {
                try {
                    JsonNode root = objectMapper.readTree(line);
                    // Extraer texto...
                } catch (Exception e) {
                    log.trace("Skipping incomplete chunk (normal in streaming): {}", line);
                    return Flux.empty();
                }
            });
}
```

### 5. Manejo Robusto de Errores

**v2 - GeminiApiClient:**
```java
// Detectar finishReason
String finishReason = firstCandidate.path("finishReason").asText("");
if (!finishReason.isEmpty() && !finishReason.equals("STOP")) {
    log.error("Gemini blocked content. finishReason: {}, Full response: {}", 
              finishReason, response);
    
    // Log safety ratings
    JsonNode safetyRatings = firstCandidate.path("safetyRatings");
    if (!safetyRatings.isEmpty()) {
        log.error("Safety ratings: {}", safetyRatings);
    }
    
    throw new GeminiApiException(
        "Content blocked by Gemini safety filters. Reason: " + finishReason
    );
}
```

**Posibles valores de `finishReason`:**
- `STOP` → ✅ Generación exitosa
- `SAFETY` → ⚠️ Bloqueado por filtros de seguridad
- `RECITATION` → ⚠️ Bloqueado por contenido con copyright
- `MAX_TOKENS` → ⚠️ Se alcanzó límite de tokens
- `OTHER` → ⚠️ Otra razón

### 6. Rate Limiting

**Configuración:**
- **Límite:** 5 requests por minuto por usuario
- **Periodo:** 60 segundos
- **Librería:** Resilience4j

```java
@RateLimiter(name = "aiExplanation")
public ResponseEntity<DashboardExplanationResponse> explainDashboard(...) {
    // ...
}
```

---

## 🔧 Problemas Resueltos Durante Implementación

### Problema 1: MAX_TOKENS con Gemini 2.5 Flash

**Error:**
```json
{
  "message": "Content blocked by Gemini safety filters. Reason: MAX_TOKENS"
}
```

**Causa:**
- Gemini 2.5 usa `thinkingConfig` que consume tokens adicionales
- Con `maxOutputTokens: 2048` no alcanzaban los tokens para thinking + respuesta

**Solución:**
1. Aumentar `gemini.max-output-tokens` de 2048 → 8192
2. Agregar `thinkingConfig` automáticamente para modelos 2.5

### Problema 2: JSON Mal Formado

**Error:**
```
JsonMappingException: Unexpected close marker '}': expected ']'
```

**Causa:** Gemini genera JSON con errores de sintaxis

**Solución:** Método `cleanGeminiJsonErrors()` que repara automáticamente

### Problema 3: Warnings Molestos en Streaming

**Error:**
```
WARN: Failed to parse stream chunk, skipping: Unexpected close marker '}'
```

**Causa:** Los chunks en streaming vienen fragmentados (esto es **normal**)

**Solución:** Cambiar `log.warn()` a `log.trace()` para chunks incompletos

### Problema 4: Bloqueos por Safety Filters

**Error:**
```
No parts in response. FinishReason: SAFETY
```

**Causa:** Solo se configuraba 1 de 4 categorías de seguridad

**Solución:** Configurar las 4 categorías con `BLOCK_NONE`

---

## 📊 Comparativa v2 vs v3

| Característica | v2 (Completo) | v3 (Streaming) |
|----------------|---------------|----------------|
| **Tiempo respuesta** | ~8-20 segundos | Inmediato (chunks) |
| **UX** | Pantalla en blanco | Texto aparece en vivo |
| **Implementación Frontend** | Más simple | Más complejo |
| **Tipo respuesta** | JSON único | Server-Sent Events |
| **API Gemini** | `generateContent` | `streamGenerateContent` |
| **Cancelación** | ❌ | ✅ |
| **Uso memoria** | Mayor (buffer completo) | Menor (streaming) |
| **Rate limit** | 5/min | 5/min |
| **Compatibilidad** | Universal | Requiere SSE |

---

## 💻 Ejemplo de Uso Frontend

### v2 - Fetch tradicional

```javascript
async function explainDashboard(dashboardId, fechaInicio, fechaFin) {
  const response = await fetch('/api/v2/ai/explain-dashboard', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      dashboardId,
      fechaInicio,
      fechaFin,
      filtros: {}
    })
  });

  const data = await response.json();
  console.log(data.resumenEjecutivo);
  console.log(data.keyPoints);
  console.log(data.insightsAccionables);
}
```

### v3 - Streaming con Fetch API

```javascript
async function explainDashboardStreaming(dashboardId, fechaInicio, fechaFin) {
  const response = await fetch('/api/v3/ai/explain-dashboard-stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      dashboardId,
      fechaInicio,
      fechaFin,
      filtros: {}
    })
  });

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();
    
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    
    // Procesar SSE
    const lines = buffer.split('\n\n');
    buffer = lines.pop();
    
    for (const line of lines) {
      if (!line.trim()) continue;
      
      const eventMatch = line.match(/^event: (.+)$/m);
      const dataMatch = line.match(/^data: (.+)$/m);
      
      if (eventMatch && dataMatch) {
        const event = eventMatch[1];
        const data = dataMatch[1];
        
        if (event === 'message') {
          // Agregar texto al UI
          appendText(data);
        } else if (event === 'done') {
          console.log('Stream complete');
          break;
        }
      }
    }
  }
}
```

### v3 - Streaming con Librería

```bash
npm install @microsoft/fetch-event-source
```

```javascript
import { fetchEventSource } from '@microsoft/fetch-event-source';

await fetchEventSource('/api/v3/ai/explain-dashboard-stream', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    dashboardId: 5,
    fechaInicio: '2025-01-01',
    fechaFin: '2025-01-31'
  }),
  onmessage(event) {
    if (event.event === 'message') {
      appendText(event.data);
    } else if (event.event === 'done') {
      console.log('Complete');
    }
  },
  onerror(err) {
    console.error('Error:', err);
    throw err;
  }
});
```

---

## 🧪 Testing

### Curl Test v2

```bash
curl -X POST http://localhost:8080/api/v2/ai/explain-dashboard \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "dashboardId": 5,
    "fechaInicio": "2025-01-01",
    "fechaFin": "2025-01-31"
  }'
```

### Curl Test v3 (Streaming)

```bash
curl -N -X POST http://localhost:8080/api/v3/ai/explain-dashboard-stream \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "dashboardId": 5,
    "fechaInicio": "2025-01-01",
    "fechaFin": "2025-01-31"
  }'
```

**Nota:** La opción `-N` deshabilita buffering para ver el stream en tiempo real.

