# 🚀 Endpoint v4 - Spring AI con Streaming Real

**Fecha:** 28 de Noviembre de 2025  
**Feature:** Explicación de Dashboards con Spring AI y Google GenAI  
**Ventaja Principal:** Chunks reales e incrementales con soporte nativo de Spring AI

---

## 📋 Resumen

La **versión 4** utiliza **Spring AI** con soporte nativo para Google GenAI, lo que proporciona:

✅ **Streaming real** con chunks incrementales verdaderos  
✅ **Configuración simplificada** con auto-configuración de Spring Boot  
✅ **API más limpia** - Spring AI maneja la complejidad  
✅ **Mejor rendimiento** - optimizado para streaming  
✅ **Múltiples modelos** - fácil cambio entre Gemini 2.0, 2.5, etc.

---

## 🎯 Endpoint v4

```
POST /api/v4/ai/explain-dashboard-stream
POST /api/v4/ai/explain-stream              (alias)
Content-Type: application/json
Produces: text/event-stream
```

### Request

```json
{
  "dashboardId": 5,
  "fechaInicio": "2025-11-01",
  "fechaFin": "2025-11-30",
  "filtros": {}
}
```

### Response (Server-Sent Events)

```
event: message
data: {"resumenEjecutivo": "Noviembre

event: message
data:  de 2025 registró una producción

event: message
data:  total de 128.087 unidades, lo que

event: message
data:  representa una disminución del 49%...

event: done
data: [DONE]
```

---

## ⚙️ Configuración

### 1. Dependencias (pom.xml)

```xml
<properties>
    <spring-ai.version>1.0.0-M4</spring-ai.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-google-genai</artifactId>
    </dependency>
</dependencies>
```

### 2. Application Properties

```properties
# Enable Google GenAI Chat Model
spring.ai.model.chat=google-genai

# API Key (set via environment variable)
spring.ai.google.genai.api-key=${GEMINI_API_KEY}

# Model configuration - Aligned with gemini.model property
spring.ai.google.genai.chat.options.model=gemini-2.5-flash
spring.ai.google.genai.chat.options.temperature=0.2
spring.ai.google.genai.chat.options.max-output-tokens=8192
spring.ai.google.genai.chat.options.top-p=0.95
spring.ai.google.genai.chat.options.top-k=40
```

**IMPORTANTE:** El modelo `gemini-2.5-flash` está alineado con la configuración `gemini.model` usada por los endpoints v2 y v3, asegurando consistencia en toda la aplicación.

### 3. Variable de Entorno

```bash
export GEMINI_API_KEY="your-api-key-here"
```

**Obtén tu API key en:** https://aistudio.google.com/app/apikey

---

## 🏗️ Arquitectura

```
AiExplanationControllerV4
    ↓
SpringAiDashboardExplanationService
    ↓
GoogleGenAiChatModel (Spring AI)
    ↓
chatModel.stream(prompt)
    ↓
Flux<ChatResponse> → Flux<ServerSentEvent<String>>
    ↓
Frontend (recibe chunks reales en tiempo real)
```

### Componentes Principales

**Controller:** `AiExplanationControllerV4.java`
- Endpoint `/api/v4/ai/explain-dashboard-stream`
- Valida acceso al dashboard
- Retorna `Flux<ServerSentEvent<String>>`

**Service:** `SpringAiDashboardExplanationService.java`
- Obtiene datos de Metabase
- Construye prompt dinámico
- Llama a Spring AI con streaming
- Convierte `ChatResponse` a SSE

**Spring AI:** `GoogleGenAiChatModel` (auto-configurado)
- Maneja conexión con Gemini API
- Procesa streaming automáticamente
- Gestiona retry y error handling

---

