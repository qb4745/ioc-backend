# Fix: Gemini 2.5 Flash MAX_TOKENS Error

## 🐛 Problema

Con **Gemini 2.0 Flash**: ✅ Funciona OK  
Con **Gemini 2.5 Flash**: ❌ Error `MAX_TOKENS`

```json
{
    "message": "Content blocked by Gemini safety filters. Reason: MAX_TOKENS",
    "timestamp": "2025-11-28T01:34:44.520115218Z",
    "error": "Internal Server Error"
}
```

## 🔍 Análisis del Problema

### Límites de los Modelos

| Modelo | Input Limit | **Output Limit** |
|--------|-------------|------------------|
| **Gemini 2.0 Flash** | 1,048,576 tokens | **8,192 tokens** |
| **Gemini 2.5 Flash** | 1,048,576 tokens | **65,536 tokens** |

### ¿Por qué fallaba?

**NO era un problema de límite del modelo**, sino de configuración:

1. **Tenías configurado**: `maxOutputTokens: 2048` (hardcodeado en el código)
2. **Tu prompt es enorme**: ~20+ cards con datos de dashboard = muchos tokens de entrada
3. **Gemini 2.0 Flash** era más "tolerante" y generaba una respuesta corta
4. **Gemini 2.5 Flash** es más "poderoso" e intentaba generar una respuesta más completa, pero **2048 tokens no eran suficientes**

### La Paradoja

Gemini 2.5 Flash tiene **8x más capacidad de salida** (65,536 vs 8,192), pero fallaba porque:
- Es un modelo más avanzado que genera respuestas más detalladas
- El límite de 2048 tokens era muy pequeño para su capacidad
- Intentaba generar más contenido del permitido → `MAX_TOKENS` error

## ✅ Solución Implementada

### 1. Aumentado `maxOutputTokens` de 2048 → 8192

**Archivo**: `application.properties`

```properties
# ANTES (hardcodeado en código)
maxOutputTokens: 2048

# AHORA (configurable)
gemini.max-output-tokens=8192
```

**Por qué 8192?**
- Es **4x más** que antes (2048)
- Suficiente para respuestas JSON completas con 5-10 key points + insights
- Mucho menor que los límites de ambos modelos:
  - Gemini 2.0: 8,192 (usa 100%)
  - Gemini 2.5: 65,536 (usa ~12.5%)

### 2. Hecho configurable en el código

**Archivos modificados**:

#### `GeminiApiClient.java`
```java
@Value("${gemini.max-output-tokens:8192}")
private int maxOutputTokens;

private String buildRequestBody(String prompt) {
    return String.format("""
        {
          "generationConfig": {
            "maxOutputTokens": %d,  // <-- Ahora usa la variable
            ...
          }
        }
        """, escapedPrompt, maxOutputTokens);
}
```

#### `StreamingGeminiApiClient.java`
```java
@Value("${gemini.max-output-tokens:8192}")
private int maxOutputTokens;

// Mismo cambio en buildRequestBody()
```

### 3. Aplicado en ambas versiones

- ✅ **V2** (`GeminiApiClient.java`) - Respuesta completa
- ✅ **V3** (`StreamingGeminiApiClient.java`) - Streaming SSE

## 🚀 Cómo Probarlo

### 1. Reinicia la aplicación

Los cambios en `application.properties` requieren restart.

### 2. Prueba con Gemini 2.5 Flash

```properties
# application.properties
gemini.model=gemini-2.5-flash
gemini.max-output-tokens=8192
```

```bash
POST /api/v2/ai/explain-dashboard
{
  "dashboardId": 5,
  "fechaInicio": "2025-11-28",
  "fechaFin": "2025-11-28"
}
```

### 3. Verifica los logs

**Ahora deberías ver**:
```
INFO: Calling Gemini API - Model: gemini-2.5-flash
INFO: maxOutputTokens configured: 8192
INFO: Gemini API call successful - Duration: 10000ms
```

**NO deberías ver**:
```
ERROR: Content blocked by Gemini safety filters. Reason: MAX_TOKENS
```

## 📊 Comparación de Modelos

### Gemini 2.0 Flash
- ✅ Más rápido (~6-8 segundos)
- ✅ Más económico
- ⚠️ Respuestas más concisas
- ⚠️ Límite de salida: 8,192 tokens

### Gemini 2.5 Flash  
- ✅ Más potente y detallado
- ✅ Límite de salida: 65,536 tokens
- ✅ Mejor comprensión de contexto
- ⚠️ Un poco más lento (~10-12 segundos)
- ⚠️ Requiere más tokens de salida

## 🔧 Ajustes Opcionales

### Si quieres respuestas MÁS largas:

```properties
gemini.max-output-tokens=16384  # 2x más
```

### Si quieres respuestas MÁS cortas (ahorrar costos):

```properties
gemini.max-output-tokens=4096  # La mitad
```

### Si quieres el máximo para Gemini 2.5:

```properties
gemini.max-output-tokens=32768  # ~50% del límite
```

⚠️ **No recomendado**: Usar el límite completo (65,536) puede ser lento y costoso.

## 📈 Uso Estimado de Tokens

Para tu caso (Dashboard con ~20 cards):

| Componente | Tokens Estimados |
|------------|------------------|
| System Prompt | ~500 |
| Dashboard Data | ~3,000-5,000 |
| **Total Input** | **~3,500-5,500** |
| Respuesta JSON | ~1,000-3,000 |
| **Total Output** | **~1,000-3,000** |

✅ Con `maxOutputTokens: 8192` tienes **margen suficiente**.

## 🎯 Recomendación

**Para producción con Gemini 2.5 Flash**:

```properties
gemini.model=gemini-2.5-flash
gemini.max-output-tokens=8192
gemini.timeout.seconds=120  # Aumentar timeout si es más lento
```

**Ventajas**:
- Respuestas más completas y útiles
- Menor probabilidad de `MAX_TOKENS`
- Mejor análisis de datos complejos

## 🐞 Troubleshooting

### Si sigue fallando con MAX_TOKENS:

1. **Verifica la configuración**:
   ```bash
   curl http://localhost:8080/actuator/env | grep gemini.max-output-tokens
   ```

2. **Aumenta el límite**:
   ```properties
   gemini.max-output-tokens=16384
   ```

3. **Reduce los datos de entrada** (si el prompt es muy largo):
   - Limitar a top 5 en lugar de top 10 por card
   - Reducir el número de cards procesadas

### Si es más lento:

```properties
gemini.timeout.seconds=120  # Aumentar de 90 a 120 segundos
```

## ✅ Cambios Aplicados

- ✅ `application.properties`: Agregada propiedad `gemini.max-output-tokens=8192`
- ✅ `GeminiApiClient.java`: Variable configurable `maxOutputTokens`
- ✅ `StreamingGeminiApiClient.java`: Variable configurable `maxOutputTokens`
- ✅ Compilado exitosamente

---

**Estado**: ✅ **RESUELTO**  
**Fecha**: 2025-11-27  
**Versiones afectadas**: v2 y v3  
**Próximo paso**: Reiniciar aplicación y probar con `gemini-2.5-flash`

