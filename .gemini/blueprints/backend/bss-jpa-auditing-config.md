# Backend Service Specification: JPA Auditing Config

## Metadata
- ID: BSS-IOC-004-23
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `JpaAuditingConfig`
- Tipo: Config
- Package: `com.cambiaso.ioc.config`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Habilitar auditoría (createdBy, lastModifiedBy) para registrar acciones de administración.

---

## 2. Config propuesta

```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
  @Bean
  public AuditorAware<String> auditorAware() {
    return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .filter(Authentication::isAuthenticated)
        .map(Authentication::getName);
  }
}
```

---

## 3. Uso en entidades (ejemplo)

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class AppUser {
  @CreatedBy
  private String createdBy;
  @LastModifiedBy
  private String lastModifiedBy;
}
```

