package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.EtlJob;
import com.cambiaso.ioc.persistence.repository.EtlJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        // Set finished time for completion/failure statuses
        if ("EXITO".equals(status) || "FALLO".equals(status)) {
            job.setFinishedAt(OffsetDateTime.now());
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
    public boolean isWindowLocked(LocalDate minDate, LocalDate maxDate) {
        return etlJobRepository.existsActiveJobInDateRange(minDate, maxDate);
    }
}
