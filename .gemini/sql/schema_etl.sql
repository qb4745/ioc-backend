-- =================================================================================
--            DATA DEFINITION LANGUAGE (DDL) PARA EL PROYECTO IOC
--
-- Versión: 5.1 (Consolidada y Definitiva)
-- Autor: Arquitecto de Soluciones de Software
-- Principios de Diseño:
--   1. Modelo Dimensional (Kimball).
--   -  2. Particionamiento Anual para escalabilidad.
-- -  3. Gobernanza de Jobs para auditoría y resiliencia.
--   4. Clave de Negocio Única y Checks de Sanidad para integridad de datos.
-- =================================================================================

-- =================================================================================
-- TABLAS DE DIMENSIONES (El Contexto Descriptivo)
-- =================================================================================

CREATE TABLE IF NOT EXISTS public.dim_maquina (
    id BIGSERIAL PRIMARY KEY,
    codigo_maquina VARCHAR(100) NOT NULL UNIQUE,
    nombre_maquina TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE public.dim_maquina IS 'Catálogo de máquinas y sus atributos descriptivos.';

CREATE TABLE IF NOT EXISTS public.dim_maquinista (
    id BIGSERIAL PRIMARY KEY,
    codigo_maquinista BIGINT NOT NULL UNIQUE,
    nombre_completo TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE public.dim_maquinista IS 'Catálogo de maquinistas y sus atributos descriptivos.';


-- =================================================================================
-- TABLA DE HECHOS (Las Métricas)
-- =================================================================================
CREATE TABLE IF NOT EXISTS public.fact_production (
    id BIGSERIAL,
    fecha_contabilizacion DATE NOT NULL,
    maquina_fk BIGINT NOT NULL REFERENCES public.dim_maquina(id),
    maquinista_fk BIGINT REFERENCES public.dim_maquinista(id),
    numero_log BIGINT NOT NULL,
    hora_contabilizacion TIME NOT NULL,
    fecha_notificacion DATE NOT NULL,
    documento BIGINT,
    material_sku BIGINT NOT NULL,
    material_descripcion TEXT,
    numero_pallet INT,
    cantidad NUMERIC(18, 4) NOT NULL, -- Movido a NOT NULL
    peso_neto NUMERIC(18, 4) NOT NULL,  -- Movido a NOT NULL
    lista VARCHAR(10),
    version_produccion VARCHAR(50),
    centro_costos BIGINT,
    turno VARCHAR(10) NOT NULL,
    jornada VARCHAR(10),
    usuario_sap VARCHAR(100),
    bodeguero VARCHAR(100),
    PRIMARY KEY (id, fecha_contabilizacion),
    -- Checks de Sanidad de Datos
    CONSTRAINT chk_cantidad_nonneg CHECK (cantidad >= 0),
    CONSTRAINT chk_peso_neto_nonneg CHECK (peso_neto >= 0)
) PARTITION BY RANGE (fecha_contabilizacion);

COMMENT ON TABLE public.fact_production IS 'Tabla de hechos particionada anualmente. Almacena las métricas de los eventos de producción.';

-- --- CREACIÓN DE PARTICIONES ANUALES ---
CREATE TABLE IF NOT EXISTS fact_production_2025 PARTITION OF public.fact_production
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');
CREATE TABLE IF NOT EXISTS fact_production_2026 PARTITION OF public.fact_production
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

-- --- ÍNDICES PARA OPTIMIZACIÓN Y GARANTÍA DE INTEGRIDAD ---

-- Índice de Clave de Negocio Única (CRÍTICO)
CREATE UNIQUE INDEX IF NOT EXISTS ux_fact_business_key
ON public.fact_production(fecha_contabilizacion, maquina_fk, numero_log);
COMMENT ON INDEX public.ux_fact_business_key IS 'Garantiza la unicidad de cada evento de producción.';

-- Índices de Apoyo Analítico (para dashboards)
CREATE INDEX IF NOT EXISTS idx_fact_prod_maquina_fecha ON public.fact_production(maquina_fk, fecha_contabilizacion);
CREATE INDEX IF NOT EXISTS idx_fact_prod_turno_fecha ON public.fact_production(turno, fecha_contabilizacion);
CREATE INDEX IF NOT EXISTS idx_fact_prod_maquinista_fecha ON public.fact_production(maquinista_fk, fecha_contabilizacion);


-- =================================================================================
-- TABLAS DE GOBERNANZA Y OPERACIÓN DEL ETL
-- =================================================================================

CREATE TABLE IF NOT EXISTS public.etl_jobs (
    job_id UUID PRIMARY KEY,
    file_name TEXT NOT NULL,
    file_hash TEXT NOT NULL,
    user_id TEXT NOT NULL,
    min_date DATE,
    max_date DATE,
    status VARCHAR(50) NOT NULL,
    details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at TIMESTAMPTZ,
    CONSTRAINT etl_jobs_file_hash_unique UNIQUE (file_hash)
);
COMMENT ON TABLE public.etl_jobs IS 'Registro de auditoría para cada ejecución del ETL.';

-- Índice operativo para consultar el estado de los jobs
CREATE INDEX IF NOT EXISTS ix_etl_jobs_status_created ON public.etl_jobs(status, created_at);


CREATE TABLE IF NOT EXISTS public.quarantined_records (
    id BIGSERIAL PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES public.etl_jobs(job_id),
    file_name TEXT NOT NULL,
    line_number INT,
    raw_line TEXT NOT NULL,
    error_details TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE public.quarantined_records IS 'Almacena registros del origen que fallaron la transformación.';

-- Índice operativo para analizar errores por job
CREATE INDEX IF NOT EXISTS ix_quar_job_created ON public.quarantined_records(job_id, created_at);
