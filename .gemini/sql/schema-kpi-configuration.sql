-- =============================================================================
-- ||                                                                         ||
-- ||    SCHEMA DE BASE DE DATOS: SISTEMA DE CONFIGURACIÓN DE KPIs           ||
-- ||    Feature: IOC-003                                                     ||
-- ||    Proyecto: Inteligencia Operacional Cambiaso (IOC Backend)           ||
-- ||                                                                         ||
-- =============================================================================
-- ||                                                                         ||
-- ||    Descripción:                                                         ||
-- ||    Este script crea las tablas necesarias para el sistema de           ||
-- ||    configuración de KPIs y gestión de alertas.                         ||
-- ||                                                                         ||
-- ||    Tablas Creadas:                                                      ||
-- ||    1. kpi_configurations - Configuración de KPIs                       ||
-- ||    2. kpi_thresholds - Umbrales de alertas por KPI                     ||
-- ||    3. kpi_alerts - Alertas generadas                                   ||
-- ||    4. kpi_evaluation_log - Log de evaluaciones (auditoría)             ||
-- ||                                                                         ||
-- ||    Dependencias:                                                        ||
-- ||    - Requiere que exista la tabla 'etl_jobs' (referencia opcional)     ||
-- ||                                                                         ||
-- ||    Instrucciones de Ejecución:                                          ||
-- ||    1. Conectarse a Supabase SQL Editor                                  ||
-- ||    2. Ejecutar este script completo                                     ||
-- ||    3. Verificar que las tablas se crearon correctamente                 ||
-- ||                                                                         ||
-- =============================================================================

-- =============================================================================
-- SECCIÓN 1: TABLA DE CONFIGURACIÓN DE KPIs
-- =============================================================================

-- Descripción: Almacena la configuración de cada KPI que el sistema monitoreará
-- Características:
--   - Soft delete (deleted_at)
--   - Auditoría (created_by, created_at, updated_at)
--   - Validaciones de rango (CHECK constraints)
--   - Soporte para múltiples unidades de medida

CREATE TABLE IF NOT EXISTS kpi_configurations (
    -- Identificador único
    id SERIAL PRIMARY KEY,

    -- Código único del KPI (usado en lógica de negocio)
    -- Ejemplo: "EFFICIENCY_LINE_A", "CYCLE_TIME_M3"
    code VARCHAR(100) UNIQUE NOT NULL,

    -- Nombre descriptivo del KPI
    name VARCHAR(200) NOT NULL,

    -- Descripción detallada (opcional)
    description TEXT,

    -- Unidad de medida del KPI
    -- PERCENTAGE: 0-100 (eficiencia, utilización)
    -- INTEGER: números enteros (cantidad de piezas)
    -- DECIMAL: números decimales (peso, tiempo con decimales)
    -- TIME: tiempo en segundos
    unit VARCHAR(20) NOT NULL CHECK (unit IN ('PERCENTAGE', 'INTEGER', 'DECIMAL', 'TIME')),

    -- Rango de valores permitidos para el KPI
    min_value NUMERIC(15, 4) NOT NULL,
    max_value NUMERIC(15, 4) NOT NULL,

    -- Valor objetivo o ideal del KPI
    target_value NUMERIC(15, 4) NOT NULL,

    -- Indicador de si el KPI está activo (genera alertas)
    is_active BOOLEAN DEFAULT true NOT NULL,

    -- Auditoría: quién creó el KPI
    created_by VARCHAR(255) NOT NULL,

    -- Auditoría: cuándo se creó
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Auditoría: última modificación
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Soft delete: si no es NULL, el KPI está "eliminado"
    deleted_at TIMESTAMP,

    -- Última vez que se evaluó este KPI (actualizado por el motor)
    last_evaluated_at TIMESTAMP,

    -- Metadata adicional en formato JSON (extensibilidad futura)
    -- Ejemplo: {"data_source": "fact_production", "calculation": "AVG(eficiencia)"}
    metadata JSONB,

    -- CONSTRAINTS DE VALIDACIÓN

    -- El valor mínimo debe ser menor que el máximo
    CONSTRAINT check_kpi_min_max CHECK (min_value < max_value),

    -- El valor objetivo debe estar dentro del rango permitido
    CONSTRAINT check_kpi_target_in_range CHECK (target_value BETWEEN min_value AND max_value)
);

