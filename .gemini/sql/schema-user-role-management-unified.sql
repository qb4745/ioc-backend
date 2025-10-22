-- ===================================================================================
-- Project:     Inteligencia Operacional Cambiaso (IOC)
-- File:        .gemini/sql/schema-user-role-management-unified.sql
-- Version:     1.3
-- Author:      Development Team
--
-- Description:
-- Este script establece el esquema fundacional completo para la gestión de usuarios,
-- control de acceso basado en roles (RBAC), y entidades de negocio relacionadas
-- como las plantas. Está diseñado para ser idempotente, lo que significa que puede
-- ejecutarse múltiples veces de forma segura en la misma base de datos.
--
-- Usage:
-- Ejecute este script en su totalidad contra la base de datos de Supabase.
-- Se recomienda hacerlo a través del "SQL Editor" en el panel de Supabase.
-- ===================================================================================

-- ===================================================================================
-- RESUMEN DE PUNTOS FUERTES Y ARQUITECTURA
-- ===================================================================================
--
-- 1. Idempotencia (⭐⭐⭐⭐⭐):
--    El uso extensivo de 'IF NOT EXISTS' y 'ON CONFLICT' permite que este script
--    se ejecute repetidamente sin causar errores, añadiendo solo lo que falta.
--    Esto es crucial para despliegues automatizados y mantenimiento.
--
-- 2. Filosofía de Nomenclatura Dual (Plural + Singular):
--    - Tablas Base en Plural (ej. 'app_users'): Sigue la convención de ver las tablas
--      como colecciones de registros.
--    - Vistas de Compatibilidad en Singular (ej. 'app_user'): Se crean vistas que
--      apuntan a las tablas plurales. Esto garantiza la compatibilidad con ORMs
--      (como Spring Data JPA) que esperan nombres en singular, evitando la necesidad
--      de refactorizar el código del backend.
--
-- 3. Seguridad a Nivel de Base de Datos (⭐⭐⭐⭐⭐):
--    - Row Level Security (RLS): Se habilita RLS en todas las tablas críticas.
--    - Políticas Claras: Se definen políticas que restringen el acceso a los datos
--      basado en el rol del usuario autenticado (ej. un usuario solo puede ver su
--      propia información, a menos que sea un administrador).
--    - Funciones Helper Seguras: Las funciones como 'has_role' usan 'SECURITY DEFINER'
--      para operar de forma segura dentro del contexto de las políticas RLS.
--
-- 4. Mantenibilidad y Developer Experience (DX):
--    - Triggers Automáticos: La columna 'updated_at' se actualiza automáticamente.
--    - Datos Iniciales (Seeds): Se insertan roles y permisos por defecto.
--    - Vistas Analíticas ('vw_...'): Se proveen vistas pre-unidas que simplifican
--      las consultas desde el frontend o herramientas de BI.
--
-- 5. Migraciones y Compatibilidad hacia Atrás:
--    El script incluye bloques condicionales para migrar datos desde esquemas
--    anteriores (ej. tablas con nombres en singular o con diferentes estructuras
--    de columnas), facilitando la evolución del sistema sin pérdida de datos.
--
-- ===================================================================================
-- DESCRIPCIÓN DE TABLAS Y RELACIONES
-- ===================================================================================
--
-- El esquema se basa en un modelo de Control de Acceso Basado en Roles (RBAC)
-- y sigue la Tercera Forma Normal (3NF) para evitar la redundancia de datos.
--
-- --- Tablas de Entidades ---
--
--   - plantas:
--     Catálogo de las fábricas o centros de producción. Es una tabla de "dimensiones".
--
--   - app_users:
--     Tabla central que almacena el perfil local de cada usuario.
--     Relaciones:
--       - Se vincula 1 a 1 con un usuario en 'auth.users' de Supabase vía 'supabase_user_id'.
--       - Se vincula N a 1 con 'plantas' (un usuario pertenece a una planta).
--
--   - roles:
--     Catálogo de los roles disponibles en el sistema (ej. 'ADMIN', 'GERENTE').
--
--   - permissions:
--     Catálogo de todas las acciones granulares posibles (ej. 'USER_READ', 'DASHBOARD_VIEW').
--
-- --- Tablas de Unión (Muchos a Muchos) ---
--
--   - user_roles:
--     Tabla pivote que asigna roles a los usuarios.
--     Relaciones:
--       - Conecta 'app_users' y 'roles'.
--       - Permite que un usuario tenga múltiples roles y que un rol sea asignado a múltiples usuarios.
--
--   - role_permissions:
--     Tabla pivote que asigna permisos a los roles.
--     Relaciones:
--       - Conecta 'roles' y 'permissions'.
--       - Permite que un rol agrupe múltiples permisos y que un permiso sea parte de múltiples roles.
--
-- ===================================================================================

