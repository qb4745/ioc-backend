-- Minimal test schema aligned with JPA entities
-- Ensure citext extension for case-insensitive email column
CREATE EXTENSION IF NOT EXISTS citext;

-- app_users: note id is BIGINT in JPA (Long)
CREATE TABLE IF NOT EXISTS app_users (
  id BIGSERIAL PRIMARY KEY,
  supabase_user_id UUID NOT NULL UNIQUE,
  email CITEXT NOT NULL UNIQUE,
  primer_nombre VARCHAR(100) NOT NULL,
  segundo_nombre VARCHAR(100),
  primer_apellido VARCHAR(100) NOT NULL,
  segundo_apellido VARCHAR(100),
  planta_id BIGINT,
  centro_costo VARCHAR(50),
  fecha_contrato DATE,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  last_login_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at TIMESTAMPTZ
);

-- roles: id is Integer in JPA so use integer identity
CREATE TABLE IF NOT EXISTS roles (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- permissions: use INTEGER identity to match Permission.id (Integer)
CREATE TABLE IF NOT EXISTS permissions (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(150) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- user_roles linking app_users(id BIGINT) and roles(id INTEGER)
-- Note: UserRole entity uses @EmbeddedId with composite key (user_id, role_id)
CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
  role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
  assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  assigned_by_user_id BIGINT REFERENCES app_users(id),
  PRIMARY KEY (user_id, role_id)
);

-- role_permissions: composite PK (role_id, permission_id) and created_at present
CREATE TABLE IF NOT EXISTS role_permissions (
  role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
  permission_id INTEGER NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (role_id, permission_id)
);

-- Indexes used by repositories (case-insensitive email lookup etc.)
CREATE INDEX IF NOT EXISTS idx_app_users_lower_email ON app_users (lower(email));
CREATE INDEX IF NOT EXISTS idx_app_users_supabase_uid ON app_users (supabase_user_id);
CREATE INDEX IF NOT EXISTS idx_app_users_active ON app_users (is_active);
