# Backend Service Specification: DTO Contracts (IOC-004)

## Metadata
- ID: BSS-IOC-004-18
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Módulo: `DTO Contracts`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Definir los contratos de datos (DTOs) usados por los Controllers y Services para el módulo de gestión de usuarios, roles y permisos.

---

## 2. DTOs de Usuario

```java
// requests/UsuarioCreateRequest.java
public class UsuarioCreateRequest {
  @NotBlank(message = "Email es requerido")
  @Email(message = "Formato de email inválido")
  private String email;

  @NotNull(message = "supabaseUserId es requerido")
  private UUID supabaseUserId;

  @NotBlank(message = "Primer nombre es requerido")
  @Size(min = 2, max = 100, message = "Primer nombre debe tener entre 2 y 100 caracteres")
  private String primerNombre;

  @Size(max = 100, message = "Segundo nombre no debe exceder 100 caracteres")
  private String segundoNombre;

  @NotBlank(message = "Primer apellido es requerido")
  @Size(min = 2, max = 100, message = "Primer apellido debe tener entre 2 y 100 caracteres")
  private String primerApellido;

  @Size(max = 100, message = "Segundo apellido no debe exceder 100 caracteres")
  private String segundoApellido;

  private Integer plantaId; // nullable

  @Size(max = 50, message = "Centro de costo no debe exceder 50 caracteres")
  private String centroCosto; // nullable

  private LocalDate fechaContrato; 

  private List<@NotBlank(message = "El nombre del rol no puede estar vacío") String> roles; // opcional
}

// requests/UsuarioUpdateRequest.java
public class UsuarioUpdateRequest {
  @NotBlank(message = "Primer nombre es requerido")
  @Size(min = 2, max = 100, message = "Primer nombre debe tener entre 2 y 100 caracteres")
  private String primerNombre;

  @Size(max = 100, message = "Segundo nombre no debe exceder 100 caracteres")
  private String segundoNombre;

  @NotBlank(message = "Primer apellido es requerido")
  @Size(min = 2, max = 100, message = "Primer apellido debe tener entre 2 y 100 caracteres")
  private String primerApellido;

  @Size(max = 100, message = "Segundo apellido no debe exceder 100 caracteres")
  private String segundoApellido;

  private Integer plantaId; // puede setearse null para desasignar

  @Size(max = 50, message = "Centro de costo no debe exceder 50 caracteres")
  private String centroCosto;

  private LocalDate fechaContrato; // LocalDate preferido

  private Boolean isActive; // opcional
}

// responses/UsuarioResponse.java
public class UsuarioResponse {
  private long id;
  private String email;
  private String fullName;
  private Integer plantaId;
  private String plantaCode;
  private String plantaName;
  private String centroCosto;
  private LocalDate fechaContrato;
  private boolean isActive;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private List<String> roles;
}

// responses/RolesResponse.java
public class RolesResponse {
  private List<String> roles;
  public RolesResponse(List<String> roles) { this.roles = roles; }
}
```

---

## 3. DTOs de Rol

```java
// requests/RoleRequest.java
public class RoleRequest {
  @NotBlank(message = "Nombre de rol es requerido")
  @Size(max = 100, message = "Nombre de rol no debe exceder 100 caracteres")
  private String name;

  @Size(max = 255, message = "Descripción no debe exceder 255 caracteres")
  private String description;
}

// responses/RoleResponse.java
public class RoleResponse {
  private int id;
  private String name;
  private String description;
  private Integer usersCount;        
  private List<String> permissions; 
}
```

---

## 4. DTOs de Permiso

```java
// requests/PermissionRequest.java
public class PermissionRequest {
  @NotBlank(message = "Nombre de permiso es requerido")
  @Size(max = 150, message = "Nombre de permiso no debe exceder 150 caracteres")
  private String name;

  @Size(max = 255, message = "Descripción no debe exceder 255 caracteres")
  private String description;
}

// responses/PermissionResponse.java
public class PermissionResponse {
  private int id;
  private String name;
  private String description;
}
```

---

## 5. Paginación Genérica (opcional)

```java
// responses/PageResponse.java 
public class PageResponse<T> {
  private List<T> items;
  private long totalElements;
  private int totalPages;
  private int currentPage;
  private int pageSize;
  private boolean hasNext;
  private boolean hasPrevious;
}
```

---

## 6. Notas de Validación
- email: formato y unicidad a nivel de DB (citext unique) y validación previa en Service
- supabaseUserId: UUID válido y único
- name (roles/permissions): case-insensitive uniqueness a nivel de repositorio/DB
- fechaContrato: LocalDate (ISO 8601); nulo permitido