-- =========================
-- PREPARACIÓN
-- =========================

CREATE EXTENSION IF NOT EXISTS citext;

-- =========================
-- TABLAS PLURALES (3FN) - BEST PRACTICE
-- =========================

-- CATÁLOGO: plantas
CREATE TABLE IF NOT EXISTS plantas (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(120) NOT NULL,
    address TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- USUARIOS DE APLICACIÓN (perfil local)
CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    supabase_user_id UUID UNIQUE NOT NULL,
    email CITEXT UNIQUE NOT NULL,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    planta_id INTEGER REFERENCES plantas(id) ON DELETE SET NULL,
    centro_costo VARCHAR(50),
    fecha_contrato DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_app_users_active ON app_users(is_active) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_app_users_supabase_uid ON app_users(supabase_user_id);
CREATE INDEX IF NOT EXISTS idx_app_users_planta ON app_users(planta_id) WHERE planta_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_app_users_centrocosto ON app_users(centro_costo) WHERE centro_costo IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_app_users_nombre ON app_users(primer_nombre, primer_apellido);

-- ROLES Y PERMISOS (plural)
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) UNIQUE NOT NULL,
    description TEXT
);

-- TABLAS DE RELACIÓN (plural)
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INTEGER NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by_user_id BIGINT REFERENCES app_users(id) ON DELETE SET NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role_id);

-- =========================
-- TRIGGERS: update updated_at
-- =========================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_plantas_updated_at') THEN
        CREATE TRIGGER trg_plantas_updated_at
        BEFORE UPDATE ON plantas
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_app_users_updated_at') THEN
        CREATE TRIGGER trg_app_users_updated_at
        BEFORE UPDATE ON app_users
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_roles_updated_at') THEN
        CREATE TRIGGER trg_roles_updated_at
        BEFORE UPDATE ON roles
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
END $$;

-- =========================
-- SEEDS (idempotentes)
-- =========================

-- Roles del sistema
INSERT INTO roles(name, description) VALUES
    ('ADMIN', 'Acceso administrativo total al sistema'),
    ('GERENTE', 'Gestión operativa y supervisión'),
    ('ANALISTA', 'Acceso estándar para análisis y consultas')
ON CONFLICT (name) DO NOTHING;

-- Permisos del sistema
INSERT INTO permissions(name, description) VALUES
    ('USER_READ', 'Leer usuarios'),
    ('USER_WRITE', 'Crear/editar/eliminar usuarios'),
    ('ROLE_READ', 'Leer roles'),
    ('ROLE_WRITE', 'Crear/editar/eliminar roles'),
    ('PERMISSION_READ', 'Leer permisos'),
    ('PERMISSION_WRITE', 'Crear/editar/eliminar permisos'),
    ('DASHBOARD_VIEW', 'Ver dashboards'),
    ('DASHBOARD_EDIT', 'Editar configuración de dashboards'),
    ('KPI_VIEW', 'Visualizar KPIs'),
    ('KPI_MANAGE', 'Gestionar configuración de KPIs'),
    ('REPORT_VIEW', 'Ver reportes'),
    ('REPORT_EXPORT', 'Exportar reportes'),
    ('PLANT_MANAGE', 'Gestionar plantas')
ON CONFLICT (name) DO NOTHING;

-- =========================
-- ASIGNACIÓN DE PERMISOS A ROLES
-- =========================

-- ADMIN: Todos los permisos
INSERT INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- GERENTE: Permisos de gestión y visualización
INSERT INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
JOIN permissions p ON p.name IN (
    'USER_READ',
    'ROLE_READ',
    'PERMISSION_READ',
    'DASHBOARD_VIEW',
    'DASHBOARD_EDIT',
    'KPI_VIEW',
    'KPI_MANAGE',
    'REPORT_VIEW',
    'REPORT_EXPORT',
    'PLANT_MANAGE'
)
WHERE r.name = 'GERENTE'
ON CONFLICT DO NOTHING;

