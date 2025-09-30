-- =============================================================
-- Script: etl_fact_production_dedup_and_unique.sql
-- Purpose: Limpieza de duplicados lógicos e instalación de índice UNIQUE
-- Contexto: Clave natural (fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log)
-- Requisitos previos: Pausar procesos ETL que inserten en fact_production durante la ejecución
-- Autor: (auto-generado)
-- Fecha: 2025-09-30
-- =============================================================
-- MODO DE USO (psql):
-- \timing on
-- BEGIN; -- (opcional, sólo si dataset pequeño; de lo contrario ejecutar pasos sueltos)
-- \i .gemini/sql/etl_fact_production_dedup_and_unique.sql
-- COMMIT; -- si se envolvió en transacción
-- =============================================================

-- 0) (Opcional) Lock global para proteger ventana de mantenimiento
-- SELECT pg_advisory_lock(424242);

-- 1) Detección inicial de duplicados (solo diagnóstico)
WITH dup AS (
    SELECT fecha_contabilizacion,
           maquina_fk,
           COALESCE(maquinista_fk,0) AS maquinista_key,
           numero_log,
           COUNT(*) AS ocurrencias
    FROM fact_production
    GROUP BY 1,2,3,4
    HAVING COUNT(*) > 1
)
SELECT 'DUP_GROUPS' AS label,
       COUNT(*) AS grupos_duplicados,
       COALESCE(SUM(ocurrencias),0) AS filas_en_grupos
FROM dup;

-- 2) Si grupos_duplicados = 0 puedes saltar directamente a paso 5.
--    Creamos SNAPSHOT temporal (no persiste tras la sesión) para auditoría y borrado seguro.
DROP TABLE IF EXISTS fact_production_dup_snapshot;
CREATE TEMP TABLE fact_production_dup_snapshot AS
SELECT fp.*
FROM fact_production fp
JOIN (
    SELECT fecha_contabilizacion,
           maquina_fk,
           COALESCE(maquinista_fk,0) AS mkey,
           numero_log
    FROM fact_production
    GROUP BY 1,2,3,4
    HAVING COUNT(*) > 1
) d ON d.fecha_contabilizacion = fp.fecha_contabilizacion
   AND d.maquina_fk = fp.maquina_fk
   AND d.mkey = COALESCE(fp.maquinista_fk,0)
   AND d.numero_log = fp.numero_log;

-- 3) Eliminar duplicados conservando el menor id por clave lógica
WITH survivor AS (
    SELECT MIN(id) AS keep_id
    FROM fact_production_dup_snapshot
    GROUP BY fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log
),
non_survivor AS (
    SELECT d.id
    FROM fact_production_dup_snapshot d
    LEFT JOIN survivor s ON d.id = s.keep_id
    WHERE s.keep_id IS NULL
)
DELETE FROM fact_production fp
USING non_survivor ns
WHERE fp.id = ns.id
RETURNING fp.id;

-- 4) Verificación post-limpieza
WITH dup_after AS (
    SELECT 1
    FROM fact_production
    GROUP BY fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log
    HAVING COUNT(*) > 1
)
SELECT CASE WHEN EXISTS (SELECT * FROM dup_after)
            THEN 'ERROR: Aún hay duplicados' ELSE 'OK: Sin duplicados' END AS verificacion_final;

-- (Opcional) Si se borraron muchas filas:
-- VACUUM (ANALYZE) fact_production;

-- 5) Índice para acelerar DELETE/consultas por rango de fecha
CREATE INDEX IF NOT EXISTS idx_fact_production_fecha ON fact_production(fecha_contabilizacion);

-- 6) Crear índice UNIQUE de clave natural (defensivo: solo si ya no hay duplicados)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM fact_production
        GROUP BY fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'No se puede crear UNIQUE: aún existen duplicados.';
    END IF;

    CREATE UNIQUE INDEX IF NOT EXISTS uq_fact_prod_natural
        ON fact_production(fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log);
END $$;

-- 7) (Opcional) Realinear secuencia sólo si se detecta desalineación posteriormente
-- SELECT setval('fact_production_id_seq', (SELECT COALESCE(MAX(id),0)+1 FROM fact_production), false);

-- 8) (Opcional) Liberar lock global
-- SELECT pg_advisory_unlock(424242);

-- 9) (Opcional) Registrar auditoría (crear tabla si se desea mantener historial)
-- CREATE TABLE IF NOT EXISTS etl_auditoria (
--   id BIGSERIAL PRIMARY KEY,
--   evento TEXT NOT NULL,
--   detalle TEXT,
--   created_at TIMESTAMPTZ DEFAULT now()
-- );
-- INSERT INTO etl_auditoria(evento, detalle) VALUES ('DEDUP_FACT_PRODUCTION', 'Índice uq_fact_prod_natural creado');

-- FIN DEL SCRIPT

