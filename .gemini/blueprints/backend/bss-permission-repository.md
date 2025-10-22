# Backend Service Specification: PermissionRepository

## Metadata
- ID: BSS-IOC-004-11
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `PermissionRepository`
- Tipo: Repository
- Package: `com.cambiaso.ioc.repository`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Acceso a datos para `permissions` con búsqueda por nombre y paginación.

---

## 2. Interfaz Pública

```java
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
  Optional<Permission> findByNameIgnoreCase(String name);
  boolean existsByNameIgnoreCase(String name);

  @Query("SELECT p FROM Permission p WHERE (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) ")
  Page<Permission> search(@Param("search") String search, Pageable pageable);
}
```

---

## 3. Tests
- Búsqueda por nombre

