package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.EtlJob;
import com.cambiaso.ioc.persistence.repository.EtlJobRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EtlJobService {

    private final EtlJobRepository etlJobRepository;
    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    public EtlJob createJob(String fileName, String fileHash, String userId) {
        EtlJob job = new EtlJob();
        job.setJobId(UUID.randomUUID());
        job.setFileName(fileName);
        job.setFileHash(fileHash);
        job.setUserId(userId);
        job.setStatus("INICIADO");
        job.setCreatedAt(OffsetDateTime.now());

        return etlJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Optional<EtlJob> findByFileHash(String fileHash) {
        return etlJobRepository.findByFileHash(fileHash);
    }

    @Transactional(readOnly = true)
    public Optional<EtlJob> findById(UUID jobId) {
        return etlJobRepository.findById(jobId);
    }

    public void updateJobStatus(UUID jobId, String status, String details) {
        EtlJob job = etlJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));

        job.setStatus(status);
        job.setDetails(details);

        boolean terminal = "EXITO".equals(status) || "FALLO".equals(status);
        if (terminal) {
            job.setFinishedAt(OffsetDateTime.now());
            if (meterRegistry != null && job.getCreatedAt() != null) {
                try {
                    long millis = java.time.Duration.between(job.getCreatedAt(), job.getFinishedAt()).toMillis();
                    Timer.builder("etl.job.total.duration")
                            .tag("status", status)
                            .publishPercentileHistogram()
                            .register(meterRegistry)
                            .record(java.time.Duration.ofMillis(Math.max(millis,1)));
                } catch (Exception ignore) { }
            }
        }

        etlJobRepository.save(job);
        log.info("Updated job {} status to: {}", jobId, status);
    }

    public void updateJobDateRange(UUID jobId, LocalDate minDate, LocalDate maxDate) {
        EtlJob job = etlJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));

        job.setMinDate(minDate);
        job.setMaxDate(maxDate);

        etlJobRepository.save(job);
        log.debug("Updated job {} date range: {} to {}", jobId, minDate, maxDate);
    }

    @Transactional(readOnly = true)
    public boolean isWindowLocked(UUID jobId, LocalDate minDate, LocalDate maxDate) {
        return etlJobRepository.existsActiveJobInDateRange(jobId, minDate, maxDate);
    }
}
