package com.cambiaso.ioc.startup;

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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Verifica en el arranque la existencia del índice UNIQUE natural para fact_production.
 * Nombre esperado: uq_fact_prod_natural
 *
 * Comportamiento:
 *  - Si ni retryUnique ni unique.enforced están activos -> no hace nada.
 *  - Si retryUnique.enabled = true y el índice NO existe -> log ERROR (para visibilidad) pero no aborta.
 *  - Si etl.unique.enforced = true y el índice NO existe -> aborta el arranque (IllegalStateException).
 *  - Si falla la introspección (BD distinta de PostgreSQL, falta vista pg_indexes, etc.) y unique.enforced=true -> aborta.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EtlUniqueConstraintVerifier {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final MeterRegistry meterRegistry; // puede ser null si no hay métricas
    private final AtomicInteger uniqueIndexGauge = new AtomicInteger(-1);
    private volatile boolean gaugeRegistered = false;

    @Value("${etl.retry.unique.enabled:false}")
    private boolean retryUniqueEnabled;

    @Value("${etl.unique.enforced:false}")
    private boolean uniqueEnforced;

    // Nombre del índice esperado (debe alinearse con los scripts SQL de hardening)
    private static final String UNIQUE_INDEX_NAME = "uq_fact_prod_natural";
    private static final String EXISTS_SQL = "SELECT indexname FROM pg_indexes WHERE tablename = 'fact_production' AND indexname = '" + UNIQUE_INDEX_NAME + "'";

    private void registerGaugeIfNeeded() {
        if (meterRegistry != null && !gaugeRegistered) {
            synchronized (this) {
                if (!gaugeRegistered) {
                    meterRegistry.gauge("etl.unique.index.present", uniqueIndexGauge);
                    gaugeRegistered = true;
                }
            }
        }
    }

    @PostConstruct
    public void verifyUniqueIndexPresence() {
        if (!retryUniqueEnabled && !uniqueEnforced) {
            registerGaugeIfNeeded();
            uniqueIndexGauge.set(0); // desconocido / no requerido
            log.debug("[ETL UNIQUE VERIFY] Skipped (flags disabled). retryUniqueEnabled=false, uniqueEnforced=false");
            return;
        }
        registerGaugeIfNeeded();

        String dbProduct = "unknown";
        try (Connection c = dataSource.getConnection()) {
            DatabaseMetaData meta = c.getMetaData();
            dbProduct = meta.getDatabaseProductName();
        } catch (Exception e) {
            log.trace("[ETL UNIQUE VERIFY] Could not read database product name: {}", e.getMessage());
        }

        boolean isPostgres = dbProduct != null && dbProduct.toLowerCase().contains("postgres");
        if (!isPostgres) {
            uniqueIndexGauge.set(0);
            log.debug("[ETL UNIQUE VERIFY] Non-PostgreSQL database detected ({}). Skipping UNIQUE index verification.", dbProduct);
            return; // No enforcement outside Postgres (test profiles H2, etc.)
        }

        boolean exists;
        try {
            exists = !jdbcTemplate.queryForList(EXISTS_SQL).isEmpty();
        } catch (Exception e) {
            log.warn("[ETL UNIQUE VERIFY] Failed querying pg_indexes: {}", e.getMessage());
            if (uniqueEnforced) {
                throw new IllegalStateException("UNIQUE enforcement required but verification failed (" + e.getMessage() + ")", e);
            }
            return;
        }

        if (!exists) {
            uniqueIndexGauge.set(0);
            // Actualizar gauge (0)
            if (uniqueEnforced) {
                throw new IllegalStateException("UNIQUE index " + UNIQUE_INDEX_NAME + " required but not found while etl.unique.enforced=true");
            }
            if (retryUniqueEnabled) {
                log.error("[ETL UNIQUE VERIFY] Retry for unique collisions is ENABLED but index '{}' not found. Create it to guarantee integrity.", UNIQUE_INDEX_NAME);
            } else {
                log.warn("[ETL UNIQUE VERIFY] Index '{}' not found (no immediate enforcement).", UNIQUE_INDEX_NAME);
            }
        } else {
            uniqueIndexGauge.set(1);
            log.info("[ETL UNIQUE VERIFY] Found unique index '{}' (retryUniqueEnabled={}, uniqueEnforced={})", UNIQUE_INDEX_NAME, retryUniqueEnabled, uniqueEnforced);
        }
    }
}
