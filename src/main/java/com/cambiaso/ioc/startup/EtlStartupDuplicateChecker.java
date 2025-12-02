package com.cambiaso.ioc.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Verifica en el arranque si existen duplicados lógicos en fact_production
 * según la clave natural candidata (fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log).
 * Solo realiza logging (WARN) a menos que etl.duplicate.fail-on-detect=true, en cuyo caso aborta el arranque.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EtlStartupDuplicateChecker {

    private final JdbcTemplate jdbcTemplate;

    @Value("${etl.duplicate.check.enabled:true}")
    private boolean duplicateCheckEnabled;
    @Value("${etl.duplicate.fail-on-detect:false}")
    private boolean failOnDetect;
    @Value("${etl.duplicate.check.sample-limit:10}")
    private int sampleLimit;

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM (" +
            "SELECT 1 FROM fact_production " +
            "GROUP BY fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log HAVING COUNT(*) > 1" +
            ") dup";

    private static final String SAMPLE_SQL = "SELECT fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0) AS maquinista_key, numero_log, COUNT(*) AS c " +
            "FROM fact_production GROUP BY 1,2,3,4 HAVING COUNT(*) > 1 ORDER BY c DESC LIMIT ?";

    @PostConstruct
    public void checkDuplicatesOnStartup() {
        if (!duplicateCheckEnabled) {
            log.debug("[ETL Duplicate Check] Disabled by configuration.");
            return;
        }
        try {
            Integer groups = jdbcTemplate.queryForObject(COUNT_SQL, Integer.class);
            if (groups == null) groups = 0;
            if (groups == 0) {
                log.info("[ETL Duplicate Check] No logical duplicate groups detected in fact_production.");
                return;
            }
            log.warn("[ETL Duplicate Check] Detected {} duplicate group(s) in fact_production. Showing up to {} sample(s).", groups, sampleLimit);
            List<String> samples = jdbcTemplate.query(SAMPLE_SQL, ps -> ps.setInt(1, sampleLimit), (rs, i) ->
                    String.format("%s|%d|%d|%d(count=%d)",
                            rs.getDate("fecha_contabilizacion"),
                            rs.getLong("maquina_fk"),
                            rs.getLong("maquinista_key"),
                            rs.getLong("numero_log"),
                            rs.getInt("c"))
            );
            samples.forEach(s -> log.warn("[ETL Duplicate Check] Sample duplicate key => {}", s));
            if (failOnDetect) {
                throw new IllegalStateException("Duplicate logical groups detected in fact_production (" + groups + "). Failing startup as configured.");
            }
        } catch (Exception e) {
            log.error("[ETL Duplicate Check] Unexpected error while checking duplicates: {}", e.getMessage(), e);
            if (failOnDetect) {
                throw new IllegalStateException("Duplicate check failed and fail-on-detect=true", e);
            }
        }
    }
}

