package com.cambiaso.ioc.controller.admin;

import com.cambiaso.ioc.dto.request.PermissionRequest;
import com.cambiaso.ioc.dto.response.PermissionResponse;
import com.cambiaso.ioc.mapper.PermissionMapper;
import com.cambiaso.ioc.persistence.entity.Permission;
import com.cambiaso.ioc.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<PermissionResponse>> search(
            @RequestParam(value = "search", required = false) String search,
            Pageable pageable) {
        Pageable effective = clamp(pageable);
        Page<Permission> page = permissionService.search(search, effective);
        List<PermissionResponse> content = page.getContent().stream()
                .map(permissionMapper::toResponse)
                .toList();
        Page<PermissionResponse> mapped = new PageImpl<>(content, effective, page.getTotalElements());
        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PermissionResponse> getById(@PathVariable Integer id) {
        Permission permission = permissionService.getById(id);
        return ResponseEntity.ok(permissionMapper.toResponse(permission));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        Permission created = permissionService.create(request);
        PermissionResponse response = permissionMapper.toResponse(created);
        URI location = URI.create(String.format("/api/v1/admin/permissions/%d", response.getId()));
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PermissionResponse> update(@PathVariable Integer id, @Valid @RequestBody PermissionRequest request) {
        Permission updated = permissionService.update(id, request);
        return ResponseEntity.ok(permissionMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Pageable clamp(Pageable pageable) {
        int size = Math.min(100, Math.max(1, pageable.getPageSize()));
        int page = Math.max(0, pageable.getPageNumber());
        return PageRequest.of(page, size, pageable.getSort());
    }
}

