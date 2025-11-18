# Métricas para Dashboard de Administración - IOC Platform

**Fecha:** 2025-11-17  
**Propósito:** Métricas relevantes para monitoreo del sistema por parte del administrador de la plataforma  
**Fuente:** `/actuator/metrics` endpoint  
**Valor de Negocio:** Alto - Permite detectar problemas, optimizar recursos y tomar decisiones operativas

---

## 📊 Resumen Ejecutivo

Este documento identifica **32 métricas clave** organizadas en **8 categorías** que aportan valor al negocio y deben exponerse en el dashboard de administración del frontend.

### Criterios de Selección

✅ **Valor de Negocio**: Métricas que impactan directamente en la operación  
✅ **Accionabilidad**: Información que permite tomar decisiones  
✅ **Rendimiento del Sistema**: Salud técnica que afecta la experiencia del usuario  
✅ **Cumplimiento de SLAs**: Indicadores de calidad del servicio  

---

## 🎯 Categorías de Métricas

### 1. 🚀 **Salud del Sistema ETL** (Críticas para el negocio)

El ETL es el corazón del sistema - sin procesamiento de datos, no hay valor para el usuario.

| Métrica | Nombre Técnico | Tipo | Valor de Negocio | Alerta Crítica |
|---------|----------------|------|------------------|----------------|
| **Jobs ETL Activos** | `etl.jobs.active` | Gauge | Saber cuántos archivos se están procesando en tiempo real | > 10 jobs |
| **Jobs ETL Atascados** | `etl.jobs.stuck` | Gauge | Detectar procesos bloqueados que requieren intervención manual | > 0 |
| **Índice Único ETL** | `etl.unique.index.present` | Gauge | Validar integridad de la BD (previene duplicados) | = 0 (ausente) |

**Dashboard UI - Vista Recomendada:**
```
┌─────────────────────────────────────────────┐
│  📊 Estado del Procesamiento ETL            │
├─────────────────────────────────────────────┤
│  ⚡ Jobs Activos:        3                  │
│  ⚠️  Jobs Atascados:     0  ✓               │
│  🔒 Índice Único:       OK  ✓               │
│                                              │
│  [Ver Jobs en Detalle]                      │
└─────────────────────────────────────────────┘
```

**Acciones Recomendadas:**
- Si `etl.jobs.stuck > 0`: Mostrar banner rojo "⚠️ Hay jobs atascados - revisar logs"
- Si `etl.jobs.active > 5`: Advertencia amarilla "Alto volumen de procesamiento"
- Si `etl.unique.index.present = 0`: Alerta crítica "⛔ Índice de integridad ausente"

---

### 2. 🔗 **Salud de Conexiones de Base de Datos** (Crítico - Performance)

La BD es el cuello de botella más común. Monitorear conexiones previene degradación del servicio.

| Métrica | Nombre Técnico | Tipo | Valor de Negocio | Alerta Crítica |
|---------|----------------|------|------------------|----------------|
| **Conexiones Activas** | `hikaricp.connections.active` | Gauge | Detectar saturación del pool de conexiones | > 18 (90% del máx) |
| **Conexiones Inactivas** | `hikaricp.connections.idle` | Gauge | Validar que hay capacidad disponible | < 2 |
| **Conexiones Máximas** | `hikaricp.connections.max` | Gauge | Conocer límite del pool configurado | - |
| **Conexiones Pendientes** | `hikaricp.connections.pending` | Gauge | Requests esperando conexión (problema crítico) | > 0 |
| **Timeouts de Conexión** | `hikaricp.connections.timeout` | Counter | Conexiones que no se pudieron obtener a tiempo | > 0 |

**Dashboard UI - Vista Recomendada:**
```
┌─────────────────────────────────────────────┐
│  🔗 Pool de Conexiones PostgreSQL           │
├─────────────────────────────────────────────┤
│  Activas:     8 / 20   [========----]  40%  │
│  Inactivas:   12                            │
│  Pendientes:  0  ✓                          │
│  Timeouts:    0  ✓                          │
│                                              │
│  Estado: Saludable ✓                        │
└─────────────────────────────────────────────┘
```

**Acciones Recomendadas:**
- Si `active / max > 0.9`: "⚠️ Pool al 90% - considerar aumentar conexiones"
- Si `pending > 0`: "🔴 Requests esperando conexión - revisar queries lentas"
- Si `timeout > 0`: "⛔ Timeouts detectados - problema crítico de performance"