-- Índices para optimizar consultas frecuentes
CREATE INDEX idx_kpi_code ON kpi_configurations(code) WHERE deleted_at IS NULL;
CREATE INDEX idx_kpi_active ON kpi_configurations(is_active) WHERE deleted_at IS NULL;
CREATE INDEX idx_kpi_last_evaluated ON kpi_configurations(last_evaluated_at);

-- Comentarios en la tabla
COMMENT ON TABLE kpi_configurations IS 'Configuración de KPIs monitoreados por el sistema';
COMMENT ON COLUMN kpi_configurations.code IS 'Código único alfanumérico del KPI (usado en lógica de negocio)';
COMMENT ON COLUMN kpi_configurations.is_active IS 'Si es false, no se generan alertas para este KPI';
COMMENT ON COLUMN kpi_configurations.deleted_at IS 'Fecha de soft delete. Si no es NULL, el KPI está eliminado';
COMMENT ON COLUMN kpi_configurations.metadata IS 'Datos adicionales en formato JSON para extensibilidad';


-- =============================================================================
-- SECCIÓN 2: TABLA DE UMBRALES DE KPIs
-- =============================================================================

-- Descripción: Define los umbrales que disparan alertas para cada KPI
-- Un KPI puede tener múltiples umbrales (ej: warning < 80, critical < 70)

CREATE TABLE IF NOT EXISTS kpi_thresholds (
    -- Identificador único
    id SERIAL PRIMARY KEY,

    -- FK al KPI al que pertenece este umbral
    kpi_configuration_id INTEGER NOT NULL,

    -- Tipo de severidad del umbral
    -- WARNING: alerta de advertencia (nivel medio)
    -- CRITICAL: alerta crítica (nivel alto, requiere acción inmediata)
    type VARCHAR(20) NOT NULL CHECK (type IN ('WARNING', 'CRITICAL')),

    -- Operador de comparación
    -- '<' : menor que (ej: eficiencia < 80)
    -- '>' : mayor que (ej: tiempo_ciclo > 120)
    -- '<=' : menor o igual que
    -- '>=' : mayor o igual que
    -- '=' : igual a
    -- '!=' : diferente de
    operator VARCHAR(5) NOT NULL CHECK (operator IN ('<', '>', '<=', '>=', '=', '!=')),

    -- Valor del umbral
    -- Debe estar dentro del rango [min_value, max_value] del KPI
    value NUMERIC(15, 4) NOT NULL,

    -- Auditoría: cuándo se creó el umbral
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Mensaje personalizado para la alerta (opcional)
    -- Ejemplo: "Eficiencia por debajo del mínimo aceptable"
    custom_message TEXT,

    -- CONSTRAINTS

    -- Relación con kpi_configurations (cascade delete)
    CONSTRAINT fk_threshold_kpi FOREIGN KEY (kpi_configuration_id)
        REFERENCES kpi_configurations(id) ON DELETE CASCADE
);

-- Índices para optimizar consultas
CREATE INDEX idx_threshold_kpi ON kpi_thresholds(kpi_configuration_id);
CREATE INDEX idx_threshold_type ON kpi_thresholds(type);

-- Comentarios
COMMENT ON TABLE kpi_thresholds IS 'Umbrales que disparan alertas para cada KPI';
COMMENT ON COLUMN kpi_thresholds.type IS 'WARNING (advertencia) o CRITICAL (crítico)';
COMMENT ON COLUMN kpi_thresholds.operator IS 'Operador de comparación: <, >, <=, >=, =, !=';
COMMENT ON COLUMN kpi_thresholds.custom_message IS 'Mensaje personalizado opcional para la alerta';


