package com.cambiaso.ioc.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "app_users", indexes = {
        @Index(name = "idx_app_users_active", columnList = "is_active"),
        @Index(name = "idx_app_users_supabase_uid", columnList = "supabase_user_id"),
        @Index(name = "idx_app_users_planta", columnList = "planta_id"),
        @Index(name = "idx_app_users_nombre", columnList = "primer_nombre, primer_apellido")
})
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supabase_user_id", nullable = false, unique = true)
    private UUID supabaseUserId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "primer_nombre", nullable = false)
    private String primerNombre;

    @Column(name = "segundo_nombre")
    private String segundoNombre;

    @Column(name = "primer_apellido", nullable = false)
    private String primerApellido;

    @Column(name = "segundo_apellido")
    private String segundoApellido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planta_id")
    private Planta planta;

    @Column(name = "centro_costo", length = 50)
    private String centroCosto;

    @Column(name = "fecha_contrato")
    private LocalDate fechaContrato;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}

