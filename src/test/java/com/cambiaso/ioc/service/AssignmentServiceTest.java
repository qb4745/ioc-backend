package com.cambiaso.ioc.service;

import com.cambiaso.ioc.exception.ResourceNotFoundException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    @BeforeEach
    void setup() {
    }

    @Test
    void assignRoleToUser_creates_when_not_exists() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.ofNullable(new com.cambiaso.ioc.persistence.entity.AppUser()));
        Role role = new Role(); role.setId(2); role.setName("R");
        when(roleRepository.findById(2)).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByIdUserIdAndIdRoleId(1L, 2)).thenReturn(false);

        assignmentService.assignRoleToUser(1L, 2, null);

        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void assignRoleToUser_idempotent_when_exists() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.ofNullable(new com.cambiaso.ioc.persistence.entity.AppUser()));
        when(roleRepository.findById(2)).thenReturn(Optional.of(new Role()));
        when(userRoleRepository.existsByIdUserIdAndIdRoleId(1L, 2)).thenReturn(true);

        assignmentService.assignRoleToUser(1L, 2, null);

        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void assignRoleToUser_userNotFound_throws() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> assignmentService.assignRoleToUser(1L, 2, null));
    }

    @Test
    void removeRoleFromUser_idempotent_when_not_exists() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(new com.cambiaso.ioc.persistence.entity.AppUser()));
        when(roleRepository.findById(2)).thenReturn(Optional.of(new Role()));
        when(userRoleRepository.existsByIdUserIdAndIdRoleId(1L, 2)).thenReturn(false);

        assignmentService.removeRoleFromUser(1L, 2);

        verify(userRoleRepository, never()).deleteByIdUserIdAndIdRoleId(1L, 2);
    }

    @Test
    void assignPermissionToRole_creates_when_not_exists() {
        Role r = new Role(); r.setId(3);
        Permission p = new Permission(); p.setId(5);
        when(roleRepository.findById(3)).thenReturn(Optional.of(r));
        when(permissionRepository.findById(5)).thenReturn(Optional.of(p));
        RolePermissionKey key = new RolePermissionKey(); key.setRoleId(3); key.setPermissionId(5);
        when(rolePermissionRepository.findById(key)).thenReturn(Optional.empty());

        assignmentService.assignPermissionToRole(3, 5);

        verify(rolePermissionRepository).save(any(RolePermission.class));
    }

    @Test
    void removePermissionFromRole_idempotent_when_not_exists() {
        when(roleRepository.findById(3)).thenReturn(Optional.of(new Role()));
        when(permissionRepository.findById(5)).thenReturn(Optional.of(new Permission()));
        RolePermissionKey key = new RolePermissionKey(); key.setRoleId(3); key.setPermissionId(5);
        when(rolePermissionRepository.findById(key)).thenReturn(Optional.empty());

        assignmentService.removePermissionFromRole(3, 5);

        verify(rolePermissionRepository, never()).deleteById(key);
    }
}

