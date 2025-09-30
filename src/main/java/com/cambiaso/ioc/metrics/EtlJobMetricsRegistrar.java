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

    @Value("${etl.jobs.stuck.threshold-minutes:30}")
    private long stuckThresholdMinutes;

    private static final List<String> ACTIVE_STATUSES = List.of("INICIADO","PROCESANDO","SINCRONIZANDO");
    private static final String UNIQUE_INDEX_SQL = "SELECT 1 FROM pg_indexes WHERE tablename='fact_production' AND indexname='uq_fact_prod_natural'";

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

            // Gauge del índice UNIQUE (implementación directa para evitar dependencia circular)
            Gauge.builder("etl.unique.index.present", this::checkUniqueIndexPresent)
                    .description("UNIQUE index presence (1=present, 0=missing)")
                    .register(meterRegistry);

            log.info("Registered gauges: etl.jobs.active, etl.jobs.stuck (threshold={}m), etl.unique.index.present", stuckThresholdMinutes);
        } catch (Exception e) {
            log.warn("Failed to register ETL job gauges: {}", e.getMessage());
        }
    }

    /**
     * Verifica si el índice UNIQUE está presente
     */
    private double checkUniqueIndexPresent() {
        try {
            return jdbcTemplate.queryForList(UNIQUE_INDEX_SQL).isEmpty() ? 0.0 : 1.0;
        } catch (Exception e) {
            return 0.0; // Considera fallo de query como índice ausente
        }
    }
}
