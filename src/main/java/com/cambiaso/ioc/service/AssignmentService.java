package com.cambiaso.ioc.service;

import com.cambiaso.ioc.exception.ResourceNotFoundException;
import com.cambiaso.ioc.persistence.entity.AppUser;
import com.cambiaso.ioc.persistence.entity.Permission;
import com.cambiaso.ioc.persistence.entity.Role;
import com.cambiaso.ioc.persistence.entity.RolePermission;
import com.cambiaso.ioc.persistence.entity.RolePermissionKey;
import com.cambiaso.ioc.persistence.entity.UserRole;
import com.cambiaso.ioc.persistence.entity.UserRoleKey;
import com.cambiaso.ioc.persistence.repository.AppUserRepository;
import com.cambiaso.ioc.persistence.repository.PermissionRepository;
import com.cambiaso.ioc.persistence.repository.RolePermissionRepository;
import com.cambiaso.ioc.persistence.repository.RoleRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public void assignRoleToUser(Long userId, Integer roleId, Long assignedByUserId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        boolean exists = userRoleRepository.existsByIdUserIdAndIdRoleId(userId, roleId);
        if (exists) return; // idempotent

        UserRole ur = new UserRole();
        UserRoleKey key = new UserRoleKey();
        key.setUserId(userId);
        key.setRoleId(roleId);
        ur.setId(key);
        ur.setUser(user);
        ur.setRole(role);
        ur.setAssignedAt(OffsetDateTime.now());
        if (assignedByUserId != null) {
            appUserRepository.findById(assignedByUserId).ifPresent(ur::setAssignedBy);
        }

        userRoleRepository.save(ur);
    }

    public void removeRoleFromUser(Long userId, Integer roleId) {
        // validate resources exist
        appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        boolean exists = userRoleRepository.existsByIdUserIdAndIdRoleId(userId, roleId);
        if (!exists) return; // idempotent

        userRoleRepository.deleteByIdUserIdAndIdRoleId(userId, roleId);
    }

    public void assignPermissionToRole(Integer roleId, Integer permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionId));

        RolePermissionKey key = new RolePermissionKey();
        key.setRoleId(roleId);
        key.setPermissionId(permissionId);

        Optional<RolePermission> existing = rolePermissionRepository.findById(key);
        if (existing.isPresent()) return; // idempotent

        RolePermission rp = new RolePermission();
        rp.setId(key);
        rp.setRole(role);
        rp.setPermission(permission);

        rolePermissionRepository.save(rp);
    }

    public void removePermissionFromRole(Integer roleId, Integer permissionId) {
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
        permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionId));

        RolePermissionKey key = new RolePermissionKey();
        key.setRoleId(roleId);
        key.setPermissionId(permissionId);

        boolean exists = rolePermissionRepository.findById(key).isPresent();
        if (!exists) return; // idempotent

        rolePermissionRepository.deleteById(key);
    }
}

