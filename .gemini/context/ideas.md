# 📊 Feature MVP: Resúmenes Ejecutivos de Dashboards con IA

## Documento de Diseño Técnico v1.0

---

## 1. Executive Summary

### Idea Ganadora
**Resúmenes Ejecutivos de Dashboards al Instante**  
Proporcionar a los usuarios de la plataforma IOC insights automáticos generados por IA cuando acceden a un dashboard de Metabase, reduciendo el tiempo de comprensión de datos de minutos a segundos.

### Decisión Arquitectónica
**Spring Boot + Gemini API (integración directa)** — Sin orquestador externo (n8n)

### Tiempo Estimado de Implementación
**8-12 horas** de desarrollo + 4 horas de testing

### ROI Esperado
- **Tiempo ahorrado por usuario:** 3-5 minutos por consulta de dashboard
- **Adopción proyectada:** 80% de usuarios activos en 2 semanas
- **Costo operativo:** ~$10-30/mes en llamadas a Gemini API

---

## 2. Definición del Problema

### Contexto Actual
Los dashboards de Metabase muestran **datos visuales** (gráficos, tablas) pero requieren que el usuario:
1. Interprete múltiples visualizaciones
2. Compare períodos manualmente
3. Identifique patrones y anomalías por sí mismo
4. Sintetice conclusiones para reportar a superiores

**Tiempo promedio:** 5-8 minutos por dashboard  
**Punto de dolor:** Gerentes con poco tiempo necesitan "el resumen ejecutivo primero"

### Oportunidad
Aprovechar **Gemini 1.5 Flash** para generar automáticamente 3-4 bullets con los hallazgos clave del dashboard, permitiendo a los usuarios captar el estado de la métrica en **menos de 10 segundos**.

---

## 3. Opciones Evaluadas

### Opción A: Spring Boot + Gemini API (Directo)
**Arquitectura:**
```
Frontend → Spring Boot → Metabase API → Gemini API → Cache (Supabase) → Frontend
```

**Características:**
- Integración directa desde el backend existente
- Endpoint REST síncrono `/api/ai/summary`
- Caché en tabla de Supabase con TTL de 10 minutos
- Fallback a resumen template si la IA falla

**Tiempo de respuesta:** 2-5 segundos

---

### Opción B: n8n como Orquestador
**Arquitectura:**
```
Frontend → Spring Boot → Webhook n8n → Metabase API → Gemini API → 
Callback/Polling → Spring Boot → Frontend
```

**Características:**
- n8n gestiona el workflow completo
- Spring Boot actúa solo como proxy
- Retry automático y control de concurrencia nativo
- Requiere infraestructura adicional (instancia n8n)

**Tiempo de respuesta:** 3-8 segundos

---

## 4. Análisis de Trade-offs

| **Dimensión** | **Spring Boot Directo** | **n8n Orchestration** | **Ganador** |
|---------------|------------------------|----------------------|-------------|
| **Latencia de respuesta** | 2–5 seg (síncrono) | 3–8 seg (webhook async) | ✅ **Spring** |
| **Complejidad arquitectónica** | Baja (1 componente nuevo) | Media-Alta (webhooks + n8n + callbacks) | ✅ **Spring** |
| **Tiempo de desarrollo MVP** | 8–12 horas | 16–24 horas | ✅ **Spring** |
| **Costo infraestructura** | $0 (usa EC2 actual) | +$15-50/mes (instancia n8n) | ✅ **Spring** |
| **Curva de aprendizaje** | Baja (stack actual) | Media (aprender n8n) | ✅ **Spring** |
| **Debugging** | IDE + breakpoints | UI de n8n + logs distribuidos | ✅ **Spring** |
| **Testing** | Unit tests estándar | Testing de workflows | ✅ **Spring** |
| **Escalabilidad async** | Requiere refactor futuro | Nativa | ✅ **n8n** |
| **Retry automático** | Manual (código Java) | Built-in | ✅ **n8n** |
| **Reutilización para otros features** | Baja | Alta (workflows modulares) | ✅ **n8n** |
| **Resiliencia** | Si Spring cae, todo cae | Desacoplado del core | ✅ **n8n** |
| **Control de concurrencia IA** | Manual | Nativo | ✅ **n8n** |
| **Observabilidad** | Centralizada (Spring logs) | Distribuida (2 sistemas) | ✅ **Spring** |
| **Auditoría de requests** | Manual (logs custom) | Automática (historial n8n) | ✅ **n8n** |

### Puntuación Final
- **Spring Boot Directo:** 9 victorias
- **n8n Orchestration:** 5 victorias

### Conclusión
Para este feature específico (síncrono, alta prioridad UX, MVP rápido), **Spring Boot directo es superior**.

---

## 5. Diseño de la Solución Ganadora

### 5.1 Arquitectura del Flujo

```
┌─────────────────────────────────────────────────────────────────┐
│                         FLUJO COMPLETO                          │
└─────────────────────────────────────────────────────────────────┘

┌──────────┐  1. GET /api/ai/summary?dashboardId=123&filters=...
│ Frontend │────────────────────────────────────────────────────┐
│  React   │                                                    │
└──────────┘                                                    ▼
     ▲                                            ┌─────────────────────────┐
     │                                            │    Spring Boot API      │
     │                                            │  AIInsightsController   │
     │                                            └───────────┬─────────────┘
     │                                                        │
     │                                            2. Validar JWT + permisos
     │                                                        │
     │                                            ┌───────────▼─────────────┐
     │                                            │   CacheService check    │
     │                                            │  (Supabase: summary_    │
     │                                            │   cache table)          │
     │                                            └───────────┬─────────────┘
     │                                                        │
     │                                            ┌───────────▼─────────────┐
     │                                     ┌──────│  Cache HIT?             │
     │                                     │  NO  └─────────────────────────┘
     │                                     │                 │ YES
     │                                     │                 │
     │                                     ▼                 ▼
     │                        ┌─────────────────────┐   [Retorna cached]
     │                        │ MetabaseService     │
     │                        │ GET /api/dashboard/ │
     │                        │ :id/cards           │
     │                        └──────────┬──────────┘
     │                                   │
     │                        3. Extraer queries de cards clave
     │                                   │
     │                        ┌──────────▼──────────┐
     │                        │ POST /api/card/:id/ │
     │                        │ query (por cada card│
     │                        │ relevante)          │
     │                        └──────────┬──────────┘
     │                                   │
     │                        4. Agregar, compactar y normalizar datos
     │                                   │
     │                        ┌──────────▼──────────┐
     │                        │ DataTransformer     │
     │                        │ - Calcular totales  │
     │                        │ - Comparar períodos │
     │                        │ - Top N categorías  │
     │                        └──────────┬──────────┘
     │                                   │
     │                        5. Construir prompt + contexto JSON
     │                                   │
     │                        ┌──────────▼──────────┐
     │                        │  GeminiService      │
     │                        │  POST /v1beta/      │
     │                        │  models/gemini-1.5- │
     │                        │  flash:generateCont.│
     │                        └──────────┬──────────┘
     │                                   │
     │                        6. Parsear response JSON
     │                                   │
     │                        ┌──────────▼──────────┐
     │                        │ Guardar en cache    │
     │                        │ (TTL: 10 min)       │
     │                        └──────────┬──────────┘
     │                                   │
     │  7. Response JSON                 │
     └───────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│ RESPONSE FORMAT                                                  │
│ {                                                                │
│   "bullets": [                                                   │
│     "Ventas totales: $1.2M (-8% vs mes anterior)",             │
│     "Región Norte lidera con $450K (+12% vs promedio)",        │
│     "Categoría Electrónica cayó 15% posiblemente por stock",   │
│     "Margen promedio se mantuvo estable en 22%"                │
│   ],                                                             │
│   "generatedAt": "2025-01-15T10:30:00Z",                        │
│   "model": "gemini-1.5-flash",                                  │
│   "cached": false,                                               │
│   "dashboardId": 123                                             │
│ }                                                                │
└──────────────────────────────────────────────────────────────────┘
```

---

### 5.2 Contrato de API

#### Request
```http
GET /api/ai/summary?dashboardId=123&filters=region:Norte,periodo:2025-W09
Authorization: Bearer <supabase_jwt_token>
```

**Query Parameters:**
- `dashboardId` (required): ID del dashboard en Metabase
- `filters` (optional): Filtros aplicados en formato `key:value,key:value`

