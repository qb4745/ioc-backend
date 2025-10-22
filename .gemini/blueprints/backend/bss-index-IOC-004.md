# Backend Service Specifications Index - IOC-004 (User Role Management)

Technical Design: `.gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md`
Feature Plan: `.gemini/sprints/feature-plans/FP-IOC-004-User-Role-Management.md`

## Controllers
- bss-admin-user-controller.md
- bss-role-controller.md
- bss-permission-controller.md
- bss-assignment-controller.md

## Services
- bss-user-admin-service.md
- bss-role-service.md
- bss-permission-service.md
- bss-assignment-service.md

## Repositories
- bss-app-user-repository.md
- bss-role-repository.md
- bss-permission-repository.md
- bss-user-role-repository.md
- bss-role-permission-repository.md
- bss-planta-repository.md

## Mappers
- bss-usuario-mapper.md
- bss-role-mapper.md
- bss-permission-mapper.md

## Contracts (DTOs y Entidades)
- bss-dto-contracts.md
- bss-entities.md
- bss-common-dtos.md

## Exception Handling
- bss-global-exception-handler.md

## Configuración
- bss-security-config.md
- bss-jpa-auditing-config.md

## Validation
- bss-custom-validators.md

## Notas
- Todos los endpoints admin requieren `ROLE_ADMIN`.
- Limitar page size a 100 por performance.
- Operaciones de asignación son idempotentes.
- AssignmentController es parte del scope (no opcional) para separar responsabilidades.

---

<!-- Implementation assessment: added by automation -->
## Implementation assessment: order, locations and mapping
A concise plan to implement the blueprints into code. Follow the numbered order to minimize merge conflicts and enable fast verification.

1) Database / Migration
- Action: Run and review SQL migration first.
- File: `.gemini/sql/schema-user-role-management-unified.sql`
- Why first: Entities/repositories depend on DB schema (types, constraints, RLS, indices).

2) JPA Entities
- Action: Create entity classes that map the SQL schema.
- Files produced from: `bss-entities.md`
- Target package: `src/main/java/com/cambiaso/ioc/model/entity`
- Example files:
  - `Planta.java` -> `plantas` table
  - `AppUser.java` -> `app_users` table
  - `Role.java`, `Permission.java`
  - `UserRole.java` (+ `UserRoleKey`), `RolePermission.java` (+ `RolePermissionKey`)

3) Repositories (Data Access)
- Action: Implement Spring Data JPA repositories and any custom search impls.
- Files produced from: `bss-app-user-repository.md`, `bss-role-repository.md`, `bss-permission-repository.md`, `bss-user-role-repository.md`, `bss-role-permission-repository.md`, `bss-planta-repository.md`
- Target package: `src/main/java/com/cambiaso/ioc/repository`
- Notes: Add a custom `AppUserSearchRepositoryImpl` (EntityManager) for dynamic filters.

4) DTOs and Mappers
- Action: Create DTO classes and MapStruct mappers.
- Files produced from: `bss-dto-contracts.md`, `bss-usuario-mapper.md`, `bss-role-mapper.md`, `bss-permission-mapper.md`, `bss-common-dtos.md`, `bss-custom-validators.md`
- Target packages:
  - DTOs: `src/main/java/com/cambiaso/ioc/dto/request` and `.../dto/response`
  - Common DTOs: `src/main/java/com/cambiaso/ioc/dto/common`
  - Mappers: `src/main/java/com/cambiaso/ioc/mapper`
  - Validators: `src/main/java/com/cambiaso/ioc/validation`
- Why now: Services depend on DTO shapes and mappers for conversion.

5) Services (Business Logic)
- Action: Implement `UserAdminService`, `RoleService`, `PermissionService`, `AssignmentService`.
- Files produced from: `bss-user-admin-service.md`, `bss-role-service.md`, `bss-permission-service.md`, `bss-assignment-service.md`
- Target package: `src/main/java/com/cambiaso/ioc/service`
- Transaction strategy: annotate service implementations with `@Transactional` (readOnly where appropriate).
- Tests: Unit tests for business rules (use Mockito + JUnit 5).