-- =============================================================================
-- SECCIÓN 3: TABLA DE ALERTAS GENERADAS
-- =============================================================================

-- Descripción: Almacena las alertas generadas cuando un KPI excede un umbral
-- Características:
--   - Vinculación con el job ETL que generó la alerta
--   - Sistema de acknowledgment (marcar como revisada)
--   - Metadata extensible en JSON

CREATE TABLE IF NOT EXISTS kpi_alerts (
    -- Identificador único
    id SERIAL PRIMARY KEY,

    -- FK al KPI que generó la alerta
    kpi_configuration_id INTEGER NOT NULL,

    -- FK al threshold que fue excedido
    threshold_id INTEGER NOT NULL,

    -- FK al job ETL que estaba corriendo cuando se detectó (opcional)
    -- Permite rastrear en qué carga de datos se detectó el problema
    etl_job_id INTEGER,

    -- Valor actual del KPI cuando se generó la alerta
    current_value NUMERIC(15, 4) NOT NULL,

    -- Valor del umbral que fue excedido (desnormalizado para historial)
    threshold_value NUMERIC(15, 4) NOT NULL,

    -- Operador del umbral (desnormalizado)
    threshold_operator VARCHAR(5) NOT NULL,

    -- Severidad de la alerta (desnormalizado desde threshold)
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('WARNING', 'CRITICAL')),

    -- Sistema de acknowledgment (marcar como revisada)
    is_acknowledged BOOLEAN DEFAULT false NOT NULL,

    -- Quién marcó la alerta como revisada
    acknowledged_by VARCHAR(255),

    -- Cuándo se marcó como revisada
    acknowledged_at TIMESTAMP,

    -- Cuándo se detectó la alerta
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Metadata adicional (extensibilidad)
    -- Ejemplo: {"machine_id": 3, "shift": "morning", "operator": "Juan Pérez"}
    metadata JSONB,

    -- Mensaje de la alerta (puede ser personalizado o auto-generado)
    message TEXT,

    -- CONSTRAINTS

    -- Relación con kpi_configurations
    CONSTRAINT fk_alert_kpi FOREIGN KEY (kpi_configuration_id)
        REFERENCES kpi_configurations(id) ON DELETE CASCADE,

    -- Relación con kpi_thresholds
    CONSTRAINT fk_alert_threshold FOREIGN KEY (threshold_id)
        REFERENCES kpi_thresholds(id) ON DELETE CASCADE,

    -- Relación con etl_jobs (opcional, puede ser NULL)
    CONSTRAINT fk_alert_etl_job FOREIGN KEY (etl_job_id)
        REFERENCES etl_jobs(id) ON DELETE SET NULL,

    -- Validación de acknowledgment: si está acknowledged, debe tener usuario y fecha
    CONSTRAINT check_alert_acknowledged CHECK (
        (is_acknowledged = false AND acknowledged_by IS NULL AND acknowledged_at IS NULL) OR
        (is_acknowledged = true AND acknowledged_by IS NOT NULL AND acknowledged_at IS NOT NULL)
    )
);

-- Índices para optimizar consultas
CREATE INDEX idx_alert_kpi ON kpi_alerts(kpi_configuration_id);
CREATE INDEX idx_alert_threshold ON kpi_alerts(threshold_id);
CREATE INDEX idx_alert_etl_job ON kpi_alerts(etl_job_id) WHERE etl_job_id IS NOT NULL;
CREATE INDEX idx_alert_acknowledged ON kpi_alerts(is_acknowledged);
CREATE INDEX idx_alert_detected ON kpi_alerts(detected_at DESC);
CREATE INDEX idx_alert_severity ON kpi_alerts(severity);
CREATE INDEX idx_alert_active ON kpi_alerts(is_acknowledged, detected_at DESC) WHERE is_acknowledged = false;

