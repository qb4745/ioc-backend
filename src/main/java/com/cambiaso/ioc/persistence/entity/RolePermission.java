package com.cambiaso.ioc.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "role_permissions")
public class RolePermission {

    @EmbeddedId
    private RolePermissionKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private Permission permission;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}