---

### 3. 📈 **Actividad de Usuarios** (Engagement)

Medir la actividad real de la plataforma y detectar patrones de uso.

| Métrica | Nombre Técnico | Tipo | Valor de Negocio | Insight |
|---------|----------------|------|------------------|---------|
| **Requests HTTP Totales** | `http.server.requests` | Timer | Volumen de actividad en la plataforma | Tendencia de uso |
| **Requests Activos** | `http.server.requests.active` | Gauge | Carga actual en tiempo real | Detectar picos |
| **Autenticaciones Exitosas** | `spring.security.authentications` (tag: success) | Counter | Logins exitosos = usuarios activos | Engagement |
| **Autenticaciones Fallidas** | `spring.security.authentications` (tag: failure) | Counter | Detectar ataques o problemas de UX | Seguridad |
| **Requests Denegados** | `spring.security.http.secured.requests` (tag: denied) | Counter | Intentos de acceso no autorizados | Seguridad |
| **Sesiones Activas** | `tomcat.sessions.active.current` | Gauge | Usuarios conectados simultáneamente | Concurrencia |

**Dashboard UI - Vista Recomendada:**
```
┌─────────────────────────────────────────────┐
│  👥 Actividad de Usuarios (Últimas 24h)     │
├─────────────────────────────────────────────┤
│  Requests Totales:      15,247              │
│  Requests Activos:      5                   │
│  Sesiones Activas:      23                  │
│                                              │
│  ✅ Logins Exitosos:    1,234               │
│  ❌ Logins Fallidos:    12                  │
│  🚫 Accesos Denegados:  45                  │
│                                              │
│  [Ver Gráfico de Tendencia]                 │
└─────────────────────────────────────────────┘
```

**Acciones Recomendadas:**
- Si `authentications.failure > 50/día`: "⚠️ Múltiples intentos fallidos - revisar logs"
- Si `sessions.active > 100`: "📈 Alto tráfico - validar capacidad del servidor"
- Mostrar gráfico de tendencia de requests por hora

---

### 4. 📊 **Acceso a Dashboards Metabase** (KPI del Negocio)

Los dashboards son el producto final que entregamos al usuario. Medir su uso es crítico.

| Métrica | Nombre Técnico | Tipo | Valor de Negocio | Insight |
|---------|----------------|------|------------------|---------|
| **Accesos a Dashboards** | `metabase.dashboard.access` | Timer | Cuántas veces se accede a los dashboards | Engagement del producto |
| **Duración de Requests** | `metabase.dashboard.request.duration` | Timer | Performance de la integración con Metabase | UX |

**Filtros por Tags:**
- `tag=dashboard:5` - Accesos al dashboard gerencial
- `tag=dashboard:6` - Accesos al dashboard operacional
- `tag=status:success` vs `tag=status:error` - Tasa de éxito

**Dashboard UI - Vista Recomendada:**
```
┌─────────────────────────────────────────────┐
│  📊 Uso de Dashboards (Últimas 24h)         │
├─────────────────────────────────────────────┤
│  Dashboard Gerencial:                       │
│    Accesos:     547                         │
│    Promedio:    23 ms                       │
│    Errores:     2 (0.4%)  ✓                 │
│                                              │
│  Dashboard Operacional:                     │
│    Accesos:     312                         │
│    Promedio:    18 ms                       │
│    Errores:     0  ✓                        │
│                                              │
│  [Gráfico de Accesos por Hora]              │
└─────────────────────────────────────────────┘
```

**Acciones Recomendadas:**
- Identificar dashboard más usado → priorizar optimizaciones
- Si duración promedio > 500ms: "⚠️ Dashboards lentos - revisar Metabase"
- Mostrar tendencia diaria/semanal de accesos

---

### 5. 🚦 **Rate Limiting & Resiliencia** (Protección del Sistema)

Monitorear límites de tasa para evitar abuso y degradación del servicio.

