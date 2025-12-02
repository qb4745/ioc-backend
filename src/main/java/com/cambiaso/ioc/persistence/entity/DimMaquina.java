package com.cambiaso.ioc.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "dim_maquina")
public class DimMaquina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_maquina", nullable = false, unique = true)
    private String codigoMaquina;

    @Column(name = "nombre_maquina")
    private String nombreMaquina;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
