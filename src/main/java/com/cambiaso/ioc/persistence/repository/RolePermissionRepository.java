package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.RolePermission;
import com.cambiaso.ioc.persistence.entity.RolePermissionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionKey> {
    boolean existsByRole_Id(Integer roleId);
    boolean existsByPermission_Id(Integer permissionId);

    @Query("SELECT p.name FROM RolePermission rp JOIN rp.permission p WHERE rp.role.id = :roleId ORDER BY p.name")
    List<String> findPermissionNamesByRoleId(@Param("roleId") Integer roleId);
}
