# Backend Service Specification: AssignmentService

## Metadata
- ID: BSS-IOC-004-08
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `AssignmentService`
- Tipo: Service
- Package: `com.cambiaso.ioc.service`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha Creación: 2025-10-22
- Última Actualización: 2025-10-22

---

## 1. Propósito
Encapsular la lógica de asignación y remoción de roles a usuarios (tabla `user_roles`), con idempotencia y validaciones.

---

## 2. Interfaz Pública

```java
public interface AssignmentService {
  List<String> assignRoleToUser(long userId, int roleId);
  void removeRoleFromUser(long userId, int roleId);
  List<String> listRolesForUser(long userId);
}

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceImpl implements AssignmentService {
  private final AppUserRepository appUserRepository;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;

  @Override
  public List<String> assignRoleToUser(long userId, int roleId) {
    var user = appUserRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    var role = roleRepository.findById(roleId)
      .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));

    if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
      userRoleRepository.save(new UserRole(userId, roleId));
    }

    return userRoleRepository.findRoleNamesByUserId(userId);
  }

  @Override
  public void removeRoleFromUser(long userId, int roleId) {
    if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
      return; // idempotente
    }
    userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> listRolesForUser(long userId) {
    if (!appUserRepository.existsById(userId)) {
      throw new ResourceNotFoundException("Usuario no encontrado");
    }
    return userRoleRepository.findRoleNamesByUserId(userId);
  }
}
```

---

## 3. Tests
- Asignar rol existente a usuario existente (200)
- Reasignar mismo rol (idempotente)
- Remover rol no asignado (no-op)

