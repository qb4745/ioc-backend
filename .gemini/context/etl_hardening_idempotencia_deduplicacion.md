# ETL Hardening: Idempotencia, De-duplicación, Concurrencia y Operación

## 1. Objetivo
Endurecer el pipeline de ingestión para evitar duplicados lógicos, controlar concurrencia, asegurar reemplazo consistente por rango de fechas y facilitar monitoreo / operación.

## 2. Alcance
Tablas implicadas:
- `fact_production`
- Dimensiones: `dim_maquina`, `dim_maquinista` (solo lectura + inserción de nuevos códigos)

No incluye (futuro): Streaming por lotes, COPY, staging tables.

## 3. Acciones Clave (Este Documento Cubre 5 Bloques)
1. Feature flag para *advisory lock* (`etl.lock.enabled`).  
2. SQL para detección y limpieza de duplicados (pre UNIQUE).  
3. SQL para índices y constraint de clave natural.  
4. Métricas y observabilidad expuestas.  
5. Procedimientos operativos: despliegue, rollback, realineo de secuencia.

---
## 4. Feature Flag de Concurrencia
| Propiedad | Default | Descripción |
|-----------|---------|-------------|
| `etl.lock.enabled` | `true` | Si está activo se ejecuta `pg_advisory_xact_lock` basado en (minDate,maxDate). Evita solapamiento de ETLs con rangos intersectados. |

Desactivación temporal (ej. pruebas de rendimiento):
```properties
etl.lock.enabled=false
```

### Cálculo de la llave
```java
long a = minDate.toEpochDay();
long b = maxDate.toEpochDay();
long key = (a << 32) ^ b; // garantiza distribución aceptable
```

---
## 5. Detección y Limpieza de Duplicados
La clave natural candidata (sin constraint aún):
```
(fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log)
```
### 5.1 Detección
```sql
SELECT fecha_contabilizacion,
       maquina_fk,
       COALESCE(maquinista_fk,0) AS maquinista_key,
       numero_log,
       COUNT(*) AS ocurrencias,
       ARRAY_AGG(id ORDER BY id) AS ids
FROM fact_production
GROUP BY 1,2,3,4
HAVING COUNT(*) > 1
ORDER BY ocurrencias DESC;
```
### 5.2 Aislar Duplicados
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
### 5.3 Eliminar Duplicados Conservando el `MIN(id)`
```sql
DELETE FROM fact_production fp
USING fact_production_dup d
WHERE fp.id = d.id
  AND fp.id NOT IN (
      SELECT MIN(id) FROM fact_production_dup GROUP BY fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log
);
```
### 5.4 Verificación Post Limpieza
```sql
SELECT COUNT(*) FROM fact_production_dup; -- referencia
SELECT COUNT(*) FROM fact_production;     -- total final
```
> Si se necesita revertir, se puede reinsertar desde `fact_production_dup` (con filtrado manual) antes de dropearla.

---
## 6. Índices e Integridad
### 6.1 Índice para DELETE por Rango
```sql
CREATE INDEX IF NOT EXISTS idx_fact_production_fecha
  ON fact_production(fecha_contabilizacion);
```
### 6.2 UNIQUE (Versión con COALESCE)
> Usar solo tras limpiar duplicados.
```sql
CREATE UNIQUE INDEX IF NOT EXISTS uq_fact_prod_natural
  ON fact_production(fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log);
```
### 6.3 Alternativa (maquinista_fk NOT NULL)
Si negocio obliga maquinista siempre:
```sql
ALTER TABLE fact_production
  ALTER COLUMN maquinista_fk SET NOT NULL;
-- Luego:
CREATE UNIQUE INDEX IF NOT EXISTS uq_fact_prod_natural
  ON fact_production(fecha_contabilizacion, maquina_fk, maquinista_fk, numero_log);
```
### 6.4 DROP en caso de rollback
```sql
DROP INDEX IF EXISTS uq_fact_prod_natural;
```