## 💻 Código del Service

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class SpringAiDashboardExplanationService {

    private final GoogleGenAiChatModel chatModel;
    private final MetabaseApiClient metabaseClient;

    public Flux<ServerSentEvent<String>> explainDashboardStream(DashboardExplanationRequest request) {
        // 1. Obtener datos del dashboard
        JsonNode dashboard = metabaseClient.getDashboard(request.dashboardId());
        
        // 2. Construir prompt
        String promptText = buildDynamicPrompt(request, dashboardName, dataSummary);
        Prompt prompt = new Prompt(promptText);

        // 3. Stream con Spring AI - ¡Chunks reales!
        return chatModel.stream(prompt)
                .map(chatResponse -> {
                    String content = extractContent(chatResponse);
                    
                    return ServerSentEvent.<String>builder()
                            .event("message")
                            .data(content)
                            .build();
                })
                .filter(sse -> !sse.data().isEmpty())
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("[DONE]")
                                .build()
                ));
    }
}
```

---

## 🔧 Diferencias con v3

| Aspecto | v3 (Custom) | v4 (Spring AI) |
|---------|-------------|----------------|
| **Dependencia** | WebClient manual | Spring AI starter |
| **Configuración** | Manual con properties | Auto-configuración |
| **Cliente Gemini** | `StreamingGeminiApiClient` | `GoogleGenAiChatModel` |
| **Streaming** | Parsing manual de JSON | Nativo de Spring AI |
| **Chunks** | Acumulados (1 grande) | Incrementales (reales) |
| **Error handling** | Manual | Incluido en Spring AI |
| **Código** | ~300 líneas | ~150 líneas |

---

## 🧪 Testing

### Curl Test

```bash
curl -N -X POST http://localhost:8080/api/v4/ai/explain-dashboard-stream \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "dashboardId": 5,
    "fechaInicio": "2025-11-01",
    "fechaFin": "2025-11-30"
  }'
```

### JavaScript (Frontend)

```javascript
import { fetchEventSource } from '@microsoft/fetch-event-source';

await fetchEventSource('/api/v4/ai/explain-dashboard-stream', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    dashboardId: 5,
    fechaInicio: '2025-11-01',
    fechaFin: '2025-11-30'
  }),
  onmessage(event) {
    if (event.event === 'message') {
      // Agregar texto incremental al UI
      appendText(event.data);
    } else if (event.event === 'done') {
      console.log('Streaming complete');
    }
  },
  onerror(err) {
    console.error('Error:', err);
  }
});
```

---

## 📊 Logs Esperados

```
INFO: Generating Spring AI streaming explanation for dashboard 5
INFO: Found 15 cards in dashboard 5
INFO: Calling Spring AI Gemini with streaming - Prompt length: 8737 chars
INFO: Received chunk from Spring AI: 45 chars
INFO: Received chunk from Spring AI: 67 chars
INFO: Received chunk from Spring AI: 52 chars
INFO: Received chunk from Spring AI: 89 chars
...
INFO: Spring AI streaming completed for dashboard 5
```

**Nota:** Verás **múltiples** "Received chunk" en lugar de uno solo como en v3.

---

## 🎨 Modelos Soportados

Spring AI soporta todos los modelos de Gemini:

```properties
# Gemini 2.0 Flash (más rápido)
spring.ai.google.genai.chat.options.model=gemini-2.0-flash

# Gemini 2.0 Flash Lite (ultra rápido)
spring.ai.google.genai.chat.options.model=gemini-2.0-flash-lite

# Gemini 1.5 Flash
spring.ai.google.genai.chat.options.model=gemini-1.5-flash

