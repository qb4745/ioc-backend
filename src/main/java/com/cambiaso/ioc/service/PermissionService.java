package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.request.PermissionRequest;
import com.cambiaso.ioc.exception.ResourceConflictException;
import com.cambiaso.ioc.exception.ResourceNotFoundException;
import com.cambiaso.ioc.persistence.entity.Permission;
import com.cambiaso.ioc.persistence.repository.PermissionRepository;
import com.cambiaso.ioc.persistence.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional(readOnly = true)
    public Page<Permission> search(String search, Pageable pageable) {
        return permissionRepository.search((search == null || search.isBlank()) ? null : search.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Permission getById(Integer id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + id));
    }

    public Permission create(PermissionRequest request) {
        String name = request.getName().trim();
        if (permissionRepository.existsByNameIgnoreCase(name)) {
            throw new ResourceConflictException("Permission name already exists: " + name);
        }
        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(request.getDescription());
        return permissionRepository.save(permission);
    }

    public Permission update(Integer id, PermissionRequest request) {
        Permission existing = getById(id);
        String newName = request.getName().trim();
        existing.setDescription(request.getDescription());

        if (!existing.getName().equalsIgnoreCase(newName)) {
            permissionRepository.findByNameIgnoreCase(newName).ifPresent(conflict -> {
                if (!conflict.getId().equals(id)) {
                    throw new ResourceConflictException("Permission name already exists: " + newName);
                }
            });
            existing.setName(newName);
        }
        return permissionRepository.save(existing);
    }

    public void delete(Integer id) {
        Permission permission = getById(id);
        boolean inUse = rolePermissionRepository.existsByPermission_Id(id);
        if (inUse) {
            throw new ResourceConflictException("Permission is in use and cannot be deleted: " + id);
        }
        permissionRepository.delete(permission);
    }
}
