-- ============================================================================
-- Migration: Convert composite PK (fecha_contabilizacion, id) -> simple PK (id)
-- Precondiciones:
--   1. No existen claves foráneas externas que referencien (fecha_contabilizacion, id) como conjunto.
--   2. La columna id es única (al ser bigserial lo es en la práctica).
--   3. Ventana de mantenimiento sin escrituras concurrentes sobre fact_production.
-- Uso (psql):
--   \timing on
--   BEGIN;  -- opcional (si tabla pequeña). Para tablas grandes ejecutar sin transacción global.
--   \i .gemini/sql/migration_fact_production_pk_simple.sql
--   COMMIT; -- si se usó BEGIN
-- Rollback:
--   Si se requiere revertir antes del COMMIT, usar ROLLBACK. Después del COMMIT, se necesitaría recrear la PK compuesta manualmente.
-- ============================================================================

-- 0) Información previa (diagnóstico)
\echo '== Diagnóstico previo de constraints en fact_production =='
SELECT conname, pg_get_constraintdef(c.oid) AS definition
FROM pg_constraint c
JOIN pg_class t ON c.conrelid = t.oid
WHERE t.relname = 'fact_production'
ORDER BY conname;

-- 1) Verificar unicidad de id (debería retornar 0)
\echo '== Verificando unicidad de id (esperado 0) =='
SELECT COUNT(*) AS duplicate_id_count
FROM (
    SELECT id, COUNT(*) c FROM fact_production GROUP BY id HAVING COUNT(*) > 1
) d;

-- 2) Verificar ausencia de FKs externas que usen fecha_contabilizacion (heurístico)
--    Si aparecen filas, inspeccionar manualmente antes de seguir.
\echo '== Verificando posibles FKs externas que referencien fact_production.fecha_contabilizacion =='
SELECT tc.table_schema, tc.table_name, kcu.column_name, ccu.table_name AS foreign_table, ccu.column_name AS foreign_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND ccu.table_name = 'fact_production'
  AND ccu.column_name IN ('fecha_contabilizacion','id')
ORDER BY 1,2,3;

-- 3) Asegurar lock exclusivo para modificar la PK
LOCK TABLE fact_production IN ACCESS EXCLUSIVE MODE;

-- 4) Drop de la PK compuesta actual
ALTER TABLE fact_production DROP CONSTRAINT IF EXISTS fact_production_pkey;

-- 5) Crear nueva PK simple sobre id
ALTER TABLE fact_production ADD CONSTRAINT fact_production_pkey PRIMARY KEY (id);

-- 6) Crear índice por fecha (si no existía previamente separado de la PK)
CREATE INDEX IF NOT EXISTS idx_fact_production_fecha ON fact_production(fecha_contabilizacion);

-- 7) Verificar secuencia asociada y realinear si necesario
-- (Se asume nombre estándar generado por bigserial: fact_production_id_seq)
\echo '== Verificando secuencia y realineando si procede =='
SELECT setval('fact_production_id_seq', (SELECT COALESCE(MAX(id),0)+1 FROM fact_production), false) AS new_sequence_start;

-- 8) Mostrar definición final de la nueva PK e índices relevantes
\echo '== PK e índices resultantes =='
SELECT indexname, indexdef FROM pg_indexes WHERE tablename='fact_production' ORDER BY indexname;

-- 9) (Opcional) Analizar tabla para estadísticas actualizadas
-- ANALYZE fact_production;

\echo '== Migración PK compuesta -> PK simple completada (si no hubo errores) =='
-- FIN DEL SCRIPT

