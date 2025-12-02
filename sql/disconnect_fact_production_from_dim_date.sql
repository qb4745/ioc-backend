-- Script para deshacer la conexión de 'fact_production' con la dimensión de tiempo 'dim_date'.
-- Elimina la clave foránea, la columna de clave foránea y el índice de 'fact_production'.

-- Paso A: Eliminar la restricción de clave foránea
-- Se usa 'DROP CONSTRAINT IF EXISTS' para idempotencia.
ALTER TABLE public.fact_production
DROP CONSTRAINT IF EXISTS fk_fact_production_dim_date;

-- Paso B: Eliminar el índice creado en la columna de clave foránea
-- Se usa 'DROP INDEX IF EXISTS' para idempotencia.
DROP INDEX IF EXISTS public.idx_fact_production_fecha_fk;

-- Paso C: Eliminar la columna de clave foránea de fact_production
-- Se usa 'DROP COLUMN IF EXISTS' para idempotencia.
ALTER TABLE public.fact_production
DROP COLUMN IF EXISTS fecha_contabilizacion_fk;

-- NOTA: Este script no elimina la tabla 'dim_date' en sí.
-- Para eliminar 'dim_date', debes usar el script 'sql/undo_dim_date.sql'.