-- Comentarios
COMMENT ON TABLE kpi_alerts IS 'Alertas generadas cuando un KPI excede un umbral';
COMMENT ON COLUMN kpi_alerts.is_acknowledged IS 'Indica si la alerta ha sido revisada por un usuario';
COMMENT ON COLUMN kpi_alerts.acknowledged_by IS 'Email del usuario que revisó la alerta';
COMMENT ON COLUMN kpi_alerts.etl_job_id IS 'Job ETL que estaba corriendo cuando se detectó (opcional)';
COMMENT ON COLUMN kpi_alerts.metadata IS 'Contexto adicional de la alerta en formato JSON';


-- =============================================================================
-- SECCIÓN 4: TABLA DE LOG DE EVALUACIONES (Auditoría)
-- =============================================================================

-- Descripción: Log de auditoría de cada evaluación de KPIs
-- Útil para debugging y análisis de tendencias
-- Esta tabla puede crecer mucho, considerar particionamiento en el futuro

CREATE TABLE IF NOT EXISTS kpi_evaluation_log (
    -- Identificador único
    id BIGSERIAL PRIMARY KEY,

    -- FK al KPI evaluado
    kpi_configuration_id INTEGER NOT NULL,

    -- FK al job ETL asociado (opcional)
    etl_job_id INTEGER,

    -- Valor calculado del KPI en esta evaluación
    calculated_value NUMERIC(15, 4),

    -- Indicador de si la evaluación fue exitosa
    -- false si hubo un error al calcular el KPI
    is_successful BOOLEAN DEFAULT true NOT NULL,

    -- Mensaje de error (si is_successful = false)
    error_message TEXT,

    -- Timestamp de la evaluación
    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Tiempo de ejecución de la evaluación (en milisegundos)
    execution_time_ms INTEGER,

    -- Si se generó una alerta en esta evaluación
    alert_generated BOOLEAN DEFAULT false NOT NULL,

    -- ID de la alerta generada (si aplica)
    alert_id INTEGER,

    -- CONSTRAINTS

    CONSTRAINT fk_eval_kpi FOREIGN KEY (kpi_configuration_id)
        REFERENCES kpi_configurations(id) ON DELETE CASCADE,

    CONSTRAINT fk_eval_etl_job FOREIGN KEY (etl_job_id)
        REFERENCES etl_jobs(id) ON DELETE SET NULL,

    CONSTRAINT fk_eval_alert FOREIGN KEY (alert_id)
        REFERENCES kpi_alerts(id) ON DELETE SET NULL
);

-- Índices para optimizar consultas
CREATE INDEX idx_eval_kpi ON kpi_evaluation_log(kpi_configuration_id);
CREATE INDEX idx_eval_evaluated_at ON kpi_evaluation_log(evaluated_at DESC);
CREATE INDEX idx_eval_successful ON kpi_evaluation_log(is_successful) WHERE is_successful = false;

-- Comentarios
COMMENT ON TABLE kpi_evaluation_log IS 'Log de auditoría de evaluaciones de KPIs';
COMMENT ON COLUMN kpi_evaluation_log.is_successful IS 'false si hubo error al calcular el KPI';
COMMENT ON COLUMN kpi_evaluation_log.execution_time_ms IS 'Tiempo de ejecución en milisegundos';
COMMENT ON COLUMN kpi_evaluation_log.alert_generated IS 'Indica si esta evaluación generó una alerta';


-- =============================================================================
-- SECCIÓN 5: FUNCIONES Y TRIGGERS
-- =============================================================================

-- Función para actualizar automáticamente el campo updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para kpi_configurations
DROP TRIGGER IF EXISTS trigger_update_kpi_updated_at ON kpi_configurations;
CREATE TRIGGER trigger_update_kpi_updated_at
    BEFORE UPDATE ON kpi_configurations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON FUNCTION update_updated_at_column() IS 'Actualiza automáticamente el campo updated_at en cada UPDATE';


-- =============================================================================
-- SECCIÓN 6: DATOS DE EJEMPLO (SEED DATA) - OPCIONAL
-- =============================================================================

-- Descomentar esta sección si deseas crear KPIs de ejemplo para testing

