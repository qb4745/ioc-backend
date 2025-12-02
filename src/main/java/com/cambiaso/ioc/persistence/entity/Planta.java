package com.cambiaso.ioc.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "plantas", indexes = {
        @Index(name = "idx_plantas_code", columnList = "code", unique = true)
})
public class Planta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "text")
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

