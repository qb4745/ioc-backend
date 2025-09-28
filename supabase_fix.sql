-- Script para corregir la tabla fact_production en Supabase
-- Ejecutar en Supabase SQL Editor

-- Paso 1: Crear secuencia para auto-generación de IDs
CREATE SEQUENCE IF NOT EXISTS fact_production_id_seq;

-- Paso 2: Configurar la columna id para usar la secuencia
ALTER TABLE fact_production
ALTER COLUMN id SET DEFAULT nextval('fact_production_id_seq');

-- Paso 3: Asignar ownership de la secuencia a la columna
ALTER SEQUENCE fact_production_id_seq OWNED BY fact_production.id;

-- Paso 4: Opcional - Si tienes datos existentes, actualizar la secuencia
-- SELECT setval('fact_production_id_seq', COALESCE(MAX(id), 1)) FROM fact_production;

-- Verificación: Comprobar que la configuración está correcta
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'fact_production'
AND column_name = 'id';

-- Deberías ver: column_default = nextval('fact_production_id_seq'::regclass)
