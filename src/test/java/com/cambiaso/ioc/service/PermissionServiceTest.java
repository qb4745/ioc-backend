package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.request.PermissionRequest;
import com.cambiaso.ioc.exception.ResourceConflictException;
import com.cambiaso.ioc.exception.ResourceNotFoundException;
import com.cambiaso.ioc.persistence.entity.Permission;
import com.cambiaso.ioc.persistence.repository.PermissionRepository;
import com.cambiaso.ioc.persistence.repository.RolePermissionRepository;
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
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Permission existing;

    @BeforeEach
    void setup() {
        existing = new Permission();
        existing.setId(1);
        existing.setName("READ_DATA");
        existing.setDescription("Read permission");
    }

    @Test
    void create_success_whenNameUnique() {
        PermissionRequest req = new PermissionRequest();
        req.setName("  NEW_PERM  ");
        req.setDescription("desc");

        when(permissionRepository.existsByNameIgnoreCase("NEW_PERM")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(inv -> {
            Permission p = inv.getArgument(0);
            p.setId(10);
            return p;
        });

        Permission created = permissionService.create(req);
        assertEquals(10, created.getId());
        assertEquals("NEW_PERM", created.getName());
        assertEquals("desc", created.getDescription());
    }

    @Test
    void create_conflict_whenNameExists() {
        PermissionRequest req = new PermissionRequest();
        req.setName("READ_DATA");
        when(permissionRepository.existsByNameIgnoreCase("READ_DATA")).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> permissionService.create(req));
    }

    @Test
    void getById_notFound_throws() {
        when(permissionRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> permissionService.getById(99));
    }

    @Test
    void update_rename_conflict() {
        PermissionRequest req = new PermissionRequest();
        req.setName("ADMIN");
        req.setDescription("x");

        Permission other = new Permission();
        other.setId(2);
        other.setName("ADMIN");

        when(permissionRepository.findById(1)).thenReturn(Optional.of(existing));
        when(permissionRepository.findByNameIgnoreCase("ADMIN")).thenReturn(Optional.of(other));

        assertThrows(ResourceConflictException.class, () -> permissionService.update(1, req));
    }

    @Test
    void delete_inUse_conflict() {
        when(permissionRepository.findById(1)).thenReturn(Optional.of(existing));
        when(rolePermissionRepository.existsByPermission_Id(1)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> permissionService.delete(1));
    }

    @Test
    void delete_ok_whenNotInUse() {
        when(permissionRepository.findById(1)).thenReturn(Optional.of(existing));
        when(rolePermissionRepository.existsByPermission_Id(1)).thenReturn(false);

        permissionService.delete(1);
        verify(permissionRepository).delete(existing);
    }

    @Test
    void search_delegatesToRepository() {
        Page<Permission> page = new PageImpl<>(List.of(existing));
        when(permissionRepository.search(eq("read"), any(PageRequest.class))).thenReturn(page);

        Page<Permission> result = permissionService.search("read", PageRequest.of(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals("READ_DATA", result.getContent().get(0).getName());
    }
}

