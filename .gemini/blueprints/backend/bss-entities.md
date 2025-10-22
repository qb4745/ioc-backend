# Backend Service Specification: JPA Entities (IOC-004)

## Metadata
- ID: BSS-IOC-004-19
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Módulo: `Entities`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Definir las entidades JPA que mapean el esquema SQL de usuario/rol/permiso descrito en `.gemini/sql/schema-user-role-management-unified.sql`.

---

## 2. Entidad: Planta

```java
@Entity
@Table(name = "plantas", indexes = { @Index(name = "idx_plantas_code", columnList = "code", unique = true) })
public class Planta {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
  @Column(nullable = false, length = 50, unique = true) private String code;
  @Column(nullable = false, length = 120) private String name;
  @Column(columnDefinition = "text") private String address;
  @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
  @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
}
```

---

## 3. Entidad: AppUser

```java
@Entity
@Table(name = "app_users",
  indexes = {
    @Index(name = "idx_app_users_active", columnList = "is_active"),
    @Index(name = "idx_app_users_supabase_uid", columnList = "supabase_user_id"),
    @Index(name = "idx_app_users_planta", columnList = "planta_id"),
    @Index(name = "idx_app_users_nombre", columnList = "primer_nombre, primer_apellido")
  }
)
public class AppUser {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(name = "supabase_user_id", nullable = false, unique = true) private UUID supabaseUserId;
  @Column(name = "email", nullable = false, unique = true) private String email; // citext en DB
  @Column(name = "primer_nombre", nullable = false) private String primerNombre;
  @Column(name = "segundo_nombre") private String segundoNombre;
  @Column(name = "primer_apellido", nullable = false) private String primerApellido;
  @Column(name = "segundo_apellido") private String segundoApellido;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "planta_id")
  private Planta planta;

  @Column(name = "centro_costo", length = 50) private String centroCosto;
  @Column(name = "fecha_contrato") private LocalDate fechaContrato;
  @Column(name = "is_active", nullable = false) private boolean isActive = true;
  @Column(name = "last_login_at") private OffsetDateTime lastLoginAt;
  @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
  @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
  @Column(name = "deleted_at") private OffsetDateTime deletedAt;
}
```

---

## 4. Entidad: Role

```java
@Entity
@Table(name = "roles")
public class Role {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
  @Column(nullable = false, unique = true, length = 100) private String name;
  private String description;
  @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
  @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
}
```

---

## 5. Entidad: Permission

```java
@Entity
@Table(name = "permissions")
public class Permission {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
  @Column(nullable = false, unique = true, length = 150) private String name;
  private String description;
}
```

---

## 6. Entidad: UserRole (Join)

```java
@Embeddable
public class UserRoleKey implements Serializable {
  @Column(name = "user_id") private Long userId;
  @Column(name = "role_id") private Integer roleId;
  // equals, hashCode
}

@Entity
@Table(name = "user_roles", indexes = {
  @Index(name = "idx_user_roles_user", columnList = "user_id"),
  @Index(name = "idx_user_roles_role", columnList = "role_id")
})
public class UserRole {
  @EmbeddedId private UserRoleKey id;
  @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId") @JoinColumn(name = "user_id") private AppUser user;
  @ManyToOne(fetch = FetchType.LAZY) @MapsId("roleId") @JoinColumn(name = "role_id") private Role role;
  @Column(name = "assigned_at", nullable = false) private OffsetDateTime assignedAt;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_by_user_id") private AppUser assignedBy;

  public UserRole() {}
  public UserRole(Long userId, Integer roleId) { this.id = new UserRoleKey(); this.id.setUserId(userId); this.id.setRoleId(roleId); }
}
```

---

## 7. Entidad: RolePermission (Join)

```java
@Embeddable
public class RolePermissionKey implements Serializable {
  @Column(name = "role_id") private Integer roleId;
  @Column(name = "permission_id") private Integer permissionId;
  // equals, hashCode
}

@Entity
@Table(name = "role_permissions")
public class RolePermission {
  @EmbeddedId private RolePermissionKey id;
  @ManyToOne(fetch = FetchType.LAZY) @MapsId("roleId") @JoinColumn(name = "role_id") private Role role;
  @ManyToOne(fetch = FetchType.LAZY) @MapsId("permissionId") @JoinColumn(name = "permission_id") private Permission permission;
  @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
}
```

---

## 8. Notas de Mapeo
- Fechas: usar OffsetDateTime para timestamps; LocalDate para `fecha_contrato`.
- citext: mapear como String; unicidad y comparación se aplican en DB.
- Cascadas: no establecer cascadas en joins M:N para evitar borrados accidentales.
- Equals/hashCode: basados en ID para entidades persistidas; en keys embebidas, incluir ambos campos.
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
  @NotBlank @Email private String email;
  @NotNull private UUID supabaseUserId;
  @NotBlank private String primerNombre;
  private String segundoNombre;
  @NotBlank private String primerApellido;
  private String segundoApellido;
  private Integer plantaId; // nullable
  @Size(max = 50) private String centroCosto; // nullable
  @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$") private String fechaContrato; // ISO date (YYYY-MM-DD) o usar LocalDate
  private List<String> roles; // opcional
}

// requests/UsuarioUpdateRequest.java
public class UsuarioUpdateRequest {
  @NotBlank private String primerNombre;
  private String segundoNombre;
  @NotBlank private String primerApellido;
  private String segundoApellido;
  private Integer plantaId; // puede setearse null
  @Size(max = 50) private String centroCosto;
  private LocalDate fechaContrato; // LocalDate preferido en updates
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
  @NotBlank @Size(max = 100) private String name;
  @Size(max = 255) private String description;
}

// responses/RoleResponse.java
public class RoleResponse {
  private int id;
  private String name;
  private String description;
  private int usersCount;        // opcional, si se proyecta desde vista/resumen
  private List<String> permissions; // opcional
}
```

---

## 4. DTOs de Permiso

```java
// requests/PermissionRequest.java
public class PermissionRequest {
  @NotBlank @Size(max = 150) private String name;
  @Size(max = 255) private String description;
}

// responses/PermissionResponse.java
public class PermissionResponse {
  private int id;
  private String name;
  private String description;
}
```

---

## 5. Paginación Genérica

```java
// responses/PageResponse.java (si se desea encapsular)
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