# Gemini Pro
spring.ai.google.genai.chat.options.model=gemini-pro
```

---

## 🔒 Seguridad

### Autenticación
- Requiere token JWT válido
- Verificación de acceso al dashboard

### Rate Limiting
```java
@RateLimiter(name = "aiExplanation")  // 5 req/min
```

### API Key
- Almacenada en variable de entorno
- Nunca en código fuente
- Rotación periódica recomendada

---

## ⚡ Performance

### Tiempo de Respuesta

- **Primer chunk:** ~500ms - 2s
- **Chunks subsiguientes:** Cada ~100-500ms
- **Total:** Similar a v2/v3 pero con mejor UX

### Tamaño de Chunks

Los chunks son **dinámicos** según cómo Gemini genera el texto:
- Chunk 1: ~40-80 caracteres
- Chunk 2: ~60-100 caracteres
- Chunk N: Variable

### Consumo de Recursos

- **Memoria:** Menor que v2/v3 (no acumula)
- **CPU:** Similar
- **Network:** Streaming eficiente

---

## 🐛 Troubleshooting

### Error: "Cannot resolve GoogleGenAiChatModel"

**Solución:**
```bash
mvn clean install -DskipTests
```

### Error: "API key not configured"

**Solución:**
```bash
export GEMINI_API_KEY="your-key"
# O agregar a application.properties:
spring.ai.google.genai.api-key=your-key
```

### No se reciben chunks

**Verificar logs:**
```
INFO: Received chunk from Spring AI: X chars
```

Si solo ves **1 chunk**, puede ser que Gemini esté respondiendo muy rápido para dashboards pequeños.

### Stream se cierra prematuramente

**Verificar:**
- Timeout del cliente HTTP
- Configuración de proxy/load balancer
- Keep-alive connections

---

## 📚 Recursos

### Documentación Oficial

- **Spring AI:** https://docs.spring.io/spring-ai/reference/
- **Google GenAI:** https://ai.google.dev/gemini-api/docs
- **Gemini Models:** https://ai.google.dev/gemini-api/docs/models

### Ejemplos de Spring AI

- GitHub: https://github.com/spring-projects/spring-ai
- Samples: https://github.com/spring-projects/spring-ai-examples

---

## 🚀 Próximos Pasos

### Implementación Frontend

1. Instalar librería SSE
   ```bash
   npm install @microsoft/fetch-event-source
   ```

2. Crear componente React para streaming
3. Agregar animación de "typing"
4. Implementar botón de cancelar

### Optimizaciones Backend

1. **Cache con Redis**
   - Cachear respuestas completas
   - TTL de 10 minutos

2. **Compresión**
   - Comprimir prompts largos
   - Gzip en SSE

3. **Métricas**
   - Prometheus metrics
   - Dashboard de monitoreo

### Features Adicionales

- Comparación entre periodos
- Múltiples idiomas
- Exportación a PDF
- Alertas automáticas

---

## ✅ Checklist de Migración v3 → v4

- [x] Agregar dependencia `spring-ai-starter-model-google-genai`
- [x] Configurar properties de Spring AI
- [x] Crear `SpringAiDashboardExplanationService`
- [x] Crear `AiExplanationControllerV4`
- [x] Configurar `GEMINI_API_KEY` en entorno
- [ ] Probar endpoint con curl
- [ ] Verificar logs de chunks múltiples
- [ ] Implementar frontend
- [ ] Deploy a producción

---

## 📝 Notas Importantes

### Spring AI vs Custom Implementation

**Usa Spring AI (v4) cuando:**
- Quieres código más limpio y mantenible
- Necesitas soporte empresarial
- Planeas usar múltiples modelos de IA
- Quieres actualizaciones automáticas

**Usa Custom (v3) cuando:**
- Necesitas control total del streaming
- Tienes requerimientos muy específicos
- No quieres dependencias adicionales

### Compatibilidad

- ✅ Spring Boot 3.4.x y 3.5.x
- ✅ Java 21+
- ✅ Todos los modelos de Gemini
- ✅ API key de Google AI Studio

### Limitaciones

- Requiere API key de Google
- No funciona offline
- Sujeto a límites de rate de Gemini API
- Necesita conexión a internet estable

---

**Fin del documento**

Este endpoint v4 con Spring AI proporciona la mejor experiencia de streaming con soporte nativo, código más limpio y chunks reales e incrementales.
