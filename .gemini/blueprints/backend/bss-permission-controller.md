# Backend Service Specification: PermissionController

## Metadata
- ID: BSS-IOC-004-03
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `PermissionController`
- Tipo: Controller
- Package: `com.cambiaso.ioc.controller.admin`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha Creación: 2025-10-22
- Última Actualización: 2025-10-22

---

## 1. Propósito
CRUD de permisos bajo `/api/v1/admin/permissions`.

---

## 2. Interfaz Pública

```java
@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor
@Tag(name = "Admin Permissions")
public class PermissionController {
  private final PermissionService permissionService;

  @GetMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Page<PermissionResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size,
                                                       @RequestParam(required = false) String search) {
    return ResponseEntity.ok(permissionService.list(page, size, search));
  }

  @PostMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<PermissionResponse> create(@Valid @RequestBody PermissionRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.create(req));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<PermissionResponse> update(@PathVariable int id, @Valid @RequestBody PermissionRequest req) {
    return ResponseEntity.ok(permissionService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable int id) {
    permissionService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
```

---

## 3. Reglas
- Nombre de permiso único
- No borrar si referenciado en `role_permissions`

---

## 4. Tests
- Crear permiso (201), duplicado (409)
- Borrar con referencia (409)

