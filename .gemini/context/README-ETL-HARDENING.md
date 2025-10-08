# ETL Hardening - Estado Operacional

## 🚨 Quick Reference Card (Para Operadores)

```bash
# Verificación diaria esencial (5 minutos)
curl -s localhost:8080/actuator/prometheus | grep -E "(etl_jobs_stuck|etl_sync_collisions|etl_unique_index_present)"

# Query crítica de integridad (SQL)
SELECT COUNT(*) - COUNT(DISTINCT fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log) 
FROM fact_production WHERE fecha_contabilizacion >= CURRENT_DATE - INTERVAL '7 days';
-- Resultado esperado: 0

# Jobs zombis manuales
SELECT job_id, status, created_at FROM etl_jobs 
WHERE status IN ('INICIADO','PROCESANDO','SINCRONIZANDO') AND created_at < NOW() - INTERVAL '1 hour';
-- Resultado esperado: vacío
```

## Estado Actual del Sistema

### ✅ FUNCIONALIDADES IMPLEMENTADAS Y OPERATIVAS

- **Retry automático en colisiones UNIQUE** - Absorbe colisiones concurrentes (0 fallos propagados)
- **Índice UNIQUE natural** - `uq_fact_prod_natural` previene duplicados en BD
- **Advisory locks PostgreSQL** - Serialización de ventanas solapadas
- **Job duration tracking** - Timer con tags de status (EXITO/FALLO)
- **Tests de integración** - Validación de concurrencia y retry
- **Health indicator básico** - `/actuator/health/etl`

### 🔶 FUNCIONALIDADES PARCIALMENTE IMPLEMENTADAS

- **Watchdog de jobs zombis** - Código implementado, test unitario pendiente de validación
- **Observabilidad** - 8 métricas core implementadas, 7 adicionales en desarrollo

### ❌ LIMITACIONES CONOCIDAS

- **Test del watchdog** - Requiere validación en ambiente real (no sintético)
- **Métricas de dimensiones** - Solo detecta nuevas máquinas/maquinistas si están implementadas
- **File size tracking** - Depende de implementación en parser service

## Métricas: Estado Real vs Aspiracional

| Métrica | Estado | Tipo | Ubicación en Código |
|---------|---------|------|-------------------|
| `etl.rows.deleted` | ✅ IMPLEMENTADA | Counter | DataSyncService.java |
| `etl.rows.inserted` | ✅ IMPLEMENTADA | Counter | DataSyncService.java |
| `etl.sync.duration` | ✅ IMPLEMENTADA | Timer | DataSyncService.java |
| `etl.sync.attempts` | ✅ IMPLEMENTADA | Counter | DataSyncService.java |
| `etl.sync.collisions` | ✅ IMPLEMENTADA | Counter | DataSyncService.java |
| `etl.job.total.duration` | ✅ IMPLEMENTADA | Timer | EtlJobService.java |
| `etl.sync.window.days` | ✅ IMPLEMENTADA | Summary | DataSyncService.java |
| `etl.sync.records.per.batch` | ✅ IMPLEMENTADA | Summary | DataSyncService.java |
| `etl.jobs.watchdog.terminations` | 🔶 CÓDIGO EXISTE | Counter | EtlJobWatchdog.java |
| `etl.jobs.active` | 🔶 PENDIENTE | Gauge | Requiere implementación |
| `etl.jobs.stuck` | 🔶 PENDIENTE | Gauge | Requiere implementación |
| `etl.window.conflicts` | ❌ NO IMPLEMENTADA | Counter | Planificado |
| `etl.dim.new.*` | ❌ NO IMPLEMENTADA | Counter | Planificado |
| `etl.file.size.bytes` | ❌ NO IMPLEMENTADA | Summary | Planificado |
| `etl.rows.duplicate.ratio` | ❌ NO IMPLEMENTADA | Gauge | Planificado |
| `etl.unique.index.present` | ❌ NO IMPLEMENTADA | Gauge | Planificado |

## Configuración Validada de Producción

```properties
# CONFIGURACIÓN MÍNIMA REQUERIDA
etl.retry.unique.enabled=true
etl.retry.unique.max-attempts=3
etl.lock.enabled=true

# CONFIGURACIÓN WATCHDOG (Código existe, validación pendiente)
etl.jobs.watchdog.enabled=true
etl.jobs.watchdog.interval-ms=300000
etl.jobs.stuck.threshold-minutes=30
```

