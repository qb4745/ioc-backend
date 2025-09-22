package com.cambiaso.ioc.dto;

import com.cambiaso.ioc.persistence.entity.EtlJob;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class EtlJobStatusDto {
    private UUID jobId;
    private String fileName;
    private String status;
    private String details;
    private LocalDate minDate;
    private LocalDate maxDate;
    private OffsetDateTime createdAt;
    private OffsetDateTime finishedAt;

    public static EtlJobStatusDto fromEntity(EtlJob job) {
        return EtlJobStatusDto.builder()
                .jobId(job.getJobId())
                .fileName(job.getFileName())
                .status(job.getStatus())
                .details(job.getDetails())
                .minDate(job.getMinDate())
                .maxDate(job.getMaxDate())
                .createdAt(job.getCreatedAt())
                .finishedAt(job.getFinishedAt())
                .build();
    }
}
