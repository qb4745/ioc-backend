# Backend Service Specification: PermissionMapper

## Metadata
- ID: BSS-IOC-004-17
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `PermissionMapper`
- Tipo: Mapper
- Package: `com.cambiaso.ioc.mapper`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Mapear `Permission` a `PermissionResponse`.

---

## 2. Interfaz Pública

```java
@Mapper(componentModel = "spring")
public interface PermissionMapper {
  PermissionResponse toResponse(Permission entity);
}
```

---

## 3. Tests
- Campos name/description
# Backend Service Specification: UsuarioMapper

## Metadata
- ID: BSS-IOC-004-15
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `UsuarioMapper`
- Tipo: Mapper
- Package: `com.cambiaso.ioc.mapper`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Convertir entidades `AppUser` y relaciones en DTOs `UsuarioResponse` y mapear requests a entidad.

---

## 2. Interfaz Pública

```java
@Mapper(componentModel = "spring")
public interface UsuarioMapper {
  @Mapping(target = "fullName", expression = "java(getFullName(entity))")
  @Mapping(target = "roles", source = "roles")
  UsuarioResponse toResponse(AppUser entity, List<String> roles);

  default String getFullName(AppUser u) {
    return Stream.of(u.getPrimerNombre(), u.getSegundoNombre(), u.getPrimerApellido(), u.getSegundoApellido())
      .filter(Objects::nonNull)
      .map(String::trim)
      .filter(s -> !s.isEmpty())
      .collect(Collectors.joining(" "));
  }
}
```

---

## 3. Tests
- Nombre completo sin segundo nombre/apellido
- Roles ordenados alfabéticamente (si el repo ya los trae ordenados)

