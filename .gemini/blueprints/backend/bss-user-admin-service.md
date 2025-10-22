- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `UserAdminService`
- Tipo: Service
- Package: `com.cambiaso.ioc.service`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha Creación: 2025-10-22
- Última Actualización: 2025-10-22

---

## 1. Propósito y Responsabilidad
Gestionar ciclo de vida de usuarios de aplicación, validando unicidad y ejecutando soft delete.

Responsabilidades:
- Crear usuario con datos validados y roles iniciales (opcional)
- Actualizar datos del usuario
- Soft delete y desactivación
- Listado con filtros y paginación

---

## 2. Interfaz Pública

```java
public interface UserAdminService {
  UsuarioResponse create(UsuarioCreateRequest request);
  UsuarioResponse update(long id, UsuarioUpdateRequest request);
  void softDelete(long id);
  UsuarioResponse getById(long id);
  Page<UsuarioResponse> list(int page, int size, String search, Integer plantaId, Boolean isActive);
}

@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminServiceImpl implements UserAdminService {
  private final AppUserRepository appUserRepository;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;
  private final PlantaRepository plantaRepository;
  private final UsuarioMapper mapper;

  @Override
  public UsuarioResponse create(UsuarioCreateRequest req) {
    // Validaciones: email único (citext), supabase_user_id único
    if (appUserRepository.existsByEmailIgnoreCase(req.getEmail())) {
      throw new ConflictException("Email ya existe");
    }
    if (appUserRepository.existsBySupabaseUserId(req.getSupabaseUserId())) {
      throw new ConflictException("supabaseUserId ya existe");
    }

    var user = new AppUser();
    user.setSupabaseUserId(req.getSupabaseUserId());
    user.setEmail(req.getEmail());
    user.setPrimerNombre(req.getPrimerNombre());
    user.setSegundoNombre(req.getSegundoNombre());
    user.setPrimerApellido(req.getPrimerApellido());
    user.setSegundoApellido(req.getSegundoApellido());
    user.setCentroCosto(req.getCentroCosto());
    user.setFechaContrato(req.getFechaContrato());
    user.setIsActive(true);

    if (req.getPlantaId() != null) {
      var planta = plantaRepository.findById(req.getPlantaId())
        .orElseThrow(() -> new ResourceNotFoundException("Planta no encontrada"));
      user.setPlanta(planta);
    }

    var saved = appUserRepository.save(user);

    if (req.getRoles() != null && !req.getRoles().isEmpty()) {
      for (String roleName : req.getRoles()) {
        var role = roleRepository.findByNameIgnoreCase(roleName)
          .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleName));
        userRoleRepository.save(new UserRole(saved.getId(), role.getId()));
      }
    }

    return mapper.toResponse(saved, fetchRoles(saved.getId()));
  }

  @Override
  public UsuarioResponse update(long id, UsuarioUpdateRequest req) {
    var user = appUserRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    // Campos editables
    user.setPrimerNombre(req.getPrimerNombre());
    user.setSegundoNombre(req.getSegundoNombre());
    user.setPrimerApellido(req.getPrimerApellido());
    user.setSegundoApellido(req.getSegundoApellido());
    user.setCentroCosto(req.getCentroCosto());
    user.setFechaContrato(req.getFechaContrato());
    if (req.getPlantaId() != null) {
      var planta = plantaRepository.findById(req.getPlantaId())
        .orElseThrow(() -> new ResourceNotFoundException("Planta no encontrada"));
      user.setPlanta(planta);
    } else {
      user.setPlanta(null);
    }

    var saved = appUserRepository.save(user);
    return mapper.toResponse(saved, fetchRoles(saved.getId()));
  }

  @Override
  public void softDelete(long id) {
    var user = appUserRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    user.setIsActive(false);
    user.setDeletedAt(OffsetDateTime.now());
    appUserRepository.save(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UsuarioResponse getById(long id) {
    var user = appUserRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    return mapper.toResponse(user, fetchRoles(user.getId()));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UsuarioResponse> list(int page, int size, String search, Integer plantaId, Boolean isActive) {
    var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
    var result = appUserRepository.search(search, plantaId, isActive, pageable);
    return result.map(u -> mapper.toResponse(u, fetchRoles(u.getId())));
  }

  private List<String> fetchRoles(long userId) {
    return userRoleRepository.findRoleNamesByUserId(userId);
  }
}
```

---

## 3. Dependencias
- AppUserRepository, RoleRepository, UserRoleRepository, PlantaRepository
- UsuarioMapper

---

## 4. Reglas de Negocio
- Email y supabase_user_id únicos
- Soft delete desactiva usuario y conserva historial
- Tamaño máximo de página 100

---

## 5. Transacciones
- create/update/delete: @Transactional (default)
- consultas: readOnly=true

---

## 6. Errores
- ConflictException (409) por duplicados
- ResourceNotFoundException (404) por entidades faltantes
- ValidationException (400)

---

## 7. Tests
- Crear usuario con planta válida y roles iniciales
- Duplicate email/supabase_user_id → 409
- Update planta nula → planta unset
- Soft delete → isActive=false y deletedAt != null
# Backend Service Specification: UserAdminService

## Metadata
- ID: BSS-IOC-004-05

