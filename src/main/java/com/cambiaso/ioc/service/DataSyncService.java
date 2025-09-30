package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.FactProduction;
import com.cambiaso.ioc.persistence.repository.FactProductionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {

    private final FactProductionRepository factProductionRepository;
    private final EntityManager entityManager; // Para advisory lock
    private final MeterRegistry meterRegistry;
    private final TransactionTemplate transactionTemplate; // Ejecuta bloques transaccionales explícitos

    @Value("${etl.lock.enabled:true}")
    private boolean etlLockEnabled;
    @Value("${etl.retry.unique.enabled:false}")
    private boolean retryUniqueEnabled;
    @Value("${etl.retry.unique.max-attempts:3}")
    private int retryMaxAttempts;
    @Value("${etl.lock.test.sleep-ms:0}")
    private long lockTestSleepMs; // SOLO para pruebas: delay artificial tras tomar el lock

    // Métricas (lazily inicializadas)
    private Counter rowsDeletedCounter() { return meterRegistry.counter("etl.rows.deleted"); }
    private Counter rowsInsertedCounter() { return meterRegistry.counter("etl.rows.inserted"); }
    private Timer syncDurationTimer() { return meterRegistry.timer("etl.sync.duration"); }

    public void syncWithDeleteInsert(LocalDate minDate, LocalDate maxDate, @NonNull List<FactProduction> records) {
        if (!retryUniqueEnabled) {
            try {
                executeOnce(minDate, maxDate, records);
            } catch (DataIntegrityViolationException dive) {
                throw new DataSyncException(buildErr(minDate, maxDate, "data integrity violation"), dive);
            } catch (RuntimeException e) {
                throw new DataSyncException(buildErr(minDate, maxDate, "unexpected failure"), e);
            }
            return;
        }
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                executeOnce(minDate, maxDate, records);
                if (attempt > 1) {
                    log.info("ETL sync succeeded after {} attempt(s) (unique collision retry mode)", attempt);
                }
                return;
            } catch (DataIntegrityViolationException dive) {
                boolean unique = isUniqueConstraintViolation(dive);
                if (!unique || attempt >= retryMaxAttempts) {
                    throw new DataSyncException(buildErr(minDate, maxDate, "data integrity violation (final)"), dive);
                }
                long backoffMs = 200L * attempt;
                log.warn("Unique constraint collision (attempt {} of {}). Retrying after {} ms...", attempt, retryMaxAttempts, backoffMs);
                sleepQuiet(backoffMs);
            } catch (RuntimeException e) {
                throw new DataSyncException(buildErr(minDate, maxDate, "unexpected failure (no retry)"), e);
            }
        }
    }

    private void executeOnce(LocalDate minDate, LocalDate maxDate, List<FactProduction> records) {
        long start = System.nanoTime();
        try {
            transactionTemplate.execute(status -> {
                log.info("Starting data sync for date range {} to {} with {} records (lockEnabled={}, retryUnique={})", minDate, maxDate, records.size(), etlLockEnabled, retryUniqueEnabled);

                if (etlLockEnabled) {
                    tryAcquireAdvisoryLock(minDate, maxDate);
                    if (lockTestSleepMs > 0) {
                        try { Thread.sleep(lockTestSleepMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    }
                }
                int deleted = factProductionRepository.deleteByFechaContabilizacionBetween(minDate, maxDate);
                rowsDeletedCounter().increment(deleted);
                log.debug("Deleted {} existing rows in date range {} to {}", deleted, minDate, maxDate);
                if (!records.isEmpty()) {
                    factProductionRepository.saveAll(records);
                    factProductionRepository.flush();
                    rowsInsertedCounter().increment(records.size());
                    log.info("Successfully synced {} records for date range {} to {}", records.size(), minDate, maxDate);
                } else {
                    log.info("No records to sync for date range {} to {}", minDate, maxDate);
                }
                return null;
            });
        } finally {
            syncDurationTimer().record(Duration.ofNanos(System.nanoTime() - start));
        }
    }

    private void tryAcquireAdvisoryLock(LocalDate minDate, LocalDate maxDate) {
        try {
            long lockKey = computeLockKey(minDate, maxDate);
            entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(?);")
                    .setParameter(1, lockKey)
                    .getSingleResult();
            log.debug("Acquired advisory lock key={} for range {} to {}", lockKey, minDate, maxDate);
        } catch (RuntimeException ex) {
            // H2 u otras BDs no soportan la función; continuar sin lock
            log.trace("Advisory lock skipped (function unavailable): {}", ex.getMessage());
        }
    }

    private long computeLockKey(LocalDate minDate, LocalDate maxDate) {
        // Combina epochDay para generar llave determinística en 64 bits.
        long a = minDate != null ? minDate.toEpochDay() : 0L;
        long b = maxDate != null ? maxDate.toEpochDay() : 0L;
        return (a << 32) ^ b;
    }

    private boolean isUniqueConstraintViolation(DataIntegrityViolationException e) {
        Throwable cur = e;
        while (cur != null) {
            String msg = cur.getMessage();
            if (msg != null && (msg.contains("uq_fact_prod_natural") || msg.toLowerCase().contains("unique"))) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private void sleepQuiet(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    private String buildErr(LocalDate minDate, LocalDate maxDate, String reason) {
        return "Failed to sync date range " + minDate + " to " + maxDate + " (" + reason + ")";
    }

    /**
     * Custom exception for data synchronization failures
     */
    public static class DataSyncException extends RuntimeException {
        public DataSyncException(String message, Throwable cause) { super(message, cause); }
    }
}
