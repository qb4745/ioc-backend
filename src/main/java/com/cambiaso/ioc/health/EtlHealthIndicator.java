package com.cambiaso.ioc.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * HealthIndicator para exponer estado de integridad ETL
 */
@Component
public class EtlHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    private static final String UNIQUE_SQL = "SELECT 1 FROM pg_indexes WHERE tablename='fact_production' AND indexname='uq_fact_prod_natural'";
    private static final String DUP_COUNT_SQL = "SELECT COUNT(*) FROM (SELECT 1 FROM fact_production GROUP BY fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log HAVING COUNT(*)>1) t";
    private static final String MAX_ID_SQL = "SELECT COALESCE(MAX(id),0) FROM fact_production";
    private static final String SEQ_SQL = "SELECT last_value FROM fact_production_id_seq";
    private static final String ROWCOUNT_SQL = "SELECT reltuples::bigint FROM pg_class WHERE relname='fact_production'";

    public EtlHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            boolean uniquePresent = !jdbcTemplate.queryForList(UNIQUE_SQL).isEmpty();
            int duplicateGroups = jdbcTemplate.queryForObject(DUP_COUNT_SQL, Integer.class);
            long maxId = jdbcTemplate.queryForObject(MAX_ID_SQL, Long.class);
            long lastVal = jdbcTemplate.queryForObject(SEQ_SQL, Long.class);
            boolean sequenceBehind = maxId >= lastVal;
            long approxRows = jdbcTemplate.queryForObject(ROWCOUNT_SQL, Long.class);

            Health.Builder builder = (uniquePresent && duplicateGroups == 0) ? Health.up() : Health.down();
            builder.withDetails(Map.of(
                    "uniqueIndexPresent", uniquePresent,
                    "duplicateGroups", duplicateGroups,
                    "sequenceBehindSuspect", sequenceBehind,
                    "approxRowCount", approxRows,
                    "maxId", maxId,
                    "sequenceLastValue", lastVal
            ));
            return builder.build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
