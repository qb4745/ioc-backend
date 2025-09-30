package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.FactProduction;
import com.cambiaso.ioc.persistence.repository.FactProductionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.DistributionSummary;
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
    @Value("${etl.sync.test.sleep-ms:0}")
    private long syncTestSleepMs; // SOLO tests: pausa tras delete antes de inserts

    // Métricas (lazily inicializadas)
    private Counter rowsDeletedCounter() { return meterRegistry.counter("etl.rows.deleted"); }
    private Counter rowsInsertedCounter() { return meterRegistry.counter("etl.rows.inserted"); }
    private Timer syncDurationTimer() { return meterRegistry.timer("etl.sync.duration"); }
    private Counter syncAttemptCounter() { return meterRegistry.counter("etl.sync.attempts"); }
    private Counter syncCollisionCounter() { return meterRegistry.counter("etl.sync.collisions"); }
    private DistributionSummary windowDaysSummary() { return DistributionSummary.builder("etl.sync.window.days").publishPercentileHistogram().register(meterRegistry); }
    private DistributionSummary batchSizeSummary() { return DistributionSummary.builder("etl.sync.records.per.batch").publishPercentileHistogram().register(meterRegistry); }

    public void syncWithDeleteInsert(LocalDate minDate, LocalDate maxDate, @NonNull List<FactProduction> records) {
        if (!retryUniqueEnabled) {
            try {
                syncAttemptCounter().increment();
                executeOnce(minDate, maxDate, records);
            } catch (DataIntegrityViolationException dive) {
                throw new DataSyncException(buildErr(minDate, maxDate, "data integrity violation"), dive);
            } catch (RuntimeException e) {
                if (isUniqueConstraintViolation(e)) {
                    log.warn("Unique constraint violation (no-retry mode) for range {} to {}: {}", minDate, maxDate, e.getMessage());
                }
                throw new DataSyncException(buildErr(minDate, maxDate, "unexpected failure"), e);
            }
            return;
        }
        int attempt = 0;
        while (true) {
            attempt++;
            syncAttemptCounter().increment();
            try {
                executeOnce(minDate, maxDate, records);
                if (attempt > 1) {
                    log.info("ETL sync succeeded after {} attempt(s) (unique collision retry mode)", attempt);
                }
                return;
            } catch (DataIntegrityViolationException dive) {
                if (!handleOrRetry(minDate, maxDate, attempt, dive, records)) {
                    throw new DataSyncException(buildErr(minDate, maxDate, "data integrity violation (final)"), dive);
                }
            } catch (RuntimeException e) {
                boolean unique = isUniqueConstraintViolation(e);
                if (unique) {
                    if (!handleOrRetry(minDate, maxDate, attempt, e, records)) {
                        throw new DataSyncException(buildErr(minDate, maxDate, "data integrity violation (final)"), e);
                    }
                } else {
                    log.error("Non-unique runtime exception during sync (attempt {}): type={}, message={}, causes={}", attempt, e.getClass().getName(), e.getMessage(), summarizeCauses(e));
                    throw new DataSyncException(buildErr(minDate, maxDate, "unexpected failure (no retry)"), e);
                }
            }
        }
    }

    private String summarizeCauses(Throwable t) {
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        while (t != null && depth < 8) { // limit depth
            sb.append('[').append(depth).append(':').append(t.getClass().getSimpleName()).append(':');
            String m = t.getMessage();
            if (m != null) sb.append(m, 0, Math.min(m.length(), 120));
            sb.append(']');
            t = t.getCause();
            depth++;
        }
        return sb.toString();
    }

    private boolean handleOrRetry(LocalDate minDate, LocalDate maxDate, int attempt, Throwable ex, List<FactProduction> records) {
        boolean unique = isUniqueConstraintViolation(ex);
        if (unique) {
            syncCollisionCounter().increment();
        }
        if (!unique || attempt >= retryMaxAttempts) {
            return false; // No se reintenta
        }
        // Reset IDs to ensure fresh INSERTs next attempt (prevent stale entity state after rollback)
        for (FactProduction fp : records) {
            try {
                if (fp.getId() != null) fp.setId(null);
            } catch (Exception ignore) { }
        }
        long backoffMs = 200L * attempt;
        log.warn("Unique constraint collision (attempt {} of {}) for range {} to {}. Retrying after {} ms... (ids reset)", attempt, retryMaxAttempts, minDate, maxDate, backoffMs);
        sleepQuiet(backoffMs);
        return true;
    }

    private void executeOnce(LocalDate minDate, LocalDate maxDate, List<FactProduction> records) {
        long start = System.nanoTime();
        try {
            transactionTemplate.execute(status -> {
                log.info("Starting data sync for date range {} to {} with {} records (lockEnabled={}, retryUnique={})", minDate, maxDate, records.size(), etlLockEnabled, retryUniqueEnabled);
                // Diagnostic: log injection of test sleep values
                log.debug("Diagnostic: lockTestSleepMs={} syncTestSleepMs={} (for tests)", lockTestSleepMs, syncTestSleepMs);

                if (etlLockEnabled) {
                    tryAcquireAdvisoryLock(minDate, maxDate);
                    if (lockTestSleepMs > 0) {
                        try { Thread.sleep(lockTestSleepMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    }
                }
                int deleted = factProductionRepository.deleteByFechaContabilizacionBetween(minDate, maxDate);
                rowsDeletedCounter().increment(deleted);
                if (syncTestSleepMs > 0) { // Pausa de test para inducir colisiones concurrentes
                    try { Thread.sleep(syncTestSleepMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
                log.debug("Deleted {} existing rows in date range {} to {}", deleted, minDate, maxDate);
                if (!records.isEmpty()) {
                    factProductionRepository.saveAll(records);
                    factProductionRepository.flush();
                    rowsInsertedCounter().increment(records.size());
                    try {
                        long days = java.time.Duration.between(minDate.atStartOfDay(), maxDate.plusDays(1).atStartOfDay()).toDays();
                        if (days < 1) days = 1;
                        windowDaysSummary().record(days);
                        batchSizeSummary().record(records.size());
                    } catch (Exception ignore) { }
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
        long lockKey = computeLockKey(minDate, maxDate);
        try {
            log.debug("Attempting to acquire advisory lock (key={}) for range {} to {}", lockKey, minDate, maxDate);
            Object res = entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(?);")
                    .setParameter(1, lockKey)
                    .getSingleResult();
            log.debug("Acquired advisory lock key={} for range {} to {} (result={})", lockKey, minDate, maxDate, res);
        } catch (RuntimeException ex) {
            // H2 u otras BDs no soportan la función; continuar sin lock
            log.warn("Advisory lock skipped/failed for key={} (range {} to {}) - exception: {}: {}", lockKey, minDate, maxDate, ex.getClass().getName(), ex.getMessage());
            log.trace("Advisory lock exception stack: ", ex);
        }
    }

    private long computeLockKey(LocalDate minDate, LocalDate maxDate) {
        // Combina epochDay para generar llave determinística en 64 bits.
        long a = minDate != null ? minDate.toEpochDay() : 0L;
        long b = maxDate != null ? maxDate.toEpochDay() : 0L;
        return (a << 32) ^ b;
    }

    private boolean isUniqueConstraintViolation(Throwable e) {
        // Diagnostic: log top-level exception class and message
        if (e == null) return false;
        log.debug("Checking unique constraint violation for exception type: {} message: {}", e.getClass().getName(), e.getMessage());
        Throwable cur = e;
        while (cur != null) {
            String msg = cur.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("uq_fact_prod_natural") || lower.contains("duplicate key") || lower.contains("unique constraint") || lower.contains("violates unique constraint")) {
                    log.debug("Detected unique violation by message on class {}", cur.getClass().getName());
                    return true;
                }
            }
            try {
                if (cur.getClass().getName().equals("org.postgresql.util.PSQLException")) {
                    String sqlState = (String) cur.getClass().getMethod("getSQLState").invoke(cur);
                    log.debug("Postgres PSQLException SQLState={}", sqlState);
                    if ("23505".equals(sqlState)) return true;
                }
            } catch (Exception inv) {
                log.trace("Could not read SQLState from exception {}: {}", cur.getClass().getName(), inv.getMessage());
            }
            if (cur instanceof java.sql.BatchUpdateException bue) {
                try {
                    String st = bue.getSQLState();
                    log.debug("BatchUpdateException SQLState={}", st);
                    if ("23505".equals(st)) return true;
                    if (bue.getNextException() != null) {
                        String nextSt = bue.getNextException().getSQLState();
                        log.debug("BatchUpdateException nextException SQLState={}", nextSt);
                        if ("23505".equals(nextSt)) return true;
                    }
                } catch (Exception ignore) { }
            }
            if (cur.getClass().getName().equals("org.hibernate.exception.ConstraintViolationException")) {
                try {
                    Object sqlEx = cur.getClass().getMethod("getSQLException").invoke(cur);
                    if (sqlEx instanceof java.sql.SQLException sql) {
                        String s = sql.getSQLState();
                        log.debug("Hibernate ConstraintViolationException SQLState={}", s);
                        if ("23505".equals(s)) return true;
                    }
                } catch (Exception ignore) { }
            }
            cur = cur.getCause();
        }
        // Fallback: scan stack trace text for SQLState 23505
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            String stack = sw.toString();
            if (stack.contains("23505")) {
                log.debug("Detected SQLState 23505 in stack trace");
                return true;
            }
        } catch (Exception ignore) { }
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
