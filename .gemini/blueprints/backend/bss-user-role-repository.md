# Backend Service Specification: UserRoleRepository

## Metadata
- ID: BSS-IOC-004-12
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `UserRoleRepository`
- Tipo: Repository
- Package: `com.cambiaso.ioc.repository`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Acceso a datos a la tabla pivote `user_roles` y consultas de roles por usuario.

---

## 2. Interfaz Pública

```java
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleKey> {
  boolean existsByUserIdAndRoleId(long userId, int roleId);
  boolean existsByRoleId(int roleId);
  @Modifying
  @Transactional
  void deleteByUserIdAndRoleId(long userId, int roleId);

  @Query("SELECT r.name FROM UserRole ur JOIN Role r ON ur.roleId = r.id WHERE ur.userId = :userId ORDER BY r.name")
  List<String> findRoleNamesByUserId(@Param("userId") long userId);
}

@Embeddable
public class UserRoleKey implements Serializable {
  private Long userId;
  private Integer roleId;
  // equals/hashCode
}
```

---

## 3. Tests
- existsByUserIdAndRoleId, deleteByUserIdAndRoleId

