# Backend Service Specification: Custom Validators

## Metadata
- ID: BSS-IOC-004-24
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Módulo: `Validation`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Definir validadores de Bean Validation reutilizables para reglas de dominio: unicidad de email y formato/validez de UUID de Supabase.

---

## 2. @UniqueEmail

```java
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
  String message() default "Email ya está en uso";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}

@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
  private final AppUserRepository appUserRepository;
  
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) return true; // NotBlank se valida aparte
    return !appUserRepository.existsByEmailIgnoreCase(value);
  }
}
```

Uso:
```java
public class UsuarioCreateRequest {
  @NotBlank @Email @UniqueEmail
  private String email;
  // ...
}
```

---

## 3. @ValidSupabaseUUID

```java
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidSupabaseUUIDValidator.class)
public @interface ValidSupabaseUUID {
  String message() default "UUID inválido";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}

public class ValidSupabaseUUIDValidator implements ConstraintValidator<ValidSupabaseUUID, UUID> {
  @Override
  public boolean isValid(UUID value, ConstraintValidatorContext context) {
    // Si es UUID nativo, ya es válido; permitir null y delegar a @NotNull cuando aplique
    return value == null || value.toString().matches("^[0-9a-fA-F-]{36}$");
  }
}
```

Uso:
```java
public class UsuarioCreateRequest {
  @NotNull @ValidSupabaseUUID
  private UUID supabaseUserId;
}
```

---

## 4. Ubicación sugerida
- `src/main/java/com/cambiaso/ioc/validation`

## 5. Tests
- @UniqueEmail con email existente → false
- @ValidSupabaseUUID con formato incorrecto → false

