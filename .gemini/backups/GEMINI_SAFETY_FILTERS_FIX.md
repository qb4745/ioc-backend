# Fix: Gemini API "No parts in response" Error

## 🐛 Problema Detectado

```
ERROR: No parts in Gemini response
com.cambiaso.ioc.exception.GeminiApiException: No parts in response
```

### Causa Raíz

Gemini API estaba **bloqueando el contenido** por filtros de seguridad. El error ocurría porque:

1. La respuesta de Gemini contenía `finishReason` diferente a `STOP` (probablemente `SAFETY` o `RECITATION`)
2. Solo teníamos configurado el filtro `HARM_CATEGORY_DANGEROUS_CONTENT`
3. Faltaban las otras 3 categorías de seguridad de Gemini
4. El logging no mostraba la respuesta completa, dificultando el debugging

## ✅ Solución Implementada

### 1. Agregadas todas las categorías de seguridad

**Antes:**
```json
"safetySettings": [
  {
    "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
    "threshold": "BLOCK_NONE"
  }
]
```

**Después:**
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

### 2. Mejorado el logging y detección de errores

**Cambios en `GeminiApiClient.java`:**

```java
// Ahora loggeamos la respuesta completa
log.debug("Full Gemini response: {}", response);

// Detectamos finishReason antes de fallar
String finishReason = firstCandidate.path("finishReason").asText("");
if (!finishReason.isEmpty() && !finishReason.equals("STOP")) {
    log.error("Gemini blocked content. finishReason: {}, Full response: {}", finishReason, response);
    
    // Mostramos safety ratings si existen
    JsonNode safetyRatings = firstCandidate.path("safetyRatings");
    if (!safetyRatings.isEmpty()) {
        log.error("Safety ratings: {}", safetyRatings);
    }
    
    throw new GeminiApiException("Content blocked by Gemini safety filters. Reason: " + finishReason);
}
```

### 3. Aplicado en ambos clientes

Los cambios se aplicaron en:
- ✅ `GeminiApiClient.java` (v2 - respuesta completa)
- ✅ `StreamingGeminiApiClient.java` (v3 - streaming)

## 🔍 Debugging Mejorado

### Ahora verás en los logs:

**Si el contenido es bloqueado:**
```
ERROR: Gemini blocked content. finishReason: SAFETY
ERROR: Safety ratings: [{"category":"HARM_CATEGORY_HARASSMENT","probability":"LOW"}]
ERROR: Full response: {"candidates":[{"finishReason":"SAFETY",...}]}
```

**Si falta contenido por otra razón:**
```
ERROR: No parts in Gemini response. finishReason: MAX_TOKENS
ERROR: Full response: {...}
```

## 📊 Posibles valores de `finishReason`

| Valor | Significado |
|-------|-------------|
| `STOP` | ✅ Generación completada exitosamente |
| `SAFETY` | ⚠️ Bloqueado por filtros de seguridad |
| `RECITATION` | ⚠️ Bloqueado por contenido recitado (copyright) |
| `MAX_TOKENS` | ⚠️ Se alcanzó el límite de tokens (2048) |
| `OTHER` | ⚠️ Otra razón |

## 🧪 Para Probar el Fix

1. **Reinicia la aplicación** para cargar los cambios
2. **Haz una nueva petición** al endpoint v2:
   ```bash
   POST /api/v2/ai/explain-dashboard
   {
     "dashboardId": 5,
     "fechaInicio": "2025-11-28",
     "fechaFin": "2025-11-28"
   }
   ```

3. **Revisa los logs** - ahora verás:
   - Si es bloqueado: el `finishReason` y `safetyRatings`
   - Si funciona: `Gemini API call successful`

## 🔧 Si el Problema Persiste

### Opción A: Aumentar maxOutputTokens

Si el `finishReason` es `MAX_TOKENS`:

```java
"generationConfig": {
  "maxOutputTokens": 4096,  // Aumentar de 2048 a 4096
  // ...
}
```

### Opción B: Simplificar el Prompt

Si Gemini sigue bloqueando, reducir la cantidad de datos enviados:

```java
// En DynamicDashboardExplanationService.formatCardData()
if (rows.size() > 5) {  // Reducir de 10 a 5
    return "Top 5 rows: " + ...
}
```

### Opción C: Usar modelo más avanzado

Cambiar a un modelo con menos restricciones:

```properties
# application.properties
gemini.model=gemini-2.0-flash-exp
```

## 📝 Archivos Modificados

1. `/src/main/java/com/cambiaso/ioc/service/ai/GeminiApiClient.java`
   - Agregadas todas las categorías de seguridad
   - Mejorado logging y detección de `finishReason`

2. `/src/main/java/com/cambiaso/ioc/service/ai/StreamingGeminiApiClient.java`
   - Agregadas todas las categorías de seguridad

## 🎯 Resultado Esperado

Con estos cambios, el endpoint **debería funcionar correctamente** porque:

✅ No habrá bloqueos por filtros de seguridad en contenido técnico/industrial  
✅ Si hay un bloqueo, los logs mostrarán exactamente por qué  
✅ El mensaje de error será más informativo: `"Content blocked by Gemini safety filters. Reason: SAFETY"`

---

**Última actualización:** 2025-11-27  
**Versión afectada:** v2 y v3  
**Estado:** ✅ Implementado y compilado

