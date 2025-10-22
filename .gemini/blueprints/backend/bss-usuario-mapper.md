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
Convertir entidades `AppUser` y relaciones en DTOs `UsuarioResponse` y mapear requests a entidad cuando aplique.

---

## 2. Interfaz Pública (MapStruct)

```java
@Mapper(componentModel = "spring")
public interface UsuarioMapper {
  @Mapping(target = "id", source = "entity.id")
  @Mapping(target = "email", source = "entity.email")
  @Mapping(target = "fullName", expression = "java(getFullName(entity))")
  @Mapping(target = "plantaId", source = "entity.planta.id")
  @Mapping(target = "plantaCode", source = "entity.planta.code")
  @Mapping(target = "plantaName", source = "entity.planta.name")
  @Mapping(target = "centroCosto", source = "entity.centroCosto")
  @Mapping(target = "fechaContrato", source = "entity.fechaContrato")
  @Mapping(target = "isActive", source = "entity.isActive")
  @Mapping(target = "createdAt", source = "entity.createdAt")
  @Mapping(target = "updatedAt", source = "entity.updatedAt")
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

## 3. Consideraciones
- El listado de roles se inyecta como parámetro secundario (`List<String> roles`), obtenido del repositorio de asignaciones.
- Campos de planta son null-safe (MapStruct manejará nulls si `entity.planta` es null).
- `fullName` se construye concatenando nombres no nulos/no vacíos en orden: primerNombre, segundoNombre, primerApellido, segundoApellido.

---

## 4. Testing (unitario)
- Nombre completo sin segundo nombre/apellido → debe omitir nulos/vacíos
- Planta null → plantaId, plantaCode, plantaName null
- Roles preservan orden si ya vienen ordenados desde el repositorio