/*
-- Ejemplo 1: KPI de Eficiencia de Línea A
INSERT INTO kpi_configurations (code, name, description, unit, min_value, max_value, target_value, is_active, created_by, metadata)
VALUES (
    'EFFICIENCY_LINE_A',
    'Eficiencia de Línea A',
    'Mide el porcentaje de eficiencia de la línea de producción A',
    'PERCENTAGE',
    0,
    100,
    90,
    true,
    'admin@cambiaso.com',
    '{"data_source": "fact_production", "calculation": "AVG(eficiencia)", "line_id": "A"}'::jsonb
);

-- Umbrales para Eficiencia de Línea A
INSERT INTO kpi_thresholds (kpi_configuration_id, type, operator, value, custom_message)
SELECT id, 'WARNING', '<', 80, 'Eficiencia por debajo del nivel esperado'
FROM kpi_configurations WHERE code = 'EFFICIENCY_LINE_A';

INSERT INTO kpi_thresholds (kpi_configuration_id, type, operator, value, custom_message)
SELECT id, 'CRITICAL', '<', 70, 'Eficiencia crítica - requiere acción inmediata'
FROM kpi_configurations WHERE code = 'EFFICIENCY_LINE_A';


-- Ejemplo 2: KPI de Tiempo de Ciclo de Máquina 3
INSERT INTO kpi_configurations (code, name, description, unit, min_value, max_value, target_value, is_active, created_by, metadata)
VALUES (
    'CYCLE_TIME_M3',
    'Tiempo de Ciclo Máquina 3',
    'Mide el tiempo promedio de ciclo de la máquina 3 en segundos',
    'TIME',
    0,
    300,
    90,
    true,
    'admin@cambiaso.com',
    '{"data_source": "fact_production", "calculation": "AVG(tiempo_ciclo)", "machine_id": 3}'::jsonb
);

-- Umbrales para Tiempo de Ciclo
INSERT INTO kpi_thresholds (kpi_configuration_id, type, operator, value, custom_message)
SELECT id, 'WARNING', '>', 120, 'Tiempo de ciclo elevado'
FROM kpi_configurations WHERE code = 'CYCLE_TIME_M3';

INSERT INTO kpi_thresholds (kpi_configuration_id, type, operator, value, custom_message)
SELECT id, 'CRITICAL', '>', 180, 'Tiempo de ciclo crítico - revisar máquina'
FROM kpi_configurations WHERE code = 'CYCLE_TIME_M3';


-- Ejemplo 3: KPI de Producción Diaria
INSERT INTO kpi_configurations (code, name, description, unit, min_value, max_value, target_value, is_active, created_by, metadata)
VALUES (
    'DAILY_PRODUCTION',
    'Producción Diaria Total',
    'Cantidad total de piezas producidas por día',
    'INTEGER',
    0,
    10000,
    8000,
    true,
    'admin@cambiaso.com',
    '{"data_source": "fact_production", "calculation": "SUM(cantidad)", "period": "daily"}'::jsonb
);

-- Umbrales para Producción Diaria
INSERT INTO kpi_thresholds (kpi_configuration_id, type, operator, value, custom_message)
SELECT id, 'WARNING', '<', 6000, 'Producción por debajo del objetivo'
FROM kpi_configurations WHERE code = 'DAILY_PRODUCTION';

INSERT INTO kpi_thresholds (kpi_configuration_id, type, operator, value, custom_message)
SELECT id, 'CRITICAL', '<', 4000, 'Producción crítica - revisar operaciones'
FROM kpi_configurations WHERE code = 'DAILY_PRODUCTION';
*/


-- =============================================================================
-- SECCIÓN 7: VIEWS ÚTILES
-- =============================================================================

-- Vista: KPIs Activos con sus Umbrales
CREATE OR REPLACE VIEW vw_active_kpis_with_thresholds AS
SELECT
    k.id AS kpi_id,
    k.code AS kpi_code,
    k.name AS kpi_name,
    k.unit AS kpi_unit,
    k.min_value,
    k.max_value,
    k.target_value,
    k.last_evaluated_at,
    t.id AS threshold_id,
    t.type AS threshold_type,
    t.operator AS threshold_operator,
    t.value AS threshold_value,
    t.custom_message AS threshold_message
