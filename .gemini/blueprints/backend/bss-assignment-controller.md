# Backend Service Specification: AssignmentController

## Metadata
- ID: BSS-IOC-004-04
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `AssignmentController`
- Tipo: Controller
- Package: `com.cambiaso.ioc.controller.admin`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha Creación: 2025-10-22
- Última Actualización: 2025-10-22

---

## 1. Propósito
Gestionar asignaciones usuario-rol con endpoints dedicados bajo `/api/v1/admin/assignments`. Separa responsabilidades del `AdminUserController`.

---

## 2. Interfaz Pública

```java
@RestController
@RequestMapping("/api/v1/admin/assignments")
@RequiredArgsConstructor
@Tag(name = "Role Assignments")
public class AssignmentController {
  private final AssignmentService assignmentService;

  @PostMapping("/users/{userId}/roles/{roleId}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<RolesResponse> assign(@PathVariable long userId, @PathVariable int roleId) {
    return ResponseEntity.ok(new RolesResponse(assignmentService.assignRoleToUser(userId, roleId)));
  }

  @DeleteMapping("/users/{userId}/roles/{roleId}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> unassign(@PathVariable long userId, @PathVariable int roleId) {
    assignmentService.removeRoleFromUser(userId, roleId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/users/{userId}/roles")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<RolesResponse> list(@PathVariable long userId) {
    return ResponseEntity.ok(new RolesResponse(assignmentService.listRolesForUser(userId)));
  }
}
```

---

## 3. Tests
- Asignar/remover con usuarios/roles válidos e inválidos
- Listado de roles del usuario
