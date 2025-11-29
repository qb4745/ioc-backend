# AI Dashboard Explanation - Endpoint V3 con Streaming (SSE)

## 📋 Resumen

El endpoint **v3** utiliza **Server-Sent Events (SSE)** para enviar la respuesta de Gemini en tiempo real conforme se genera, mejorando significativamente la experiencia del usuario.

### Diferencias entre versiones

| Característica | V2 (Actual) | V3 (Nuevo - Streaming) |
|----------------|-------------|------------------------|
| Tiempo de espera | ~8 segundos completos | Respuesta inmediata (fragmentos) |
| Método API Gemini | `generateContent` | `streamGenerateContent` |
| Tipo de respuesta | JSON único | Server-Sent Events (SSE) |
| UX | Usuario espera pantalla en blanco | Usuario ve texto aparecer en vivo |
| Endpoint | `POST /api/v2/ai/explain-dashboard` | `POST /api/v3/ai/explain-dashboard-stream` |

---

## 🚀 Uso del Endpoint V3

### Endpoint

```
POST /api/v3/ai/explain-dashboard-stream
Content-Type: application/json
```

### Request Body (igual que v2)

```json
{
  "dashboardId": 5,
  "fechaInicio": "2025-01-01",
  "fechaFin": "2025-01-31",
  "filtros": {}
}
```

### Response

El endpoint retorna un **stream de Server-Sent Events** con el siguiente formato:

```
event: message
data: {"resumenEjecutivo":"En el periodo analizado...

event: message  
data: se observa un crecimiento del 15%...

event: message
data: en las métricas clave de producción...

event: done
data: [DONE]
```

---

## 💻 Implementación Frontend

### Opción 1: EventSource API (Recomendada)

**Limitación**: EventSource solo soporta GET. Para POST con body, usar Opción 2.

### Opción 2: Fetch API con ReadableStream (Recomendada para POST)

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
    
    if (done) {
      console.log('Stream completed');
      break;
    }

    buffer += decoder.decode(value, { stream: true });
    
    // Procesar eventos SSE del buffer
    const lines = buffer.split('\n\n');
    buffer = lines.pop(); // Guardar fragmento incompleto
    
    for (const line of lines) {
      if (line.trim() === '') continue;
      
      const eventMatch = line.match(/^event: (.+)$/m);
      const dataMatch = line.match(/^data: (.+)$/m);
      
      if (eventMatch && dataMatch) {
        const event = eventMatch[1];
        const data = dataMatch[1];
        
        if (event === 'message') {
          // Agregar fragmento al UI
          appendTextToExplanation(data);
        } else if (event === 'done') {
          console.log('Explanation complete');
          break;
        } else if (event === 'error') {
          console.error('Error:', data);
        }
      }
    }
  }
}

function appendTextToExplanation(text) {
  const container = document.getElementById('explanation-container');
  container.textContent += text;
}
```

### Opción 3: Usando librería (más simple)

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
      appendTextToExplanation(event.data);
    } else if (event.event === 'done') {
      console.log('Stream complete');
    }
  },
  onerror(err) {
    console.error('Stream error:', err);
    throw err;
  }
});
```

Instalación:
```bash
npm install @microsoft/fetch-event-source
```

---

## 🔧 Componente React Ejemplo

```jsx
import React, { useState } from 'react';
import { fetchEventSource } from '@microsoft/fetch-event-source';

function DashboardExplanationStream({ dashboardId, fechaInicio, fechaFin }) {
  const [explanation, setExplanation] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleExplain = async () => {
    setIsLoading(true);
    setExplanation('');
    setError(null);

    try {
      await fetchEventSource('/api/v3/ai/explain-dashboard-stream', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${getToken()}`
        },
        body: JSON.stringify({
          dashboardId,
          fechaInicio,
          fechaFin,
          filtros: {}
        }),
        onmessage(event) {
          if (event.event === 'message') {
            setExplanation(prev => prev + event.data);
          } else if (event.event === 'done') {
            setIsLoading(false);
          }
        },
        onerror(err) {
          setError('Error al generar explicación');
          setIsLoading(false);
          throw err;
        }
      });
    } catch (err) {
      setError(err.message);
      setIsLoading(false);
    }
  };

  return (
    <div>
      <button onClick={handleExplain} disabled={isLoading}>
        {isLoading ? 'Generando...' : 'Explicar Dashboard'}
      </button>
      
      {error && <div className="error">{error}</div>}
      
      <div className="explanation-container">
        {explanation}
        {isLoading && <span className="cursor">▊</span>}
      </div>
    </div>
  );
}
```

---

## ⚙️ Arquitectura Backend

### Flujo de Datos

```
Controller V3 (SSE)
    ↓
StreamingDashboardExplanationService
    ↓