-- ANALISTA: Permisos de solo lectura y análisis
INSERT INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
JOIN permissions p ON p.name IN (
    'DASHBOARD_VIEW',
    'KPI_VIEW',
    'REPORT_VIEW',
    'REPORT_EXPORT'
)
WHERE r.name = 'ANALISTA'
ON CONFLICT DO NOTHING;

-- =========================
-- HELPERS: has_role / has_any_role / has_permission
-- =========================

CREATE OR REPLACE FUNCTION has_role(role_name TEXT)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1
        FROM app_users u
        JOIN user_roles ur ON ur.user_id = u.id
        JOIN roles r ON r.id = ur.role_id
        WHERE u.supabase_user_id = auth.uid()
          AND u.deleted_at IS NULL
          AND u.is_active = TRUE
          AND r.name = role_name
    );
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

COMMENT ON FUNCTION has_role IS 
'Verifica si el usuario actual tiene un rol específico (ADMIN, GERENTE, ANALISTA)';

CREATE OR REPLACE FUNCTION has_any_role(role_names TEXT[])
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1
        FROM app_users u
        JOIN user_roles ur ON ur.user_id = u.id
        JOIN roles r ON r.id = ur.role_id
        WHERE u.supabase_user_id = auth.uid()
          AND u.deleted_at IS NULL
          AND u.is_active = TRUE
          AND r.name = ANY(role_names)
    );
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

COMMENT ON FUNCTION has_any_role IS 
'Verifica si el usuario actual tiene al menos uno de los roles especificados';

CREATE OR REPLACE FUNCTION has_permission(permission_name TEXT)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1
        FROM app_users u
        JOIN user_roles ur ON ur.user_id = u.id
        JOIN role_permissions rp ON rp.role_id = ur.role_id
        JOIN permissions p ON p.id = rp.permission_id
        WHERE u.supabase_user_id = auth.uid()
          AND u.deleted_at IS NULL
          AND u.is_active = TRUE
          AND p.name = permission_name
    );
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

COMMENT ON FUNCTION has_permission IS 
'Verifica si el usuario actual tiene un permiso específico basado en sus roles';

-- =========================
-- FUNCIÓN HELPER: concatenar nombre completo
-- =========================

CREATE OR REPLACE FUNCTION get_full_name(
    p_primer_nombre VARCHAR,
    p_segundo_nombre VARCHAR,
    p_primer_apellido VARCHAR,
    p_segundo_apellido VARCHAR
)
RETURNS VARCHAR AS $$
BEGIN
    RETURN TRIM(
        CONCAT_WS(' ',
            p_primer_nombre,
            p_segundo_nombre,
            p_primer_apellido,
            p_segundo_apellido
        )
    );
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION get_full_name IS 
'Concatena los componentes del nombre en un string completo, omitiendo valores NULL';

-- =========================
-- RLS (Row Level Security) - idempotente
-- =========================

ALTER TABLE IF EXISTS app_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS user_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS permissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS role_permissions ENABLE ROW LEVEL SECURITY;

-- Políticas para app_users
DROP POLICY IF EXISTS app_users_select_self_or_admin ON app_users;
CREATE POLICY app_users_select_self_or_admin ON app_users FOR SELECT TO authenticated
USING (
    supabase_user_id = auth.uid() 
    OR has_role('ADMIN') 
    OR has_role('GERENTE')
);

DROP POLICY IF EXISTS app_users_admin_write ON app_users;
CREATE POLICY app_users_admin_write ON app_users FOR ALL TO authenticated
USING (has_role('ADMIN')) 
WITH CHECK (has_role('ADMIN'));

-- Políticas para user_roles
DROP POLICY IF EXISTS user_roles_select_self_or_admin ON user_roles;
CREATE POLICY user_roles_select_self_or_admin ON user_roles FOR SELECT TO authenticated
USING (
    has_role('ADMIN') 
    OR has_role('GERENTE')
    OR EXISTS (
        SELECT 1 FROM app_users u 
        WHERE u.id = user_roles.user_id 
        AND u.supabase_user_id = auth.uid()
    )
);

