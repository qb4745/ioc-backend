package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.request.RoleRequest;
import com.cambiaso.ioc.exception.ResourceConflictException;
import com.cambiaso.ioc.exception.ResourceNotFoundException;
import com.cambiaso.ioc.persistence.entity.Role;
import com.cambiaso.ioc.persistence.repository.RolePermissionRepository;
import com.cambiaso.ioc.persistence.repository.RoleRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional(readOnly = true)
    public Page<Role> search(String search, Pageable pageable) {
        return roleRepository.search((search == null || search.isBlank()) ? null : search.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Role getById(Integer id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
    }

    public Role create(RoleRequest request) {
        String name = request.getName().trim();
        if (roleRepository.existsByNameIgnoreCase(name)) {
            throw new ResourceConflictException("Role name already exists: " + name);
        }
        Role role = new Role();
        role.setName(name);
        role.setDescription(request.getDescription());
        return roleRepository.save(role);
    }

    public Role update(Integer id, RoleRequest request) {
        Role existing = getById(id);
        String newName = request.getName().trim();
        existing.setDescription(request.getDescription());

        if (!existing.getName().equalsIgnoreCase(newName)) {
            roleRepository.findByNameIgnoreCase(newName).ifPresent(conflict -> {
                if (!conflict.getId().equals(id)) {
                    throw new ResourceConflictException("Role name already exists: " + newName);
                }
            });
            existing.setName(newName);
        }
        return roleRepository.save(existing);
    }

    public void delete(Integer id) {
        Role role = getById(id);
        boolean inUse = userRoleRepository.existsByIdRoleId(id) || rolePermissionRepository.existsByRole_Id(id);
        if (inUse) {
            throw new ResourceConflictException("Role is in use and cannot be deleted: " + id);
        }
        roleRepository.delete(role);
    }
}