#### Response Success (200 OK)
```json
{
  "bullets": [
    "string",
    "string",
    "string",
    "string"
  ],
  "generatedAt": "ISO8601 timestamp",
  "model": "gemini-1.5-flash",
  "cached": boolean,
  "dashboardId": number
}
```

#### Response Error (400/500)
```json
{
  "error": "string",
  "fallbackSummary": [
    "Dashboard cargado con datos del período seleccionado",
    "Consulta los gráficos para detalles específicos"
  ]
}
```

---

### 5.3 Modelo de Datos (Caché)

**Tabla: `ai_summary_cache`**

```sql
CREATE TABLE ai_summary_cache (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  dashboard_id INTEGER NOT NULL,
  filters_hash TEXT NOT NULL, -- MD5 de los filtros aplicados
  summary_bullets JSONB NOT NULL,
  model_used TEXT NOT NULL,
  tokens_consumed INTEGER,
  generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMPTZ NOT NULL, -- TTL de 10 minutos
  user_id UUID REFERENCES auth.users(id),
  
  UNIQUE(dashboard_id, filters_hash)
);

-- Índice para cleanup automático
CREATE INDEX idx_summary_cache_expires 
ON ai_summary_cache(expires_at) 
WHERE expires_at < NOW();

-- Política RLS (Row Level Security)
ALTER TABLE ai_summary_cache ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read own cached summaries"
ON ai_summary_cache FOR SELECT
USING (auth.uid() = user_id);
```

---

### 5.4 Prompt Engineering

**System Prompt (fijo):**
```
Eres un analista de datos experto que trabaja para Cambiaso. 
Tu trabajo es sintetizar dashboards empresariales en 3-4 bullets ejecutivos.

REGLAS ESTRICTAS:
- Máximo 4 bullets, mínimo 3
- Cada bullet debe ser una oración completa y accionable
- Incluye números específicos y variaciones porcentuales cuando estén disponibles
- NO inventes causas o explicaciones que no estén en los datos
- Si detectas caídas/subidas significativas (>10%), menciónalas primero
- Usa lenguaje profesional en español
- Responde SOLO con JSON válido en este formato exacto:
  { "bullets": ["...", "...", "..."] }
```

**User Prompt (dinámico):**
```json
{
  "dashboard": "Ventas Semanales Q1",
  "periodo": "2025-W09 (Feb 24 - Mar 2)",
  "filtros_activos": {
    "region": "Norte",
    "canal": "Retail"
  },
  "metricas_actuales": {
    "ventas_totales": 1234567,
    "unidades_vendidas": 8543,
    "margen_promedio": 0.22,
    "tasa_devolucion": 0.038
  },
  "comparativa_periodo_anterior": {
    "ventas_totales": 1340000,
    "variacion_pct": -0.08,
    "unidades_vendidas": 9200,
    "variacion_pct": -0.07
  },
  "top_categorias": [
    {"nombre": "Electrónica", "ventas": 500000, "variacion_vs_anterior": -0.15},
    {"nombre": "Hogar", "ventas": 320000, "variacion_vs_anterior": 0.08},
    {"nombre": "Ropa", "ventas": 280000, "variacion_vs_anterior": 0.02}
  ],
  "anomalias_detectadas": [
    "Spike en devoluciones de categoría Electrónica (12% vs promedio de 4%)"
  ]
}
```

**Expected Output:**
```json
{
  "bullets": [
    "Ventas totales de $1.23M representan una caída del 8% respecto a la semana anterior en la región Norte",
    "Categoría Electrónica lidera con $500K pero cayó 15% vs semana previa, con aumento atípico de devoluciones (12% vs 4% promedio)",
    "Hogar muestra crecimiento saludable del 8% alcanzando $320K",
    "Margen promedio se mantiene estable en 22%, indicando que la caída es por volumen, no por precio"
  ]
}
```

---

## 6. Plan de Implementación de Alto Nivel

### Fase 1: Setup e Infraestructura (2 horas)

#### 6.1 Configuración de Gemini API
- [ ] Crear cuenta en Google AI Studio
- [ ] Generar API Key de Gemini
- [ ] Agregar variable de entorno al Backend:
  ```properties
  # application.properties
  google.ai.api-key=${GOOGLE_AI_API_KEY}
  google.ai.model=gemini-1.5-flash
  google.ai.max-tokens=1024
  google.ai.temperature=0.3
  ```

#### 6.2 Crear tabla de caché en Supabase
```sql
-- Ejecutar en Supabase SQL Editor
-- (Script completo de la sección 5.3)
```

#### 6.3 Agregar dependencias Maven
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
</dependency>
```

---

### Fase 2: Backend Development (6-8 horas)

#### 6.4 Capa de Servicio - Estructura de Paquetes
```
src/main/java/com/cambiaso/ioc/
├── ai/
│   ├── controller/
│   │   └── AIInsightsController.java
│   ├── service/
│   │   ├── GeminiService.java
│   │   ├── MetabaseDataExtractor.java
│   │   ├── DataTransformerService.java
│   │   └── SummaryCacheService.java
│   ├── dto/
│   │   ├── SummaryRequest.java
│   │   ├── SummaryResponse.java
│   │   ├── DashboardContext.java
│   │   └── GeminiPrompt.java
│   └── config/
│       └── GeminiConfig.java
```

#### 6.5 Implementación por Componente

**A. AIInsightsController.java**
```java
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIInsightsController {
    
    private final SummaryCacheService cacheService;
    private final MetabaseDataExtractor metabaseExtractor;
    private final DataTransformerService transformer;
    private final GeminiService geminiService;
    
    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getDashboardSummary(
        @RequestParam Long dashboardId,
        @RequestParam(required = false) String filters,
        @AuthenticationPrincipal SupabaseUser user
    ) {
        // 1. Validar permisos del usuario para el dashboard
        // 2. Check cache
        // 3. Si no hay cache, extraer datos de Metabase
        // 4. Transformar y compactar datos
        // 5. Llamar a Gemini
        // 6. Guardar en cache
        // 7. Retornar respuesta
    }
}
```

**Estimado:** 1.5 horas

---

**B. GeminiService.java**
```java
@Service
@Slf4j
public class GeminiService {
    
    @Value("${google.ai.api-key}")
    private String apiKey;
    
    @Value("${google.ai.model}")
    private String model;
    
    private final WebClient webClient;
    
    public SummaryResponse generateSummary(DashboardContext context) {
        try {
            // Construir prompt
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(context);
            
            // Llamar a Gemini API
            GeminiRequest request = GeminiRequest.builder()
                .model(model)
                .systemInstruction(systemPrompt)
                .contents(List.of(Content.text(userPrompt)))
                .generationConfig(GenerationConfig.builder()
                    .temperature(0.3)
                    .maxOutputTokens(1024)
                    .responseMimeType("application/json")
                    .build())
                .build();
            
            GeminiResponse response = webClient.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent", model)
                .header("x-goog-api-key", apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .block(Duration.ofSeconds(10));
            
            // Parsear y validar response
            return parseResponse(response);
            
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return getFallbackSummary(context);
        }
    }
    
    private SummaryResponse getFallbackSummary(DashboardContext ctx) {
        // Template simple sin IA
        return SummaryResponse.builder()
            .bullets(List.of(
                "Dashboard cargado correctamente con datos del período seleccionado",
                "Consulta las visualizaciones para análisis detallado"
            ))
            .model("fallback-template")
            .cached(false)
            .build();
    }
}
```

**Estimado:** 2 horas

---

**C. MetabaseDataExtractor.java**
```java
@Service
@RequiredArgsConstructor
public class MetabaseDataExtractor {
    
    private final MetabaseClient metabaseClient;
    
    public DashboardRawData extractDashboardData(
        Long dashboardId, 
        Map<String, String> filters
    ) {
        // 1. GET /api/dashboard/:id para obtener estructura
        Dashboard dashboard = metabaseClient.getDashboard(dashboardId);
        
        // 2. Identificar cards "clave" (configuración previa)
        List<DashboardCard> keyCards = dashboard.getCards().stream()
            .filter(this::isKeyMetricCard)
            .collect(Collectors.toList());
        
        // 3. Ejecutar query de cada card
        Map<Long, CardQueryResult> cardResults = keyCards.stream()
            .collect(Collectors.toMap(
                DashboardCard::getId,
                card -> executeCardQuery(card, filters)
            ));
        
        return DashboardRawData.builder()
            .dashboardName(dashboard.getName())
            .filters(filters)
            .cardResults(cardResults)
            .build();
    }
    