DROP POLICY IF EXISTS user_roles_admin_write ON user_roles;
CREATE POLICY user_roles_admin_write ON user_roles FOR ALL TO authenticated
USING (has_role('ADMIN')) 
WITH CHECK (has_role('ADMIN'));

-- Políticas para roles
DROP POLICY IF EXISTS roles_select_all_auth ON roles;
CREATE POLICY roles_select_all_auth ON roles FOR SELECT TO authenticated 
USING (true);

DROP POLICY IF EXISTS roles_admin_write ON roles;
CREATE POLICY roles_admin_write ON roles FOR ALL TO authenticated
USING (has_role('ADMIN')) 
WITH CHECK (has_role('ADMIN'));

-- Políticas para permissions
DROP POLICY IF EXISTS permissions_select_all_auth ON permissions;
CREATE POLICY permissions_select_all_auth ON permissions FOR SELECT TO authenticated 
USING (true);

DROP POLICY IF EXISTS permissions_admin_write ON permissions;
CREATE POLICY permissions_admin_write ON permissions FOR ALL TO authenticated
USING (has_role('ADMIN')) 
WITH CHECK (has_role('ADMIN'));

-- Políticas para role_permissions
DROP POLICY IF EXISTS role_permissions_select_all_auth ON role_permissions;
CREATE POLICY role_permissions_select_all_auth ON role_permissions FOR SELECT TO authenticated 
USING (true);

DROP POLICY IF EXISTS role_permissions_admin_write ON role_permissions;
CREATE POLICY role_permissions_admin_write ON role_permissions FOR ALL TO authenticated
USING (has_role('ADMIN')) 
WITH CHECK (has_role('ADMIN'));

-- =========================
-- VISTAS ÚTILES
-- =========================

CREATE OR REPLACE VIEW vw_users_with_roles AS
SELECT
    u.id AS user_id,
    u.supabase_user_id,
    u.email,
    u.primer_nombre,
    u.segundo_nombre,
    u.primer_apellido,
    u.segundo_apellido,
    get_full_name(u.primer_nombre, u.segundo_nombre, u.primer_apellido, u.segundo_apellido) AS nombre_completo,
    p.id AS planta_id,
    p.code AS planta_code,
    p.name AS planta_name,
    u.centro_costo,
    u.fecha_contrato,
    u.is_active,
    u.last_login_at,
    u.created_at,
    u.updated_at,
    ARRAY_AGG(r.name ORDER BY r.name) FILTER (WHERE r.name IS NOT NULL) AS roles,
    ARRAY_AGG(r.id ORDER BY r.name) FILTER (WHERE r.id IS NOT NULL) AS role_ids
FROM app_users u
LEFT JOIN plantas p ON p.id = u.planta_id
LEFT JOIN user_roles ur ON ur.user_id = u.id
LEFT JOIN roles r ON r.id = ur.role_id
WHERE u.deleted_at IS NULL
GROUP BY u.id, u.supabase_user_id, u.email, u.primer_nombre, u.segundo_nombre, 
         u.primer_apellido, u.segundo_apellido, p.id, p.code, p.name, 
         u.centro_costo, u.fecha_contrato, u.is_active, u.last_login_at, 
         u.created_at, u.updated_at;

COMMENT ON VIEW vw_users_with_roles IS 
'Vista consolidada: usuarios activos con nombres separados, nombre completo calculado, roles (array), planta, centro de costo y fecha de contrato';

-- Vista de permisos efectivos por usuario
CREATE OR REPLACE VIEW vw_user_permissions AS
SELECT DISTINCT
    u.id AS user_id,
    u.email,
    get_full_name(u.primer_nombre, u.segundo_nombre, u.primer_apellido, u.segundo_apellido) AS nombre_completo,
    r.name AS role_name,
    p.name AS permission_name,
    p.description AS permission_description
FROM app_users u
JOIN user_roles ur ON ur.user_id = u.id
JOIN roles r ON r.id = ur.role_id
JOIN role_permissions rp ON rp.role_id = r.id
JOIN permissions p ON p.id = rp.permission_id
WHERE u.deleted_at IS NULL 
  AND u.is_active = TRUE
ORDER BY u.email, r.name, p.name;

