-- ============================================================================
-- Migration: V002__create_metabase_readonly_user.sql
-- Objetivo: Crear un rol de base de datos dedicado y de solo lectura para Metabase.
-- Principio: Mínimo Privilegio (OWASP A01).
--
-- Uso:
-- 1. Reemplace 'UNA_CONTRASENA_MUY_SEGURA' con una contraseña fuerte y segura.
-- 2. Guarde esa contraseña en un gestor de secretos.
-- 3. Ejecute este script completo en el SQL Editor de Supabase.
-- ============================================================================

-- Iniciar una transacción para asegurar que todos los comandos se apliquen o ninguno.
BEGIN;

-- Paso 1: Crear el Rol (el "usuario")
-- ----------------------------------------------------------------------------
-- Creamos el rol 'metabase_reader' que puede iniciar sesión (LOGIN).
-- Es crucial establecer una contraseña fuerte aquí.
CREATE ROLE metabase_reader WITH
  LOGIN
  NOSUPERUSER
  NOCREATEDB
  NOCREATEROLE
  NOINHERIT
  NOREPLICATION
  NOBYPASSRLS
  PASSWORD 'UNA_CONTRASENA_MUY_SEGURA';

-- Comentario para documentar el propósito del rol en la base de datos.
COMMENT ON ROLE metabase_reader IS 'Rol de solo lectura para la herramienta de BI Metabase. Sus permisos están restringidos a SELECT en tablas específicas.';


-- Paso 2: Otorgar Permisos Mínimos
-- ----------------------------------------------------------------------------
-- Por defecto, un nuevo rol no puede hacer nada. Primero, le damos el permiso
-- más básico: "ver" el esquema 'public', pero sin acceso a las tablas todavía.
GRANT USAGE ON SCHEMA public TO metabase_reader;


-- Paso 3: Otorgar Permisos de Lectura (SELECT) en Tablas Específicas
-- ----------------------------------------------------------------------------
-- Otorgamos explícitamente permiso de SELECT solo en las tablas que Metabase
-- necesita para construir los dashboards. No se otorgan permisos de INSERT,
-- UPDATE ni DELETE.
GRANT SELECT ON public.fact_production TO metabase_reader;
GRANT SELECT ON public.dim_maquina TO metabase_reader;
GRANT SELECT ON public.dim_maquinista TO metabase_reader;
GRANT SELECT ON public.etl_jobs TO metabase_reader; -- Útil para dashboards de monitoreo del ETL.


-- Paso 4: (Opcional pero Recomendado) Permisos para Futuras Tablas
-- ----------------------------------------------------------------------------
-- Este comando asegura que si en el futuro se crean nuevas tablas en el esquema
-- 'public', el rol 'metabase_reader' automáticamente tendrá permiso de SELECT
-- sobre ellas. Esto simplifica el mantenimiento.
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO metabase_reader;

COMMIT;

-- ============================================================================
-- Verificación (Opcional)
-- ============================================================================
-- Para verificar que los permisos se aplicaron correctamente, puedes ejecutar:
--
-- SELECT grantee, table_schema, table_name, privilege_type
-- FROM information_schema.table_privileges
-- WHERE grantee = 'metabase_reader';
--
-- Deberías ver una lista de las tablas anteriores con el privilege_type 'SELECT'.
-- ============================================================================