    private boolean isKeyMetricCard(DashboardCard card) {
        // Lógica para identificar cards relevantes
        // Opción 1: Por nombre de card (contiene "Total", "KPI", etc)
        // Opción 2: Por tags en Metabase
        // Opción 3: Configuración manual en BD
        return card.getName().toLowerCase().contains("total") ||
               card.getName().toLowerCase().contains("kpi") ||
               card.getVisualizationType().equals("scalar");
    }
}
```

**Estimado:** 2 horas

---

**D. DataTransformerService.java**
```java
@Service
public class DataTransformerService {
    
    public DashboardContext transformToContext(DashboardRawData rawData) {
        // Agregar datos
        Map<String, Object> currentMetrics = aggregateCurrentPeriod(rawData);
        Map<String, Object> previousMetrics = aggregatePreviousPeriod(rawData);
        
        // Calcular variaciones
        Map<String, Double> variations = calculateVariations(
            currentMetrics, 
            previousMetrics
        );
        
        // Extraer top N categorías
        List<CategoryMetric> topCategories = extractTopCategories(rawData, 5);
        
        // Detectar anomalías simples (> 2 std dev)
        List<String> anomalies = detectSimpleAnomalies(rawData);
        
        return DashboardContext.builder()
            .dashboardName(rawData.getDashboardName())
            .period(determinePeriod(rawData))
            .activeFilters(rawData.getFilters())
            .currentMetrics(currentMetrics)
            .previousComparison(Map.of(
                "metrics", previousMetrics,
                "variations", variations
            ))
            .topCategories(topCategories)
            .detectedAnomalies(anomalies)
            .build();
    }
    
    private Map<String, Double> calculateVariations(
        Map<String, Object> current, 
        Map<String, Object> previous
    ) {
        // Implementar cálculo de % de cambio
        // Manejar divisiones por cero
        // Redondear a 2 decimales
    }
}
```

**Estimado:** 2 horas

---

**E. SummaryCacheService.java**
```java
@Service
@RequiredArgsConstructor
public class SummaryCacheService {
    
    private final SupabaseClient supabase;
    
    public Optional<SummaryResponse> getCached(
        Long dashboardId, 
        String filtersHash,
        UUID userId
    ) {
        String query = """
            SELECT * FROM ai_summary_cache
            WHERE dashboard_id = ?
              AND filters_hash = ?
              AND user_id = ?
              AND expires_at > NOW()
            LIMIT 1
            """;
        
        return supabase.query(query, dashboardId, filtersHash, userId)
            .map(this::mapToSummaryResponse);
    }
    
    public void save(
        Long dashboardId,
        String filtersHash,
        SummaryResponse summary,
        UUID userId,
        int ttlMinutes
    ) {
        String insert = """
            INSERT INTO ai_summary_cache 
            (dashboard_id, filters_hash, summary_bullets, model_used, 
             tokens_consumed, user_id, expires_at)
            VALUES (?, ?, ?::jsonb, ?, ?, ?, NOW() + INTERVAL '? minutes')
            ON CONFLICT (dashboard_id, filters_hash) 
            DO UPDATE SET 
              summary_bullets = EXCLUDED.summary_bullets,
              generated_at = NOW(),
              expires_at = NOW() + INTERVAL '? minutes'
            """;
        
        supabase.execute(insert, 
            dashboardId, 
            filtersHash, 
            toJson(summary.getBullets()),
            summary.getModel(),
            summary.getTokensConsumed(),
            userId,
            ttlMinutes,
            ttlMinutes
        );
    }
    
