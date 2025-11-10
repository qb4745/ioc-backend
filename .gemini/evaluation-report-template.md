-- Initial schema proposal for ioc-backend
-- Generated as a starting point. Review and adapt types/constraints to match application entities.

-- Table: plantas
CREATE TABLE IF NOT EXISTS plantas (
  id               SERIAL PRIMARY KEY,
  code             VARCHAR(64) NOT NULL UNIQUE,
  name             TEXT NOT NULL,
  address          TEXT,
  created_at       TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at       TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Table: roles
CREATE TABLE IF NOT EXISTS roles (
  id               SERIAL PRIMARY KEY,
  name             VARCHAR(100) NOT NULL UNIQUE,
  description      TEXT,
  created_at       TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at       TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Table: permissions
CREATE TABLE IF NOT EXISTS permissions (
  id               SERIAL PRIMARY KEY,
  name             VARCHAR(150) NOT NULL UNIQUE,
  description      TEXT,
  created_at       TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at       TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Table: role_permissions (many-to-many)
CREATE TABLE IF NOT EXISTS role_permissions (
  role_id          INTEGER NOT NULL,
  permission_id    INTEGER NOT NULL,
  created_at       TIMESTAMP WITH TIME ZONE DEFAULT now(),
  PRIMARY KEY (role_id, permission_id),
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Table: app_users
CREATE TABLE IF NOT EXISTS app_users (
  id                 BIGSERIAL PRIMARY KEY,
  supabase_user_id   UUID UNIQUE,
  email              CITEXT UNIQUE,
  primer_nombre      VARCHAR(255),
  segundo_nombre     VARCHAR(255),
  primer_apellido    VARCHAR(255),
  segundo_apellido   VARCHAR(255),
  planta_id          INTEGER REFERENCES plantas(id),
  centro_costo       VARCHAR(128),
  fecha_contrato     DATE,
  is_active          BOOLEAN DEFAULT true,
  last_login_at      TIMESTAMP WITH TIME ZONE,
  created_at         TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at         TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted_at         TIMESTAMP WITH TIME ZONE
);

-- Table: user_roles (many-to-many)
CREATE TABLE IF NOT EXISTS user_roles (
  user_id            BIGINT NOT NULL,
  role_id            INTEGER NOT NULL,
  assigned_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  assigned_by_user_id BIGINT,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  FOREIGN KEY (assigned_by_user_id) REFERENCES app_users(id)
);

-- Table: etl_jobs
CREATE TABLE IF NOT EXISTS etl_jobs (
  job_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  file_name         VARCHAR(512),
  file_hash         VARCHAR(128) UNIQUE,
  user_id           VARCHAR(128),
  min_date          DATE,
  max_date          DATE,
  status            VARCHAR(64),
  details           TEXT,
  created_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  finished_at       TIMESTAMP WITH TIME ZONE
);

-- Table: quarantined_records
CREATE TABLE IF NOT EXISTS quarantined_records (
  id                BIGSERIAL PRIMARY KEY,
  etl_job_id        UUID REFERENCES etl_jobs(job_id) ON DELETE CASCADE,
  file_name         VARCHAR(512),
  line_number       INTEGER,
  raw_line          TEXT,
  error_details     TEXT,
  created_at        TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Table: dim_maquina
CREATE TABLE IF NOT EXISTS dim_maquina (
  id                BIGSERIAL PRIMARY KEY,
  codigo_maquina    VARCHAR(128) UNIQUE,
  nombre_maquina    TEXT,
  created_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at        TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Table: dim_maquinista
CREATE TABLE IF NOT EXISTS dim_maquinista (
  id                BIGSERIAL PRIMARY KEY,
  codigo_maquinista BIGINT UNIQUE,
  nombre_completo   TEXT,
  created_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at        TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Table: fact_production (simplified)
CREATE TABLE IF NOT EXISTS fact_production (
  id                        BIGSERIAL PRIMARY KEY,
  fecha_contabilizacion     DATE,
  maquina_id                BIGINT REFERENCES dim_maquina(id),
  maquinista_id             BIGINT REFERENCES dim_maquinista(id),
  numero_log                BIGINT,
  hora_contabilizacion      TIME,
  fecha_notificacion        DATE,
  documento                 BIGINT,
  material_sku              BIGINT,
  material_descripcion      TEXT,
  numero_pallet             INTEGER,
  cantidad                  NUMERIC(18,4),
  peso_neto                 NUMERIC(18,4),
  centro_costos             VARCHAR(128),
  turno                     VARCHAR(64),
  created_at                TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Indexes: common improvements
CREATE INDEX IF NOT EXISTS idx_app_users_supabase_user_id ON app_users(supabase_user_id);
CREATE INDEX IF NOT EXISTS idx_app_users_is_active ON app_users(is_active);
CREATE INDEX IF NOT EXISTS idx_etl_jobs_file_hash ON etl_jobs(file_hash);

-- End of initial schema

