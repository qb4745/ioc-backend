package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.request.RoleRequest;
import com.cambiaso.ioc.exception.ResourceConflictException;
import com.cambiaso.ioc.exception.ResourceNotFoundException;
import com.cambiaso.ioc.persistence.entity.Role;
import com.cambiaso.ioc.persistence.repository.RolePermissionRepository;
import com.cambiaso.ioc.persistence.repository.RoleRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private RoleService roleService;

    private Role existing;

    @BeforeEach
    void setup() {
        existing = new Role();
        existing.setId(1);
        existing.setName("OPERATOR");
        existing.setDescription("Operator role");
    }

    @Test
    void create_success_whenNameUnique() {
        RoleRequest req = new RoleRequest();
        req.setName("  ADMIN  ");
        req.setDescription("desc");

        when(roleRepository.existsByNameIgnoreCase("ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> {
            Role r = inv.getArgument(0);
            r.setId(7);
            return r;
        });

        Role created = roleService.create(req);
        assertEquals(7, created.getId());
        assertEquals("ADMIN", created.getName());
        assertEquals("desc", created.getDescription());
    }

    @Test
    void create_conflict_whenNameExists() {
        RoleRequest req = new RoleRequest();
        req.setName("OPERATOR");
        when(roleRepository.existsByNameIgnoreCase("OPERATOR")).thenReturn(true);
        assertThrows(ResourceConflictException.class, () -> roleService.create(req));
    }

    @Test
    void getById_notFound_throws() {
        when(roleRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roleService.getById(99));
    }

    @Test
    void update_rename_conflict() {
        RoleRequest req = new RoleRequest();
        req.setName("ADMIN");
        req.setDescription("x");

        Role other = new Role();
        other.setId(2);
        other.setName("ADMIN");

        when(roleRepository.findById(1)).thenReturn(Optional.of(existing));
        when(roleRepository.findByNameIgnoreCase("ADMIN")).thenReturn(Optional.of(other));

        assertThrows(ResourceConflictException.class, () -> roleService.update(1, req));
    }

    @Test
    void delete_inUse_conflict_whenAssignedToUser() {
        when(roleRepository.findById(1)).thenReturn(Optional.of(existing));
        when(userRoleRepository.existsByIdRoleId(1)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> roleService.delete(1));
    }

    @Test
    void delete_inUse_conflict_whenHasPermissions() {
        when(roleRepository.findById(1)).thenReturn(Optional.of(existing));
        when(userRoleRepository.existsByIdRoleId(1)).thenReturn(false);
        when(rolePermissionRepository.existsByRole_Id(1)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> roleService.delete(1));
    }

    @Test
    void delete_ok_whenNotInUse() {
        when(roleRepository.findById(1)).thenReturn(Optional.of(existing));
        when(userRoleRepository.existsByIdRoleId(1)).thenReturn(false);
        when(rolePermissionRepository.existsByRole_Id(1)).thenReturn(false);

        roleService.delete(1);
        verify(roleRepository).delete(existing);
    }

    @Test
    void search_delegatesToRepository() {
        Page<Role> page = new PageImpl<>(List.of(existing));
        when(roleRepository.search(eq("oper"), any(PageRequest.class))).thenReturn(page);

        Page<Role> result = roleService.search("oper", PageRequest.of(0, 5));
        assertEquals(1, result.getContent().size());
        assertEquals("OPERATOR", result.getContent().get(0).getName());
    }
}

