package com.cambiaso.ioc.controller.admin;

import com.cambiaso.ioc.dto.request.UsuarioCreateRequest;
import com.cambiaso.ioc.dto.request.UsuarioUpdateRequest;
import com.cambiaso.ioc.dto.response.UsuarioResponse;
import com.cambiaso.ioc.service.UserAdminService;
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
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserAdminService userAdminService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<UsuarioResponse>> search(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "plantaId", required = false) Integer plantaId,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            Pageable pageable) {
        Pageable effective = clamp(pageable);
        Page<com.cambiaso.ioc.dto.response.UsuarioResponse> page = userAdminService.search(search, plantaId, isActive, effective);
        List<UsuarioResponse> content = page.getContent();
        Page<UsuarioResponse> mapped = new PageImpl<>(content, effective, page.getTotalElements());
        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UsuarioResponse> getById(@PathVariable Long id) {
        UsuarioResponse res = userAdminService.getById(id);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UsuarioResponse> create(@Valid @RequestBody UsuarioCreateRequest req) {
        UsuarioResponse created = userAdminService.create(req);
        URI location = URI.create(String.format("/api/v1/admin/users/%d", created.getId()));
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UsuarioResponse> update(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest req) {
        UsuarioResponse updated = userAdminService.update(id, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Pageable clamp(Pageable pageable) {
        int size = Math.min(100, Math.max(1, pageable.getPageSize()));
        int page = Math.max(0, pageable.getPageNumber());
        return PageRequest.of(page, size, pageable.getSort());
    }
}

