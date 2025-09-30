# ETL Hardening & Idempotency Guide

Este README resume la implementación actual y los pasos operativos para endurecer el proceso ETL de `fact_production`.

## 1. Objetivo
Asegurar que las cargas:
- No generen duplicados lógicos.
- Sean idempotentes por rango de fechas (delete + insert).
- Eviten condiciones de carrera (concurrency-safe via advisory lock).
- Expongan métricas de observabilidad.
- Sean operables (rollback / limpieza / monitoreo).

## 2. Estado Actual (Código)
| Elemento | Implementado | Detalle |
|----------|--------------|---------|
| De-dup intra-archivo (temprana + tardía) | Sí | Early key + verificación final |
| Advisory lock | Sí (flag) | `etl.lock.enabled` (default true) |
| Checker de duplicados al arranque | Sí | `EtlStartupDuplicateChecker` con flags de control |
| Métricas parse/sync | Sí | Counters + Timers Micrometer |
| Eliminación por rango | Sí | `deleteByFechaContabilizacionBetween` (retorna filas afectadas) |
| Retry colisión UNIQUE | Sí (desactivado) | Flags `etl.retry.unique.*` (índice aún no creado) |
| UNIQUE natural key | No | Requiere limpieza previa de duplicados |
| Índice fecha | Script listo | En `/.gemini/sql/V001__fact_production_indices_and_unique.sql` |
| Optimización parse numérico | Sí | Ruta rápida Long.parseLong |
| Early dedup antes de parse completo | Sí | Reduce construcciones innecesarias |

## 3. Propiedades de Configuración
```properties
etl.lock.enabled=true
etl.duplicate.check.enabled=true
etl.duplicate.fail-on-detect=false
etl.duplicate.check.sample-limit=10
etl.retry.unique.enabled=false
etl.retry.unique.max-attempts=3
# etl.unique.enforced=false (futuro)
```

## 4. Métricas (Micrometer / Prometheus)
| Nombre | Tipo | Descripción |
|--------|------|-------------|
| etl.rows.parsed | Counter | Registros parseados (antes dedup tardía) |
| etl.rows.duplicate.skipped | Counter | Duplicados saltados (early + late) |
| etl.rows.deleted | Counter | Filas eliminadas (valor exacto) |
| etl.rows.inserted | Counter | Filas insertadas |
| etl.parse.duration | Timer | Tiempo de parse total |
| etl.sync.duration | Timer | Tiempo de sync (delete+insert) |

Consultar:
```bash
curl -s http://localhost:8080/actuator/prometheus | grep etl_rows
```

## 5. Flujo Operativo (Despliegue Completo con UNIQUE)
1. Detectar duplicados (SQL sección blueprint).
2. Limpiar duplicados si existen.
3. Ejecutar script `V001__fact_production_indices_and_unique.sql` (índice fecha + UNIQUE).
4. Activar `etl.retry.unique.enabled=true` (opcional, sólo si probable colisión concurrente).
5. Desplegar código (ya optimizado).
6. Reprocesar archivo ya cargado (ver idempotencia: mismo rango, sin crecimiento).
7. Monitorear métricas / logs 24h.

## 6. Idempotencia – Cómo Funciona
- Rango min/max fecha calculado del input.
- DELETE por rango.
- INSERT masivo (batch JPA + sequence IDs).
- Resultado: datos fuera del rango intactos; dentro, reemplazados.

## 7. Clave Natural Candidata
```
(fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log)
```
Pendiente de consolidación final (aplicar UNIQUE tras limpieza).

## 8. Concurrencia
Advisory lock por rango (hash de fechas) — configurable.
Retry UNIQUE preparado (disabled) para futuro índice.

## 9. Startup Duplicate Checker
Componente: `EtlStartupDuplicateChecker`
- Escanea grupos duplicados.
- Muestra hasta `etl.duplicate.check.sample-limit`.
- Si `etl.duplicate.fail-on-detect=true` aborta arranque.

## 10. Retry por Colisión (Futuro con UNIQUE)
Cuando el índice `uq_fact_prod_natural` exista y se habilite `etl.retry.unique.enabled=true`, se reintenta hasta `etl.retry.unique.max-attempts` con backoff lineal (200ms*n).

## 11. Early Dedup
Construye clave mínima antes de parsear todos los campos; evita trabajo extra en archivos con repeticiones. Fallback a dedup tardía mantiene robustez.

## 12. Script SQL
Ruta: `.gemini/sql/V001__fact_production_indices_and_unique.sql`
Incluye:
- Índice por fecha.
- UNIQUE natural (COALESCE maquinista_fk).
- Rollback comentado.

## 13. Tests Agregados
| Test | Propósito | Resultado esperado |
|------|-----------|--------------------|
| `ParserServiceTest` | Validación general parse | 8 registros reales |
| `ParserServiceDedupTest` | Deduplicación (líneas idénticas) | 1 registro final |
| (Futuro) DataSyncRetryTest | Validar retry con UNIQUE activo | Reintentos + éxito |
| (Futuro) DuplicateCheckerIT | Verifica abort con fail-on-detect | Lanza excepción de arranque |

## 14. Troubleshooting Rápido
| Síntoma | Causa | Acción |
|---------|-------|--------|
| duplicate key pkey | Secuencia desalineada | Realinear con setval |
| duplicate key uq_fact_prod_natural | Duplicado concurrente | Revisar lock + retry + fuente |
| Parser lento | Input con muchas líneas inválidas | Bajar logging WARN o limpiar fuente |
| Métricas en cero | Actuator deshabilitado | Revisar configuración endpoints |
| Memoria alta | Archivo gigante sin streaming | Considerar streaming / COPY |

## 15. Roadmap Corto
- Aplicar UNIQUE tras limpieza.
- Posible streaming parser (para >100k líneas).
- COPY / staging table para ETL masivo.
- Auditoría y versionado de filas.

## 16. Checklist Operativo Final
| Item | OK |
|------|----|
| Duplicados limpios | ☐ |
| Índice fecha creado | ☐ |
| UNIQUE creado | ☐ |
| Retry UNIQUE habilitado (si procede) | ☐ |
| Prometheus expone métricas | ☐ |
| Reprocesar archivo estable | ☐ |
| Checker sin alertas | ☐ |

---
**Fin – ETL Hardening & Idempotency Guide (Actualizado)**
