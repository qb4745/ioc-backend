package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.RolePermission;
import com.cambiaso.ioc.persistence.entity.RolePermissionKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionKey> {
    boolean existsByRole_Id(Integer roleId);
    boolean existsByPermission_Id(Integer permissionId);
}