6) Controllers (REST layer)
- Action: Implement `AdminUserController`, `RoleController`, `PermissionController`, `AssignmentController`.
- Files produced from: `bss-admin-user-controller.md`, `bss-role-controller.md`, `bss-permission-controller.md`, `bss-assignment-controller.md`
- Target package: `src/main/java/com/cambiaso/ioc/controller/admin`
- Security: Use `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` as specified in BSS.

7) Exception Handling
- Action: Implement `GlobalExceptionHandler` and custom exceptions, use consistent `ErrorResponse`.
- Files produced from: `bss-global-exception-handler.md`, `bss-common-dtos.md`
- Target package: `src/main/java/com/cambiaso/ioc/exception`

8) Security and Integration
- Action: Wire JWT converter and map supabase claims to authorities. Ensure `JwtAuthenticationConverter` returns ROLE_* and optionally PERM_*.
- Files to inspect/edit: existing security config under `src/main/java/com/cambiaso/ioc/config` (if present) and implement as per `bss-security-config.md`.

9) Tests and QA
- Unit Tests: put under `src/test/java/com/cambiaso/ioc/service` and `.../controller`. Aim >= 85% coverage for new modules.
- Integration Tests: use @SpringBootTest with testcontainers or an in-memory DB matching Postgres features (if possible use Testcontainers for Postgres to validate RLS/functions).
- SQL/RLS checks: run scripts using `psql` or Supabase SQL editor to validate functions `has_role`, `has_permission` and views.

10) Documentation and OpenAPI
- Action: Generate OpenAPI (Springdoc) using controller annotations and DTOs, export YAML under `docs/openapi/IOC-004.yaml`.
- Files: update project's Swagger config if exists or add `springdoc-openapi` dependency.

11) Observability and Release
- Action: Add logging, metrics and a smoke-test rollout to QA.
- Locations: logging in services/controllers (`com.cambiaso.ioc.*`), metrics via Micrometer if available.

---

### Quick mapping: BSS file → suggested Java artifact location (path examples)
- `bss-entities.md` → `src/main/java/com/cambiaso/ioc/model/entity/*.java`
- `bss-dto-contracts.md` → `src/main/java/com/cambiaso/ioc/dto/request` and `/dto/response`
- `bss-common-dtos.md` → `src/main/java/com/cambiaso/ioc/dto/common`
- `bss-usuario-mapper.md`, `bss-role-mapper.md` → `src/main/java/com/cambiaso/ioc/mapper/*Mapper.java`
- `bss-app-user-repository.md`, `bss-role-repository.md`, `bss-permission-repository.md`, `bss-user-role-repository.md`, `bss-role-permission-repository.md`, `bss-planta-repository.md` → `src/main/java/com/cambiaso/ioc/repository/*Repository.java`
- `bss-user-admin-service.md`, `bss-role-service.md`, `bss-permission-service.md`, `bss-assignment-service.md` → `src/main/java/com/cambiaso/ioc/service/*Service.java` and `*ServiceImpl.java`
- `bss-admin-user-controller.md`, `bss-role-controller.md`, `bss-permission-controller.md`, `bss-assignment-controller.md` → `src/main/java/com/cambiaso/ioc/controller/admin/*Controller.java`
- `bss-global-exception-handler.md` → `src/main/java/com/cambiaso/ioc/exception/*`
- `bss-security-config.md` → `src/main/java/com/cambiaso/ioc/config/SecurityConfig.java`
- `bss-jpa-auditing-config.md` → `src/main/java/com/cambiaso/ioc/config/JpaAuditingConfig.java`
- `bss-custom-validators.md` → `src/main/java/com/cambiaso/ioc/validation/*`

---

If you want, I can now:
- A) Normalize filename casing (lowercase) to remove duplicates and update this index.
- B) Scaffold the Java stubs for steps 2–7 and run `mvn -DskipTests package` to check compilation.
- C) Generate an OpenAPI YAML from the controller specs.

Which action (A, B, C, or combination) do you want me to run next?
