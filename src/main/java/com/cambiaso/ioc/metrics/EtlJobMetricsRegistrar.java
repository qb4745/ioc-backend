package com.cambiaso.ioc.metrics;

import com.cambiaso.ioc.persistence.repository.EtlJobRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Registers Micrometer gauges for tracking active and stuck ETL jobs, plus UNIQUE index presence.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EtlJobMetricsRegistrar {

    private final EtlJobRepository etlJobRepository;
    private final MeterRegistry meterRegistry;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Value("${etl.jobs.stuck.threshold-minutes:30}")
    private long stuckThresholdMinutes;

    private static final List<String> ACTIVE_STATUSES = List.of("INICIADO","PROCESANDO","SINCRONIZANDO");
    private static final String UNIQUE_INDEX_SQL = "SELECT 1 FROM pg_indexes WHERE tablename='fact_production' AND indexname='uq_fact_prod_natural'";

    private volatile Boolean postgres; // cache

    private boolean isPostgres() {
        if (postgres != null) return postgres;
        try (Connection c = dataSource.getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            postgres = md.getDatabaseProductName().toLowerCase().contains("postgres");
        } catch (Exception e) {
            postgres = false;
        }
        return postgres;
    }

    @PostConstruct
    void registerGauges() {
        try {
            // Gauges de jobs existentes
            Gauge.builder("etl.jobs.active", () -> etlJobRepository.countByStatusIn(ACTIVE_STATUSES))
                    .description("Current number of active ETL jobs (INICIADO/PROCESANDO/SINCRONIZANDO)")
                    .register(meterRegistry);

            Gauge.builder("etl.jobs.stuck", () -> etlJobRepository.countStuck(ACTIVE_STATUSES, OffsetDateTime.now().minusMinutes(stuckThresholdMinutes)))
                    .description("Number of potentially stuck ETL jobs older than threshold minutes without finished_at")
                    .register(meterRegistry);

            // Gauge del índice UNIQUE (implementación tolerante a otros motores)
            Gauge.builder("etl.unique.index.present", () -> uniqueIndexPresenceSafe())
                    .description("UNIQUE index presence (1=present, 0=missing, skips non-Postgres)")
                    .register(meterRegistry);

            log.info("Registered gauges: etl.jobs.active, etl.jobs.stuck (threshold={}m), etl.unique.index.present", stuckThresholdMinutes);
        } catch (Exception e) {
            log.warn("Failed to register ETL job gauges: {}", e.getMessage());
        }
    }

    private double uniqueIndexPresenceSafe() {
        if (!isPostgres()) return 0.0; // En H2 / otros: no aplica, devolvemos 0 sin error
        try {
            return jdbcTemplate.queryForList(UNIQUE_INDEX_SQL).isEmpty() ? 0.0 : 1.0;
        } catch (Exception e) {
            // No propagar: métrica debe ser best-effort
            return 0.0;
        }
    }
}
