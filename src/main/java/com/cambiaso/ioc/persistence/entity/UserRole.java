package com.cambiaso.ioc.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "user_roles", indexes = {
        @Index(name = "idx_user_roles_user", columnList = "user_id"),
        @Index(name = "idx_user_roles_role", columnList = "role_id")
})
public class UserRole {

    @EmbeddedId
    private UserRoleKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    private AppUser assignedBy;

    public UserRole() { }

    public UserRole(Long userId, Integer roleId) {
        this.id = new UserRoleKey();
        this.id.setUserId(userId);
        this.id.setRoleId(roleId);
    }
}