## Casos de Falla y Degradación

### ⚠️ Qué pasa cuando el sistema falla

| Escenario | Comportamiento Actual | Acción Requerida |
|-----------|----------------------|------------------|
| **PostgreSQL caído** | ETL falla completamente | Restart manual necesario |
| **Prometheus caído** | ETL continúa, métricas se pierden | Solo afecta observabilidad |
| **Advisory lock falla** | Posible corrupción de datos | Deshabilitar `etl.lock.enabled` |
| **Retry agotado** | Job falla con DataSyncException | Revisión manual del archivo |
| **Watchdog deshabilitado** | Jobs zombis acumulan | Monitoreo manual necesario |

### 🔧 Procedimientos de Emergencia

```bash
# Deshabilitar retry si causa problemas
curl -X POST localhost:8080/actuator/env -d '{"etl.retry.unique.enabled":"false"}'

# Verificar jobs stuck manualmente
SELECT job_id, status, created_at, EXTRACT(EPOCH FROM NOW() - created_at)/60 as minutes_stuck 
FROM etl_jobs WHERE status IN ('INICIADO','PROCESANDO','SINCRONIZANDO') ORDER BY created_at;

# Terminar job zombie manualmente
UPDATE etl_jobs SET status='FALLO', details='Manual termination', finished_at=NOW() WHERE job_id='xxx';
```

## Arquitectura de Retry (Verificada)

### Flujo Real Implementado
1. Thread A y B ejecutan `syncWithDeleteInsert()` concurrentemente
2. Ambos pasan validación de lock y fechas
3. Thread A completa DELETE + INSERT exitosamente
4. Thread B ejecuta DELETE (0 rows) + INSERT → **UNIQUE violation**
5. `isUniqueConstraintViolation()` detecta SQLState 23505
6. Reset de entity IDs: `fp.setId(null)` para evitar stale state
7. Backoff: `Thread.sleep(200L * attemptNumber)`
8. Reintento hasta `max-attempts=3`
9. Thread B reintenta: DELETE (0 rows) + INSERT (0 rows, datos ya existen)
10. **Resultado**: 0 excepciones propagadas, datos íntegros

### Beneficios Validados
- ✅ **0 fallos** en tests de integración concurrente
- ✅ **Idempotencia** confirmada en múltiples ejecuciones
- ✅ **Métricas** registran correctamente attempts y collisions

## Health Check Real

### Endpoint Implementado: `/actuator/health/etl`
```json
// Respuesta real del sistema
{
  "status": "UP",
  "details": {
    "uniqueIndex": "NOT_VERIFIED",  // Requiere implementación
    "retryEnabled": true,
    "activeJobs": "NOT_IMPLEMENTED", // Requiere gauge
    "stuckJobs": "NOT_IMPLEMENTED"   // Requiere gauge
  }
}
```

## Próximos Pasos Críticos (No Opcionales)

### 🚨 Prioridad Alta - Completar Implementación
1. **Validar watchdog** en ambiente PostgreSQL real
2. **Implementar gauges** faltantes: `etl.jobs.active`, `etl.jobs.stuck`
3. **Completar EtlHealthIndicator** con verificación real de índice
4. **Testing end-to-end** del watchdog en integración

### 🔧 Prioridad Media - Observabilidad
1. **Métricas de archivo**: `etl.file.size.bytes`
2. **Ratio de duplicados**: `etl.rows.duplicate.ratio`
3. **Verificador de índice**: `etl.unique.index.present`

### 📊 Prioridad Baja - Optimización
1. Streaming para archivos > 100MB
2. Cache de dimensiones
3. Dry-run endpoint

## Umbrales de Alerta Recomendados

| Métrica | Umbral Crítico | Umbral Warning | Frecuencia Check |
|---------|----------------|----------------|------------------|
| Jobs stuck (manual query) | > 0 | N/A | Cada 15 min |
| Sync collisions/hora | > 10 | > 3 | Continuo |
| Job duration P95 | > 10 min | > 5 min | Continuo |
| Duplicados últimos 7d | > 0 | N/A | Diario |

---

**Estado Real**: NÚCLEO OPERATIVO COMPLETO, OBSERVABILIDAD PARCIAL  
**Última verificación**: 2025-09-30  
**Responsable**: GitHub Copilot  
**Próxima revisión**: Tras implementar gauges faltantes