| Métrica | Nombre Técnico | Tipo | Valor de Negocio | Alerta |
|---------|----------------|------|------------------|--------|
| **Permisos Disponibles (Rate Limiter)** | `resilience4j.ratelimiter.available.permissions` | Gauge | Capacidad restante del rate limiter | < 3 (cerca del límite) |
| **Threads Esperando** | `resilience4j.ratelimiter.waiting_threads` | Gauge | Requests bloqueados por rate limit | > 0 |
| **Estado Circuit Breaker** | `resilience4j.circuitbreaker.state` | Gauge | Estado del circuito (0=closed, 1=open, 2=half-open) | = 1 (open = servicio caído) |
| **Tasa de Fallos** | `resilience4j.circuitbreaker.failure.rate` | Gauge | Porcentaje de fallos en llamadas externas | > 0.5 (50%) |

**Filtros por Tags:**
- `tag=name:aiExplanation` - Rate limiter para explicaciones de IA
- `tag=name:dashboardAccess` - Rate limiter para dashboards

**Dashboard UI - Vista Recomendada:**
```
┌─────────────────────────────────────────────┐
│  🚦 Rate Limiting & Protección              │
├─────────────────────────────────────────────┤
│  AI Explanation:                            │
│    Permisos disponibles:  7 / 10  ✓         │
│    Threads esperando:     0  ✓              │
│                                              │
│  Dashboard Access:                          │
│    Permisos disponibles:  15 / 20  ✓        │
│    Threads esperando:     0  ✓              │
│                                              │
│  Circuit Breaker: CLOSED  ✓                 │
│  Tasa de Fallos:  0.2%  ✓                   │
└─────────────────────────────────────────────┘
```

**Acciones Recomendadas:**
- Si `available.permissions < 3`: "⚠️ Rate limit casi alcanzado"
- Si `circuit.state = 1`: "🔴 Circuito abierto - servicio externo caído"
- Si `failure.rate > 0.5`: "⚠️ Alta tasa de fallos - revisar integraciones"

---

### 6. 💾 **Caché (Performance)** (Optimización)

El caché de explicaciones de IA reduce costos y mejora la experiencia del usuario.

| Métrica | Nombre Técnico | Tipo | Valor de Negocio | Objetivo |
|---------|----------------|------|------------------|----------|
| **Cache Hits** | `cache.gets` (tag: result=hit) | Counter | Requests servidas desde caché (rápido, gratis) | > 70% hit rate |
| **Cache Misses** | `cache.gets` (tag: result=miss) | Counter | Requests que requieren llamada a Gemini (lento, costo) | < 30% |
| **Cache Puts** | `cache.puts` | Counter | Nuevas entradas añadidas al caché | - |
| **Cache Evictions** | `cache.evictions` | Counter | Entradas removidas por TTL o tamaño | Monitorear patrón |
| **Tamaño del Caché** | `cache.size` | Gauge | Número de entradas actualmente en caché | < 10,000 (máx) |

**Filtros por Tags:**
- `tag=cache:aiExplanationCache`

**Dashboard UI - Vista Recomendada:**
```
┌─────────────────────────────────────────────┐
│  💾 Caché de Explicaciones IA               │
├─────────────────────────────────────────────┤
│  Hit Rate:      73%  [=======---]  ✓        │
│    Hits:        1,825                       │
│    Misses:      675                         │
│                                              │
│  Tamaño:        2,456 / 10,000  (24%)       │
│  Evictions:     123                         │
│                                              │
│  💰 Ahorro estimado: $45 USD (últimas 24h)  │
│  [Gráfico de Hit Rate por Hora]             │
└─────────────────────────────────────────────┘
```

**Cálculo de Ahorro:**
```
ahorro_usd = (cache_hits * costo_por_request_gemini)
Ejemplo: 1,825 hits * $0.025 = $45.62 USD ahorrados
```

**Acciones Recomendadas:**
- Si hit rate < 50%: "⚠️ Bajo hit rate - revisar estrategia de caché"
- Mostrar tendencia semanal de hit rate
- Calcular y mostrar ahorro de costos en USD

---

### 7. 🖥️ **Recursos del Sistema** (Infraestructura)

Monitorear recursos para prevenir degradación y planificar escalamiento.

