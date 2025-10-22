-- Migration: V003__migrate_departments_regions_to_plantas.sql
-- Objetivo: asegurar/crear `planta`, añadir columnas en `app_user` (planta_id, centro_costo, fecha_contrato)
-- y ofrecer pasos seguros para migrar datos desde tablas antiguas `departments`/`regions` si existen.

BEGIN;

-- 1) Asegurar tabla planta
CREATE TABLE IF NOT EXISTS planta (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(120) NOT NULL,
    address TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2) Añadir columnas a app_user si no existen
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='app_user' AND column_name='planta_id') THEN
        ALTER TABLE app_user ADD COLUMN planta_id INTEGER REFERENCES planta(id) ON DELETE SET NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='app_user' AND column_name='centro_costo') THEN
        ALTER TABLE app_user ADD COLUMN centro_costo VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='app_user' AND column_name='fecha_contrato') THEN
        ALTER TABLE app_user ADD COLUMN fecha_contrato DATE;
    END IF;
END $$;

-- 3) Intentar migrar datos desde departments => planta si existen (no destructivo)
DO $$
DECLARE
    has_departments BOOLEAN := EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name='departments');
    has_regions BOOLEAN := EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name='regions');
BEGIN
    IF has_departments THEN
        -- Insert distinct departments as planta (idempotente via code uniqueness)
        EXECUTE 'INSERT INTO planta(code, name, created_at, updated_at)
                 SELECT COALESCE(code, ''dept_'' || id::text), name, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                 FROM departments
                 ON CONFLICT (code) DO NOTHING';

        -- If app_user has a department_id column, migrate references
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='app_user' AND column_name='department_id') THEN
            EXECUTE 'UPDATE app_user au SET planta_id = p.id
                     FROM departments d
                     JOIN planta p ON p.code = COALESCE(d.code, ''dept_'' || d.id::text)
                     WHERE au.department_id = d.id AND au.planta_id IS NULL';
        END IF;
    END IF;

    IF has_regions THEN
        -- If departments didn't exist, or additionally, insert regions as planta with code 'reg_<id>'
        EXECUTE 'INSERT INTO planta(code, name, created_at, updated_at)
                 SELECT COALESCE(code, ''reg_'' || id::text), name, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                 FROM regions
                 ON CONFLICT (code) DO NOTHING';

        -- If app_user has a region_id column, migrate references when planta_id is null
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='app_user' AND column_name='region_id') THEN
            EXECUTE 'UPDATE app_user au SET planta_id = p.id
                     FROM regions r
                     JOIN planta p ON p.code = COALESCE(r.code, ''reg_'' || r.id::text)
                     WHERE au.region_id = r.id AND au.planta_id IS NULL';
        END IF;
    END IF;
END $$;

-- 4) Nota: no borramos tablas antiguas aquí. Revisar los resultados y, si todo está bien, ejecutar DROP manualmente.
-- Ejemplos para ejecutar manualmente después de verificar:
-- DROP TABLE IF EXISTS departments CASCADE;
-- DROP TABLE IF EXISTS regions CASCADE;

COMMIT;

-- Verificación sugerida (ejecutar tras migración):
-- SELECT COUNT(*) FROM planta;
-- SELECT * FROM vw_users_with_roles LIMIT 20;

-- FIN
