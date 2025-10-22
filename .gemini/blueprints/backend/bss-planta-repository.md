# Backend Service Specification: PlantaRepository

## Metadata
- ID: BSS-IOC-004-14
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `PlantaRepository`
- Tipo: Repository
- Package: `com.cambiaso.ioc.repository`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Acceso a datos de `plantas` para validación de pertenencia organizacional de usuarios.

---

## 2. Interfaz Pública

```java
public interface PlantaRepository extends JpaRepository<Planta, Integer> {
  Optional<Planta> findByCodeIgnoreCase(String code);
}
```

---

## 3. Tests
- findByCodeIgnoreCase