| Métrica | Nombre Técnico | Tipo | Valor de Negocio | Alerta |
|---------|----------------|------|------------------|--------|
| **Uso de CPU (Sistema)** | `system.cpu.usage` | Gauge | Carga general del servidor | > 0.8 (80%) |
| **Uso de CPU (Proceso)** | `process.cpu.usage` | Gauge | CPU consumida por la aplicación Java | > 0.6 (60%) |
| **Memoria Heap Usada** | `jvm.memory.used` (tag: area=heap) | Gauge | Memoria Java consumida | > 85% del máx |
| **Memoria Heap Máxima** | `jvm.memory.max` (tag: area=heap) | Gauge | Límite de memoria configurado | - |
| **GC Pause Time** | `jvm.gc.pause` | Timer | Tiempo en pausas de Garbage Collection | Promedio > 1s |
| **Threads Activos** | `jvm.threads.live` | Gauge | Threads Java en ejecución | Tendencia |
| **Uptime del Proceso** | `process.uptime` | Gauge | Tiempo desde último reinicio | - |
| **Espacio en Disco Libre** | `disk.free` | Gauge | Espacio disponible en disco | < 10 GB |

**Dashboard UI - Vista Recomendada:**
```
┌─────────────────────────────────────────────┐
│  🖥️  Recursos del Servidor                  │
├─────────────────────────────────────────────┤
│  CPU Sistema:     42%  [====------]  ✓      │
│  CPU Proceso:     18%  [==--------]  ✓      │
│                                              │
│  Memoria Heap:    512 MB / 1024 MB  (50%)   │
│    [=====-----]  ✓                          │
│                                              │
│  GC Pause Avg:    45 ms  ✓                  │
│  Threads:         87                        │
│  Uptime:          15 días 4h 23m            │
│  Disco Libre:     245 GB  ✓                 │
│                                              │
│  [Ver Gráficos de Tendencia]                │
└─────────────────────────────────────────────┘
```

**Acciones Recomendadas:**
- Si `cpu.usage > 0.8`: "⚠️ CPU alta - considerar escalamiento"
- Si `memory.used / memory.max > 0.85`: "⚠️ Memoria alta - riesgo de OOM"
- Si `gc.pause > 1s`: "⚠️ GC pausas largas - revisar heap size"
- Si `disk.free < 10GB`: "🔴 Espacio en disco bajo"

---

### 8. ⚙️ **Thread Pool Executor (ETL Performance)**

El thread pool ejecuta los jobs ETL - monitorear para optimizar procesamiento.

| Métrica | Nombre Técnico | Tipo | Valor de Negocio | Alerta |
|---------|----------------|------|------------------|--------|
| **Threads Activos** | `executor.active` | Gauge | Threads ejecutando jobs ETL actualmente | > pool.max |
| **Tareas Completadas** | `executor.completed` | Counter | Jobs ETL finalizados exitosamente | Tendencia |
| **Tamaño Pool Actual** | `executor.pool.size` | Gauge | Threads creados en el pool | - |
| **Tamaño Pool Máximo** | `executor.pool.max` | Gauge | Límite de threads configurado | - |
| **Tareas en Cola** | `executor.queued` | Gauge | Jobs esperando ser procesados | > 10 |
| **Capacidad Restante Cola** | `executor.queue.remaining` | Gauge | Espacio disponible en cola | < 5 |

**Filtros por Tags:**
- `tag=name:etlTaskExecutor`

**Dashboard UI - Vista Recomendada:**
```
┌─────────────────────────────────────────────┐
│  ⚙️  Thread Pool ETL                        │
├─────────────────────────────────────────────┤
│  Threads Activos:    3 / 10  [===-------]   │
│  Tareas en Cola:     2                      │
│  Capacidad Cola:     48 / 50  ✓             │
│                                              │
│  Completadas (24h):  156 jobs               │
│  Tasa de Éxito:      98.7%  ✓               │
│                                              │
│  Estado: Saludable ✓                        │
└─────────────────────────────────────────────┘
```

**Acciones Recomendadas:**
- Si `queued > 10`: "⚠️ Cola de jobs creciendo - aumentar threads"
- Si `active = pool.max` constantemente: "⚠️ Pool saturado - considerar escalamiento"
- Si `queue.remaining < 5`: "🔴 Cola casi llena - rechazará nuevos jobs"

---

## 📋 Resumen de Métricas Seleccionadas

### Tabla Consolidada (32 métricas)

