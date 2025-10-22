# Backend Service Specification: AdminUserController

## Metadata
- ID: BSS-IOC-004-01
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `AdminUserController`
- Tipo: Controller
- Package: `com.cambiaso.ioc.controller.admin`
- Sprint: Sprint 3
- Estado:
  - [x] Draft
  - [ ] En Revisión
  - [ ] Aprobado
  - [ ] Implementado
- Autor: IA
- Fecha Creación: 2025-10-22
- Última Actualización: 2025-10-22

---

## 1. Propósito y Responsabilidad

### 1.1. Descripción
Expose CRUD de usuarios de aplicación y endpoints de asignación de roles bajo `/api/v1/admin/users`. Endpoints protegidos para administración.

Responsabilidad Principal:
- Crear, leer, actualizar, eliminar (soft) usuarios
- Asignar y remover roles a usuarios
- Paginación y filtros básicos

NO es responsable de:
- Lógica de negocio compleja de asignaciones (Service)
- Acceso a datos (Repositories)

### 1.2. Ubicación en la Arquitectura

Capa: Presentation (REST Controller)

```
[Frontend] -> [AdminUserController] -> [UserAdminService, AssignmentService] -> [Repositories] -> [DB]
```

---

## 2. Interfaz Pública (Endpoints)

```java
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users")
public class AdminUserController {

  private final UserAdminService userAdminService;
  private final AssignmentService assignmentService;

  @PostMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<UsuarioResponse> create(@Valid @RequestBody UsuarioCreateRequest req) {
    var created = userAdminService.create(req);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Page<UsuarioResponse>> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) Integer plantaId,
      @RequestParam(required = false) Boolean isActive
  ) {
    var result = userAdminService.list(page, size, search, plantaId, isActive);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<UsuarioResponse> get(@PathVariable long id) {
    return ResponseEntity.ok(userAdminService.getById(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<UsuarioResponse> update(@PathVariable long id, @Valid @RequestBody UsuarioUpdateRequest req) {
    return ResponseEntity.ok(userAdminService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable long id) {
    userAdminService.softDelete(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/roles/{roleId}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<RolesResponse> assignRole(@PathVariable long id, @PathVariable int roleId) {
    var roles = assignmentService.assignRoleToUser(id, roleId);
    return ResponseEntity.ok(new RolesResponse(roles));
  }

  @DeleteMapping("/{id}/roles/{roleId}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> removeRole(@PathVariable long id, @PathVariable int roleId) {
    assignmentService.removeRoleFromUser(id, roleId);
    return ResponseEntity.noContent().build();
  }
}
```

---

## 3. Dependencias
- UserAdminService
- AssignmentService
- DTOs: UsuarioCreateRequest, UsuarioUpdateRequest, UsuarioResponse, RolesResponse

---

## 4. Seguridad y Errores
- Seguridad: `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` en todos los endpoints
- Errores comunes: 400 (validación), 404 (no encontrado), 409 (conflicto), 401/403 (authz)
- Manejo global: `@ControllerAdvice` centraliza excepciones (Validation, Conflict, NotFound)

---

## 5. Testing (MockMvc)
- Crear usuario válido → 201 con body
- Listado paginado (filtro search/planta/activo) → 200
- Obtener por id existente → 200; inexistente → 404
- Update con cambios válidos → 200
- Soft delete → 204
- Asignar rol existente → 200; rol inexistente → 404
- Remover rol asignado → 204 (idempotente si no asignado)

