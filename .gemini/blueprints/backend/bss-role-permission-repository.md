# Backend Service Specification: RolePermissionRepository

## Metadata
- ID: BSS-IOC-004-13
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `RolePermissionRepository`
- Tipo: Repository
- Package: `com.cambiaso.ioc.repository`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Acceso a datos de la tabla pivote `role_permissions` y consultas de permisos por rol.

---

## 2. Interfaz Pública

```java
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionKey> {
  boolean existsByRoleId(int roleId);
  boolean existsByPermissionId(int permissionId);
}

@Embeddable
public class RolePermissionKey implements Serializable {
  private Integer roleId;
  private Integer permissionId;
  // equals/hashCode
}
```

---

## 3. Tests
- existsByRoleId, existsByPermissionId

