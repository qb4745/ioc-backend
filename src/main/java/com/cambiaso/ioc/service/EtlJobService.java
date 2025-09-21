package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.EtlJob;
import com.cambiaso.ioc.persistence.repository.EtlJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EtlJobService {

    private final EtlJobRepository etlJobRepository;

    @Transactional
    public EtlJob createJob(String fileName, String fileHash, String userId) {
        EtlJob job = new EtlJob();
        job.setJobId(UUID.randomUUID());
        job.setFileName(fileName);
        job.setFileHash(fileHash);
        job.setUserId(userId);
        job.setStatus("INICIADO");
        EtlJob savedJob = etlJobRepository.save(job);
        etlJobRepository.flush(); // Force Hibernate to execute the insert and populate @CreationTimestamp
        return savedJob;
    }

    @Transactional
    public void updateJobStatus(UUID jobId, String status, String details) {
        EtlJob job = etlJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));
        job.setStatus(status);
        job.setDetails(details);
        if ("EXITO".equals(status) || "FALLO".equals(status)) {
            job.setFinishedAt(OffsetDateTime.now());
        }
        etlJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Optional<EtlJob> findByFileHash(String fileHash) {
        return etlJobRepository.findByFileHash(fileHash);
    }

    @Transactional(readOnly = true)
    public boolean isWindowLocked(LocalDate minDate, LocalDate maxDate) {
        return etlJobRepository.existsByStatusInAndMaxDateGreaterThanEqualAndMinDateLessThanEqual(
                java.util.List.of("INICIADO", "PROCESANDO", "SINCRONIZANDO"),
                minDate,
                maxDate
        );
    }
    
    @Transactional
    public void updateJobDateRange(UUID jobId, LocalDate minDate, LocalDate maxDate) {
        EtlJob job = etlJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));
        job.setMinDate(minDate);
        job.setMaxDate(maxDate);
        etlJobRepository.save(job);
    }
}
