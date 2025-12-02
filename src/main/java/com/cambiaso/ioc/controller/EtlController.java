package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.dto.EtlJobStatusDto;
import com.cambiaso.ioc.exception.JobConflictException;
import com.cambiaso.ioc.persistence.entity.EtlJob;
import com.cambiaso.ioc.service.EtlJobService;
import com.cambiaso.ioc.service.EtlProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/etl")
@RequiredArgsConstructor
public class EtlController {

    private final EtlProcessingService etlProcessingService;
    private final EtlJobService etlJobService;

    // Maximum file size: 50MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    @PostMapping("/start-process")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> startEtlProcess(
            @RequestParam("file") @NotNull MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {

        // Enhanced file validation (includes null/empty checks)
        validateFile(file);

        // Ensure non-null filename for downstream usage
        final String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "File name cannot be null");

        log.info("Starting ETL process for file: {} by user: {}",
                originalFilename, jwt.getSubject());

        String userId = jwt.getSubject();
        String fileHash = etlProcessingService.calculateFileHash(file);

        // Idempotency check
        Optional<EtlJob> existingJob = etlJobService.findByFileHash(fileHash);
        if (existingJob.isPresent()) {
            throw new JobConflictException("This file has already been processed. Job ID: " + existingJob.get().getJobId());
        }

        EtlJob newJob = etlJobService.createJob(originalFilename, fileHash, userId);

        // Start async processing
        etlProcessingService.processFile(file, userId, newJob.getJobId());

        log.info("ETL job created with ID: {} for file: {}", newJob.getJobId(), originalFilename);
        return ResponseEntity.accepted().body(Map.of(
                "jobId", newJob.getJobId(),
                "fileName", originalFilename,
                "status", "INICIADO"
        ));
    }

    @GetMapping("/jobs/{jobId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EtlJobStatusDto> getJobStatus(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal Jwt jwt) {

        log.debug("Fetching job status for ID: {} by user: {}", jobId, jwt.getSubject());

        return etlJobService.findById(jobId)
                .map(EtlJobStatusDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of 50MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".txt")) {
            throw new IllegalArgumentException("Only TXT files are allowed");
        }

        // For .txt files, "text/plain" is the most common and sufficient content type to check.
        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals("text/plain")) {
            log.warn("File '{}' has an unusual content type: {}. Allowing it, but expected 'text/plain'.", filename, contentType);
        }

        log.debug("File validation passed for: {} (size: {} bytes)", filename, file.getSize());
    }
}
