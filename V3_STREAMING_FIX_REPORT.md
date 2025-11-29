# Fix del Endpoint V3 Streaming - Reporte

## Fecha
27 de noviembre de 2025

## Problema Original
El endpoint `/api/v3/ai/explain-dashboard-stream` solo enviaba el evento `done` pero no los chunks de texto de la respuesta de Gemini, a pesar de que los logs del backend mostraban "Streaming completed".

### Síntoma
```bash
curl -N -X POST 'http://localhost:8080/api/v3/ai/explain-dashboard-stream' ...
# Respuesta:
event:done
data:[DONE]
# ❌ No se reciben los chunks de texto
```

## Causa Raíz
El problema estaba en `StreamingGeminiApiClient.callGeminiStream()`:

1. **Uso incorrecto del operador `scan`**: Se intentaba mantener un `StringBuilder` mutable entre chunks, lo cual no funciona correctamente en reactive streams porque el mismo objeto se pasa entre operadores.

2. **Complejidad innecesaria**: Se intentaba manejar líneas JSON parciales cuando Gemini ya envía objetos JSON completos separados por `\n`.

## Solución Implementada

### Cambios en `StreamingGeminiApiClient.java`

#### Antes (código problemático):
```java
.bodyToFlux(DataBuffer.class)
.scan(new StringBuilder(), (buffer, chunk) -> {
    buffer.append(chunk);
    return buffer;
})
.skip(1)
.flatMap(buffer -> {
    // Manejo complejo del buffer...
})
```

#### Después (código corregido):
```java
.bodyToFlux(String.class)  // ✅ Más simple y funciona mejor con Gemini
.doOnNext(chunk -> log.debug("Received chunk from Gemini: {} chars", chunk.length()))
.flatMap(this::extractTextFromStreamChunk)
.doOnNext(text -> log.info("Emitting text chunk to client: {} chars", text.length()))
```

### Mejoras Adicionales
1. **Logging mejorado**: Se agregaron logs en DEBUG e INFO para rastrear el flujo de datos
2. **Simplificación del parsing**: Cada chunk de Gemini se procesa directamente
3. **Mejor manejo de errores**: Se mantiene el stack trace completo en los logs

## Cómo Probar

### 1. Reiniciar la aplicación
```bash
cd /mnt/ssd-480/repos/captone/ioc-backend
# Si usas systemd:
sudo systemctl restart ioc-backend

# Si ejecutas manualmente:
java -jar target/iocbackend-0.0.1-SNAPSHOT.jar
```

### 2. Probar con curl (con logs visibles)
```bash
curl -N -X POST 'http://localhost:8080/api/v3/ai/explain-dashboard-stream' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  --data-raw '{"dashboardId":3,"fechaInicio":"2025-11-28","fechaFin":"2025-11-28","filtros":{}}'
```

### 3. Resultado Esperado
```
event:message
data:{"

event:message
data:insights

event:message
data":[

event:message
data:{

... más chunks con el JSON generado por Gemini ...

event:done
data:[DONE]
```

### 4. Verificar logs del backend
Con `logging.level.com.cambiaso.ioc=DEBUG` deberías ver:
```
[INFO] Calling Gemini Streaming API - Prompt length: 19873 chars, model: gemini-2.5-flash
[DEBUG] Received chunk from Gemini: 450 chars
[INFO] Processing Gemini chunk: 450 chars, content preview: {"candidates":[...
[INFO] Split into 2 lines
[DEBUG] Processing line: 220 chars
[DEBUG] Parsing JSON line: {"candidates":[{"content":...
[DEBUG] Parsed JSON structure: {...
[INFO] ✓ Successfully extracted text chunk: 123 chars - Preview: {"insights":[...
[INFO] Emitting text chunk to client: 123 chars
[DEBUG] Sending SSE chunk: 123 chars
[INFO] ✓ Successfully extracted text chunk: 89 chars - Preview: ...
[INFO] Emitting text chunk to client: 89 chars
[DEBUG] Sending SSE chunk: 89 chars
...
[INFO] Streaming completed for dashboard 3
```

**Si NO ves logs "Processing Gemini chunk"**: Significa que Gemini no está enviando datos o `bodyToFlux(String.class)` está esperando la respuesta completa.

**Si ves "Processing Gemini chunk" pero NO "Successfully extracted"**: El problema está en el parsing del JSON de Gemini.

**Si ves "Successfully extracted" pero NO "Emitting text chunk"**: Hay un problema en el pipeline reactive entre la extracción y la emisión.

**Si ves "Emitting text chunk" pero el cliente no recibe nada**: El problema está en la serialización SSE o en el cliente.

## Verificaciones Post-Fix

### ✅ Checklist
- [ ] La aplicación compila sin errores
- [ ] El endpoint retorna chunks de texto antes del evento `done`
- [ ] Los logs muestran "Emitting text chunk to client"
- [ ] El frontend puede procesar el stream correctamente
- [ ] No hay memory leaks (verificar con múltiples requests)

## Próximos Pasos

### Frontend
El frontend debe consumir el stream con EventSource o fetch:
```javascript
const response = await fetch('/api/v3/ai/explain-dashboard-stream', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({ dashboardId, fechaInicio, fechaFin, filtros })
});

const reader = response.body.getReader();
const decoder = new TextDecoder();

while (true) {
  const { done, value } = await reader.read();
  if (done) break;
  
  const chunk = decoder.decode(value);
  // Parsear SSE events y actualizar UI
}
```

## Archivos Modificados
- `src/main/java/com/cambiaso/ioc/service/ai/StreamingGeminiApiClient.java`
- `src/main/java/com/cambiaso/ioc/service/ai/StreamingDashboardExplanationService.java` (logs adicionales)

## Referencias
- [Spring WebFlux Streaming](https://docs.spring.io/spring-framework/reference/web/webflux/reactive-spring.html)
- [Server-Sent Events (SSE)](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)
- [Gemini Streaming API](https://ai.google.dev/gemini-api/docs/text-generation?lang=rest#generate-a-text-stream)
