-- WARNING: Este script crea y popula una tabla de dimensión de tiempo.
-- No afecta ni hace backfill de tablas existentes como fact_production.

-- Paso 1: Eliminar la tabla dim_date si ya existe (para idempotencia)
DROP TABLE IF EXISTS public.dim_date;

-- Paso 2: Crear la tabla de dimensión de tiempo (dim_date)
CREATE TABLE public.dim_date (
    date_key INT PRIMARY KEY,         -- Clave única (ej: 20251122)
    full_date DATE NOT NULL UNIQUE,   -- La fecha completa (ej: 2025-11-22)
    day_of_week INT NOT NULL,         -- Día de la semana (1 = Lunes, 7 = Domingo)
    day_name VARCHAR(10) NOT NULL,    -- Nombre del día (ej: 'Lunes')
    day_of_month INT NOT NULL,        -- Día del mes (1-31)
    day_of_year INT NOT NULL,         -- Día del año (1-366)
    week_of_year INT NOT NULL,        -- Semana del año (1-53)
    month INT NOT NULL,               -- Número del mes (1-12)
    month_name VARCHAR(10) NOT NULL,  -- Nombre del mes (ej: 'Noviembre')
    quarter INT NOT NULL,             -- Trimestre (1-4)
    quarter_name VARCHAR(2) NOT NULL, -- Nombre del trimestre (ej: 'Q4')
    year INT NOT NULL,                -- Año (ej: 2025)
    is_weekend BOOLEAN NOT NULL,      -- Es fin de semana (true/false)
    is_holiday BOOLEAN NOT NULL,      -- Es festivo (true/false - por defecto false, se puede actualizar)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Paso 3: Poblar la tabla dim_date con un rango de fechas (ej. de 2020 a 2040)
INSERT INTO public.dim_date (
    date_key,
    full_date,
    day_of_week,
    day_name,
    day_of_month,
    day_of_year,
    week_of_year,
    month,
    month_name,
    quarter,
    quarter_name,
    year,
    is_weekend,
    is_holiday
)
SELECT
    -- date_key (YYYYMMDD)
    TO_CHAR(datum, 'YYYYMMDD')::INT AS date_key,
    -- full_date
    datum AS full_date,
    -- day_of_week (1=Monday, 7=Sunday)
    EXTRACT(ISODOW FROM datum)::INT AS day_of_week,
    -- day_name
    TO_CHAR(datum, 'Day') AS day_name,
    -- day_of_month
    EXTRACT(DAY FROM datum)::INT AS day_of_month,
    -- day_of_year
    EXTRACT(DOY FROM datum)::INT AS day_of_year,
    -- week_of_year
    EXTRACT(WEEK FROM datum)::INT AS week_of_year,
    -- month
    EXTRACT(MONTH FROM datum)::INT AS month,
    -- month_name
    TO_CHAR(datum, 'Month') AS month_name,
    -- quarter
    EXTRACT(QUARTER FROM datum)::INT AS quarter,
    -- quarter_name
    'Q' || EXTRACT(QUARTER FROM datum)::INT AS quarter_name,
    -- year
    EXTRACT(YEAR FROM datum)::INT AS year,
    -- is_weekend
    CASE WHEN EXTRACT(ISODOW FROM datum) IN (6, 7) THEN TRUE ELSE FALSE END AS is_weekend,
    -- is_holiday (por defecto false, se puede actualizar manualmente o con otro proceso)
    FALSE AS is_holiday
FROM (
    -- Rango de fechas: Desde el 1 de Enero de 2020 hasta el 31 de Diciembre de 2040
    SELECT '2020-01-01'::DATE + GENERATE_SERIES(0, ('2040-12-31'::DATE - '2020-01-01'::DATE)) AS datum
) AS sq
ORDER BY datum;

-- Paso 4 (Opcional): Añadir índices adicionales para mejorar el rendimiento de búsqueda
CREATE INDEX idx_dim_date_year_month ON public.dim_date (year, month);
CREATE INDEX idx_dim_date_quarter_year ON public.dim_date (quarter, year);

-- Paso 5 (Opcional): Añadir comentarios a la tabla y columnas para mejor documentación
COMMENT ON TABLE public.dim_date IS 'Tabla de dimensión de tiempo para análisis de datos.';
COMMENT ON COLUMN public.dim_date.date_key IS 'Clave única para la fecha (YYYYMMDD).';
COMMENT ON COLUMN public.dim_date.full_date IS 'Fecha completa.';
COMMENT ON COLUMN public.dim_date.day_of_week IS 'Número del día de la semana (1=Lunes, 7=Domingo).';
COMMENT ON COLUMN public.dim_date.day_name IS 'Nombre del día de la semana.';
COMMENT ON COLUMN public.dim_date.day_of_month IS 'Día del mes.';
COMMENT ON COLUMN public.dim_date.day_of_year IS 'Día del año.';
COMMENT ON COLUMN public.dim_date.week_of_year IS 'Número de la semana del año.';
COMMENT ON COLUMN public.dim_date.month IS 'Número del mes.';
COMMENT ON COLUMN public.dim_date.month_name IS 'Nombre del mes.';
COMMENT ON COLUMN public.dim_date.quarter IS 'Número del trimestre.';
COMMENT ON COLUMN public.dim_date.quarter_name IS 'Nombre del trimestre.';
COMMENT ON COLUMN public.dim_date.year IS 'Año.';
COMMENT ON COLUMN public.dim_date.is_weekend IS 'Indica si la fecha es fin de semana.';
COMMENT ON COLUMN public.dim_date.is_holiday IS 'Indica si la fecha es festivo (debe actualizarse).';
