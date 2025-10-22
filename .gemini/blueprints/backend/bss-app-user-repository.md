# Backend Service Specification: AppUserRepository

## Metadata
- ID: BSS-IOC-004-09
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `AppUserRepository`
- Tipo: Repository
- Package: `com.cambiaso.ioc.repository`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Acceso a datos para `app_users` con búsquedas por email, supabase_user_id y filtros.

---

## 2. Interfaz Pública

```java
public interface AppUserRepository extends JpaRepository<AppUser, Long>, AppUserSearchRepository {
  Optional<AppUser> findByEmailIgnoreCase(String email);
  boolean existsByEmailIgnoreCase(String email);
  Optional<AppUser> findBySupabaseUserId(UUID supabaseUserId);
  boolean existsBySupabaseUserId(UUID supabaseUserId);
}

public interface AppUserSearchRepository {
  Page<AppUser> search(String search, Integer plantaId, Boolean isActive, Pageable pageable);
}
```

---

## 3. Implementación de búsqueda (ejemplo)

```java
@Repository
@RequiredArgsConstructor
public class AppUserSearchRepositoryImpl implements AppUserSearchRepository {
  private final EntityManager em;

  @Override
  public Page<AppUser> search(String search, Integer plantaId, Boolean isActive, Pageable pageable) {
    // JPQL dinámico simplificado
    var jpql = new StringBuilder("SELECT u FROM AppUser u WHERE u.deletedAt IS NULL");
    var countJpql = new StringBuilder("SELECT COUNT(u) FROM AppUser u WHERE u.deletedAt IS NULL");

    if (search != null && !search.isBlank()) {
      jpql.append(" AND (LOWER(u.email) LIKE :q OR LOWER(u.primerNombre) LIKE :q OR LOWER(u.primerApellido) LIKE :q)");
      countJpql.append(" AND (LOWER(u.email) LIKE :q OR LOWER(u.primerNombre) LIKE :q OR LOWER(u.primerApellido) LIKE :q)");
    }
    if (plantaId != null) {
      jpql.append(" AND u.planta.id = :plantaId");
      countJpql.append(" AND u.planta.id = :plantaId");
    }
    if (isActive != null) {
      jpql.append(" AND u.isActive = :active");
      countJpql.append(" AND u.isActive = :active");
    }

    var query = em.createQuery(jpql.toString(), AppUser.class);
    var countQuery = em.createQuery(countJpql.toString(), Long.class);

    if (search != null && !search.isBlank()) {
      var q = "%" + search.toLowerCase() + "%";
      query.setParameter("q", q);
      countQuery.setParameter("q", q);
    }
    if (plantaId != null) {
      query.setParameter("plantaId", plantaId);
      countQuery.setParameter("plantaId", plantaId);
    }
    if (isActive != null) {
      query.setParameter("active", isActive);
      countQuery.setParameter("active", isActive);
    }

    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());

    var items = query.getResultList();
    var total = countQuery.getSingleResult();
    return new PageImpl<>(items, pageable, total);
  }
}
```

---

## 4. Tests
- Búsqueda por email y apellido
- Filtros planta y activo

