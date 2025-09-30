-- ============================================================================
-- Script: run_now_verify_fact_production_hardening.sql
-- Objetivo: Verificar estado de hardening (Checklist Puntos 1 y 2)
-- Fecha: 2025-09-30
-- Uso: psql -v ON_ERROR_STOP=1 -d <database> -f run_now_verify_fact_production_hardening.sql
-- Requisitos: PostgreSQL (aprovecha pg_indexes / pg_class). No altera datos.
-- ============================================================================
\echo ==== (0) CONTEXTO DE CONEXION ==========================================
SELECT current_database()   AS database,
       current_schema()     AS schema,
       current_user         AS user_name,
       version()            AS postgres_version;

\echo
\echo ==== (1.3) INDICES QUE INCLUYEN fecha_contabilizacion ==================
WITH idxcols AS (
    SELECT i.indexname,
           i.indexdef,
           regexp_matches(i.indexdef, '\((.*)\)') AS collist_raw
    FROM pg_indexes i
    WHERE i.tablename = 'fact_production'
)
SELECT indexname,
       indexdef,
       CASE WHEN indexdef ILIKE '%fecha_contabilizacion%' THEN 'YES' ELSE 'NO' END AS contains_fecha_contabilizacion
FROM idxcols
ORDER BY 1;

\echo
\echo ==== (1.4) TAMANO Y VOLUMEN (APROX Y EXACTO) ===========================
SELECT reltuples::bigint AS approx_row_count,
       pg_size_pretty(pg_total_relation_size('fact_production')) AS total_relation_size,
       pg_size_pretty(pg_relation_size('fact_production'))       AS heap_size,
       pg_size_pretty(pg_total_relation_size('fact_production') - pg_relation_size('fact_production')) AS total_indexes_size
FROM pg_class
WHERE relname = 'fact_production';

\echo -- WARNING: COUNT(*) exacto puede ser costoso en tablas muy grandes.
SELECT COUNT(*) AS exact_row_count FROM fact_production;

\echo
\echo ==== (2.1 / 2.2) EXISTENCIA INDICE UNIQUE NATURAL ======================
SELECT indexname,
       indexdef,
       CASE WHEN indexdef ILIKE '%UNIQUE%' THEN 'UNIQUE' ELSE 'NOT UNIQUE' END AS uniqueness_flag
FROM pg_indexes
WHERE tablename = 'fact_production'
  AND indexname = 'uq_fact_prod_natural';

\echo
\echo ==== (2.x) COLUMNAS DEL INDICE uq_fact_prod_natural (SI EXISTE) =========
WITH idx AS (
  SELECT c.oid AS indexrelid
  FROM pg_class c
  JOIN pg_namespace n ON n.oid = c.relnamespace
  WHERE c.relname = 'uq_fact_prod_natural'
)
SELECT a.attname AS column_name,
       a.attnum  AS position_in_table
FROM pg_attribute a
JOIN pg_index i ON i.indexrelid = (SELECT indexrelid FROM idx)
WHERE a.attnum = ANY(i.indkey)
ORDER BY array_position(i.indkey, a.attnum);

\echo
\echo ==== (2.x) COMPROBACION DE DUPLICADOS LOGICOS (DEBERIA = 0) =============
SELECT COUNT(*) AS duplicate_groups
FROM (
    SELECT fecha_contabilizacion,
           maquina_fk,
           COALESCE(maquinista_fk,0) AS maquinista_key,
           numero_log,
           COUNT(*) AS c
    FROM fact_production
    GROUP BY 1,2,3,4
    HAVING COUNT(*) > 1
) t;

\echo
\echo ==== (2.x) MUESTRA DE DUPLICADOS (SI EXISTEN) ===========================
SELECT fecha_contabilizacion,
       maquina_fk,
       COALESCE(maquinista_fk,0) AS maquinista_key,
       numero_log,
       COUNT(*) AS occurrences
FROM fact_production
GROUP BY 1,2,3,4
HAVING COUNT(*) > 1
ORDER BY occurrences DESC
LIMIT 15;

\echo
\echo ==== (EXTRA) ALINEACION SECUENCIA VS MAX(id) ============================
WITH seq AS (
    SELECT 'fact_production_id_seq'::text AS seq_name,
           last_value, is_called
    FROM fact_production_id_seq
),
mx AS (
    SELECT COALESCE(MAX(id),0) AS max_id FROM fact_production
)
SELECT seq.seq_name,
       seq.last_value,
       mx.max_id,
       (mx.max_id >= seq.last_value) AS sequence_behind_suspect
FROM seq, mx;

\echo
\echo ==== (EXTRA) CARDINALIDAD DE COLUMNAS CLAVE =============================
SELECT COUNT(DISTINCT fecha_contabilizacion)        AS distinct_fechas,
       COUNT(DISTINCT maquina_fk)                   AS distinct_maquinas,
       COUNT(DISTINCT COALESCE(maquinista_fk,0))    AS distinct_maquinistas_norm,
       COUNT(DISTINCT numero_log)                   AS distinct_numero_log
FROM fact_production;

\echo
\echo ==== (EXTRA) NULOS EN CAMPOS CRITICOS (DEBERIAN SER 0) ==================
SELECT SUM(CASE WHEN fecha_contabilizacion IS NULL THEN 1 ELSE 0 END) AS null_fecha,
       SUM(CASE WHEN maquina_fk IS NULL           THEN 1 ELSE 0 END) AS null_maquina_fk,
       SUM(CASE WHEN numero_log IS NULL           THEN 1 ELSE 0 END) AS null_numero_log
FROM fact_production;

\echo
\echo ==== (SUGERENCIAS SI FALTAN INDICES) ====================================
-- Si NO hay indice con fecha_contabilizacion como leading column y consultas por rango son frecuentes:
--   CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_fact_production_fecha ON fact_production(fecha_contabilizacion);
-- Si NO existe uq_fact_prod_natural y duplicate_groups = 0, entonces crear (modo concurrente en produccion):
--   CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS uq_fact_prod_natural
--     ON fact_production (fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log);

\echo ==== (RESUMEN COMPACTO PARA CI/CD) ======================================
-- Genera una sola fila con estado resumido;
-- Útil para pipelines: 0=OK / 1=PROBLEMA
WITH dup AS (
    SELECT COUNT(*)::bigint AS duplicate_groups
    FROM (
        SELECT 1 FROM fact_production
        GROUP BY fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log
        HAVING COUNT(*) > 1
    ) s
), ui AS (
    SELECT COUNT(*)::int AS unique_index_present
    FROM pg_indexes
    WHERE tablename='fact_production' AND indexname='uq_fact_prod_natural'
), dt AS (
    SELECT reltuples::bigint AS approx_rows FROM pg_class WHERE relname='fact_production'
)
SELECT ui.unique_index_present,
       dup.duplicate_groups,
       dt.approx_rows,
       CASE WHEN ui.unique_index_present=1 AND dup.duplicate_groups=0 THEN 'GREEN' ELSE 'ATTENTION' END AS hardening_status
FROM ui, dup, dt;

\echo ==== FIN VERIFICACION HARDENING FACT_PRODUCTION ========================