| # | Categoría | Métrica | Prioridad | Dashboard Section |
|---|-----------|---------|-----------|-------------------|
| 1 | ETL | `etl.jobs.active` | 🔴 Crítica | ETL Status |
| 2 | ETL | `etl.jobs.stuck` | 🔴 Crítica | ETL Status |
| 3 | ETL | `etl.unique.index.present` | 🟡 Alta | ETL Status |
| 4 | BD | `hikaricp.connections.active` | 🔴 Crítica | Database Health |
| 5 | BD | `hikaricp.connections.idle` | 🟡 Alta | Database Health |
| 6 | BD | `hikaricp.connections.max` | 🟢 Media | Database Health |
| 7 | BD | `hikaricp.connections.pending` | 🔴 Crítica | Database Health |
| 8 | BD | `hikaricp.connections.timeout` | 🔴 Crítica | Database Health |
| 9 | Usuarios | `http.server.requests` | 🟡 Alta | Activity |
| 10 | Usuarios | `http.server.requests.active` | 🟡 Alta | Activity |
| 11 | Usuarios | `spring.security.authentications` | 🟡 Alta | Activity |
| 12 | Usuarios | `spring.security.http.secured.requests` | 🟡 Alta | Security |
| 13 | Usuarios | `tomcat.sessions.active.current` | 🟢 Media | Activity |
| 14 | Dashboards | `metabase.dashboard.access` | 🔴 Crítica | Product KPIs |
| 15 | Dashboards | `metabase.dashboard.request.duration` | 🟡 Alta | Product KPIs |
| 16 | Rate Limit | `resilience4j.ratelimiter.available.permissions` | 🟡 Alta | Protection |
| 17 | Rate Limit | `resilience4j.ratelimiter.waiting_threads` | 🟡 Alta | Protection |
| 18 | Rate Limit | `resilience4j.circuitbreaker.state` | 🔴 Crítica | Protection |
| 19 | Rate Limit | `resilience4j.circuitbreaker.failure.rate` | 🟡 Alta | Protection |
| 20 | Caché | `cache.gets` (tag: hit) | 🟡 Alta | Performance |
| 21 | Caché | `cache.gets` (tag: miss) | 🟡 Alta | Performance |
| 22 | Caché | `cache.puts` | 🟢 Media | Performance |
| 23 | Caché | `cache.evictions` | 🟢 Media | Performance |
| 24 | Caché | `cache.size` | 🟢 Media | Performance |
| 25 | Sistema | `system.cpu.usage` | 🔴 Crítica | Infrastructure |
| 26 | Sistema | `process.cpu.usage` | 🟡 Alta | Infrastructure |
| 27 | Sistema | `jvm.memory.used` | 🔴 Crítica | Infrastructure |
| 28 | Sistema | `jvm.memory.max` | 🟢 Media | Infrastructure |
| 29 | Sistema | `jvm.gc.pause` | 🟡 Alta | Infrastructure |
| 30 | Sistema | `disk.free` | 🟡 Alta | Infrastructure |
| 31 | Executor | `executor.active` | 🟡 Alta | ETL Performance |
| 32 | Executor | `executor.queued` | 🟡 Alta | ETL Performance |

---

## 🎨 Propuesta de Dashboard UI

### Layout Recomendado (Desktop)