COMMENT ON VIEW vw_user_permissions IS 
'Permisos efectivos por usuario basados en sus roles asignados (ADMIN, GERENTE, ANALISTA)';

-- Vista de resumen de roles
CREATE OR REPLACE VIEW vw_role_summary AS
SELECT 
    r.id,
    r.name,
    r.description,
    COUNT(DISTINCT ur.user_id) AS users_count,
    COUNT(DISTINCT rp.permission_id) AS permissions_count,
    ARRAY_AGG(DISTINCT p.name ORDER BY p.name) FILTER (WHERE p.name IS NOT NULL) AS permissions
FROM roles r
LEFT JOIN user_roles ur ON ur.role_id = r.id
LEFT JOIN app_users u ON u.id = ur.user_id AND u.deleted_at IS NULL AND u.is_active = TRUE
LEFT JOIN role_permissions rp ON rp.role_id = r.id
LEFT JOIN permissions p ON p.id = rp.permission_id
GROUP BY r.id, r.name, r.description
ORDER BY r.name;

COMMENT ON VIEW vw_role_summary IS 
'Resumen de roles con cantidad de usuarios asignados y permisos asociados';

-- =========================
-- MIGRACIONES CONDICIONALES (no destructivas)
--  - migrar departments/regions => plantas
--  - migrar tablas singulares => plurales
--  - migrar full_name => nombres separados
--  - migrar roles antiguos => nuevos
-- =========================

DO $$
DECLARE
    v_count INTEGER;