1. Obtener metadata del dashboard (Metabase)
2. Construir prompt con datos
3. Llamar StreamingGeminiApiClient
    ↓
Gemini API (streamGenerateContent)
    ↓
Flux<String> → Flux<ServerSentEvent<String>>
    ↓
Frontend (recibe fragmentos en tiempo real)
```

### Clases Implementadas

1. **`AiExplanationControllerV3`**
   - Endpoint: `POST /api/v3/ai/explain-dashboard-stream`
   - Retorna: `Flux<ServerSentEvent<String>>`
   - Rate limiting: 5 requests/minuto (igual que v2)

2. **`StreamingDashboardExplanationService`**
   - Obtiene datos del dashboard
   - Construye el prompt
   - Coordina el streaming

3. **`StreamingGeminiApiClient`**
   - Llama a `streamGenerateContent` de Gemini
   - Parsea chunks JSON del stream
   - Retorna `Flux<String>` con fragmentos de texto

---

## 🔑 Configuración

El endpoint v3 usa la misma configuración que v2 en `application.properties`:

```properties
# Modelo Gemini (IMPORTANTE: usa el mismo modelo)
gemini.model=gemini-2.5-flash
gemini.api-key=${GEMINI_API_KEY}
gemini.base-url=https://generativelanguage.googleapis.com
gemini.timeout.seconds=90

# Rate Limiting
resilience4j.ratelimiter.instances.aiExplanation.limit-for-period=5
resilience4j.ratelimiter.instances.aiExplanation.limit-refresh-period=60s
```

---

## 🧪 Pruebas

### Curl Test

```bash
curl -N -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -d '{
       "dashboardId": 5,
       "fechaInicio": "2025-01-01",
       "fechaFin": "2025-01-31"
     }' \
     http://localhost:8080/api/v3/ai/explain-dashboard-stream
```

La opción `-N` deshabilita el buffering para ver el stream en tiempo real.

### Test Browser (Console)

```javascript
const response = await fetch('/api/v3/ai/explain-dashboard-stream', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    dashboardId: 5,
    fechaInicio: '2025-01-01',
    fechaFin: '2025-01-31'
  })
});

const reader = response.body.getReader();
const decoder = new TextDecoder();

while (true) {
  const { done, value } = await reader.read();
  if (done) break;
  console.log(decoder.decode(value));
}
```

---

## 📊 Ventajas del Streaming

1. **Mejor UX**: Usuario ve respuesta aparecer inmediatamente
2. **Percepción de velocidad**: Aunque el tiempo total sea similar, se siente más rápido
3. **Feedback visual**: Usuario sabe que el sistema está procesando
4. **Manejo de timeouts**: Menos probabilidad de timeout percibido
5. **Cancelación**: Frontend puede cancelar el stream si el usuario navega

---

## 🔄 Migración de V2 a V3

### Mantener V2
- V2 seguirá funcionando para clientes que prefieran respuesta completa
- Útil para integraciones que no soporten SSE

### Estrategia de Migración

1. **Fase 1**: Implementar v3 en paralelo (HECHO ✅)
2. **Fase 2**: Actualizar frontend para usar v3
3. **Fase 3**: A/B testing para comparar UX
4. **Fase 4**: Deprecar v2 si v3 funciona mejor

---

## 🐛 Troubleshooting

### Error: "Stream is closed"
- **Causa**: Timeout del proxy/nginx
- **Solución**: Aumentar timeout en proxy para endpoints SSE

### Error: "Cannot read property of null"
- **Causa**: Stream cerrado antes de completar
- **Solución**: Implementar manejo de reconexión en frontend

### Chunks no llegan
- **Causa**: Buffering del proxy
- **Solución**: Configurar nginx con `proxy_buffering off` para rutas SSE

---

## 📝 Notas Importantes

1. **Gemini Model**: Asegúrate de usar `gemini-2.5-flash` o superior (soporta streaming)
2. **CORS**: Si frontend está en otro dominio, configurar CORS para SSE
3. **Authentication**: El token debe enviarse en cada request (no se mantiene conexión persistente como WebSocket)
4. **Rate Limiting**: Aplica igual que v2 (5 requests/minuto por usuario)

---

## 🎯 Próximos Pasos

1. ✅ Implementar endpoint v3 con streaming
2. ⏳ Actualizar frontend para consumir SSE
3. ⏳ Agregar métricas de latencia por chunk
4. ⏳ Implementar tests de integración para streaming
5. ⏳ Documentar en Swagger/OpenAPI

---

**Endpoint V3 creado y listo para usar** 🚀

Archivos implementados:
- `AiExplanationControllerV3.java`
- `StreamingDashboardExplanationService.java`
- `StreamingGeminiApiClient.java`