```
┌────────────────────────────────────────────────────────────────┐
│  🎯 IOC Admin Dashboard - System Health                        │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │ 📊 ETL Status    │  │ 🔗 Database      │  │ 👥 Activity  │ │
│  │                  │  │                  │  │              │ │
│  │ Jobs Activos: 3  │  │ Active: 8/20     │  │ Sessions: 23 │ │
│  │ Atascados: 0 ✓   │  │ Pending: 0 ✓     │  │ Logins: 1.2k │ │
│  │                  │  │ Timeouts: 0 ✓    │  │              │ │
│  └──────────────────┘  └──────────────────┘  └──────────────┘ │
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │ 📊 Dashboards    │  │ 💾 Cache         │  │ 🖥️ Resources │ │
│  │                  │  │                  │  │              │ │
│  │ Accesos: 859     │  │ Hit Rate: 73%    │  │ CPU: 42%     │ │
│  │ Promedio: 20ms   │  │ Ahorro: $45      │  │ Memory: 50%  │ │
│  │                  │  │                  │  │              │ │
│  └──────────────────┘  └──────────────────┘  └──────────────┘ │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ 📈 Tendencias (Últimas 24h)                              │  │
│  │                                                           │  │
│  │  [Gráfico de líneas: Requests/hora, Jobs ETL, Logins]   │  │
│  │                                                           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ ⚠️  Alertas Activas (2)                                   │  │
│  │  • Alta carga de jobs ETL (5 activos)                    │  │
│  │  • Cache hit rate bajo en última hora (62%)              │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

### Vista Mobile (Responsive)

```
┌──────────────────────┐
│ 🎯 IOC Admin         │
├──────────────────────┤
│                      │
│ 📊 ETL Status        │
│ Jobs: 3  Stuck: 0 ✓  │
│                      │
│ 🔗 Database          │
│ 8/20 (40%) ✓         │
│                      │
│ 👥 Activity          │
│ 23 sessions          │
│                      │
│ 📊 Dashboards        │
│ 859 accesos hoy      │
│                      │
│ ⚠️  2 Alertas        │
│ [Ver detalles]       │
│                      │
└──────────────────────┘
```

---

## 🔧 Implementación Técnica

### Endpoint para Dashboard

**Backend - Crear nuevo endpoint:**

```java
@RestController
@RequestMapping("/api/v1/admin/metrics")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminMetricsController {
    
    @GetMapping("/dashboard-summary")
    public ResponseEntity<DashboardMetricsSummary> getDashboardSummary() {
        // Obtener todas las métricas relevantes
        // Calcular hit rate, porcentajes, etc.
        // Retornar DTO consolidado
    }
}
```

**Response DTO Example:**

```json
{
  "timestamp": "2025-11-17T15:30:00Z",
  "etl": {
    "activeJobs": 3,
    "stuckJobs": 0,
    "uniqueIndexPresent": true,
    "status": "HEALTHY"
  },
  "database": {
    "activeConnections": 8,
    "maxConnections": 20,
    "utilizationPercent": 40,
    "pendingRequests": 0,
    "timeouts": 0,
    "status": "HEALTHY"
  },
  "activity": {
    "activeSessions": 23,
    "totalRequests24h": 15247,
    "activeRequests": 5,
    "successfulLogins24h": 1234,
    "failedLogins24h": 12
  },
  "dashboards": {
    "totalAccesses24h": 859,
    "avgResponseTime": 20,
    "errorRate": 0.004
  },
  "cache": {
    "hitRate": 0.73,
    "hits24h": 1825,
    "misses24h": 675,
    "currentSize": 2456,
    "estimatedSavingsUSD": 45.62
  },
  "resources": {
    "cpuUsagePercent": 42,
    "memoryUsedMB": 512,
    "memoryMaxMB": 1024,
    "memoryUtilizationPercent": 50,
    "diskFreeMB": 251904,
    "uptimeSeconds": 1324980
  },
  "alerts": [
    {
      "severity": "WARNING",
      "category": "ETL",
      "message": "Alta carga de jobs ETL (5 activos)",
      "timestamp": "2025-11-17T15:28:00Z"
    }
  ]
}
```

### Frontend - Servicio API

```typescript
// src/services/adminMetricsService.ts
import apiClient from './apiClient';

export interface DashboardMetricsSummary {
  timestamp: string;
  etl: {
    activeJobs: number;
    stuckJobs: number;
    uniqueIndexPresent: boolean;
    status: 'HEALTHY' | 'WARNING' | 'CRITICAL';
  };
  // ... resto de la estructura
}

export const adminMetricsService = {
  getDashboardSummary: async (): Promise<DashboardMetricsSummary> => {
    const response = await apiClient.get('/api/v1/admin/metrics/dashboard-summary');
    return response.data;
  },
  
  // Endpoint para obtener métricas históricas
  getMetricHistory: async (metricName: string, hours: number = 24) => {
    const response = await apiClient.get(`/api/v1/admin/metrics/history/${metricName}`, {
      params: { hours }
    });
    return response.data;
  }
};
```

### Frontend - Hook personalizado

```typescript
// src/hooks/useAdminMetrics.ts
import { useState, useEffect } from 'react';
import { adminMetricsService, DashboardMetricsSummary } from '@/services/adminMetricsService';