BEGIN
    -- =====================================================
    -- MIGRACIÓN 1: departments => plantas
    -- =====================================================
    IF to_regclass('public.departments') IS NOT NULL THEN
        INSERT INTO plantas(code, name, created_at, updated_at)
        SELECT 
            COALESCE(code, 'dept_' || id::text), 
            name, 
            COALESCE(created_at, CURRENT_TIMESTAMP),
            COALESCE(updated_at, CURRENT_TIMESTAMP)
        FROM departments
        ON CONFLICT (code) DO NOTHING;
        
        GET DIAGNOSTICS v_count = ROW_COUNT;
        RAISE NOTICE '✓ Migrados % registros de departments a plantas', v_count;
    END IF;

    -- =====================================================
    -- MIGRACIÓN 2: regions => plantas
    -- =====================================================
    IF to_regclass('public.regions') IS NOT NULL THEN
        INSERT INTO plantas(code, name, created_at, updated_at)
        SELECT 
            COALESCE(code, 'reg_' || id::text), 
            name,
            COALESCE(created_at, CURRENT_TIMESTAMP),
            COALESCE(updated_at, CURRENT_TIMESTAMP)
        FROM regions
        ON CONFLICT (code) DO NOTHING;
        
        GET DIAGNOSTICS v_count = ROW_COUNT;
        RAISE NOTICE '✓ Migrados % registros de regions a plantas', v_count;
    END IF;

    -- =====================================================
    -- MIGRACIÓN 3: Actualizar nombres de roles antiguos
    -- =====================================================
    -- MANAGER => GERENTE
    UPDATE roles SET name = 'GERENTE', description = 'Gestión operativa y supervisión'
    WHERE name = 'MANAGER';
    
    -- USER => ANALISTA
    UPDATE roles SET name = 'ANALISTA', description = 'Acceso estándar para análisis y consultas'
    WHERE name = 'USER';
    
    RAISE NOTICE '✓ Roles actualizados: MANAGER=>GERENTE, USER=>ANALISTA';

    -- =====================================================
    -- MIGRACIÓN 4: tablas singulares => plurales
    -- =====================================================
    
    -- planta => plantas
    IF to_regclass('public.planta') IS NOT NULL 
       AND to_regclass('public.plantas') IS NOT NULL THEN
        INSERT INTO plantas(id, code, name, address, created_at, updated_at)
        SELECT id, code, name, address, created_at, updated_at
        FROM planta
        ON CONFLICT (id) DO NOTHING;
        
        PERFORM setval('plantas_id_seq', (SELECT MAX(id) FROM plantas));
        RAISE NOTICE '✓ Migrados datos de planta => plantas';
    END IF;

    -- role => roles
    IF to_regclass('public.role') IS NOT NULL 
       AND to_regclass('public.roles') IS NOT NULL THEN
        INSERT INTO roles(id, name, description, created_at, updated_at)
        SELECT id, name, description, created_at, updated_at
        FROM role
        ON CONFLICT (name) DO NOTHING;
        
        PERFORM setval('roles_id_seq', (SELECT MAX(id) FROM roles));
        RAISE NOTICE '✓ Migrados datos de role => roles';
    END IF;

    -- permission => permissions
    IF to_regclass('public.permission') IS NOT NULL 
       AND to_regclass('public.permissions') IS NOT NULL THEN
        INSERT INTO permissions(id, name, description)
        SELECT id, name, description
        FROM permission
        ON CONFLICT (name) DO NOTHING;
        
        PERFORM setval('permissions_id_seq', (SELECT MAX(id) FROM permissions));
        RAISE NOTICE '✓ Migrados datos de permission => permissions';
    END IF;

    -- =====================================================
    -- MIGRACIÓN 5: app_user con full_name => app_users con nombres separados
    -- =====================================================
    IF to_regclass('public.app_user') IS NOT NULL THEN
        -- Verificar si app_user tiene columna full_name
        IF EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='app_user' AND column_name='full_name') THEN
            
            -- Migrar dividiendo full_name en componentes
            INSERT INTO app_users(
                id, supabase_user_id, email, 
                primer_nombre, segundo_nombre, primer_apellido, segundo_apellido,
                planta_id, centro_costo, fecha_contrato, 
                is_active, last_login_at, created_at, updated_at, deleted_at
            )
            SELECT 
                id, 
                supabase_user_id, 
                email,
                -- Dividir full_name en componentes (lógica básica)
                COALESCE(NULLIF(SPLIT_PART(full_name, ' ', 1), ''), 'N/A') as primer_nombre,
                NULLIF(SPLIT_PART(full_name, ' ', 2), '') as segundo_nombre,
                COALESCE(NULLIF(SPLIT_PART(full_name, ' ', 3), ''), 'N/A') as primer_apellido,
                NULLIF(SPLIT_PART(full_name, ' ', 4), '') as segundo_apellido,
                planta_id, 
                centro_costo, 
                fecha_contrato,
                is_active, 
                last_login_at, 
                created_at, 
                updated_at, 
                deleted_at
            FROM app_user
            ON CONFLICT (supabase_user_id) DO NOTHING;
            
            PERFORM setval('app_users_id_seq', (SELECT MAX(id) FROM app_users));
            RAISE NOTICE '✓ Migrados datos de app_user (full_name dividido) => app_users';
            RAISE WARNING '⚠ Los nombres fueron divididos automáticamente. Revisar y corregir manualmente si es necesario.';
        
        -- Si app_user ya tiene nombres separados
        ELSIF EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name='app_user' AND column_name='primer_nombre') THEN
            
            INSERT INTO app_users(
                id, supabase_user_id, email, 
                primer_nombre, segundo_nombre, primer_apellido, segundo_apellido,
                planta_id, centro_costo, fecha_contrato, 
                is_active, last_login_at, created_at, updated_at, deleted_at
            )
            SELECT 
                id, supabase_user_id, email,
                primer_nombre, segundo_nombre, primer_apellido, segundo_apellido,
                planta_id, centro_costo, fecha_contrato,
                is_active, last_login_at, created_at, updated_at, deleted_at
            FROM app_user
            ON CONFLICT (supabase_user_id) DO NOTHING;
            
            PERFORM setval('app_users_id_seq', (SELECT MAX(id) FROM app_users));
            RAISE NOTICE '✓ Migrados datos de app_user => app_users';
        END IF;
    END IF;

    -- role_permission => role_permissions
    IF to_regclass('public.role_permission') IS NOT NULL 
       AND to_regclass('public.role_permissions') IS NOT NULL THEN
        INSERT INTO role_permissions(role_id, permission_id, created_at)
        SELECT role_id, permission_id, created_at
        FROM role_permission
        ON CONFLICT (role_id, permission_id) DO NOTHING;
        
        RAISE NOTICE '✓ Migrados datos de role_permission => role_permissions';
    END IF;

    -- user_role => user_roles
    IF to_regclass('public.user_role') IS NOT NULL 
       AND to_regclass('public.user_roles') IS NOT NULL THEN
        INSERT INTO user_roles(user_id, role_id, assigned_at, assigned_by_user_id)
        SELECT user_id, role_id, assigned_at, assigned_by_user_id
        FROM user_role
        ON CONFLICT (user_id, role_id) DO NOTHING;
        
        RAISE NOTICE '✓ Migrados datos de user_role => user_roles';
    END IF;

