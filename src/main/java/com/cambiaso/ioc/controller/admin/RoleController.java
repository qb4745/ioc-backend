package com.cambiaso.ioc.controller.admin;

import com.cambiaso.ioc.dto.request.RoleRequest;
import com.cambiaso.ioc.dto.response.RoleResponse;
import com.cambiaso.ioc.mapper.RoleMapper;
import com.cambiaso.ioc.persistence.entity.Role;
import com.cambiaso.ioc.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<RoleResponse>> search(
            @RequestParam(value = "search", required = false) String search,
            Pageable pageable) {
        Pageable effective = clamp(pageable);
        Page<Role> page = roleService.search(search, effective);
        List<RoleResponse> content = page.getContent().stream()
                .map(roleMapper::toResponse)
                .toList();
        Page<RoleResponse> mapped = new PageImpl<>(content, effective, page.getTotalElements());
        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoleResponse> getById(@PathVariable Integer id) {
        Role role = roleService.getById(id);
        return ResponseEntity.ok(roleMapper.toResponse(role));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        Role created = roleService.create(request);
        return ResponseEntity.ok(roleMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoleResponse> update(@PathVariable Integer id, @Valid @RequestBody RoleRequest request) {
        Role updated = roleService.update(id, request);
        return ResponseEntity.ok(roleMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Pageable clamp(Pageable pageable) {
        int size = Math.min(100, Math.max(1, pageable.getPageSize()));
        int page = Math.max(0, pageable.getPageNumber());
        return PageRequest.of(page, size, pageable.getSort());
    }
}

