package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.request.RoleRequest;
import com.cambiaso.ioc.dto.response.RoleResponse;
import com.cambiaso.ioc.exception.ResourceConflictException;
import com.cambiaso.ioc.exception.ResourceNotFoundException;
import com.cambiaso.ioc.mapper.RoleMapper;
import com.cambiaso.ioc.persistence.entity.Role;
import com.cambiaso.ioc.persistence.repository.RolePermissionRepository;
import com.cambiaso.ioc.persistence.repository.RoleRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleMapper roleMapper;

    @Transactional(readOnly = true)
    public Page<Role> search(String search, Pageable pageable) {
        return roleRepository.search((search == null || search.isBlank()) ? null : search.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Role getById(Integer id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
    }

    @Transactional(readOnly = true)
    public RoleResponse getByIdWithDetails(Integer id) {
        Role role = getById(id);
        return enrichResponse(role);
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> searchWithDetails(String search, Pageable pageable) {
        Page<Role> roles = search(search, pageable);
        return roles.map(this::enrichResponse);
    }

    private RoleResponse enrichResponse(Role role) {
        long usersCount = userRoleRepository.countByIdRoleId(role.getId());
        List<String> permissions = rolePermissionRepository.findPermissionNamesByRoleId(role.getId());
        return roleMapper.toResponse(role, usersCount, permissions);
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
