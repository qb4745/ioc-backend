package com.cambiaso.ioc.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class RolePermissionKey implements Serializable {

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "permission_id")
    private Integer permissionId;
}

