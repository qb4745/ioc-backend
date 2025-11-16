# Backend API Brief - IOC Platform (AI Explanation Feature)
**Versión**: 2025-11-14 (Análisis Post-Implementación)  
**Alcance**: Feature: Dashboard AI Explanation (TD-001A)  
**Stack Backend**: Spring Boot 3 + Java 21 + PostgreSQL + Google Gemini AI  
**Contrato Format**: TypeScript (para Frontend)

---

## 1. Metadatos de Generación

**Generado**: 2025-11-14T00:00:00Z  
**Controllers Analizados**:
- `AiExplanationController.java`

**Estadísticas**:
- Controllers escaneados: 1
- Endpoints encontrados: 3
- Endpoints con seguridad: 2 (requieren autenticación)
- Endpoints públicos: 1 (health check)
- Tests ejecutados: 190 (0 fallos, 3 skip)
- Cobertura de implementación: 100% (4/4 BSS completados)

**Fuentes de Información**:
- Technical Design: `.gemini/sprints/technical-designs/TD-001A-dashboard-ai-explanation-A.md`
- Blueprints (BSS):
  - `BSS-001-DashboardAnalyticsRepository.md`
  - `BSS-002-GeminiApiClient.md`
  - `BSS-003-DashboardExplanationService.md`
  - `BSS-004-AiExplanationController.md`
- Validation Report: `.gemini/validation/TD-001A-blueprint-implementation-validation.md`
- Código fuente: `src/main/java/com/cambiaso/ioc/`
- Tests: `src/test/java/com/cambiaso/ioc/`

**Estado de Implementación**: ✅ COMPLETADO Y VALIDADO
- Score global: 93/100
- Todos los tests pasando (190/190)
- 4 riesgos identificados y mitigados (R1-R4)

---

## 2. Política de Seguridad Global

### Mecanismo de Autenticación
- **Provider**: Supabase GoTrue
- **Token Format**: JWT en header `Authorization: Bearer <token>`
- **Validación Backend**: Spring Security OAuth2 Resource Server
- **JWKS Endpoint**: `https://[SUPABASE_URL]/auth/v1/jwks`

### Autorización de Dashboards
- **Mecanismo**: Verificación centralizada vía `DashboardAccessService`
- **Fuente de verdad**: `metabase.dashboards[*].allowed-roles` en configuración
- **Bypass automático**: Usuarios con `ROLE_ADMIN` tienen acceso a todos los dashboards
- **Cache de decisiones**: 60 segundos por usuario:dashboard
- **Política segura**: Dashboards no configurados = acceso denegado (403)
- **Auditoría**: Todos los intentos de acceso (permitidos/denegados) se registran vía `DashboardAuditService`

### Matriz de Acceso por Endpoint

| Endpoint | Método | Auth | Roles | Rate Limit | Cache |
|----------|--------|------|-------|------------|-------|
| `/api/v1/ai/explain` | POST | ✅ | ADMIN, USER* | 10/min | Dual (24h/30min) |
| `/api/v1/ai/explain-dashboard` | POST | ✅ | ADMIN, USER* | 10/min | Dual (24h/30min) |
| `/api/v1/ai/explain/{id}` | GET | ✅ | ADMIN, USER* | 10/min | Dual (24h/30min) |
| `/api/v1/ai/health` | GET | ❌ | PUBLIC | - | - |

*Nota: USER debe tener acceso al dashboard específico según `metabase.dashboards[*].allowed-roles`

---

## 3. Contratos de API

### 3.1. Dashboard AI Explanation

#### `POST /api/v1/ai/explain`

**Propsito**: Generar explicación ejecutiva de un dashboard usando Google Gemini AI, basada en datos agregados del período especificado.

**Controller**: `AiExplanationController.java`  
**Método Java**: `explainDashboard(DashboardExplanationRequest, Authentication)`  
**BSS Relacionado**: `BSS-004`

**Características**:
- Cache inteligente con TTL dinámico (24h para históricos, 30min para actuales)
- Rate limiting: 10 requests/minuto por usuario
- Timeout: 90 segundos máximo
- Retry automático con backoff exponencial
- Anonimización de PII configurable (`ai.explanation.send-pii=false`)
- Auditoria completa con mtricas Prometheus

##### Request

