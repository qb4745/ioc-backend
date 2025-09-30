# Blueprint: Hardening ETL – Idempotencia, De-duplicación, Concurrencia y Observabilidad

## 1. Objetivo y Alcance
* **Objetivo:** Endurecer el pipeline ETL para garantizar: (a) no duplicación lógica, (b) reemplazo seguro por rango de fechas, (c) protección contra ejecuciones concurrentes solapadas, (d) diagnósticos rápidos ante fallas y (e) métricas de salud y rendimiento.
* **Alcance:** Fact table `fact_production` y dimensiones `dim_maquina`, `dim_maquinista`. No aborda aún mejoras de streaming ni COPY.
* **No Incluye:** Cambios funcionales del parser más allá de la deduplicación interna; optimización JDBC/COPY (queda como futura fase si el batching JPA no alcanza SLA).

## 2. Justificación Estratégica
| Riesgo Actual | Impacto | Mitigación en este Blueprint |
|--------------|---------|------------------------------|
| Duplicados intra-file | Inflado de métricas, inconsistencias | De-dup en parser + UNIQUE natural key |
| Overlap de ejecuciones | Corrupción parcial de rango | Advisory lock (global o por rango) |
| Falta de natural key | Dificultad para detectar anomalías | Definición + constraint |
| Secuencia desalineada (casos legado) | Violaciones PK | Verificación y realineo condicional |
| Log ruidoso | Dificulta troubleshooting | Nivel TRACE para duplicados |
| Sin métricas de proceso | Ceguera operacional | Contadores + duración |
| Falta plan rollback constraint | Downtime extendido | Procedimiento de reversión documentado |

## 3. Prerrequisitos
* Acceso SQL (supabase) para ejecutar DDL.
* Revisión de datos existentes (conteo, duplicados potenciales).
* Confirmar que la aplicación ya usa `SEQUENCE` para `fact_production.id`.

## 4. Definición de Clave Natural Candidata
Propuesta (ajustar si negocio indica otra):
```
(fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk, 0), numero_log)
```
**Checklist Validación:**
- `numero_log` es único dentro de la combinación máquina + fecha + (maquinista opcional).  
- `maquinista_fk` puede ser NULL → se normaliza a 0 sólo para constraint funcional (o usar índice parcial).  
- Ningún campo cambia una vez insertado (inmutabilidad lógica).  
- Cardinalidad suficiente (colisiones raras).  
Si alguna condición falla: redefinir antes de continuar.

## 5. Estrategias de Deduplicación (Trade-offs)
| Estrategia | Ventaja | Desventaja | Uso Final |
|-----------|---------|-----------|----------|
| Set in-memory en Parser | Evita inserts redundantes | Usa RAM (O(n)) | Adoptar ahora |
| UNIQUE constraint | Garantía global | Lanza excepción si colisión | Adoptar tras limpieza |
| ON CONFLICT (UPSERT) | Idempotencia directa | Lógica SQL adicional | Fase futura opcional |
| Staging + MERGE | Auditoría & control | Complejidad | Solo volúmenes masivos |

## 6. Plan de Implementación (Orden de Despliegue)
1. **Detección de duplicados existentes** (consulta GROUP BY).  
2. **Remediación de duplicados** (tabla cuarentena + delete selectivo).  
3. **Verificar y realinear secuencias sólo si necesario**.  
4. **Crear índice por fecha** (si no existe) para acelerar DELETE.  
5. **Crear UNIQUE (o índice único) sobre clave natural**.  
6. **Actualizar Parser: de-dup interna antes de agregar a la lista**.  
7. **Agregar advisory lock en DataSyncService**.  
8. **Bajar nivel log duplicados a TRACE**.  
9. **Agregar métricas y contadores**.  
10. **Actualizar comentarios obsoletos (BIGSERIAL → SEQUENCE)**.  
11. **Monitoreo post-despliegue (24h)**.  

> Estado actual: pasos 6, 7, 8, 9 y 10 implementados en código; pasos 1–5 y 11 pendientes de ejecución operativa / migraciones.

## 7. SQL – Detección y Limpieza de Duplicados
### 7.1 Detección
```sql
SELECT fecha_contabilizacion,
       maquina_fk,
       COALESCE(maquinista_fk, 0) AS maquinista_key,
       numero_log,
       COUNT(*) AS ocurrencias,
       ARRAY_AGG(id ORDER BY id) AS ids
FROM fact_production
GROUP BY 1,2,3,4
HAVING COUNT(*) > 1
ORDER BY ocurrencias DESC;
```
### 7.2 Aislar Duplicados
```sql
CREATE TABLE IF NOT EXISTS fact_production_dup AS
SELECT fp.*
FROM fact_production fp
JOIN (
    SELECT fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0) mkey, numero_log
    FROM fact_production
    GROUP BY 1,2,3,4
    HAVING COUNT(*) > 1
) d ON d.fecha_contabilizacion = fp.fecha_contabilizacion
   AND d.maquina_fk = fp.maquina_fk
   AND d.mkey = COALESCE(fp.maquinista_fk,0)
   AND d.numero_log = fp.numero_log;
```
### 7.3 Eliminar Duplicados Conservando el Mínimo ID
```sql
DELETE FROM fact_production fp
USING fact_production_dup d
WHERE fp.id = d.id
  AND fp.id NOT IN (
      SELECT MIN(id) FROM fact_production_dup GROUP BY fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log
);
```

