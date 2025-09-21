package com.cambiaso.ioc.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@Table(name = "etl_jobs")
public class EtlJob {

    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_hash", nullable = false, unique = true)
    private String fileHash;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "min_date")
    private java.time.LocalDate minDate;

    @Column(name = "max_date")
    private java.time.LocalDate maxDate;

    @Column(nullable = false)
    private String status;

    @Column
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;
}
