# Backend Service Specification: RoleService

## Metadata
- ID: BSS-IOC-004-06
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `RoleService`
- Tipo: Service
- Package: `com.cambiaso.ioc.service`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha Creación: 2025-10-22
- Última Actualización: 2025-10-22

---

## 1. Propósito
Gestionar catálogo de roles y validaciones de unicidad; impedir borrado con referencias.

---

## 2. Interfaz Pública

```java
public interface RoleService {
  Page<RoleResponse> list(int page, int size, String search);
  RoleResponse create(RoleRequest req);
  RoleResponse update(int id, RoleRequest req);
  void delete(int id);
}

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;
  private final RolePermissionRepository rolePermissionRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<RoleResponse> list(int page, int size, String search) {
    var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("name"));
    var items = roleRepository.search(search, pageable);
    return items.map(RoleMapper::toResponse);
  }

  @Override
  public RoleResponse create(RoleRequest req) {
    if (roleRepository.existsByNameIgnoreCase(req.getName())) {
      throw new ConflictException("Rol duplicado");
    }
    var role = new Role();
    role.setName(req.getName());
    role.setDescription(req.getDescription());
    return RoleMapper.toResponse(roleRepository.save(role));
  }

  @Override
  public RoleResponse update(int id, RoleRequest req) {
    var role = roleRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));
    if (!role.getName().equalsIgnoreCase(req.getName()) && roleRepository.existsByNameIgnoreCase(req.getName())) {
      throw new ConflictException("Nombre de rol en uso");
    }
    role.setName(req.getName());
    role.setDescription(req.getDescription());
    return RoleMapper.toResponse(roleRepository.save(role));
  }

  @Override
  public void delete(int id) {
    if (userRoleRepository.existsByRoleId(id) || rolePermissionRepository.existsByRoleId(id)) {
      throw new ConflictException("No se puede eliminar: rol en uso");
    }
    roleRepository.deleteById(id);
  }
}
```

---

## 3. Tests
- Crear rol → 201; duplicado → 409
- Update con cambio de nombre a existente → 409
- Delete en uso → 409