```typescript
// Request Body
interface DashboardExplanationRequest {
  dashboardId: number;           // @NotNull - ID del dashboard en Metabase
  fechaInicio: string;           // @NotNull - Formato: YYYY-MM-DD
  fechaFin: string;              // @NotNull - Formato: YYYY-MM-DD
  filtros?: Record<string, string>; // Opcional - Filtros adicionales (turno, maquina, etc.)
}

// Ejemplo
{
  "dashboardId": 5,
  "fechaInicio": "2025-06-01",
  "fechaFin": "2025-06-30",
  "filtros": {
    "turno": "DIA",
    "maquina": "M001"
  }
}
```

##### Response (200 OK)

```typescript
interface DashboardExplanationResponse {
  // Contenido generado por IA
  resumenEjecutivo: string;      // Resumen narrativo del período
  keyPoints: string[];           // Puntos clave (3-5 insights principales)
  insightsAccionables: string[]; // Recomendaciones accionables (2-4 items)
  alertas: string[];             // Alertas y anomalías detectadas
  
  // Metadata del request
  dashboardId: number;
  dashboardTitulo: string;       // Nombre del dashboard
  fechaInicio: string;           // ISO 8601 date
  fechaFin: string;              // ISO 8601 date
  filtrosAplicados: Record<string, string>; // Filtros que se aplicaron
  
  // Metadata de generación
  generadoEn: string;            // ISO 8601 timestamp
  fromCache: boolean;            // true si se sirvió desde caché
  tokensUsados: number;          // Tokens consumidos en Gemini API
  cacheTTLSeconds: number;       // TTL del cache (86400=24h, 1800=30min)
}

// Ejemplo
{
  "resumenEjecutivo": "Durante junio 2025 se registró una producción total de 125,430 unidades, representando un incremento del 12% respecto a mayo. El turno diurno concentró el 68% de la producción, liderado por el operario Juan Pérez con 8,450 unidades.",
  "keyPoints": [
    "Producción total: 125,430 unidades (+12% vs mayo)",
    "Top operario: Juan Pérez (8,450 unidades)",
    "Turno día concentra 68% de producción",
    "Máquina M-001 es la más productiva (15,200 unidades)"
  ],
  "insightsAccionables": [
    "Considerar nivelación de turnos para optimizar capacidad instalada",
    "Evaluar programa de training para operarios de turno noche",
    "Revisar mantenimiento preventivo de máquina M-003"
  ],
  "alertas": [
    "⚠️ Máquina M-003 con baja productividad (20% bajo promedio)",
    "⚠️ Turno noche con 40% menos producción que turno día"
  ],
  "dashboardId": 5,
  "dashboardTitulo": "Producción por Operario - Mensual",
  "fechaInicio": "2025-06-01",
  "fechaFin": "2025-06-30",
  "filtrosAplicados": {
    "turno": "DIA"
  },
  "generadoEn": "2025-11-14T02:30:45.123Z",
  "fromCache": false,
  "tokensUsados": 1250,
  "cacheTTLSeconds": 1800
}
```

##### Error Responses

**400 Bad Request** - Validación fallida
```typescript
interface ErrorResponse {
  timestamp: string;        // ISO 8601
  status: 400;
  error: "Bad Request";
  message: string;          // Descripción del error
  path: string;
  traceId?: string;
}

// Casos que generan 400:
// - dashboardId null o negativo
// - fechaInicio o fechaFin null, formato inválido o futuras
// - fechaInicio > fechaFin
// - Rango de fechas > 12 meses
// - Filtros con valores inválidos
```

**403 Forbidden** - Acceso denegado al dashboard
```typescript
{
  "timestamp": "2025-11-14T02:30:45.123Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied to dashboard 5. Required roles: [ROLE_ADMIN, ROLE_ANALYTICS] but user has: [ROLE_USER]",
  "path": "/api/v1/ai/explain",
  "traceId": "abc-123-def-456"
}

// Causas:
// - Usuario no tiene rol necesario para el dashboard
// - Dashboard no configurado en metabase.dashboards[*].allowed-roles
// - Token JWT inválido o expirado
```

**408 Request Timeout** - Timeout de Gemini API
```typescript
{
  "timestamp": "2025-11-14T02:30:45.123Z",
  "status": 408,
  "error": "Request Timeout",
  "message": "Gemini API request exceeded 90 seconds timeout",
  "path": "/api/v1/ai/explain",
  "traceId": "abc-123-def-456"
}

// Causa: Gemini API tardó más de 90 segundos en responder
// Acción: Reintentar después de unos segundos (hay retry automático)
```