FROM kpi_configurations k
LEFT JOIN kpi_thresholds t ON k.id = t.kpi_configuration_id
WHERE k.is_active = true
  AND k.deleted_at IS NULL
ORDER BY k.code, t.type;

COMMENT ON VIEW vw_active_kpis_with_thresholds IS 'Vista de KPIs activos con sus umbrales configurados';


-- Vista: Alertas Activas (No Revisadas)
CREATE OR REPLACE VIEW vw_active_alerts AS
SELECT
    a.id AS alert_id,
    a.detected_at,
    a.severity,
    a.current_value,
    a.threshold_operator,
    a.threshold_value,
    a.message,
    k.code AS kpi_code,
    k.name AS kpi_name,
    k.unit AS kpi_unit,
    e.id AS etl_job_id,
    e.status AS etl_job_status
FROM kpi_alerts a
JOIN kpi_configurations k ON a.kpi_configuration_id = k.id
LEFT JOIN etl_jobs e ON a.etl_job_id = e.id
WHERE a.is_acknowledged = false
ORDER BY
    CASE a.severity
        WHEN 'CRITICAL' THEN 1
        WHEN 'WARNING' THEN 2
    END,
    a.detected_at DESC;

COMMENT ON VIEW vw_active_alerts IS 'Vista de alertas no revisadas, ordenadas por severidad y fecha';


-- Vista: Resumen de KPIs (estadísticas)
CREATE OR REPLACE VIEW vw_kpi_summary AS
SELECT
    k.id,
    k.code,
    k.name,
    k.is_active,
    k.last_evaluated_at,
    COUNT(DISTINCT t.id) AS num_thresholds,
    COUNT(DISTINCT CASE WHEN a.is_acknowledged = false THEN a.id END) AS active_alerts,
    COUNT(DISTINCT a.id) AS total_alerts,
    MAX(a.detected_at) AS last_alert_at
FROM kpi_configurations k
LEFT JOIN kpi_thresholds t ON k.id = t.kpi_configuration_id
LEFT JOIN kpi_alerts a ON k.id = a.kpi_configuration_id
WHERE k.deleted_at IS NULL
GROUP BY k.id, k.code, k.name, k.is_active, k.last_evaluated_at
ORDER BY k.code;

COMMENT ON VIEW vw_kpi_summary IS 'Resumen estadístico de cada KPI configurado';


-- =============================================================================
-- SECCIÓN 8: POLÍTICAS DE SEGURIDAD (Row Level Security - RLS)
-- =============================================================================

-- NOTA: Estas políticas asumen que Supabase Auth está configurado
-- y que auth.uid() devuelve el ID del usuario autenticado

-- Habilitar RLS en todas las tablas
ALTER TABLE kpi_configurations ENABLE ROW LEVEL SECURITY;
ALTER TABLE kpi_thresholds ENABLE ROW LEVEL SECURITY;
ALTER TABLE kpi_alerts ENABLE ROW LEVEL SECURITY;
ALTER TABLE kpi_evaluation_log ENABLE ROW LEVEL SECURITY;

-- Política: Solo usuarios autenticados pueden leer KPIs
CREATE POLICY "Usuarios autenticados pueden leer KPIs"
ON kpi_configurations FOR SELECT
TO authenticated
USING (deleted_at IS NULL);

-- Política: Solo administradores pueden crear/modificar/eliminar KPIs
-- NOTA: Ajustar esta política según tu tabla de usuarios y roles
-- Ejemplo asumiendo que existe una función que valida el rol

CREATE POLICY "Solo admins pueden modificar KPIs"
ON kpi_configurations FOR ALL
TO authenticated
USING (
    -- Aquí debes implementar la lógica de verificación de rol
    -- Ejemplo: auth.jwt() ->> 'role' = 'admin'
    true  -- Temporal: permitir a todos (cambiar en producción)
);