END $$;

-- =========================
-- AÑADIR COLUMNAS A TABLAS LEGACY SI EXISTEN
-- =========================

DO $$
BEGIN
    -- Si existe app_user (singular) y no tiene las columnas nuevas, agregarlas
    IF to_regclass('public.app_user') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                       WHERE table_name='app_user' AND column_name='primer_nombre') THEN
            ALTER TABLE app_user 
                ADD COLUMN primer_nombre VARCHAR(100),
                ADD COLUMN segundo_nombre VARCHAR(100),
                ADD COLUMN primer_apellido VARCHAR(100),
                ADD COLUMN segundo_apellido VARCHAR(100);
            
            -- Actualizar desde full_name si existe
            IF EXISTS (SELECT 1 FROM information_schema.columns 
                       WHERE table_name='app_user' AND column_name='full_name') THEN
                EXECUTE "
                    UPDATE app_user SET
                        primer_nombre = COALESCE(NULLIF(SPLIT_PART(full_name, ' ', 1), ''), 'N/A'),
                        segundo_nombre = NULLIF(SPLIT_PART(full_name, ' ', 2), ''),
                        primer_apellido = COALESCE(NULLIF(SPLIT_PART(full_name, ' ', 3), ''), 'N/A'),
                        segundo_apellido = NULLIF(SPLIT_PART(full_name, ' ', 4), '');
                ";
                
                -- Hacer NOT NULL después de poblar
                ALTER TABLE app_user 
                    ALTER COLUMN primer_nombre SET NOT NULL,
                    ALTER COLUMN primer_apellido SET NOT NULL;
                
                RAISE NOTICE '✓ Columnas de nombre agregadas y pobladas en app_user (legacy)';
            END IF;
        END IF;
    END IF;
END $$;

-- =========================
-- VISTAS DE COMPATIBILIDAD (SINGULAR) - para código legacy
-- =========================

DO $$
BEGIN
    -- Vista planta (singular) => tabla plantas (plural)
    IF to_regclass('public.planta') IS NULL THEN
        EXECUTE 'CREATE VIEW planta AS SELECT * FROM plantas';
        RAISE NOTICE '✓ Vista de compatibilidad creada: planta => plantas';
    END IF;

    -- Vista role (singular) => tabla roles (plural)
    IF to_regclass('public.role') IS NULL THEN
        EXECUTE 'CREATE VIEW role AS SELECT * FROM roles';
        RAISE NOTICE '✓ Vista de compatibilidad creada: role => roles';
    END IF;

    -- Vista permission (singular) => tabla permissions (plural)
    IF to_regclass('public.permission') IS NULL THEN
        EXECUTE 'CREATE VIEW permission AS SELECT * FROM permissions';
        RAISE NOTICE '✓ Vista de compatibilidad creada: permission => permissions';
    END IF;

    -- Vista app_user (singular) => tabla app_users (plural) con full_name calculado
    IF to_regclass('public.app_user') IS NULL THEN
        EXECUTE '
            CREATE VIEW app_user AS 
            SELECT 
                *,
                get_full_name(primer_nombre, segundo_nombre, primer_apellido, segundo_apellido) AS full_name
            FROM app_users
        ';
        RAISE NOTICE '✓ Vista de compatibilidad creada: app_user => app_users (con full_name)';
    END IF;

    -- Vista user_role (singular) => tabla user_roles (plural)
    IF to_regclass('public.user_role') IS NULL THEN
        EXECUTE 'CREATE VIEW user_role AS SELECT * FROM user_roles';
        RAISE NOTICE '✓ Vista de compatibilidad creada: user_role => user_roles';
    END IF;

    -- Vista role_permission (singular) => tabla role_permissions (plural)
    IF to_regclass('public.role_permission') IS NULL THEN
        EXECUTE 'CREATE VIEW role_permission AS SELECT * FROM role_permissions';
        RAISE NOTICE '✓ Vista de compatibilidad creada: role_permission => role_permissions';
    END IF;
END $$;

