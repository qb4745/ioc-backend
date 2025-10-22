# Backend Service Specification: RoleController

## Metadata
- ID: BSS-IOC-004-02
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `RoleController`
- Tipo: Controller
- Package: `com.cambiaso.ioc.controller.admin`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha Creación: 2025-10-22
- Última Actualización: 2025-10-22

---

## 1. Propósito
Expose CRUD de roles bajo `/api/v1/admin/roles`.

---

## 2. Interfaz Pública

```java
@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Admin Roles")
public class RoleController {
  private final RoleService roleService;

  @GetMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Page<RoleResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String search) {
    return ResponseEntity.ok(roleService.list(page, size, search));
  }

  @PostMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(req));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<RoleResponse> update(@PathVariable int id, @Valid @RequestBody RoleRequest req) {
    return ResponseEntity.ok(roleService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable int id) {
    roleService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
```

---

## 3. Reglas
- Nombre de rol único (case-insensitive)
- No borrar si está referenciado en `user_roles` o `role_permissions`

---

## 4. Tests
- Crear rol único (201), duplicado (409)
- Actualizar descripción (200)
- Borrar con referencias (409)

