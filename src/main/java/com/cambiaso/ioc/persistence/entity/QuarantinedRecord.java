package com.cambiaso.ioc.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@Table(name = "quarantined_records")
public class QuarantinedRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private EtlJob etlJob;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "raw_line", nullable = false)
    private String rawLine;

    @Column(name = "error_details", nullable = false)
    private String errorDetails;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