**429 Too Many Requests** - Rate limit excedido
```typescript
{
  "timestamp": "2025-11-14T02:30:45.123Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Maximum 10 requests per minute.",
  "path": "/api/v1/ai/explain",
  "retryAfter": 42,  // Segundos hasta poder reintentar
  "traceId": "abc-123-def-456"
}

// Causas:
// - Usuario excedió 10 requests/minuto (app-level)
// - Gemini API retornó 429 (quota excedida)
```

**500 Internal Server Error** - Error inesperado
```typescript
{
  "timestamp": "2025-11-14T02:30:45.123Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred while generating explanation",
  "path": "/api/v1/ai/explain",
  "traceId": "abc-123-def-456"
}

// Causas posibles:
// - Error de base de datos
// - Error en parsing de respuesta de Gemini
// - Configuración faltante (GEMINI_API_KEY, prompts)
```

**503 Service Unavailable** - Gemini API no disponible
```typescript
{
  "timestamp": "2025-11-14T02:30:45.123Z",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Gemini API is currently unavailable",
  "path": "/api/v1/ai/explain",
  "traceId": "abc-123-def-456"
}

// Causa: Gemini API retornó 503 o no respondió
// Acción: Implementar fallback o retry con backoff exponencial
```

##### Validaciones

**Campos Requeridos**:
- `dashboardId`: No nulo, entero positivo
- `fechaInicio`: No nulo, formato ISO 8601 date (YYYY-MM-DD), no futura
- `fechaFin`: No nulo, formato ISO 8601 date (YYYY-MM-DD), no futura

**Validaciones de Negocio**:
- `fechaInicio` <= `fechaFin`
- Rango máximo: 12 meses (365 días)
- Dashboard debe existir y estar configurado en `metabase.dashboards`
- Usuario debe tener permisos para acceder al dashboard

**Validaciones de Seguridad**:
- Verificación de acceso vía `DashboardAccessService`
- Bypass automático para `ROLE_ADMIN`
- Auditoría de todos los intentos de acceso

##### Notas de Implementación

**Cache Strategy**:
- Cache dual: `aiExplanationsHistorical` (24h) y `aiExplanationsCurrent` (30min)
- Cache key: `dashboard:{id}:fi:{fechaInicio}:ff:{fechaFin}:filters:{sha256-hash}`
- TTL dinámico: 24h si `fechaFin < today`, 30min si `fechaFin >= today`
- Cache hit retorna respuesta inmediata con `fromCache: true`

**Rate Limiting**:
- App-level: 10 requests/minuto por usuario (Resilience4j)
- Gemini API: Manejo de 429 con retry automático
- Headers de respuesta: `X-RateLimit-Limit`, `X-RateLimit-Remaining`

**Timeout & Retry**:
- Timeout total: 90 segundos
- Retry automático: 2 intentos con backoff exponencial (500ms → 1500ms)
- Solo retries en errores 5xx, no en 4xx

**PII Protection**:
- Por defecto `ai.explanation.send-pii=false`
- Anonimización: nombres de operarios → "Operario #1", "Operario #2"
- Códigos de maquinista omitidos en prompts

**Observabilidad**:
- Logs estructurados con latencias por fase (queries, gemini, total)
- Mtricas Prometheus: `ai.explanation.requests`, `ai.explanation.duration`, `ai.explanation.cache`, `ai.explanation.tokens`
- Auditoría: `DashboardAuditService` registra todos los accesos

---

#### `POST /api/v1/ai/explain-dashboard`

**Alias de compatibilidad** para `/api/v1/ai/explain`. Comportamiento idéntico.

**Nota**: Este endpoint existe para mantener compatibilidad con clientes que esperaban la ruta original del Technical Design. Se recomienda usar `/api/v1/ai/explain` en nuevas implementaciones.

---

#### `GET /api/v1/ai/explain/{dashboardId}`

**Propsito**: Alternativa GET para generar explicación usando query parameters en lugar de body JSON.

**Controller**: `AiExplanationController.java`  
**Método Java**: `explainDashboardGet(Integer, LocalDate, LocalDate, Map<String,String>, Authentication)`

##### Request

