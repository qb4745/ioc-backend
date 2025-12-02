package com.cambiaso.ioc.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "dim_maquinista")
public class DimMaquinista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_maquinista", nullable = false, unique = true)
    private Long codigoMaquinista;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