-- Política: Usuarios autenticados pueden leer alertas
CREATE POLICY "Usuarios autenticados pueden leer alertas"
ON kpi_alerts FOR SELECT
TO authenticated
USING (true);

-- Política: Usuarios autenticados pueden marcar alertas como revisadas
CREATE POLICY "Usuarios pueden marcar alertas como revisadas"
ON kpi_alerts FOR UPDATE
TO authenticated
USING (true)
WITH CHECK (true);


-- =============================================================================
-- SECCIÓN 9: FUNCIONES DE UTILIDAD
-- =============================================================================

-- Función para obtener alertas activas de un KPI específico
CREATE OR REPLACE FUNCTION get_active_alerts_for_kpi(p_kpi_code VARCHAR)
RETURNS TABLE (
    alert_id INTEGER,
    detected_at TIMESTAMP,
    severity VARCHAR,
    current_value NUMERIC,
    threshold_value NUMERIC,
    message TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        a.id,
        a.detected_at,
        a.severity,
        a.current_value,
        a.threshold_value,
        a.message
    FROM kpi_alerts a
    JOIN kpi_configurations k ON a.kpi_configuration_id = k.id
    WHERE k.code = p_kpi_code
      AND a.is_acknowledged = false
    ORDER BY a.detected_at DESC;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_active_alerts_for_kpi(VARCHAR) IS 'Obtiene alertas activas de un KPI específico por código';


-- Función para verificar si un valor excede un umbral
CREATE OR REPLACE FUNCTION check_threshold_violation(
    p_value NUMERIC,
    p_operator VARCHAR,
    p_threshold NUMERIC
)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN CASE p_operator
        WHEN '<' THEN p_value < p_threshold
        WHEN '>' THEN p_value > p_threshold
        WHEN '<=' THEN p_value <= p_threshold
        WHEN '>=' THEN p_value >= p_threshold
        WHEN '=' THEN p_value = p_threshold
        WHEN '!=' THEN p_value != p_threshold
        ELSE false
    END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION check_threshold_violation(NUMERIC, VARCHAR, NUMERIC) IS 'Evalúa si un valor viola un umbral según el operador';


-- =============================================================================
-- SECCIÓN 10: VERIFICACIÓN DE INSTALACIÓN
-- =============================================================================

-- Query de verificación: Contar tablas creadas
SELECT
    'Tablas creadas' AS verificacion,
    COUNT(*) AS cantidad
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('kpi_configurations', 'kpi_thresholds', 'kpi_alerts', 'kpi_evaluation_log');

-- Query de verificación: Listar índices creados
SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename LIKE 'kpi_%'
ORDER BY tablename, indexname;

-- Query de verificación: Listar vistas creadas
SELECT
    table_name
FROM information_schema.views
WHERE table_schema = 'public'
  AND table_name LIKE 'vw_%kpi%';


-- =============================================================================
-- FIN DEL SCRIPT
-- =============================================================================

-- Mensaje de confirmación
DO $$
BEGIN
    RAISE NOTICE '✅ Schema de KPI Configuration creado exitosamente';
    RAISE NOTICE '📊 Tablas: kpi_configurations, kpi_thresholds, kpi_alerts, kpi_evaluation_log';
    RAISE NOTICE '👁️ Vistas: vw_active_kpis_with_thresholds, vw_active_alerts, vw_kpi_summary';
    RAISE NOTICE '🔒 RLS habilitado en todas las tablas';
    RAISE NOTICE '';
    RAISE NOTICE 'Próximos pasos:';
    RAISE NOTICE '1. Verificar que todas las tablas se crearon correctamente';
    RAISE NOTICE '2. Ajustar las políticas RLS según tu sistema de roles';
    RAISE NOTICE '3. Descomentar la sección de SEED DATA si deseas datos de ejemplo';
    RAISE NOTICE '4. Implementar las entidades JPA en el backend (Java)';
END $$;