## 8. Secuencia (Sólo si hubo reinicios manuales)
Verificación:
```sql
SELECT max(id) AS table_max FROM fact_production;
SELECT setval('fact_production_id_seq', (SELECT COALESCE(MAX(id),0)+1 FROM fact_production), false);
```
Ejecutar **solo** si un `INSERT` futuro arroja `duplicate key` y la secuencia está detrás.

## 9. Índices y Constraint
```sql
-- Índice para DELETE por rango
CREATE INDEX IF NOT EXISTS idx_fact_production_fecha ON fact_production(fecha_contabilizacion);

-- UNIQUE (versión con columna derivada)
CREATE UNIQUE INDEX IF NOT EXISTS uq_fact_prod_natural
ON fact_production(fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log);
```
*Alternativa más estricta:* Forzar `maquinista_fk` NOT NULL previo a constraint si negocio lo permite.

## 10. Cambios en Código – Parser (De-dup In-memory)
```java
Set<String> seen = new HashSet<>();
String key = fecha + "|" + maquina + "|" + (maquinista == null ? 0 : maquinista) + "|" + numeroLog;
if (!seen.add(key)) {
    log.trace("Skipping duplicate logical fact key={}", key);
    continue;
}
records.add(fact);
```

## 11. Control de Concurrencia (Advisory Lock)
* **Feature flag:** `etl.lock.enabled` (default `true`).
* **Rango hash:** `(a << 32) ^ b` con epochDay de min/max.
```java
if (etlLockEnabled) {
    long lockKey = computeLockKey(minDate, maxDate);
    entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(?);")
        .setParameter(1, lockKey)
        .getSingleResult();
}
```

## 12. Métricas (Micrometer)
| Métrica | Tipo | Implementado | Descripción |
|---------|------|--------------|-------------|
| `etl.rows.parsed` | Counter | Sí | Registros parseados antes de de-dup |
| `etl.rows.duplicate.skipped` | Counter | Sí | Registros duplicados intra-archivo omitidos |
| `etl.rows.deleted` | Counter | Sí | Filas eliminadas en el rango (incrementa por count real) |
| `etl.rows.inserted` | Counter | Sí | Filas insertadas en la sincronización actual |
| `etl.parse.duration` | Timer | Sí | Duración total del parse |
| `etl.sync.duration` | Timer | Sí | Duración delete + insert |

## 13. Logging
| Evento | Nivel | Razón |
|--------|-------|-------|
| Inicio ETL | INFO | Auditoría |
| Rango fechas | INFO | Trazabilidad |
| Duplicado detectado | TRACE | Evitar ruido en INFO |
| Borrado completado | DEBUG | Diagnóstico selectivo |
| Inserción lote | INFO | Métrica principal |
| Resumen parse | INFO | Visibilidad operacional |
| Excepción | ERROR | Alerting |

## 14. Comentarios Obsoletos
Se limpiaron referencias a BIGSERIAL en `DataSyncService`; ahora se documenta uso de SEQUENCE + batching.

## 15. Retry Opcional (Colisión Transitoria)
```java
for (int i = 0; i < 3; i++) {
  try { syncWithDeleteInsert(...); break; }
  catch (DataIntegrityViolationException e) {
    if (!isUniqueConstraint(e) || i == 2) throw e;
    Thread.sleep(200L * (i + 1));
  }
}
```

## 16. Consideraciones de Performance
- UNIQUE O(log N) – coste aceptable en volúmenes actuales.
- De-dup Set O(N) memoria transitoria.
- Advisory lock serializa rangos que se solapan.

## 17. Plan de Rollback (Constraint)
1. `DROP INDEX IF EXISTS uq_fact_prod_natural;`  
2. Revisar duplicados (Sec. 7.1).  
3. Corregir fuente / parser.  
4. Recrear índice.  

## 18. Validación Post-Despliegue
- `parsed - duplicates == inserted` (mismo rango).  
- Métricas visibles en `/actuator/prometheus`.  
- Reprocesar mismo archivo: filas borradas = filas insertadas (reemplazo).  

## 19. Riesgos Residuales
| Riesgo | Mitigación Futuro |
|--------|-------------------|
| Crece volumen > RAM | Streaming + partición / COPY |
| Clave natural cambia | Versionar constraint |
| Necesidad de paralelismo | Lock por sub-rango o particionar por fecha |

## 20. Siguientes Fases (Opcional)
- JDBC batch / COPY para >100k.  
- Staging + validación semántica.  
- Auditoría histórica.  
- ON CONFLICT / MERGE.  

## 21. Resumen de Implementación Actual
| Elemento | Estado | Nota |
|----------|--------|------|
| De-dup parser | Implementado | Evita duplicados intra-archivo |
| Advisory lock | Implementado (flag) | `etl.lock.enabled` configurable |
| Métricas parse/sync | Implementadas | Timers + counters |
| UNIQUE natural key | Pendiente | Requiere limpieza previa |
| Índice fecha | Pendiente (verificar en DB) | Crear si no existe |
| Retry constraint | Pendiente | Solo si aparece colisión real |

---
**Fin del Blueprint – IOC-022 (Actualizado)**