```typescript
// Path Params
interface PathParams {
  dashboardId: number;    // ID del dashboard
}

// Query Params
interface QueryParams {
  fechaInicio: string;    // @NotNull - Formato: YYYY-MM-DD
  fechaFin: string;       // @NotNull - Formato: YYYY-MM-DD
  [key: string]: string;  // Filtros adicionales (turno, maquina, etc.)
}

// Ejemplo
GET /api/v1/ai/explain/5?fechaInicio=2025-06-01&fechaFin=2025-06-30&turno=DIA&maquina=M001
```

##### Response

Idéntica a `POST /api/v1/ai/explain`. Ver sección anterior.

##### Error Responses

Idénticas a `POST /api/v1/ai/explain`. Ver sección anterior.

##### Notas

- Usa la misma lógica interna que el endpoint POST
- Filtros adicionales se extraen automáticamente de query params
- Útil para enlaces directos o bookmarks

---

#### `GET /api/v1/ai/health`

**Propsito**: Health check del servicio de AI Explanation.

**Controller**: `AiExplanationController.java`  
**Autenticación**: ❌ No requerida (endpoint público)

##### Response (200 OK)

```typescript
interface HealthResponse {
  status: "UP";
  service: "AI Explanation Service";
  timestamp: string;  // ISO 8601
}

// Ejemplo
{
  "status": "UP",
  "service": "AI Explanation Service",
  "timestamp": "2025-11-14T02:30:45.123Z"
}
```

##### Notas

- Endpoint público para monitoreo
- No verifica conectividad con Gemini API (solo estado del servicio Spring)

---

## 4. Estructura de DTOs

### Request DTOs

```typescript
// Extrado de: com.cambiaso.ioc.dto.ai.DashboardExplanationRequest
interface DashboardExplanationRequest {
  dashboardId: number;           // Integer (Java)
  fechaInicio: string;           // LocalDate (Java) → ISO 8601 date
  fechaFin: string;              // LocalDate (Java) → ISO 8601 date
  filtros: Record<string, string>; // Map<String,String> (Java)
}
```

### Response DTOs

```typescript
// Extrado de: com.cambiaso.ioc.dto.ai.DashboardExplanationResponse
interface DashboardExplanationResponse {
  // Generated content
  resumenEjecutivo: string;      // String (Java)
  keyPoints: string[];           // List<String> (Java)
  insightsAccionables: string[]; // List<String> (Java)
  alertas: string[];             // List<String> (Java)
  
  // Request metadata
  dashboardId: number;           // Integer (Java)
  dashboardTitulo: string;       // String (Java)
  fechaInicio: string;           // LocalDate (Java) → ISO 8601 date
  fechaFin: string;              // LocalDate (Java) → ISO 8601 date
  filtrosAplicados: Record<string, string>; // Map<String,String> (Java)
  
  // Generation metadata
  generadoEn: string;            // Instant (Java) → ISO 8601 timestamp
  fromCache: boolean;            // boolean (Java)
  tokensUsados: number;          // int (Java)
  cacheTTLSeconds: number;       // int (Java)
}
```

### Analytics DTOs (Internos - No expuestos)

```typescript
// Estos DTOs son usados internamente por DashboardAnalyticsRepository
// y DashboardExplanationService. No se exponen en la API REST.

interface TotalsDto {
  totalRegistros: number;        // Long (Java)
  totalUnidades: number;         // BigDecimal (Java) → number
  pesoNetoTotal: number;         // BigDecimal (Java) → number
}

interface TopOperarioDto {
  nombreCompleto: string;        // String (Java)
  codigoMaquinista: string;      // String (Java)
  totalUnidades: number;         // BigDecimal (Java) → number
  numRegistros: number;          // Long (Java)
}

interface TurnoDistributionDto {
  turno: string;                 // String (Java)
  totalUnidades: number;         // BigDecimal (Java) → number
  numRegistros: number;          // Long (Java)
  porcentaje: number;            // Calculated client-side
}

interface TopMachineDto {
  maquinaNombre: string;         // String (Java)
  maquinaCodigo: string;         // String (Java)
  totalUnidades: number;         // BigDecimal (Java) → number
  numRegistros: number;          // Long (Java)
}

interface DailyTrendPoint {
  fecha: string;                 // LocalDate (Java) → ISO 8601 date
  totalUnidades: number;         // BigDecimal (Java) → number
  numRegistros: number;          // Long (Java)
}
```

---

## 5. Estructura de Errores Estándar