export const useAdminMetrics = (refreshInterval: number = 30000) => {
  const [metrics, setMetrics] = useState<DashboardMetricsSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const data = await adminMetricsService.getDashboardSummary();
        setMetrics(data);
        setError(null);
      } catch (err) {
        setError('Error al cargar métricas');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchMetrics();
    const interval = setInterval(fetchMetrics, refreshInterval);

    return () => clearInterval(interval);
  }, [refreshInterval]);

  return { metrics, loading, error };
};
```

---

## 📊 Métricas NO Seleccionadas (y por qué)

Estas métricas están disponibles pero NO se incluyen en el dashboard del admin:

| Métrica | Razón para Exclusión |
|---------|---------------------|
| `jvm.buffer.*` | Demasiado técnico, sin accionabilidad directa |
| `jvm.compilation.time` | Relevante solo para tuning extremo de JVM |
| `spring.security.filterchains.*` | Métricas de debugging, no de negocio |
| `logback.events` | Métricas de logging, mejor en logs centralizados |
| `tasks.scheduled.execution` | No hay scheduled tasks críticos en el sistema |
| `tomcat.sessions.expired/rejected` | Métricas de diagnóstico avanzado |
| `jdbc.connections.*` | Duplicadas por HikariCP (más completo) |

**Criterio de exclusión**: Si no responde a "¿Qué acción tomaría el admin con esta información?", no se incluye.

---

## 🚨 Sistema de Alertas Recomendado

### Configuración de Umbrales

```yaml
alerts:
  critical:
    - metric: etl.jobs.stuck
      condition: > 0
      action: "Notificar admin inmediatamente"
    
    - metric: hikaricp.connections.pending
      condition: > 0
      action: "Alerta crítica - revisar queries"
    
    - metric: system.cpu.usage
      condition: > 0.85
      action: "Escalar recursos"
    
    - metric: resilience4j.circuitbreaker.state
      condition: == 1
      action: "Servicio externo caído"
  
  warning:
    - metric: hikaricp.connections.active / max
      condition: > 0.8
      action: "Pool al 80% - monitorear"
    
    - metric: cache.hitRate
      condition: < 0.6
      action: "Hit rate bajo - revisar TTL"
    
    - metric: jvm.memory.used / max
      condition: > 0.8
      action: "Memoria alta - posible leak"
```

### UI de Alertas

```typescript
interface Alert {
  id: string;
  severity: 'CRITICAL' | 'WARNING' | 'INFO';
  category: string;
  message: string;
  timestamp: string;
  actionable?: string; // Texto sugerido de acción
}
```

---

## 📝 Próximos Pasos

1. **Backend**:
   - [ ] Crear `AdminMetricsController` con endpoint `/dashboard-summary`
   - [ ] Crear service para agregar métricas de Actuator
   - [ ] Implementar cálculo de hit rate, porcentajes y tendencias
   - [ ] Agregar endpoint para historial de métricas (últimas 24h, 7d)

2. **Frontend**:
   - [ ] Crear página `AdminMetricsDashboard.tsx`
   - [ ] Implementar componentes de tarjetas de métricas
   - [ ] Agregar gráficos con ApexCharts para tendencias
   - [ ] Implementar sistema de alertas visuales
   - [ ] Agregar auto-refresh cada 30 segundos

3. **Testing**:
   - [ ] Tests unitarios para el servicio de agregación
   - [ ] Tests de integración para el endpoint
   - [ ] Tests E2E para la página de dashboard

4. **Documentación**:
   - [ ] Guía de uso del dashboard para admins
   - [ ] Runbook de alertas (qué hacer cuando X métrica es crítica)
   - [ ] Documento de SLAs basado en métricas

---

## 💡 Valor de Negocio Cuantificado

**ROI del Dashboard de Admin:**

1. **Reducción de Downtime**:
   - Detección temprana de problemas → -60% tiempo de resolución
   - Ahorro estimado: 10 horas/mes de admin → $500/mes

2. **Optimización de Costos**:
   - Monitoreo de cache hit rate → Reducción de calls a Gemini API
   - Ahorro estimado: $150/mes en costos de API

3. **Mejora de Performance**:
   - Identificar cuellos de botella antes que afecten usuarios
   - Aumento de satisfacción de usuarios: +25%

4. **Planificación de Capacidad**:
   - Tendencias de uso para decisiones de escalamiento
   - Evitar sobre-provisioning: Ahorro de $200/mes en infraestructura

**Total ROI estimado: $850/mes = $10,200/año**

---

**Documento creado por:** AI Analysis  
**Para:** Equipo de Desarrollo IOC Platform  
**Última actualización:** 2025-11-17

