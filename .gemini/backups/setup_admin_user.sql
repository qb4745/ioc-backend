-- =====================================================
-- Script para crear usuario ADMIN inicial
-- Compatible con Supabase FREE tier
-- =====================================================

-- 1. Insertar roles básicos (si no existen)
INSERT INTO roles (name, description)
VALUES
    ('ADMIN', 'Administrator with full access'),
    ('USER', 'Regular user with limited access')
ON CONFLICT (name) DO NOTHING;

-- 2. Crear usuario admin en app_users
-- ⚠️ REEMPLAZA 'uuid-del-admin-en-supabase' con el UUID real del paso anterior
INSERT INTO app_users (
    supabase_user_id,
    email,
    primer_nombre,
    primer_apellido,
    planta_id,
    is_active,
    created_at,
    updated_at
)
VALUES (
    'REEMPLAZAR-CON-UUID-DE-SUPABASE'::uuid, -- 🔥 OBTENER del paso 1 del flujo
    'admin@example.com',
    'Admin',
    'Sistema',
    1, -- Ajustar según tu planta_id real
    true,
    NOW(),
    NOW()
)
ON CONFLICT (supabase_user_id) DO NOTHING;

-- 3. Asignar rol ADMIN al usuario
INSERT INTO user_roles (user_id, role_id, assigned_at)
SELECT
    u.id,
    r.id,
    NOW()
FROM app_users u, roles r
WHERE u.email = 'admin@example.com'
  AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- 4. Verificar la creación
SELECT
    u.id,
    u.email,
    u.supabase_user_id,
    r.name as role
FROM app_users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.email = 'admin@example.com';