    @Scheduled(cron = "0 */30 * * * *") // Cada 30 min
    public void cleanupExpiredCache() {
        supabase.execute("DELETE FROM ai_summary_cache WHERE expires_at < NOW()");
    }
}
```

**Estimado:** 1.5 horas

---

### Fase 3: Frontend Integration (2 horas)

#### 6.6 Componente React

**A. Hook personalizado: `useAISummary`**
```typescript
// hooks/useAISummary.ts
export const useAISummary = (dashboardId: number, filters?: Record<string, string>) => {
  const [summary, setSummary] = useState<AISummary | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  useEffect(() => {
    const fetchSummary = async () => {
      setLoading(true);
      try {
        const queryParams = new URLSearchParams({
          dashboardId: dashboardId.toString(),
          ...(filters && { filters: encodeFilters(filters) })
        });
        
        const response = await fetch(
          `/api/ai/summary?${queryParams}`,
          {
            headers: {
              'Authorization': `Bearer ${supabaseClient.auth.session()?.access_token}`
            }
          }
        );
        
        if (!response.ok) throw new Error('Failed to fetch summary');
        
        const data = await response.json();
        setSummary(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    
    if (dashboardId) fetchSummary();
  }, [dashboardId, JSON.stringify(filters)]);
  
  return { summary, loading, error };
};
```

**B. Componente visual: `DashboardSummaryPanel`**
```typescript
// components/DashboardSummaryPanel.tsx
export const DashboardSummaryPanel: React.FC<Props> = ({ 
  dashboardId, 
  filters 
}) => {
  const { summary, loading, error } = useAISummary(dashboardId, filters);
  
  if (loading) {
    return (
      <div className="bg-blue-50 border-l-4 border-blue-500 p-4 animate-pulse">
        <div className="flex items-center gap-2">
          <Sparkles className="w-5 h-5 text-blue-600 animate-spin" />
          <span className="text-sm text-blue-700">Generando insights...</span>
        </div>
      </div>
    );
  }
  
  if (error) {
    return (
      <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4">
        <p className="text-sm text-yellow-700">
          No se pudo generar el resumen automático. 
          Consulta las visualizaciones manualmente.
        </p>
      </div>
    );
  }
  
  if (!summary) return null;
  
  return (
    <div className="bg-gradient-to-r from-purple-50 to-blue-50 border border-purple-200 rounded-lg p-5 mb-6">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <Sparkles className="w-5 h-5 text-purple-600" />
          <h3 className="font-semibold text-gray-900">Resumen Ejecutivo</h3>
        </div>
        <span className="text-xs text-gray-500">
          {summary.cached ? '📦 Cache' : '✨ Generado ahora'}
        </span>
      </div>
      
      <ul className="space-y-2">
        {summary.bullets.map((bullet, idx) => (
          <li key={idx} className="flex gap-3 text-sm text-gray-700">
            <span className="text-purple-600 font-bold mt-0.5">•</span>
            <span>{bullet}</span>
          </li>
        ))}
      </ul>
      
      <div className="mt-4 pt-3 border-t border-purple-200 flex items-center justify-between text-xs text-gray-500">
        <span>Modelo: {summary.model}</span>
        <span>{new Date(summary.generatedAt).toLocaleString('es')}</span>
      </div>
    </div>
  );
};
```

**C. Integración en `DashboardView`**
```typescript
// pages/DashboardView.tsx
export const DashboardView: React.FC = () => {
  const { dashboardId } = useParams();
  const [activeFilters, setActiveFilters] = useState({});
  
  return (
    <div className="dashboard-container">
      <DashboardHeader />
      
      {/* Nuevo componente */}
      <DashboardSummaryPanel 
        dashboardId={Number(dashboardId)} 
        filters={activeFilters} 
      />
      
      {/* Embed de Metabase existente */}
      <MetabaseEmbed 
        dashboardId={dashboardId}
        onFiltersChange={setActiveFilters}
      />
    </div>
  );
};
```

**Estimado:** 2 horas

---

### Fase 4: Testing & Optimización (4 horas)

#### 6.7 Testing Backend

**A. Unit Tests**
```java
@SpringBootTest
class GeminiServiceTest {
    
    @MockBean
    private WebClient webClient;
    
    @Test
    void shouldGenerateValidSummary() {
        // Arrange
        DashboardContext mockContext = createMockContext();
        mockGeminiResponse();
        
        // Act
        SummaryResponse result = geminiService.generateSummary(mockContext);
        
        // Assert
        assertThat(result.getBullets()).hasSize(4);
        assertThat(result.getModel()).isEqualTo("gemini-1.5-flash");
    }
    
    @Test
    void shouldReturnFallbackOnAPIError() {
        // Arrange
        when(webClient.post().retrieve()).thenThrow(WebClientException.class);
        
        // Act
        SummaryResponse result = geminiService.generateSummary(mockContext);
        
        // Assert
        assertThat(result.getModel()).isEqualTo("fallback-template");
    }
}
```

**B. Integration Tests**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AIInsightsControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockSupabaseUser
    void shouldReturnSummaryForAuthorizedUser() throws Exception {
        mockMvc.perform(get("/api/ai/summary")
                .param("dashboardId", "1")
                .header("Authorization", "Bearer valid_token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bullets").isArray())
            .andExpect(jsonPath("$.bullets.length()").value(greaterThanOrEqualTo(3)));
    }
}
```

**Estimado:** 2 horas

---

#### 6.8 Testing Frontend

**A. Component Tests (React Testing Library)**
```typescript
describe('DashboardSummaryPanel', () => {
  it('shows loading state initially', () => {
    render(<DashboardSummaryPanel dashboardId={1} />);
    expect(screen.getByText(/generando insights/i)).toBeInTheDocument();
  });
  
  it('displays bullets when loaded', async () => {
    mockAPI.get('/api/ai/summary').reply(200, mockSummaryResponse);
    
    render(<DashboardSummaryPanel dashboardId={1} />);
    
    await waitFor(() => {
      expect(screen.getByText(/ventas totales/i)).toBeInTheDocument();
    });
  });
  
  it('shows fallback on error', async () => {
    mockAPI.get('/api/ai/summary').reply(500);
    
    render(<DashboardSummaryPanel dashboardId={1} />);
    
    await waitFor(() => {
      expect(screen.getByText(/no se pudo generar/i)).toBeInTheDocument();
    });
  });
});
```

**Estimado:** 1 hora

---

#### 6.9 Optimizaciones

**A. Performance**
- [ ] Implementar circuit breaker para llamadas a Gemini (Resilience4j)
- [ ] Configurar timeout de 5 segundos máximo
- [ ] Agregar métricas de latencia (Micrometer)
- [ ] Pre-warmup de cache para dashboards más consultados

**B. Costos**
- [ ] Limitar requests por usuario: 10/hora
- [ ] Monitorear tokens consumidos diariamente
- [ ] Alert si el costo supera $50/mes

**C. UX**
- [ ] Agregar animación de "typing" a los bullets
- [ ] Permitir regenerar summary manualmente
- [ ] Botón "Feedback" (thumbs up/down) para mejorar prompts

**Estimado:** 1 hora

---

### Fase 5: Deployment & Monitoreo (2 horas)

#### 6.10 Checklist de Deployment

**Pre-deployment:**
- [ ] Ejecutar migrations de Supabase en producción
- [ ] Configurar `GOOGLE_AI_API_KEY` en variables de entorno de EC2
- [ ] Build y test en staging
- [ ] Validar políticas RLS en Supabase

**Deployment:**
```bash
# Backend
./mvnw clean package -DskipTests
scp target/ioc-backend.jar ec2-user@backend-server:/opt/ioc/
ssh ec2-user@backend-server 'sudo systemctl restart ioc-backend'

# Frontend
npm run build
aws s3 sync dist/ s3://ioc-frontend-bucket/
aws cloudfront create-invalidation --distribution-id XXX --paths "/*"
```

**Post-deployment:**
- [ ] Smoke test en producción (dashboard de prueba)
- [ ] Verificar logs de Spring Boot
- [ ] Confirmar que el cache se está creando correctamente

---

#### 6.11 Monitoreo y Alertas

**Métricas clave (Grafana/CloudWatch):**
```yaml
Dashboards:
  - Nombre: "AI Insights Performance"
    Paneles:
      - Latencia P50/P95/P99 de /api/ai/summary
      - Tasa de cache hit (goal: >70%)
      - Errores de Gemini API (rate y tipos)
      - Tokens consumidos por hora
      - Requests por usuario
      
Alertas:
  - Latencia P95 > 8 segundos durante 5 minutos
  - Error rate > 10% durante 10 minutos
  - Costo de tokens > $2/día
  - Cache hit rate < 50%
```

**Logs estructurados (JSON):**
```java
log.info("AI summary generated", 
    Map.of(
        "dashboardId", dashboardId,
        "userId", userId,
        "cached", cached,
        "latencyMs", latency,
        "tokensUsed", tokens,
        "model", model
    )
);
```

**Estimado:** 2 horas

---

## 7. Criterios de Éxito

### Métricas Técnicas
| Métrica | Target | Medición |
|---------|--------|----------|
| Latencia P95 | < 5 segundos | CloudWatch |
| Disponibilidad | > 99% | Uptime monitoring |
| Cache hit rate | > 60% | Custom metric |
| Error rate | < 5% | Log aggregation |

### Métricas de Producto
| Métrica | Target | Medición |
|---------|--------|----------|
| Adopción (usuarios que ven el panel) | > 70% en 2 semanas | Analytics |
| Feedback positivo | > 80% thumbs up | In-app voting |
| Tiempo ahorrado por usuario | 3-5 min/dashboard | User survey |
| Dashboards con summary habilitado | 100% de dashboards críticos | Config audit |

### Métricas de Negocio
| Métrica | Target | Medición |
|---------|--------|----------|
| Costo mensual | < $30 | Gemini API billing |
| ROI (tiempo ahorrado vs costo) | > 10x | Calculation |
| Reducción de consultas a IT | -20% | Ticket tracking |

---

## 8. Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Latencia alta de Gemini (>10s) | Media | Alto | Timeout + fallback template + caché agresivo |
| Costos impredecibles de API | Baja | Medio | Rate limiting + budget alerts + cap mensual |
| Prompts generan outputs inválidos | Media | Medio | JSON schema validation + retry con prompt ajustado |
| Metabase API cambia estructura | Baja | Alto | Versionado de endpoints + tests de integración |
| Usuarios confían ciegamente en IA | Media | Alto | Disclaimer visible + feedback loop + audit trail |
| Cache stale con datos críticos | Media | Medio | TTL corto (5 min) + invalidación manual + warmup post-ETL |

---

## 9. Roadmap Futuro (Post-MVP)

### Versión 1.1 (Mes 2)
- **Summaries personalizados por rol:** CFO ve métricas financieras, COO ve operacionales
- **Comparaciones inteligentes:** "vs mismo período año anterior" automático
- **Explicaciones profundas:** Click en un bullet para ver el análisis detallado

### Versión 1.2 (Mes 3)
- **Preguntas en lenguaje natural:** "¿Por qué cayeron las ventas en Norte?"
- **Alertas proactivas:** "Te notificamos cuando un KPI cambia >15%"
- **Exportar summaries:** PDF/Email del resumen ejecutivo

### Versión 2.0 (Mes 6)
- **Migración a n8n:** Para workflows asíncronos (reportes programados)
- **Multi-modelo:** A/B testing entre Gemini, Claude, GPT-4
- **Fine-tuning:** Modelo custom entrenado con feedback de usuarios

---

## 10. Apéndices

### A. Configuración Completa de `application.properties`
```properties
# Google AI Configuration
google.ai.api-key=${GOOGLE_AI_API_KEY:sk-test-key}
google.ai.base-url=https://generativelanguage.googleapis.com/v1beta
google.ai.model=gemini-1.5-flash
google.ai.max-tokens=1024
google.ai.temperature=0.3
google.ai.timeout-seconds=10

# Cache Configuration
ai.cache.enabled=true
ai.cache.ttl-minutes=10
ai.cache.cleanup-cron=0 */30 * * * *

# Rate Limiting
ai.rate-limit.enabled=true
ai.rate-limit.requests-per-user-per-hour=10

# Cost Control
ai.cost.monthly-budget-usd=50
ai.cost.alert-threshold-usd=40
```

### B. Ejemplo de Request/Response Real
```bash
# Request
curl -X GET 'http://localhost:8080/api/ai/summary?dashboardId=1&filters=region:Norte' \
  -H 'Authorization: Bearer eyJhbGc...'

# Response
{
  "bullets": [
    "Ventas totales alcanzaron $1.2M en la semana W09, representando una caída del 8% respecto a W08 ($1.31M)",
    "Región Norte (filtro aplicado) contribuyó con $450K, manteniéndose 12% por encima del promedio regional",
    "Categoría Electrónica mostró la mayor caída (-15% a $500K) con un aumento atípico en devoluciones del 12% vs 4% histórico",
    "El margen promedio se mantuvo estable en 22%, indicando que la reducción es por volumen y no por presión de precios"
  ],
  "generatedAt": "2025-01-15T14:30:22Z",
  "model": "gemini-1.5-flash",
  "cached": false,
  "dashboardId": 1,
  "tokensConsumed": 856
}
```

### C. Contactos y Referencias
- **Documentación Gemini API:** https://ai.google.dev/docs
- **Metabase API Reference:** https://www.metabase.com/docs/latest/api-documentation
- **Spring WebClient Guide:** https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html
- **Owner del Feature:** [Tu nombre] - [Email]
- **Revisor Técnico:** [Tech Lead] - [Email]

---

## Aprobaciones

| Rol | Nombre | Firma | Fecha |
|-----|--------|-------|-------|
| Product Owner | | | |
| Tech Lead | | | |
| DevOps | | | |
| Security | | | |

---

**Versión del Documento:** 1.0  
**Última Actualización:** 2025-01-15  
**Próxima Revisión:** Post-MVP (2 semanas después del deployment)

---

**FIN DEL DOCUMENTO**



# Comparativa: Spring Boot Direct vs n8n Orchestration

## Tabla de Trade-offs

| **Dimensión** | **Opción 1: Spring Boot + Gemini Directo** | **n8n como Orquestador** | **Ganador** |
|---------------|-------------------------------------------|--------------------------|-------------|
| **Latencia típica** | 2–5 seg (síncrono, respuesta directa al Frontend) | 3–8 seg (webhook → n8n → Gemini → callback o polling) | ✅ **Spring** (más rápido) |
| **Complejidad arquitectónica** | Baja: 1 componente nuevo (cliente Gemini en Spring) | Media-Alta: Añades n8n + webhooks + posible sistema de callbacks | ✅ **Spring** (menos moving parts) |
| **Tiempo de desarrollo** | 4–8 horas (endpoint + integración Gemini + caché) | 8–16 horas (workflow n8n + webhooks bidireccionales + manejo de estado) | ✅ **Spring** (MVP más rápido) |
| **Escalabilidad vertical** | Limitada: cada request bloquea un thread en Spring | Mejor: n8n puede procesar múltiples requests async independientes | ✅ **n8n** (mejor uso de recursos) |
| **Costo operativo** | Bajo: $0 adicional (usa tu EC2 actual) | Medio: Requiere instancia n8n (EC2 t3.small ~$15/mes o n8n Cloud $20–50/mes) | ✅ **Spring** (sin infra nueva) |
| **Resiliencia a fallos** | Media: Si Spring cae, todo cae (pero es tu core) | Alta: Si n8n cae, el resto de IOC sigue operativo | ✅ **n8n** (desacoplamiento) |
| **Manejo de retries** | Manual: Debes programar lógica de reintentos en Java | Nativo: n8n tiene retry automático con backoff exponencial | ✅ **n8n** (built-in) |
| **Throttling de IA** | Manual: Implementar rate limiting + queue en Spring | Nativo: n8n tiene control de concurrencia por workflow | ✅ **n8n** (menos código) |
| **Testing** | Estándar: Unit tests con Mockito + integration tests | Más complejo: Requiere testing de workflows + webhooks | ✅ **Spring** (tooling maduro) |
| **Observabilidad** | Centralizada: Todo en logs de Spring + métricas de Actuator | Distribuida: Logs de Spring + logs de n8n + trazas entre sistemas | ✅ **Spring** (single pane) |
| **Debugging** | Fácil: IDE + breakpoints + stack traces familiares | Medio: UI de n8n + logs, pero más indirecto | ✅ **Spring** (DX superior) |
| **Caché/Optimización** | Manual: Redis o tabla en Supabase + lógica de invalidación | Manual: Misma complejidad, pero puedes usar nodos de caché de n8n | **Empate** |
| **Flexibilidad para cambiar IA** | Media: Cambias código Java, redeploy | Alta: Cambias nodo en workflow, zero downtime | ✅ **n8n** (sin código) |
| **Reutilización de lógica** | Baja: Código específico para este endpoint | Alta: El workflow puede servir para otros features (reportes, alertas) | ✅ **n8n** (más modular) |
| **Control de versiones** | Estándar: Git para código Java | Medio: Workflows en JSON exportados a Git (menos legible) | ✅ **Spring** (mejor DX) |
| **Seguridad de API keys** | Alta: Variables de entorno en Spring, nunca expuestas | Alta: Variables en n8n, nunca expuestas (igual de seguro) | **Empate** |
| **Curva de aprendizaje** | Baja: Tu equipo ya conoce Spring | Media: Requiere aprender n8n + conceptos de workflows | ✅ **Spring** (skill existente) |
| **Evolución a async** | Compleja: Requiere refactor a @Async + sistema de notificaciones | Nativa: n8n ya es async, solo cambias trigger y output | ✅ **n8n** (preparado para async) |
| **Multitenancy** | Manual: Implementar isolación por usuario/empresa | Manual: Misma complejidad | **Empate** |
| **Logs de auditoría** | Manual: Guardar en tabla `ai_requests` con userId, timestamp, tokens | Automático: n8n guarda historial de ejecuciones + datos | ✅ **n8n** (built-in) |
| **Fallback strategies** | Manual: try-catch + respuesta template en Java | Visual: Nodos de error handling + rutas alternativas | ✅ **n8n** (más declarativo) |

---

## Resumen Estratégico

### **🏆 Ganador para MVP (próximos 2 meses): Spring Boot Directo**

**Justificación:**
- **Velocidad de entrega:** 50% menos tiempo de desarrollo
- **Riesgo técnico:** Zero infraestructura nueva que mantener
- **Simplicidad:** El equipo ya domina Spring; debugging trivial
- **Costo:** $0 adicional en el corto plazo

**Cuándo migrar a n8n:**
```
SI (
  requests de IA > 500/día 
  O necesitas async para UX
  O planeas 3+ features con IA en los próximos 3 meses
) ENTONCES considera n8n
```

---

## Escenarios donde n8n GANARÍA desde el inicio

| **Escenario** | **Por qué n8n es mejor** |
|---------------|-------------------------|
| **Feature asíncrona desde día 1** | Si el resumen se envía por email/Slack en lugar de mostrarse en el panel lateral, n8n evita complejidad de queues en Spring |
| **Múltiples providers de IA** | Quieres probar Gemini vs Claude vs GPT-4 sin tocar código Java |
| **Equipo sin backend developers** | Analistas de datos pueden modificar workflows sin tocar Spring |
| **Pipeline complejo:** Metabase → IA → Resumen → Email → Actualizar Supabase → Notificar Slack | n8n orquesta esto visualmente; en Spring son 6 servicios a integrar manualmente |
| **Ya tienes n8n** | Si ya lo usas para otros procesos (ETL, notificaciones), reutilizarlo es obvio |

---

## Arquitectura Híbrida (Best of Both Worlds)

```
┌─────────────┐
│  Frontend   │
└──────┬──────┘
       │ GET /api/ai/summary (síncrono, latencia crítica)
       ▼
┌─────────────────────┐
│  Spring Boot        │──► Gemini directo (para features sync)
└─────────┬───────────┘
          │ POST webhook a n8n (para features async)
          ▼
    ┌──────────┐
    │   n8n    │──► Gemini + workflows complejos
    └──────────┘
         │
         ▼
    [Email/Slack/Batch]
```

**Usa Spring directo para:**
- Resúmenes de dashboards (el usuario espera)
- Validaciones pre-ETL rápidas
- Cualquier cosa con latencia < 5 seg

**Usa n8n para:**
- Reportes programados semanales
- Alertas inteligentes asíncronas
- Monitor de errores ETL (no crítico en tiempo real)
- Generación de contenido batch

---

## Decisión en 3 preguntas

```
1. ¿El usuario espera la respuesta en pantalla YA?
   → SÍ = Spring directo
   → NO = n8n

2. ¿Tu equipo tiene tiempo/skill para aprender n8n?
   → NO = Spring directo (por ahora)
   → SÍ = Evalúa n8n

3. ¿Planeas 5+ integraciones con IA en 6 meses?
   → SÍ = Invierte en n8n ahora
   → NO = Spring es suficiente
```

---

## Mi recomendación final

**Para "Resúmenes Ejecutivos de Dashboards al Instante":**

👉 **Empieza con Spring Boot + Gemini directo**

**Razón pragmática:**  
Este feature específico es **inherentemente síncrono** (el usuario abre el panel y espera ver bullets). n8n añade complejidad sin beneficios reales aquí.

**Plan de evolución:**
1. **Semana 1-2:** Implementa en Spring, valida con usuarios
2. **Semana 3-4:** Mide latencia real y costos de tokens
3. **Mes 2:** Si detectas que cachear/pre-generar funciona mejor, añade un job async (puede ser un `@Scheduled` simple o, si ya tienes n8n para otras cosas, un workflow)

**Cuándo SÍ usar n8n para IA:**  
Cuando implementes las features de la categoría **"Features de Generación de Contenido e Insights"** (reportes semanales, análisis batch), ahí n8n brillará porque son cargas asíncronas por naturaleza.

¿Quieres que profundice en la implementación técnica de Spring + Gemini o prefieres ver cómo se vería el workflow de n8n para comparar en código real?

---

# 🎨 Mockups Visuales: Resúmenes Ejecutivos con IA

## Opción 1: Panel Lateral Colapsable (RECOMENDADA)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  IOC - Cambiaso                                    🔔 👤 Admin    ☰         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📊 Dashboard: Ventas Semanales Q1 2025                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                                             │
│  🎯 Filtros: Norte • Retail • Feb 24 - Mar 2                      [Limpiar]│
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │ ✨ Resumen Ejecutivo                              📦 Cache • hace 2min│ │
│  │ ═══════════════════════════════════════════════════════════════════════│ │
│  │                                                                        │ │
│  │  💡 Puntos Clave:                                                      │ │
│  │                                                                        │ │
│  │  • Ventas totales alcanzaron $1.2M en la semana W09,                  │ │
│  │    representando una caída del 8% respecto a W08 ($1.31M)             │ │
│  │                                                                        │ │
│  │  • Región Norte contribuyó con $450K, manteniéndose 12%               │ │
│  │    por encima del promedio regional                                   │ │
│  │                                                                        │ │
│  │  • Categoría Electrónica mostró la mayor caída (-15% a $500K)         │ │
│  │    con aumento atípico en devoluciones (12% vs 4% histórico)          │ │
│  │                                                                        │ │
│  │  • Margen promedio estable en 22%, la reducción es por volumen        │ │
│  │    y no por presión de precios                                        │ │
│  │                                                                        │ │
│  │ ───────────────────────────────────────────────────────────────────── │ │
│  │  🤖 gemini-1.5-flash • 856 tokens • 15 Ene 2025, 14:30               │ │
│  │                                                                        │ │
│  │  [🔄 Regenerar]  [👍 12]  [👎 1]  [📤 Exportar]         [✕ Cerrar]   │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                                                                        │ │
│  │               📈 METABASE DASHBOARD EMBED                              │ │
│  │                                                                        │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                   │ │
│  │  │ Ventas Tot. │  │  Margen %   │  │ Devoluciones│                   │ │
│  │  │             │  │             │  │             │                   │ │
│  │  │   $1.2M     │  │    22%      │  │    3.8%     │                   │ │
│  │  │   ▼ 8%      │  │   ━ 0%      │  │   ▲ 1.2%    │                   │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘                   │ │
│  │                                                                        │ │
│  │  ┌──────────────────────────────────────────────────────────────────┐ │ │
│  │  │ Ventas por Categoría                                             │ │ │
│  │  │ ═════════════════════                                             │ │ │
│  │  │ ██████████████████████ Electrónica  $500K                        │ │ │
│  │  │ █████████████ Hogar  $320K                                       │ │ │
│  │  │ ███████████ Ropa  $280K                                          │ │ │
│  │  └──────────────────────────────────────────────────────────────────┘ │ │
│  │                                                                        │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Características del Diseño:
- **Posición:** Sticky top, siempre visible al hacer scroll
- **Color scheme:** Gradiente sutil (purple-50 → blue-50) para diferenciarlo del contenido
- **Iconografía:** ✨ para IA, emojis para contexto visual rápido
- **Interacciones:**
  - 👍/👎 para feedback del usuario
  - 🔄 para regenerar si cambian filtros
  - ✕ para colapsar (queda una barra minimizada)

---

## Opción 2: Sidebar Deslizable (Mejor para dashboards complejos)

```
┌────────────────────────────────────────┬────────────────────────────────────┐
│  📊 Dashboard: Ventas Semanales        │ ✨ AI INSIGHTS                     │
│                                        │ ══════════════                     │
│  🎯 Norte • Retail • W09               │                                    │
│                                        │  🎯 Contexto                       │
│  ┌──────────────┐  ┌──────────────┐   │  Dashboard: Ventas Semanales       │
│  │ Ventas: $1.2M│  │ Margen: 22%  │   │  Período: Feb 24 - Mar 2          │
│  │    ▼ 8%      │  │    ━ 0%      │   │  Filtros: Norte, Retail            │
│  └──────────────┘  └──────────────┘   │                                    │
│                                        │  💡 Resumen                        │
│  ┌─────────────────────────────────┐   │                                    │
│  │ Ventas por Región               │   │  🔴 Alerta: Caída significativa   │
│  │ ═════════════                   │   │  Ventas cayeron 8% vs semana      │
│  │ ███████ Norte   $450K           │   │  anterior ($1.31M → $1.2M)        │
│  │ ████ Sur   $320K                │   │                                    │
│  │ ███ Centro $280K                │   │  ✅ Positivo: Margen estable      │
│  │ ██ Oeste $150K                  │   │  Región Norte mantiene liderazgo  │
│  └─────────────────────────────────┘   │  con $450K (+12% vs promedio)     │
│                                        │                                    │
│  ┌─────────────────────────────────┐   │  ⚠️  Atención: Electrónica        │
│  │ Top Productos                   │   │  Caída del 15% con aumento de     │
│  │ ═════════════                   │   │  devoluciones (12% vs 4%)         │
│  │ 1. Laptop Pro    $120K          │   │                                    │
│  │ 2. Smartphone X  $98K           │   │  📊 Análisis de Margen            │
│  │ 3. Tablet Mini   $87K           │   │  22% estable indica que caída     │
│  │ 4. Monitor 4K    $76K           │   │  es por volumen, no por precio    │
│  └─────────────────────────────────┘   │                                    │
│                                        │  ────────────────────────────      │
│  ┌─────────────────────────────────┐   │  🤖 Generado con gemini-1.5-flash │
│  │ Tendencia Semanal               │   │  hace 3 minutos                   │
│  │ ═════════════════               │   │                                    │
│  │     ╱╲                          │   │  [🔄 Actualizar]                  │
│  │    ╱  ╲      ╱                  │   │  [📤 Exportar PDF]                │
│  │   ╱    ╲    ╱                   │   │  [💬 Hacer pregunta]              │
│  │  ╱      ╲__╱                    │   │                                    │
│  └─────────────────────────────────┘   │  👍 ¿Te fue útil? 👎              │
│                                        │                                    │
│  [🔍 Explorar más] [📥 Exportar]       │                          [Cerrar ✕│
│                                        │                                    │
└────────────────────────────────────────┴────────────────────────────────────┘
```

### Características:
- **Ancho:** 350-400px en desktop
- **Comportamiento:** Slide-in desde la derecha con overlay translúcido
- **Scroll independiente:** El sidebar hace scroll si el contenido es largo
- **Secciones colapsables:** Cada insight puede expandirse para ver detalles

---

## Opción 3: Card Flotante Minimalista (Menos intrusivo)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  IOC - Cambiaso                                    🔔 👤 Admin    ☰         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📊 Dashboard: Ventas Semanales Q1 2025                          ✨ Ver IA  │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                                             │
│                                    ┌─────────────────────────────────────┐  │
│                                    │ ✨ Resumen IA        [━] [✕]        │  │
│                                    ├─────────────────────────────────────┤  │
│                                    │ • Ventas $1.2M (▼8%)                │  │
│                                    │ • Norte lidera: $450K               │  │
│  ┌──────────────┐                  │ • Alerta: Electrónica -15%          │  │
│  │ Ventas: $1.2M│                  │ • Margen estable: 22%               │  │
│  │    ▼ 8%      │                  │                                     │  │
│  └──────────────┘                  │ [Ver completo]   [👍]   [👎]        │  │
│                                    └─────────────────────────────────────┘  │
│  ┌──────────────┐                                                           │
│  │ Margen: 22%  │                                                           │
│  │    ━ 0%      │                                                           │
│  └──────────────┘                                                           │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │ Ventas por Categoría                                                  │ │
│  │ ═════════════════════                                                  │ │
│  │ ██████████████████████ Electrónica  $500K                            │ │
│  │ █████████████ Hogar  $320K                                           │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Características:
- **Posición:** Floating card, draggable por el usuario
- **Estado inicial:** Minimizado (solo título)
- **Expandible:** Click para ver bullets completos
- **No obstruye:** Se puede mover o minimizar completamente

---

## Opción 4: Banner Top Persistente (Máxima visibilidad)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  IOC - Cambiaso                                    🔔 👤 Admin    ☰         │
├─────────────────────────────────────────────────────────────────────────────┤
│  ✨ RESUMEN EJECUTIVO GENERADO POR IA                           [Ocultar ✕]│
├─────────────────────────────────────────────────────────────────────────────┤
│  🔴 Ventas cayeron 8% a $1.2M  │  ✅ Norte lidera +12%  │  ⚠️  Electrónica │
│     vs semana anterior          │     con $450K          │     -15% + ↑     │
│                                 │                        │     devoluciones │
├─────────────────────────────────┴────────────────────────┴──────────────────┤
│                                                                             │
│  📊 Dashboard: Ventas Semanales Q1 2025                                     │
│  🎯 Filtros: Norte • Retail • Feb 24 - Mar 2                                │
│                                                                             │
│  [Dashboard content below...]                                               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Características:
- **Visibilidad:** 100% de usuarios lo ven siempre
- **Compacto:** 1 línea por bullet, condensado
- **Priorización:** Usa emojis de estado (🔴 negativo, ✅ positivo, ⚠️ atención)
- **Riesgo:** Puede ser percibido como "banner ciego"

---

## Estados de la UI

### Estado 1: Loading (Generando)

```
┌───────────────────────────────────────────────────────────────────────┐
│ ✨ Resumen Ejecutivo                                                  │
│ ═══════════════════════════════════════════════════════════════════   │
│                                                                        │
│          ⚡ Analizando dashboard con IA...                            │
│                                                                        │
│          ┌────────────────────────────────────────┐                   │
│          │ ████████████████░░░░░░░░░░░░░░░░░░░░  │ 65%               │
│          └────────────────────────────────────────┘                   │
│                                                                        │
│          📊 Extrayendo métricas clave...                              │
│          🤖 Consultando a Gemini...                                   │
│                                                                        │
│          Tiempo estimado: ~3 segundos                                 │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

**Variante con skeleton loader:**
```
┌───────────────────────────────────────────────────────────────────────┐
│ ✨ Resumen Ejecutivo                                     [pulso animado]│
│ ═══════════════════════════════════════════════════════════════════   │
│                                                                        │
│  • ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░                   │
│                                                                        │
│  • ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░                   │
│                                                                        │
│  • ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░                   │
│                                                                        │
│  • ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░░░                     │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

---

### Estado 2: Error (Fallback)

```
┌───────────────────────────────────────────────────────────────────────┐
│ ✨ Resumen Ejecutivo                                                  │
│ ═══════════════════════════════════════════════════════════════════   │
│                                                                        │
│  ⚠️  No se pudo generar el resumen automáticamente                    │
│                                                                        │
│  El servicio de IA está temporalmente no disponible.                  │
│  Puedes consultar las visualizaciones manualmente abajo.              │
│                                                                        │
│  💡 Resumen básico:                                                   │
│  • Dashboard cargado correctamente                                    │
│  • Filtros aplicados: Norte, Retail, W09                              │
│  • Período: Feb 24 - Mar 2, 2025                                      │
│                                                                        │
│  [🔄 Reintentar]                                                       │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

---

### Estado 3: Success con Feedback Positivo

```
┌───────────────────────────────────────────────────────────────────────┐
│ ✨ Resumen Ejecutivo                              ✅ Útil para 24 usuarios│
│ ═══════════════════════════════════════════════════════════════════   │
│                                                                        │
│  💡 Puntos Clave:                                                      │
│                                                                        │
│  ✓ Ventas totales alcanzaron $1.2M en W09 (▼8% vs W08)               │
│                                                                        │
│  ✓ Región Norte contribuyó con $450K (+12% vs promedio regional)     │
│                                                                        │
│  ✓ Categoría Electrónica cayó 15% con devoluciones atípicas          │
│                                                                        │
│  ✓ Margen estable en 22%, caída es por volumen                       │
│                                                                        │
│ ────────────────────────────────────────────────────────────────────  │
│  🤖 gemini-1.5-flash • hace 2 min • 📦 desde caché                    │
│                                                                        │
│  ¿Te fue útil este resumen?  [👍 Sí (24)] [👎 No (1)]                │
│                                                                        │
│  [🔄 Regenerar]  [📤 Exportar]  [💬 Hacer una pregunta]              │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Interacciones Avanzadas

### Tooltip de Explicación (Hover en un bullet)

```
┌───────────────────────────────────────────────────────────────────────┐
│  • Categoría Electrónica cayó 15% con devoluciones atípicas          │
│     ▲                                                                  │
│     │  ┌──────────────────────────────────────────────────────┐      │
│     └──│ 📊 Detalle de este insight                           │      │
│        │                                                       │      │
│        │ Datos analizados:                                    │      │
│        │ • Ventas Electrónica: $500K (antes: $588K)           │      │
│        │ • Variación: -15% (-$88K)                            │      │
│        │ • Devoluciones: 12% (vs histórico de 4%)             │      │
│        │                                                       │      │
│        │ Fuentes:                                             │      │
│        │ • Card "Ventas por Categoría"                        │      │
│        │ • Card "Tasa de Devoluciones"                        │      │
│        │                                                       │      │
│        │ [Ver en dashboard ↗]                                 │      │
│        └───────────────────────────────────────────────────────┘      │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

---

### Modal de "Hacer una Pregunta"

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│  ╔═══════════════════════════════════════════════════════════════════════╗ │
│  ║  💬 Pregúntale a la IA sobre este Dashboard                 [Cerrar ✕]║ │
│  ╠═══════════════════════════════════════════════════════════════════════╣ │
│  ║                                                                         ║ │
│  ║  Tu pregunta:                                                           ║ │
│  ║  ┌────────────────────────────────────────────────────────────────┐    ║ │
│  ║  │ ¿Por qué cayeron las ventas de Electrónica?                   │    ║ │
│  ║  └────────────────────────────────────────────────────────────────┘    ║ │
│  ║                                                                         ║ │
│  ║  💡 Sugerencias:                                                        ║ │
│  ║  • ¿Qué producto tuvo mejor desempeño?                                 ║ │
│  ║  • ¿Cuál es la tendencia de los últimos 3 meses?                       ║ │
│  ║  • ¿Qué región necesita atención urgente?                              ║ │
│  ║                                                                         ║ │
│  ║                                           [Cancelar]  [Preguntar 🚀]   ║ │
│  ║                                                                         ║ │
│  ╚═══════════════════════════════════════════════════════════════════════╝ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### Export Preview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│  ╔═══════════════════════════════════════════════════════════════════════╗ │
│  ║  📤 Exportar Resumen Ejecutivo                            [Cerrar ✕]  ║ │
│  ╠═══════════════════════════════════════════════════════════════════════╣ │
│  ║                                                                         ║ │
│  ║  Formato:  ○ PDF  ● Email  ○ Copiar al portapapeles                   ║ │
│  ║                                                                         ║ │
│  ║  ┌──────────────────────────────────────────────────────────────────┐  ║ │
│  ║  │ Preview:                                                         │  ║ │
│  ║  │ ══════════════════════════════════════════════════════════════   │  ║ │
│  ║  │                                                                  │  ║ │
│  ║  │ RESUMEN EJECUTIVO - DASHBOARD VENTAS SEMANALES Q1               │  ║ │
│  ║  │ Generado: 15 Ene 2025, 14:30                                    │  ║ │
│  ║  │ Usuario: admin@cambiaso.com                                     │  ║ │
│  ║  │                                                                  │  ║ │
│  ║  │ HALLAZGOS CLAVE:                                                │  ║ │
│  ║  │                                                                  │  ║ │
│  ║  │ • Ventas totales alcanzaron $1.2M en W09 (▼8% vs W08)          │  ║ │
│  ║  │ • Región Norte contribuyó con $450K (+12% vs promedio)         │  ║ │
│  ║  │ • Categoría Electrónica cayó 15% con devoluciones atípicas     │  ║ │
│  ║  │ • Margen estable en 22%, caída es por volumen                  │  ║ │
│  ║  │                                                                  │  ║ │
│  ║  │ ────────────────────────────────────────────────────────────    │  ║ │
│  ║  │ Generado automáticamente por IOC AI • gemini-1.5-flash         │  ║ │
│  ║  └──────────────────────────────────────────────────────────────────┘  ║ │
│  ║                                                                         ║ │
│  ║  Para (Email):                                                          ║ │
│  ║  ┌────────────────────────────────────────────────────────────────┐    ║ │
│  ║  │ direccion@cambiaso.com; gerente@cambiaso.com                   │    ║ │
│  ║  └────────────────────────────────────────────────────────────────┘    ║ │
│  ║                                                                         ║ │
│  ║  Asunto:                                                                ║ │
│  ║  ┌────────────────────────────────────────────────────────────────┐    ║ │
│  ║  │ [IOC] Resumen Ventas Semanales - W09 2025                      │    ║ │
│  ║  └────────────────────────────────────────────────────────────────┘    ║ │
│  ║                                                                         ║ │
│  ║                                           [Cancelar]  [Enviar 📧]      ║ │
│  ║                                                                         ║ │
│  ╚═══════════════════════════════════════════════════════════════════════╝ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Versión Móvil (Responsive)

```
┌───────────────────────┐
│ ☰  IOC    🔔  👤     │
├───────────────────────┤
│                       │
│ 📊 Ventas Semanales   │
│                       │
│ [✨ Ver Resumen IA]   │
│                       │
├───────────────────────┤
│                       │
│ ┌───────────────────┐ │
│ │ Ventas: $1.2M     │ │
│ │     ▼ 8%          │ │
│ └───────────────────┘ │
│                       │
│ ┌───────────────────┐ │
│ │ Margen: 22%       │ │
│ │     ━ 0%          │ │
│ └───────────────────┘ │
│                       │
│ [Gráficos abajo...]   │
│                       │
└───────────────────────┘

// Al tocar "Ver Resumen IA":

┌───────────────────────┐
│ ✨ Resumen IA    [✕]  │
├───────────────────────┤
│                       │
│ 💡 Puntos Clave:      │
│                       │
│ • Ventas $1.2M        │
│   (▼8% vs semana      │
│   anterior)           │
│                       │
│ • Norte lidera        │
│   con $450K           │
│   (+12%)              │
│                       │
│ • Alerta:             │
│   Electrónica         │
│   -15% + ↑            │
│   devoluciones        │
│                       │
│ • Margen estable      │
│   en 22%              │
│                       │
├───────────────────────┤
│ [👍]  [👎]  [📤]     │
└───────────────────────┘
```

---

## Código React del Componente Visual (Opción 1)

```tsx
// components/AIInsightPanel.tsx
import { Sparkles, ThumbsUp, ThumbsDown, RefreshCw, Share2, X } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export const AIInsightPanel: React.FC<Props> = ({ 
  summary, 
  loading, 
  onRegenerate,
  onFeedback,
  onClose 
}) => {
  const [isMinimized, setIsMinimized] = useState(false);

  if (loading) {
    return (
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-gradient-to-r from-purple-50 to-blue-50 border border-purple-200 rounded-lg p-6 mb-6"
      >
        <div className="flex items-center gap-3">
          <Sparkles className="w-6 h-6 text-purple-600 animate-pulse" />
          <div className="flex-1">
            <div className="h-4 bg-purple-200 rounded animate-pulse mb-2 w-48" />
            <div className="h-3 bg-purple-100 rounded animate-pulse w-64" />
          </div>
        </div>
        
        <div className="mt-4 space-y-2">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="flex gap-3">
              <div className="w-2 h-2 bg-purple-400 rounded-full mt-1.5 animate-pulse" />
              <div className="flex-1 h-4 bg-purple-100 rounded animate-pulse" 
                   style={{ width: `${Math.random() * 40 + 60}%` }} />
            </div>
          ))}
        </div>
      </motion.div>
    );
  }

  if (isMinimized) {
    return (
      <motion.button
        initial={{ scale: 0.8 }}
        animate={{ scale: 1 }}
        onClick={() => setIsMinimized(false)}
        className="fixed top-20 right-4 bg-gradient-to-r from-purple-600 to-blue-600 text-white px-4 py-2 rounded-full shadow-lg hover:shadow-xl transition-shadow flex items-center gap-2 z-50"
      >
        <Sparkles className="w-4 h-4" />
        <span className="font-medium">Ver Resumen IA</span>
      </motion.button>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      className="bg-gradient-to-r from-purple-50 via-blue-50 to-indigo-50 border-2 border-purple-200 rounded-xl shadow-md hover:shadow-lg transition-shadow p-6 mb-6 relative overflow-hidden"
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-purple-100 rounded-lg">
            <Sparkles className="w-5 h-5 text-purple-600" />
          </div>
          <div>
            <h3 className="font-bold text-gray-900 text-lg">Resumen Ejecutivo</h3>
            <p className="text-xs text-gray-500">Generado automáticamente con IA</p>
          </div>
        </div>
        
        <div className="flex items-center gap-2">
          {summary.cached && (
            <span className="px-2 py-1 bg-blue-100 text-blue-700 text-xs rounded-full flex items-center gap-1">
              📦 Cache • hace {summary.cacheAge}
            </span>
          )}
          <button
            onClick={() => setIsMinimized(true)}
            className="p-1 hover:bg-purple-100 rounded transition-colors"
          >
            <X className="w-4 h-4 text-gray-500" />
          </button>
        </div>
      </div>

      {/* Bullets */}
      <div className="space-y-3 mb-5">
        {summary.bullets.map((bullet, idx) => (
          <motion.div
            key={idx}
            initial={{ opacity: 0, x: -10 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: idx * 0.1 }}
            className="flex gap-3 group"
          >
            <span className="text-purple-600 font-bold mt-1 text-lg">•</span>
            <p className="text-gray-800 text-sm leading-relaxed flex-1 group-hover:text-gray-900 transition-colors">
              {highlightNumbers(bullet)}
            </p>
          </motion.div>
        ))}
      </div>

      {/* Footer */}
      <div className="pt-4 border-t border-purple-200">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2 text-xs text-gray-500">
            <span className="px-2 py-1 bg-gray-100 rounded font-mono">
              🤖 {summary.model}
            </span>
            <span>•</span>
            <span>{summary.tokensConsumed} tokens</span>
            <span>•</span>
            <span>{new Date(summary.generatedAt).toLocaleString('es')}</span>
          </div>
        </div>

        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-600">¿Te fue útil?</span>
            <button
              onClick={() => onFeedback('positive')}
              className="p-2 hover:bg-green-50 rounded-lg transition-colors group"
            >
              <ThumbsUp className="w-4 h-4 text-gray-400 group-hover:text-green-600" />
              <span className="text-xs text-gray-500 ml-1">12</span>
            </button>
            <button
              onClick={() => onFeedback('negative')}
              className="p-2 hover:bg-red-50 rounded-lg transition-colors group"
            >
              <ThumbsDown className="w-4 h-4 text-gray-400 group-hover:text-red-600" />
              <span className="text-xs text-gray-500 ml-1">1</span>
            </button>
          </div>

          <div className="flex gap-2">
            <button
              onClick={onRegenerate}
              className="px-3 py-1.5 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors flex items-center gap-2 text-sm font-medium text-gray-700"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              Regenerar
            </button>
            <button
              className="px-3 py-1.5 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center gap-2 text-sm font-medium"
            >
              <Share2 className="w-3.5 h-3.5" />
              Exportar
            </button>
          </div>
        </div>
      </div>

      {/* Decorative gradient overlay */}
      <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-purple-200/30 to-transparent rounded-full blur-3xl -z-10" />
    </motion.div>
  );
};

// Helper para resaltar números y porcentajes
const highlightNumbers = (text: string) => {
  return text.split(/(\$[\d,]+[KM]?|\d+%|[+-]?\d+%)/g).map((part, i) => {
    if (part.match(/\$[\d,]+[KM]?/)) {
      return <span key={i} className="font-bold text-blue-700">{part}</span>;
    }
    if (part.match(/[+-]?\d+%/)) {
      const isNegative = part.startsWith('-') || part.includes('▼');
      return (
        <span key={i} className={`font-bold ${isNegative ? 'text-red-600' : 'text-green-600'}`}>
          {part}
        </span>
      );
    }
    return part;
  });
};
```

---

## Recomendación Final

### **Opción ganadora: Panel Lateral Colapsable (Opción 1)**

**Por qué:**
- ✅ **Balance perfecto** entre visibilidad y no-intrusión
- ✅ **Sticky**: Siempre accesible sin scroll
- ✅ **Colapsable**: El usuario tiene control
- ✅ **Espacio suficiente**: Para 3-4 bullets sin truncar
- ✅ **Contextual**: Aparece cuando el dashboard carga, natural en el flujo

**Implementación rápida:**
1. Usa el componente React de arriba
2. Posición: `sticky top-4 z-10` 
3. Ancho: `100%` del contenedor del dashboard
4. Altura máxima: `auto` (crece con contenido, máx 400px)

¿Quieres que profundice en alguna parte del diseño o generemos una versión interactiva en CodeSandbox/Figma?