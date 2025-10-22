# Backend Service Specification: PermissionService

## Metadata
- ID: BSS-IOC-004-07
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `PermissionService`
- Tipo: Service
- Package: `com.cambiaso.ioc.service`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha Creación: 2025-10-22
- Última Actualización: 2025-10-22

---

## 1. Propósito
Gestionar catálogo de permisos. Unicidad por nombre. Impedir borrado si referenciado.

---

## 2. Interfaz Pública

```java
public interface PermissionService {
  Page<PermissionResponse> list(int page, int size, String search);
  PermissionResponse create(PermissionRequest req);
  PermissionResponse update(int id, PermissionRequest req);
  void delete(int id);
}

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements PermissionService {
  private final PermissionRepository permissionRepository;
  private final RolePermissionRepository rolePermissionRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<PermissionResponse> list(int page, int size, String search) {
    var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("name"));
    return permissionRepository.search(search, pageable).map(PermissionMapper::toResponse);
  }

  @Override
  public PermissionResponse create(PermissionRequest req) {
    if (permissionRepository.existsByNameIgnoreCase(req.getName())) {
      throw new ConflictException("Permiso duplicado");
    }
    var p = new Permission();
    p.setName(req.getName());
    p.setDescription(req.getDescription());
    return PermissionMapper.toResponse(permissionRepository.save(p));
  }

  @Override
  public PermissionResponse update(int id, PermissionRequest req) {
    var p = permissionRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado"));
    if (!p.getName().equalsIgnoreCase(req.getName()) && permissionRepository.existsByNameIgnoreCase(req.getName())) {
      throw new ConflictException("Nombre de permiso en uso");
    }
    p.setName(req.getName());
    p.setDescription(req.getDescription());
    return PermissionMapper.toResponse(permissionRepository.save(p));
  }

  @Override
  public void delete(int id) {
    if (rolePermissionRepository.existsByPermissionId(id)) {
      throw new ConflictException("No se puede eliminar: permiso en uso");
    }
    permissionRepository.deleteById(id);
  }
}
```

---

## 3. Tests
- Crear/duplicado; update nombre; delete en uso

