# Backend Service Specification: RoleRepository

## Metadata
- ID: BSS-IOC-004-10
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `RoleRepository`
- Tipo: Repository
- Package: `com.cambiaso.ioc.repository`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Acceso a datos para `roles` con búsqueda por nombre y paginación.

---

## 2. Interfaz Pública

```java
public interface RoleRepository extends JpaRepository<Role, Integer> {
  Optional<Role> findByNameIgnoreCase(String name);
  boolean existsByNameIgnoreCase(String name);

  @Query("SELECT r FROM Role r WHERE (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))) ")
  Page<Role> search(@Param("search") String search, Pageable pageable);

  boolean existsById(int id);
}
```

---

## 3. Tests
- Búsqueda por nombre