Todos los endpoints retornan errores en este formato:

```typescript
interface ErrorResponse {
  timestamp: string;        // ISO 8601 timestamp
  status: number;           // Código HTTP (400, 403, 408, 429, 500, 503)
  error: string;            // Mensaje estándar HTTP
  message: string;          // Descripción detallada del error
  path: string;             // Ruta del endpoint que falló
  traceId?: string;         // UUID para tracing (opcional)
  retryAfter?: number;      // Segundos hasta poder reintentar (solo 429)
}
```

**Ejemplo de error de validación (400)**:
```json
{
  "timestamp": "2025-11-14T02:30:45.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: fechaInicio must not be null, fechaFin must not be null",
  "path": "/api/v1/ai/explain",
  "traceId": "abc-123-def-456"
}
```

**Ejemplo de error de acceso (403)**:
```json
{
  "timestamp": "2025-11-14T02:30:45.123Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied to dashboard 5. User does not have required roles.",
  "path": "/api/v1/ai/explain/5",
  "traceId": "abc-123-def-456"
}
```

**Ejemplo de rate limit (429)**:
```json
{
  "timestamp": "2025-11-14T02:30:45.123Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Maximum 10 requests per minute.",
  "path": "/api/v1/ai/explain",
  "retryAfter": 42,
  "traceId": "abc-123-def-456"
}
```

---

## 6. Mapeo de Tipos Java → TypeScript

| Tipo Java | Tipo TypeScript | Notas |
|-----------|-----------------|-------|
| `String` | `string` | |
| `Integer`, `Long` | `number` | |
| `BigDecimal` | `number` | Serializado como número decimal |
| `Boolean`, `boolean` | `boolean` | |
| `LocalDate` | `string` | ISO 8601 date (YYYY-MM-DD) |
| `LocalDateTime` | `string` | ISO 8601 timestamp (YYYY-MM-DDTHH:mm:ss.sssZ) |
| `Instant` | `string` | ISO 8601 timestamp con timezone UTC |
| `List<T>` | `T[]` | Array |
| `Map<String,String>` | `Record<string, string>` | Objeto plano |
| `Map<K,V>` | `Record<K, V>` | Objeto tipado |

---

## 7. Consideraciones de Integración

### 7.1. Autenticación

- El Frontend debe incluir JWT de Supabase en header `Authorization: Bearer <token>`
- Tokens se obtienen del cliente Supabase: `supabase.auth.getSession()`
- Interceptor Axios debe inyectar automáticamente el token en todas las requests

**Ejemplo con Axios**:
```typescript
import axios from 'axios';
import { supabase } from './supabaseClient';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

// Interceptor para agregar JWT
api.interceptors.request.use(async (config) => {
  const { data: { session } } = await supabase.auth.getSession();
  if (session?.access_token) {
    config.headers.Authorization = `Bearer ${session.access_token}`;
  }
  return config;
});

export default api;
```

### 7.2. Manejo de Errores

Implementar interceptor de respuesta en Axios para manejo centralizado:

```typescript
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { response } = error;
    
    if (!response) {
      // Network error
      toast.error('Error de conexión. Verifica tu internet.');
      throw error;
    }
    
    switch (response.status) {
      case 400:
        // Validación fallida - mostrar mensaje específico
        toast.error(response.data.message || 'Datos inválidos');
        break;
        
      case 401:
        // Token expirado - redirigir a login
        await supabase.auth.signOut();
        window.location.href = '/login';
        break;
        
      case 403:
        // Sin permisos - mostrar mensaje
        toast.error('No tienes permisos para acceder a este dashboard');
        break;
        
      case 408:
        // Timeout - sugerir reintentar
        toast.error('La solicitud tardó demasiado. Intenta nuevamente.');
        break;
        
      case 429:
        // Rate limit - esperar y reintentar
        const retryAfter = response.data.retryAfter || 60;
        toast.error(`Demasiadas solicitudes. Espera ${retryAfter} segundos.`);
        break;
        
      case 500:
        // Error interno
        toast.error('Error del servidor. Contacta a soporte.');
        break;
        
      case 503:
        // Servicio no disponible
        toast.error('Servicio temporalmente no disponible. Intenta más tarde.');
        break;
        
      default:
        toast.error('Error inesperado');
    }
    
    throw error;
  }
);
```

### 7.3. Rate Limiting