---
## 7. Secuencia (Solo si desalineada)
Verificar y realinear (ej. tras un `ALTER SEQUENCE ... RESTART WITH 1` accidental):
```sql
SELECT MAX(id) AS table_max FROM fact_production;
SELECT setval('fact_production_id_seq', (SELECT COALESCE(MAX(id),0)+1 FROM fact_production), false);
```

---
## 8. Métricas Expuestas (Micrometer)
| Métrica | Tipo | Descripción |
|---------|------|-------------|
| `etl.rows.parsed` | Counter | Registros parseados (antes de de-dup in-file) |
| `etl.rows.duplicate.skipped` | Counter | Duplicados saltados en un mismo archivo |
| `etl.rows.deleted` | Counter | Filas borradas (incrementadas exacto por DELETE) |
| `etl.rows.inserted` | Counter | Filas insertadas en sync actual |
| `etl.parse.duration` | Timer | Duración total del parse (ns→prometheus) |
| `etl.sync.duration` | Timer | Duración de delete + insert |
| `etl.lock.enabled` | (expuesta via config) | Control de concurrencia |

Prometheus endpoint: `/actuator/prometheus`.

### 8.1 Validación Rápida
- Ejecutar carga ETL y consultar métricas:
```bash
curl -s http://localhost:8080/actuator/prometheus | grep etl_rows_parsed
```

---
## 9. Flujo de Despliegue Recomendado
1. Ejecutar **detección duplicados** (Sec. 5.1).  
2. Si existen duplicados → aislar + limpiar (Sec. 5.2–5.3).  
3. Crear índice por fecha (Sec. 6.1) si no existe.  
4. (Opcional) Realinear secuencia si hubo intervenciones pasadas (Sec. 7).  
5. Crear índice UNIQUE (Sec. 6.2).  
6. Desplegar código actualizado (Parser + DataSync).  
7. Verificar métricas y logs.  

Rollback (si falla creación UNIQUE):
1. DROP INDEX (Sec. 6.4).  
2. Revisar duplicados residuales (repetir Sec. 5.1).  
3. Corregir fuente / parser.  
4. Reintentar.  

---
## 10. Validación Operativa Post Despliegue
| Check | Comando / Observación |
|-------|------------------------|
| No duplicados | Consulta Sec. 5.1 retorna 0 filas |
| Métricas fluyen | `/actuator/prometheus` contiene counters incrementados |
| Reprocesar mismo archivo | `etl.rows.inserted` no crece (rango reemplazado) |
| Concurrencia | Segundo ETL con rango solapado bloqueado/serializado |

---
## 11. Riesgos y Mitigaciones
| Riesgo | Mitigación |
|--------|-----------|
| Clave natural incompleta | Revisar negocio antes de UNIQUE |
| Cambios futuros en semántica | Versionar constraint (nuevo índice + deprecación) |
| ETL masivo futuro | Evaluar JDBC batch / COPY / staging |
| Null en maquinista rompe unicidad | Decidir: COALESCE vs forzar NOT NULL |

---
## 12. Troubleshooting Rápido
| Síntoma | Posible Causa | Acción |
|---------|---------------|--------|
| `duplicate key ... uq_fact_prod_natural` | Duplicado en archivo concurrente | Revisar parser de-dup + constraint, validar lock |
| `duplicate key ... pkey` | Secuencia desalineada | Ejecutar realineo (Sec. 7) |
| Métricas en 0 | Micrometer/actuator no habilitado | Verificar dependencias y endpoints |
| ETL lento | Falta índice fecha o lock serializando demasiado | Crear índice / revisar batch size |

---
## 13. Próximas Extensiones (No incluidas aquí)
- COPY (bulk ingest) para volúmenes > 100k.
- Staging table + validación semántica antes de merge.
- Auditoría de cambios (tabla histórica).
- ON CONFLICT DO NOTHING/UPDATE para upsert parcial.

---
**Fin del Documento – etl_hardening_idempotencia_deduplicacion**

