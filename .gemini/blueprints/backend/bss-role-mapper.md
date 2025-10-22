# Backend Service Specification: RoleMapper

## Metadata
- ID: BSS-IOC-004-16
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `RoleMapper`
- Tipo: Mapper
- Package: `com.cambiaso.ioc.mapper`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Mapear `Role` a `RoleResponse` y requests a entidad.

---

## 2. Interfaz Pública

```java
@Mapper(componentModel = "spring")
public interface RoleMapper {
  RoleResponse toResponse(Role entity);
}
```

---

## 3. Tests
- Campos name/description