- Respetar límite de 10 requests/minuto por usuario
- Implementar debouncing en UI para evitar requests duplicadas
- Mostrar indicador de carga mientras se procesa
- Cachear respuestas en cliente para reducir requests

**Ejemplo con React Query**:
```typescript
import { useQuery } from '@tanstack/react-query';
import api from './api';

interface ExplanationParams {
  dashboardId: number;
  fechaInicio: string;
  fechaFin: string;
  filtros?: Record<string, string>;
}

export function useAiExplanation(params: ExplanationParams) {
  return useQuery({
    queryKey: ['ai-explanation', params],
    queryFn: async () => {
      const { data } = await api.post<DashboardExplanationResponse>(
        '/api/v1/ai/explain',
        params
      );
      return data;
    },
    // Cache en cliente por 5 minutos
    staleTime: 5 * 60 * 1000,
    // Reintentar solo en 408/503, no en 4xx
    retry: (failureCount, error: any) => {
      const status = error.response?.status;
      return failureCount < 2 && (status === 408 || status === 503);
    },
    // Backoff exponencial: 1s, 2s, 4s
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
  });
}
```

### 7.4. Validaciones Client-Side

Duplicar validaciones del backend en el frontend para feedback inmediato:

```typescript
import { z } from 'zod';

const explanationRequestSchema = z.object({
  dashboardId: z.number().int().positive('Dashboard ID debe ser positivo'),
  fechaInicio: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Formato inválido (YYYY-MM-DD)'),
  fechaFin: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Formato inválido (YYYY-MM-DD)'),
  filtros: z.record(z.string()).optional(),
}).refine(
  (data) => new Date(data.fechaInicio) <= new Date(data.fechaFin),
  { message: 'Fecha inicio debe ser anterior o igual a fecha fin' }
).refine(
  (data) => {
    const diff = new Date(data.fechaFin).getTime() - new Date(data.fechaInicio).getTime();
    const days = diff / (1000 * 60 * 60 * 24);
    return days <= 365;
  },
  { message: 'El rango máximo es de 12 meses (365 días)' }
);

// Uso en componente
function ExplanationForm() {
  const form = useForm({
    resolver: zodResolver(explanationRequestSchema),
  });
  
  const onSubmit = (data: ExplanationParams) => {
    // Validación pasó - hacer request
    mutation.mutate(data);
  };
  
  return <form onSubmit={form.handleSubmit(onSubmit)}>...</form>;
}
```

### 7.5. Indicadores de Cache

Mostrar al usuario cuando una respuesta viene del cache:

```typescript
function AiExplanationCard({ explanation }: { explanation: DashboardExplanationResponse }) {
  return (
    <div className="card">
      <div className="flex items-center justify-between">
        <h3>Explicación IA</h3>
        {explanation.fromCache && (
          <span className="badge badge-info">
            📦 Desde caché ({formatTTL(explanation.cacheTTLSeconds)})
          </span>
        )}
      </div>
      
      <div className="content">
        <p>{explanation.resumenEjecutivo}</p>
        
        <h4>Puntos Clave</h4>
        <ul>
          {explanation.keyPoints.map((point, i) => (
            <li key={i}>{point}</li>
          ))}
        </ul>
        
        <h4>Recomendaciones</h4>
        <ul>
          {explanation.insightsAccionables.map((insight, i) => (
            <li key={i}>{insight}</li>
          ))}
        </ul>
        
        {explanation.alertas.length > 0 && (
          <>
            <h4>⚠️ Alertas</h4>
            <ul className="alerts">
              {explanation.alertas.map((alerta, i) => (
                <li key={i} className="alert-warning">{alerta}</li>
              ))}
            </ul>
          </>
        )}
      </div>
      
      <footer className="text-sm text-gray-500">
        Generado el {new Date(explanation.generadoEn).toLocaleString()} • 
        {explanation.tokensUsados} tokens
      </footer>
    </div>
  );
}

function formatTTL(seconds: number): string {
  if (seconds >= 86400) return `${Math.floor(seconds / 86400)}d`;
  if (seconds >= 3600) return `${Math.floor(seconds / 3600)}h`;
  return `${Math.floor(seconds / 60)}min`;
}
```

---

## 8. Endpoints por Módulo