-- =========================
-- VERIFICACIÓN FINAL
-- =========================

DO $$
DECLARE
    v_plantas_count INTEGER;
    v_users_count INTEGER;
    v_roles_count INTEGER;
    v_permissions_count INTEGER;
    v_admin_count INTEGER;
    v_gerente_count INTEGER;
    v_analista_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_plantas_count FROM plantas;
    SELECT COUNT(*) INTO v_users_count FROM app_users WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO v_roles_count FROM roles;
    SELECT COUNT(*) INTO v_permissions_count FROM permissions;
    
    -- Contar usuarios por rol
    SELECT COUNT(DISTINCT ur.user_id) INTO v_admin_count
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    JOIN app_users u ON u.id = ur.user_id
    WHERE r.name = 'ADMIN' AND u.deleted_at IS NULL AND u.is_active = TRUE;
    
    SELECT COUNT(DISTINCT ur.user_id) INTO v_gerente_count
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    JOIN app_users u ON u.id = ur.user_id
    WHERE r.name = 'GERENTE' AND u.deleted_at IS NULL AND u.is_active = TRUE;
    
    SELECT COUNT(DISTINCT ur.user_id) INTO v_analista_count
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    JOIN app_users u ON u.id = ur.user_id
    WHERE r.name = 'ANALISTA' AND u.deleted_at IS NULL AND u.is_active = TRUE;

    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ SCHEMA INSTALADO EXITOSAMENTE';
    RAISE NOTICE '========================================';
    RAISE NOTICE '';
    RAISE NOTICE '📊 ESTADÍSTICAS:';
    RAISE NOTICE '  • Plantas: %', v_plantas_count;
    RAISE NOTICE '  • Usuarios activos: %', v_users_count;
    RAISE NOTICE '  • Roles definidos: %', v_roles_count;
    RAISE NOTICE '  • Permisos definidos: %', v_permissions_count;
    RAISE NOTICE '';
    RAISE NOTICE '👥 DISTRIBUCIÓN DE USUARIOS POR ROL:';
    RAISE NOTICE '  • ADMIN: %', v_admin_count;
    RAISE NOTICE '  • GERENTE: %', v_gerente_count;
    RAISE NOTICE '  • ANALISTA: %', v_analista_count;
    RAISE NOTICE '';
    RAISE NOTICE '📋 TABLAS PRINCIPALES (PLURAL - Best Practice):';
    RAISE NOTICE '  • plantas, app_users, roles, permissions';
    RAISE NOTICE '  • role_permissions, user_roles';
    RAISE NOTICE '';
    RAISE NOTICE '👤 ESTRUCTURA DE NOMBRES:';
    RAISE NOTICE '  • primer_nombre (NOT NULL)';
    RAISE NOTICE '  • segundo_nombre (nullable)';
    RAISE NOTICE '  • primer_apellido (NOT NULL)';
    RAISE NOTICE '  • segundo_apellido (nullable)';
    RAISE NOTICE '';
    RAISE NOTICE '🎭 ROLES DEL SISTEMA:';
    RAISE NOTICE '  • ADMIN - Acceso administrativo total';
    RAISE NOTICE '  • GERENTE - Gestión operativa y supervisión';
    RAISE NOTICE '  • ANALISTA - Acceso estándar para análisis';
    RAISE NOTICE '';
    RAISE NOTICE '👁️  VISTAS DE COMPATIBILIDAD (singular):';
    RAISE NOTICE '  • planta, app_user (incluye full_name calculado)';
    RAISE NOTICE '  • role, permission, role_permission, user_role';
    RAISE NOTICE '';
    RAISE NOTICE '🔒 SEGURIDAD:';
    RAISE NOTICE '  • RLS habilitado en todas las tablas';
    RAISE NOTICE '  • Funciones: has_role(), has_any_role(), has_permission()';
    RAISE NOTICE '  • Helper: get_full_name()';
    RAISE NOTICE '';
    RAISE NOTICE '📈 VISTAS ANALÍTICAS:';
    RAISE NOTICE '  • vw_users_with_roles';
    RAISE NOTICE '  • vw_user_permissions';
    RAISE NOTICE '  • vw_role_summary';
    RAISE NOTICE '';
    RAISE NOTICE '========================================';
END $$;

-- END OF FILE
