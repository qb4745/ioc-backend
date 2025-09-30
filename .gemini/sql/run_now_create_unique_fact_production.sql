-- ============================================================================
-- Script: run_now_create_unique_fact_production.sql
-- Objetivo: Crear el índice UNIQUE natural en fact_production en un entorno donde:
--   * Ya se migró la PK a (id) simple
--   * Se confirmó que existen 0 duplicados lógicos
--   * Existe índice por fecha (idx_fact_production_fecha)
-- Clave natural: (fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log)
-- Uso seguro:
--   1. Confirmar nuevamente que no hay duplicados (sección PRE-CHECK)
--   2. Ejecutar este archivo completo
--   3. Verificar creación y ausencia de duplicados post-creación
-- Rollback: DROP INDEX IF EXISTS uq_fact_prod_natural;
-- ============================================================================
\echo '== PRE-CHECK: Verificando duplicados lógicos antes de UNIQUE =='
WITH dup AS (
    SELECT fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0) AS maquinista_key, numero_log, COUNT(*) c
    FROM fact_production
    GROUP BY 1,2,3,4
    HAVING COUNT(*) > 1
)
SELECT COUNT(*) AS duplicate_groups, COALESCE(SUM(c),0) AS rows_in_duplicate_groups FROM dup;

\echo '== Si duplicate_groups > 0, ABORTAR y LIMPIAR antes de continuar =='

-- (Opcional) abortar automáticamente si hay duplicados
DO $$
DECLARE v_cnt bigint;
BEGIN
  SELECT COUNT(*) INTO v_cnt FROM (
      SELECT 1 FROM fact_production
      GROUP BY fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log
      HAVING COUNT(*) > 1
  ) x;
  IF v_cnt > 0 THEN
     RAISE EXCEPTION 'Se detectaron % duplicado(s) lógicos. Abortar UNIQUE y limpiar primero.', v_cnt;
  END IF;
END $$;

\echo '== Creando índice UNIQUE uq_fact_prod_natural (idempotente) =='
CREATE UNIQUE INDEX IF NOT EXISTS uq_fact_prod_natural
  ON fact_production (fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log);

\echo '== Verificando existencia del índice UNIQUE =='
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename='fact_production'
  AND indexname='uq_fact_prod_natural';

\echo '== POST-CHECK: Confirmar 0 duplicados tras creación (sanity) =='
WITH dup AS (
    SELECT fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0) AS maquinista_key, numero_log, COUNT(*) c
    FROM fact_production
    GROUP BY 1,2,3,4
    HAVING COUNT(*) > 1
)
SELECT COUNT(*) AS duplicate_groups_after FROM dup;

\echo '== Sugerencia: Habilitar en aplicación etl.retry.unique.enabled=true si se esperan cargas concurrentes =='
\echo '== Rollback (si algo sale mal): DROP INDEX IF EXISTS uq_fact_prod_natural; =='
-- FIN DEL SCRIPT