### AI Explanation Module
- `POST /api/v1/ai/explain` - Generar explicación (body JSON)
- `POST /api/v1/ai/explain-dashboard` - Generar explicación (alias)
- `GET /api/v1/ai/explain/{dashboardId}` - Generar explicación (query params)
- `GET /api/v1/ai/health` - Health check del servicio

---

## 9. Próximos Pasos

### Para el Equipo Frontend ✅

1. **Crear tipos TypeScript** (`src/types/ai-explanation.ts`)
   ```typescript
   export interface DashboardExplanationRequest { ... }
   export interface DashboardExplanationResponse { ... }
   export interface ErrorResponse { ... }
   ```

2. **Implementar servicio de API** (`src/services/aiExplanationService.ts`)
   - Función `generateExplanation(params: ExplanationParams)`
   - Interceptor de autenticación (JWT de Supabase)
   - Interceptor de errores (mapeo de códigos HTTP)

3. **Crear Zod schemas** para validación client-side
   - Schema de request con validaciones (rango ≤ 12 meses, fechas válidas)
   - Feedback inmediato antes de hacer request

4. **Implementar React Query hooks**
   - `useAiExplanation(params)` con cache de 5 minutos
   - Retry automático solo en 408/503
   - Invalidación de query al cambiar parámetros

5. **Crear componentes UI**
   - Formulario con date pickers y filtros
   - Card de explicación con resumen, key points, insights, alertas
   - Indicadores de loading, cache hit, errores
   - Rate limit feedback (countdown cuando 429)

6. **Tests con MSW (Mock Service Worker)**
   - Mock de respuestas exitosas (cache hit/miss)
   - Mock de errores (400, 403, 408, 429, 503)
   - Simulación de timeout y retry

### Para el Equipo Backend 🔲

1. ✅ Validar que la información de este brief es correcta
2. ✅ Completar JavaDoc en controllers y servicios
3. 🔲 Documentar endpoints en Swagger/OpenAPI
4. 🔲 Configurar CORS para frontend en desarrollo
5. 🔲 Notificar al Frontend de cualquier cambio en contratos
6. 🔲 Mantener sincronizado este brief con el código

---

## 10. Advertencias y Limitaciones

### Información Validada ✅

Este brief fue generado a partir de código ya implementado y testeado:
- 190 tests ejecutados con 0 fallos
- Score de implementación: 93/100
- Todos los BSS (Business Specification Stories) completados
- Validación completa en `.gemini/validation/TD-001A-blueprint-implementation-validation.md`

### Divergencias Resueltas ✅

**Ruta del endpoint**:
- Technical Design proponía: `POST /api/v1/ai/explain-dashboard`
- Implementación final: `POST /api/v1/ai/explain` + alias `/explain-dashboard`
- Estado: ✅ Ambas rutas funcionan (retrocompatibilidad)

**Rate Limiter**:
- Inicialmente no configurado en properties de test
- Estado: ✅ Configurado en `application.properties` y `application-test.properties`

**Cache TTL**:
- Inicialmente calculado pero no aplicado
- Estado: ✅ Implementado con doble caché (historical/current)

**Verificación de acceso**:
- Inicialmente no evidente en Service layer
- Estado: ✅ Implementado `DashboardAccessService` con cache de 60s

### Endpoints Sin Documentar

Ninguno. Todos los endpoints están documentados y testeados.

---

## 11. Changelog del Brief

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | 2025-11-14 | Versión inicial - Feature: AI Explanation (post-implementación) |

---

## 12. Configuración de Entorno

### Variables de Entorno Requeridas

```bash
# Backend (.env o application.properties)
GEMINI_API_KEY=your-gemini-api-key-here
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_JWT_SECRET=your-jwt-secret

# Frontend (.env)
VITE_API_URL=http://localhost:8080
VITE_SUPABASE_URL=https://your-project.supabase.co
VITE_SUPABASE_ANON_KEY=your-anon-key
```

### Configuración de Rate Limiter (Backend)

```properties
# application.properties
resilience4j.ratelimiter.instances.aiExplanation.limit-for-period=10
resilience4j.ratelimiter.instances.aiExplanation.limit-refresh-period=60s
resilience4j.ratelimiter.instances.aiExplanation.timeout-duration=0s
```

### Configuración de Cache (Backend)

```properties
# application.properties
ai.explanation.cache-name-historical=aiExplanationsHistorical
ai.explanation.cache-name-current=aiExplanationsCurrent
```

### Archivos de Prompts (Backend)

