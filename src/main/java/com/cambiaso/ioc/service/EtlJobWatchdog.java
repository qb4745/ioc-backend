package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.repository.EtlJobRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "etl.jobs.watchdog.enabled", havingValue = "true", matchIfMissing = true)
public class EtlJobWatchdog {

    private final EtlJobRepository etlJobRepository;
    private final MeterRegistry meterRegistry;

    @Value("${etl.jobs.stuck.threshold-minutes:30}")
    private long stuckThresholdMinutes;

    // Interval configurable in ms (fixed delay between executions end->start)
    @Value("${etl.jobs.watchdog.interval-ms:60000}")
    private long intervalMs;

    private Counter terminationsCounter() {
        return Counter.builder("etl.jobs.watchdog.terminations")
                .description("Count of ETL jobs force-failed by watchdog due to timeout")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${etl.jobs.watchdog.interval-ms:60000}")
    @Transactional
    public void scheduledRun() {
        runOnceInternal();
    }

    @Transactional
    public int runOnce() { // For tests
        return runOnceInternal();
    }

    private int runOnceInternal() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(stuckThresholdMinutes);
        int updated = etlJobRepository.markStuckAsFailed(cutoff);
        if (updated > 0) {
            terminationsCounter().increment(updated);
            log.warn("Watchdog terminated {} stuck ETL job(s) older than {} (threshold={}m)", updated, cutoff, stuckThresholdMinutes);
        } else {
            log.debug("Watchdog scan complete: no stuck jobs (threshold={}m)", stuckThresholdMinutes);
        }
        return updated;
    }
}

