-- Script para conectar la tabla de hechos 'fact_production' con la dimensión de tiempo 'dim_date'.
-- ASUME QUE 'dim_date' YA EXISTE.
-- Este script es idempotente: si las columnas o constraints ya existen, los comandos fallarán
-- de forma segura o no harán nada si se usa 'IF NOT EXISTS' (dependiendo de la versión de PostgreSQL).

-- Paso A: Añadir la columna de clave foránea a fact_production
-- Añade una columna para almacenar la clave de la dimensión de tiempo (ej: 20251122).
ALTER TABLE public.fact_production
ADD COLUMN IF NOT EXISTS fecha_contabilizacion_fk INT;

COMMENT ON COLUMN public.fact_production.fecha_contabilizacion_fk IS 'Clave foránea a la tabla dim_date, derivada de fecha_contabilizacion.';

-- Paso B: Poblar la nueva columna con datos existentes (Backfill)
-- Este comando puede tardar si la tabla fact_production es muy grande.
-- Calcula el valor de la clave (YYYYMMDD) a partir de la columna de fecha existente.
UPDATE public.fact_production
SET fecha_contabilizacion_fk = TO_CHAR(fecha_contabilizacion, 'YYYYMMDD')::INT
WHERE fecha_contabilizacion_fk IS NULL; -- Solo actualiza las filas que no han sido pobladas

-- Paso C: Crear la restricción de clave foránea (El enlace final)
-- Primero, eliminamos cualquier constraint antiguo con el mismo nombre para evitar errores.
ALTER TABLE public.fact_production
DROP CONSTRAINT IF EXISTS fk_fact_production_dim_date;

-- Ahora, creamos la restricción que une formalmente las dos tablas.
-- Esto asegura la integridad referencial.
ALTER TABLE public.fact_production
ADD CONSTRAINT fk_fact_production_dim_date
FOREIGN KEY (fecha_contabilizacion_fk) REFERENCES public.dim_date(date_key);

-- Paso D (Opcional pero recomendado): Crear un índice en la nueva columna de clave foránea
-- Esto acelerará drásticamente las consultas que unen fact_production con dim_date.
CREATE INDEX IF NOT EXISTS idx_fact_production_fecha_fk ON public.fact_production (fecha_contabilizacion_fk);