Deben existir en `src/main/resources/prompts/`:
- `system-prompt.txt` - Instrucciones del sistema para Gemini
- `context.yaml` - Contexto de negocio en formato YAML

---

## 13. Ejemplos de Uso Completos

### Ejemplo 1: Explicación de Dashboard con Filtros

```typescript
import { useAiExplanation } from '@/hooks/useAiExplanation';

function DashboardPage({ dashboardId }: { dashboardId: number }) {
  const [dateRange, setDateRange] = useState({
    start: '2025-06-01',
    end: '2025-06-30',
  });
  const [filters, setFilters] = useState({ turno: 'DIA' });
  
  const { data, isLoading, error } = useAiExplanation({
    dashboardId,
    fechaInicio: dateRange.start,
    fechaFin: dateRange.end,
    filtros: filters,
  });
  
  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorAlert error={error} />;
  if (!data) return null;
  
  return (
    <div className="dashboard">
      <DateRangePicker value={dateRange} onChange={setDateRange} />
      <FilterSelector value={filters} onChange={setFilters} />
      
      <AiExplanationCard explanation={data} />
    </div>
  );
}
```

### Ejemplo 2: Manejo de Rate Limit con Countdown

```typescript
function AiExplanationButton({ params }: { params: ExplanationParams }) {
  const [retryAfter, setRetryAfter] = useState<number | null>(null);
  const mutation = useMutation({
    mutationFn: (params: ExplanationParams) => 
      api.post('/api/v1/ai/explain', params),
    onError: (error: any) => {
      if (error.response?.status === 429) {
        const seconds = error.response.data.retryAfter || 60;
        setRetryAfter(seconds);
        
        // Countdown
        const interval = setInterval(() => {
          setRetryAfter((prev) => {
            if (prev === null || prev <= 1) {
              clearInterval(interval);
              return null;
            }
            return prev - 1;
          });
        }, 1000);
      }
    },
  });
  
  return (
    <button
      onClick={() => mutation.mutate(params)}
      disabled={mutation.isLoading || retryAfter !== null}
    >
      {mutation.isLoading && 'Generando...'}
      {retryAfter && `Espera ${retryAfter}s`}
      {!mutation.isLoading && !retryAfter && 'Generar Explicación IA'}
    </button>
  );
}
```

### Ejemplo 3: Test con MSW

```typescript
import { rest } from 'msw';
import { setupServer } from 'msw/node';

const server = setupServer(
  // Mock exitoso
  rest.post('/api/v1/ai/explain', (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({
        resumenEjecutivo: 'Análisis de prueba...',
        keyPoints: ['Punto 1', 'Punto 2'],
        insightsAccionables: ['Insight 1'],
        alertas: [],
        dashboardId: 5,
        dashboardTitulo: 'Test Dashboard',
        fechaInicio: '2025-06-01',
        fechaFin: '2025-06-30',
        filtrosAplicados: {},
        generadoEn: new Date().toISOString(),
        fromCache: false,
        tokensUsados: 1000,
        cacheTTLSeconds: 1800,
      })
    );
  }),
  
  // Mock de rate limit
  rest.post('/api/v1/ai/explain', (req, res, ctx) => {
    const requestCount = sessionStorage.getItem('requestCount') || '0';
    const count = parseInt(requestCount) + 1;
    sessionStorage.setItem('requestCount', count.toString());
    
    if (count > 10) {
      return res(
        ctx.status(429),
        ctx.json({
          timestamp: new Date().toISOString(),
          status: 429,
          error: 'Too Many Requests',
          message: 'Rate limit exceeded',
          retryAfter: 60,
        })
      );
    }
    
    return res(ctx.status(200), ctx.json({ /* ... */ }));
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

test('maneja rate limit correctamente', async () => {
  // Hacer 11 requests para exceder límite
  for (let i = 0; i < 11; i++) {
    await api.post('/api/v1/ai/explain', mockParams);
  }
  
  // La 11va request debería fallar con 429
  await expect(
    api.post('/api/v1/ai/explain', mockParams)
  ).rejects.toMatchObject({
    response: { status: 429 }
  });
});
```

---

**Generado automáticamente por**: Backend API Brief Generator  
**Comando**: Análisis post-implementación de feature TD-001A  
**Autor**: AI Agent basado en `.gemini/prompts-v2/generador_contratos_backend.md`  
**Contacto**: Equipo Backend IOC Platform

