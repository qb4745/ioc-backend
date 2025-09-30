-- ============================================================================
-- Migration Blueprint: fact_production supporting indexes & UNIQUE constraint
-- This file is NOT automatically executed (no Flyway/Liquibase wired yet).
-- Execute manually after verifying there are NO duplicate logical groups:
--   (fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log)
-- ============================================================================

-- 1. Optional: quick duplicate detection (comment/uncomment to run):
-- SELECT fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0) AS maquinista_key, numero_log, COUNT(*) c
-- FROM fact_production
-- GROUP BY 1,2,3,4 HAVING COUNT(*) > 1 ORDER BY c DESC LIMIT 20;

-- 2. Index to accelerate range deletes & queries by date
CREATE INDEX IF NOT EXISTS idx_fact_production_fecha
  ON fact_production(fecha_contabilizacion);

-- 3. Optional: if business enforces maquinista_fk is always present, uncomment below to enforce NOT NULL
-- ALTER TABLE fact_production
--   ALTER COLUMN maquinista_fk SET NOT NULL;

-- 4. UNIQUE index on the natural key (using COALESCE to treat null maquinista_fk as 0)
CREATE UNIQUE INDEX IF NOT EXISTS uq_fact_prod_natural
  ON fact_production (fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log);

-- 5. Verification queries (run manually):
-- SELECT relname, indexrelname, idx_scan, idx_tup_read, idx_tup_fetch
-- FROM pg_stat_user_indexes sui JOIN pg_index i ON sui.indexrelid = i.indexrelid
-- WHERE relname = 'fact_production';

-- 6. Rollback (if needed):
-- DROP INDEX IF EXISTS uq_fact_prod_natural;
-- DROP INDEX IF EXISTS idx_fact_production_fecha;

-- End of migration blueprint.

