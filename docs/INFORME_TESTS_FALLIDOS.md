INFORME: Cambios que rompieron tests
=====================================

Fecha: 2025-11-10
Repositorio: ioc-backend
Commit principal analizado: dfc5741 (fix(iam): Corrige asignación de roles y adapta seguridad a Supabase Free Tier)

Resumen ejecutivo
-----------------
Se identificaron dos cambios principales introducidos en el commit `dfc5741` que muy probablemente provocan los fallos de los tests reportados:

- En `SecurityConfig.java` se añadió lógica para enriquecer las autoridades (roles) leyendo `user_roles` desde la base de datos local usando el `sub` (UUID) del JWT. Esto hace que la validación/autoría dependa de la existencia de filas en la BD durante la conversión del JWT a GrantedAuthorities.

- En `UserAdminService.java` se modificó el flujo de creación de usuarios para (1) crear el usuario también en Supabase cuando se proporciona `password`, y (2) asignar roles automáticamente al usuario en la base de datos según el request. Ambos cambios aumentan las dependencias externas y de datos durante la ejecución de los endpoints y los tests.

Estos cambios explican los fallos observados (500 y 404) en los tests de controladores y en la carga del contexto de Spring.

Tests afectados (según log inicial)
----------------------------------
- AdminUserControllerTest (create, delete, getById, search, update) → errores 500
- AssignmentControllerTest (assign/remove role/permission) → 404
- PermissionControllerTest.search_ok_returns200 → 404
- RoleControllerTest (getById, search) → 404
- IocbackendApplicationTests.contextLoads → fallo de ApplicationContext (umbral de reintentos excedido)

Archivos y cambios clave
------------------------
1) `src/main/java/com/cambiaso/ioc/security/SecurityConfig.java`
   - Nueva lógica en `jwtGrantedAuthoritiesConverter()`:
     - Extrae `sub` del JWT (subject) y asume que es un UUID de Supabase.
     - Busca `AppUser` por `supabaseUserId` en la BD local (`appUserRepository.findBySupabaseUserId(...)`).
     - Si encuentra el usuario, obtiene roles desde `userRoleRepository.findRoleNamesByUserId(...)` y los convierte a `SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase())`.
   - Efecto: la conversión del JWT a autoridades ya no depende únicamente del token; depende de datos en la BD.
   - Riesgos:
     - Si en tests el `subject` no es un UUID válido o no existe en la BD, no se añadirá rol alguno.
     - Si la consulta a la BD lanza excepciones por configuración de pruebas, el proceso de autorización/arranque puede fallar.

2) `src/main/java/com/cambiaso/ioc/service/UserAdminService.java`
   - Flujo de creación (`create(UsuarioCreateRequest req)`):
     - Si `req.getPassword()` está presente, se llama a `supabaseAuthService.createSupabaseUser(email, password)` y se almacena el `supabaseUserId` en `AppUser`.
     - Si se proporcionan `roles` en el request, por cada rol hace `roleRepository.findByNameIgnoreCase(roleName).orElseThrow(...)` y crea una entidad `UserRole` para enlazar el rol con el usuario.
     - En caso de fallo al persistir en la BD, si se creó el usuario en Supabase, el código intenta eliminarlo (rollback de Supabase).
   - Efecto: la creación de usuario en los endpoints de prueba ahora puede:
     - Llamar a `SupabaseAuthService` (requiere mock o infra accesible en tests).
     - Lanzar `ResourceNotFoundException` si un rol enviado no existe en la BD de pruebas.
   - Riesgos:
     - Tests que antes eran unit/integration y no esperaban dependencias externas ahora fallan por falta de mocks/fixtures.

Por qué estos cambios provocan **500** y **404**
---------------------------------------------
- 500 Internal Server Error en tests de AdminUserController:
  - Si `supabaseAuthService.createSupabaseUser(...)` no está mockeado, su ejecución puede lanzar excepciones (conexión, configuración), provocando 500.
  - Si la creación en BD falla mientras el usuario ya fue creado en Supabase, se lanza excepción y el handler puede propagar 500.

- 404 / autorización fallida en Assignment/Permission/Role controllers:
  - La nueva lógica de `SecurityConfig` añade autoridades basadas en la BD. Si el JWT usado por los tests no produce autoridades (por ejemplo, `sub` no mapea a un `AppUser` con roles en la BD), las peticiones requieren roles y devuelven 403/404 según la configuración.

Evidencias (extractos relevantes)
---------------------------------
- SecurityConfig (se agregó la consulta a BD durante la conversión del JWT):
  - Código (conceptual):
    - extraer subject: `String supabaseUserIdStr = jwt.getSubject();`
    - parse UUID y `appUserRepository.findBySupabaseUserId(...)`
    - `userRoleRepository.findRoleNamesByUserId(appUser.getId())` → convertir a `SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase())`

- UserAdminService (nuevo flujo de creación + asignación de roles):
  - Crear en Supabase: `supabaseAuthService.createSupabaseUser(email, req.getPassword())`
  - Asignar roles: `roleRepository.findByNameIgnoreCase(roleName).orElseThrow(...)` y `userRoleRepository.save(userRole)`

Recomendaciones rápidas (para restablecer tests)
------------------------------------------------
1. En el corto plazo (tests):
   - Mockear `SupabaseAuthService` en los tests que ejercen la creación de usuarios para que devuelva un UUID predecible sin llamar a Supabase.
   - Asegurar que los tests de integración carguen fixtures con los roles esperados en la BD (insertar filas en `roles` y `user_roles` según corresponda).
   - Alternativa rápida: condicionar la lógica de enriquecimiento de roles en `SecurityConfig` para evitar la consulta a la BD cuando el perfil `test` esté activo (p. ej. `if (!profileIsTest) { ... }`). Esto mantiene la compatibilidad con el nuevo comportamiento en entornos reales pero evita dependencia DB en tests.

2. En el mediano plazo:
   - Revisar los tests para que simulen correctamente el flujo actual (crear roles antes de asignar, mockear Supabase, o usar testcontainers con datos iniciales).
   - Añadir pruebas de integración que cubran el nuevo flujo de creación y la eventual eliminación/rollback en Supabase.

Acciones realizadas
------------------
- Se creó este informe: `docs/INFORME_TESTS_FALLIDOS.md`, con la identificación del commit responsable y el análisis de los fragmentos de código que probablemente rompen los tests.

Acciones realizadas (actualizado 2025-11-10)
------------------------------------------

- Corregí las pruebas de `AdminUserControllerTest` y validé localmente:
  - Actualicé las rutas en los tests de `/api/admin/users` a la versión correcta `/api/v1/admin/users` para que coincidan con el `@RequestMapping` del controlador.
  - Ajusté la expectativa HTTP en el test de creación para esperar `201 Created` en lugar de `200 OK` (el controlador devuelve `201` al crear recursos).
  - Ejecuté `./mvnw -Dtest=AdminUserControllerTest test` y todos los tests de esa clase pasaron (`5/5`, BUILD SUCCESS).

Acciones realizadas (actualizado 2025-11-10 v2)
---------------------------------------------

- Corregí las pruebas de `AssignmentControllerTest`:
  - Actualicé las rutas en los tests de `/api/admin/assignments/...` a la versión correcta `/api/v1/admin/assignments/...` para que coincidan con el `@RequestMapping` del controlador `AssignmentController`.
  - Ejecuté `./mvnw -Dtest=AssignmentControllerTest test` y todos los tests de esa clase pasaron (`4/4`, BUILD SUCCESS).

Acciones realizadas (actualizado 2025-11-10 v3)
---------------------------------------------

- Corregí las pruebas de `PermissionControllerTest`:
  - Actualicé la ruta en el test de `/api/admin/permissions` a `/api/v1/admin/permissions` para que coincida con el `@RequestMapping` del `PermissionController`.
  - Ejecuté `./mvnw -Dtest=PermissionControllerTest test` y el test pasó (`1/1`, BUILD SUCCESS).

Acciones realizadas (actualizado 2025-11-10 v4)
---------------------------------------------

- Corregí las pruebas de `RoleControllerTest`:
  - Actualicé las rutas en los tests de `/api/admin/roles` a `/api/v1/admin/roles` para que coincidan con el `@RequestMapping` del controlador `RoleController`.
  - Ejecuté `./mvnw -Dtest=RoleControllerTest test` y todos los tests de esa clase pasaron (`2/2`, BUILD SUCCESS).

Incidente reciente: fallo en tests de integración de Dashboard
------------------------------------------------------------

- Observación: Al ejecutar la suite de tests se reporta una excepción de umbral de carga del `ApplicationContext` y errores en `DashboardControllerIntegrationTest` (varias pruebas con ERROR en lugar de FAILURE). La traza muestra `ApplicationContext failure threshold (1) exceeded` y una `IllegalStateException` cuando el `ServletTestExecutionListener` intenta preparar el test.

- Síntomas concretos del log:
  - Mensaje: "ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context for [WebMergedContextConfiguration... activeProfiles = [\"test\"]]"
  - Error en `DashboardControllerIntegrationTest.shouldReturn401ForUnauthenticatedUser` (ERROR, no simple assertion failure)
  - Esto sugiere que el contexto de Spring no se carga correctamente para tests de integración (posibles causas: cambios en `SecurityConfig`, beans faltantes, dependencias externas en inicialización como Supabase, o excepciones en inicializers).

Plan inmediato:
1. Abrir `src/test/java/.../DashboardControllerIntegrationTest.java` para ver qué configura y cuáles mocks/beans espera.
2. Ejecutar ese test individualmente para capturar el stack trace completo si es necesario.
3. Corregir problemas de configuración: mockear servicios externos (p. ej. `SupabaseAuthService`), asegurar `GlobalTestConfiguration` está presente y correcto, o condicionar lógica en `SecurityConfig` para el perfil `test` si es necesario.

Ahora leeré el test `DashboardControllerIntegrationTest` para analizar su contenido.

DashboardControllerIntegrationTest — acciones, estado y sospechas
----------------------------------------------------------------

Resumen de acciones realizadas relacionadas con el fallo del Dashboard:

- Identificación inicial: al ejecutar la suite los tests de integración del dashboard fallaban con una excepción al cargar el ApplicationContext (`Failed to load ApplicationContext`), reportada en el informe de Surefire y en los logs como `BeanDefinitionOverrideException` para el bean `jwtDecoder`.

- Acciones aplicadas en código (para aislar y permitir la carga del contexto en `test`):
  1. Añadí un bean mock de `JwtDecoder` en `src/test/java/com/cambiaso/ioc/config/GlobalTestConfiguration.java` (bean primario de prueba) para evitar que el decoder real intente resolver JWKS/issuer durante los tests.
  2. Hice el bean real del decoder en `src/main/java/com/cambiaso/ioc/security/SecurityConfig.java` condicional con `@ConditionalOnMissingBean(JwtDecoder.class)`. Con esto, cuando `GlobalTestConfiguration` provee un `JwtDecoder` mock, el bean productivo no se registra y se evita el conflicto de definición.
  3. Ejecuté iterativamente los tests para verificar el comportamiento y corregí varios tests de controller que usaban rutas antiguas (`/api/admin/...`) a la versión actual (`/api/v1/admin/...`). Tras esas correcciones, las siguientes clases pasaron localmente:
     - `AdminUserControllerTest` (5/5)
     - `AssignmentControllerTest` (4/4)
     - `PermissionControllerTest` (1/1)
     - `RoleControllerTest` (2/2)

Estado actual (tras las correcciones anteriores):

- El error original en los logs era:
  - `org.springframework.beans.factory.support.BeanDefinitionOverrideException: Invalid bean definition with name 'jwtDecoder' ... since there is already ... defined in GlobalTestConfiguration`.
- Para mitigar esto implementé los cambios 1) y 2) (mock en tests y `@ConditionalOnMissingBean`).
- Después de esos cambios, intenté ejecutar `DashboardControllerIntegrationTest` para validar la carga del contexto. Debido a la salida parcial en algunos intentos de ejecución (seciones interrumpidas por herramientas), la ejecución no siempre imprimió toda la traza en la terminal interactiva, pero el cambio aplicado resuelve de forma explícita la colisión de beans detectada en la traza de Surefire.

Sospechas (causas raíz probables) — por orden de prioridad:

1. Colisión de beans `JwtDecoder` (evidencia confirmada):
   - La traza mostró explícitamente `BeanDefinitionOverrideException` por `jwtDecoder` (definido en `SecurityConfig` y en `GlobalTestConfiguration`). Esto impide la carga del contexto. El remedio aplicado fue permitir que el test inyecte su propio `JwtDecoder` (mock) y que el bean productivo sea condicional.

2. Inicialización de componentes que realizan llamadas externas durante el arranque del contexto (alta probabilidad):
   - `JwtDecoder` real intenta resolver JWKS/issuer al inicializarse (network). En tests debemos evitarlo (mockear o condicionar).
   - Servicios como `SupabaseAuthService` o `MetabaseEmbeddingService` pueden intentar llamadas HTTP o inicializaciones que dependen de propiedades no definidas en `application-test.properties`. Si alguno de estos se inicializa sin mock, puede lanzar excepciones en el arranque.

3. Lógica de enriquecimiento de roles en `SecurityConfig` que realiza consultas a BD durante la conversión del JWT (media probabilidad de causar fallos en tests de seguridad):
   - El código añadió: buscar `AppUser` por `supabaseUserId` y luego `userRoleRepository.findRoleNamesByUserId(...)`. Si la BD de tests no contiene datos o los repositorios no están preparados, esto podría provocar comportamientos inesperados en la fase de autorización o en endpoints protegidos.

4. Fixtures faltantes en H2 para tests de integración (roles/datos esperados):
   - En ausencia de filas en `roles` o `user_roles`, los endpoints protegidos por roles pueden devolver 403/404. Recomendación: asegurar inserts mínimos en `src/test/resources/init-h2.sql` o crear un `@BeforeEach` que inserte los roles necesarios.

5. Metabase / configuración de dashboads en `application-test.properties`:
   - `DashboardControllerIntegrationTest` espera que exista un dashboard con id=1 y que `MetabaseEmbeddingService` pueda generar una URL firmada. Si el servicio real intenta comunicarse con Metabase, el test fallará. Posible solución: mockear `MetabaseEmbeddingService` en `GlobalTestConfiguration` o proveer configuraciones de prueba que emulen Metabase (o ajustar la prueba para mockear la llamada).

Próximos pasos recomendados (tácticos)

1. Ejecutar localmente: confirmar `./mvnw -Dtest=DashboardControllerIntegrationTest test` (ya incluí los cambios, confirma resultado en tu entorno).
2. Si el test sigue fallando, aplicar una de estas soluciones puntuales (en orden):
   - Mockear `MetabaseEmbeddingService` en `GlobalTestConfiguration` (bajo `@Bean @Primary`) para devolver una URL firmada de ejemplo.
   - Mockear `SupabaseAuthService` globalmente en `GlobalTestConfiguration` si hay tests que lo requieren en el arranque.
   - Asegurar `init-h2.sql` incluya inserts mínimos para `roles` y `user_roles` necesarios por los tests.
3. Como medida temporal (si necesitas correr la suite deprisa), condicionar el enriquecimiento desde BD en `SecurityConfig` cuando el perfil `test` esté activo (ej: inyectar `Environment` y saltar la consulta para `test`). Esto evita dependencias a la BD para la conversión de JWT en tests; sin embargo, es solo temporal y no sustituye preparar fixtures/mocks.

Resumen de lo que agregué al repo (archivos modificados)

- `docs/INFORME_TESTS_FALLIDOS.md` (esta sección añadida)
- `src/test/java/com/cambiaso/ioc/config/GlobalTestConfiguration.java` (añadí `JwtDecoder` mock)
- `src/main/java/com/cambiaso/ioc/security/SecurityConfig.java` (anoté `jwtDecoder()` con `@ConditionalOnMissingBean`)
- Varios tests actualizados para usar `/api/v1/...` (correcciones en `src/test/java/com/cambiaso/ioc/controller/*ControllerTest.java`): `AdminUserControllerTest`, `AssignmentControllerTest`, `PermissionControllerTest`, `RoleControllerTest`.

Si quieres, aplico inmediatamente cualquiera de los pasos de "Próximos pasos" (por ejemplo: mockear `MetabaseEmbeddingService` en `GlobalTestConfiguration` y ejecutar de nuevo `DashboardControllerIntegrationTest`) y te proporcionaré los resultados. También puedo ejecutar toda la suite de tests si quieres que continúe hasta que todo pase.

Acciones realizadas (actualizado 2025-11-10 v4) — Reparación de DashboardControllerIntegrationTest
-----------------------------------------------------------------------------------------------

Fecha: 2025-11-10
Responsable: Equipo de integración / IA (cambios automáticos y verificados localmente)

Resumen de la falla encontrada
- El test `DashboardControllerIntegrationTest` esperaba generar una URL firmada real usando el servicio `MetabaseEmbeddingService`.
- La configuración global de tests (`GlobalTestConfiguration`) mockeaba `MetabaseEmbeddingService`, por lo que el test no ejercitaba el servicio real y las aserciones de URL fallaban.
- Además, al intentar cargar un contexto de integración sin el mock correcto de `SupabaseAuthService` y sin propiedades de Supabase, Spring no pudo crear el bean y el context fail-fast provocó error al iniciar los tests.

Cambios aplicados (detallado)
1) `src/test/java/com/cambiaso/ioc/controller/DashboardControllerTestConfiguration.java` (nuevo)
   - Creado un `@TestConfiguration` específico para el test `DashboardControllerIntegrationTest`.
   - Provee mocks selectivos y primarios para:
     - `SimpMessagingTemplate` (mock)
     - `NotificationService` (mock)
     - `MeterRegistry` (SimpleMeterRegistry)
     - `JwtDecoder` (mock)
     - `SupabaseAuthService` (mock)
   - Importante: NO mockea `MetabaseEmbeddingService`, de modo que el test use el servicio real configurado por `application-test.properties`.

2) `src/test/java/com/cambiaso/ioc/controller/DashboardControllerIntegrationTest.java` (modificado)
   - Se cambió la anotación para usar `@SpringBootTest`, `@AutoConfigureMockMvc` y `@Import(DashboardControllerTestConfiguration.class)`.
   - Añadido `@MockBean DashboardAuditService` para evitar efectos secundarios de auditoría durante el test (se hace `doNothing()` en las llamadas).
   - Ajustadas las anotaciones `@WithMockUser(roles = {"USER","ADMIN"})` para garantizar autoridades en las pruebas.

3) `src/test/resources/application-test.properties` (modificado)
   - Añadidas propiedades de Supabase necesarias para la creación correcta del bean `SupabaseAuthService` en el contexto de tests:
     - `supabase.url=http://localhost:54321`
     - `supabase.service-role-key=test-service-role-key-for-testing-only`
   - Confirmado que ya existía la configuración de Metabase (clave secreta hex de 64 caracteres y `metabase.site-url=http://localhost:3000`) necesaria para que `MetabaseEmbeddingService` se inicialice correctamente.

Comandos ejecutados y verificación
- Ejecuté localmente (en el workspace) el test concreto y confirmé el PASS:

```bash
./mvnw -Dtest=DashboardControllerIntegrationTest test
```

Salida relevante (extracto):
- "MetabaseEmbeddingService initialized successfully with 1 configured dashboards"
- Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
- BUILD SUCCESS

Lecciones / recomendaciones (corto plazo)
- Evitar mockear globalmente servicios que algunos tests de integración deben probar con su implementación real (p.ej. `MetabaseEmbeddingService`). En su lugar, preferir:
  - un `@TestConfiguration` por test que mockee solo lo estrictamente necesario y deje el servicio real cuando el test lo requiera, o
  - usar `@Import` en test específicos para anular beans globales.
- Para servicios externos (Supabase, Metabase, etc.) asegurar en `application-test.properties` valores de prueba (URLs y claves sintéticas) para permitir la creación de beans y que los tests no hagan llamadas externas.

Lecciones / recomendaciones (mediano plazo)
- Documentar en la estrategia de testing (ver `.gemini/docs/TESTING_STRATEGY.md`) el patrón: "Mocks globales vs per-test overrides" y ejemplos de `@TestConfiguration` + `@Import` para casos donde el test necesita el servicio real.
- Añadir un test de integración que verifique la carga de contexto mínima (smoke test) para detectar fallos de autoconfiguración rápidos (p.ej. `ContextLoadSmokeTest` que arranque el contexto con profile `test`).

Estado actual
- `DashboardControllerIntegrationTest` pasa localmente en mi entorno: BUILD SUCCESS.
- Cambios mínimos y localizados: añadidos 1 nuevo archivo de test-config, modificado el test y actualizado `application-test.properties`.

Próximos pasos sugeridos
1. Revisar otras clases de `GlobalTestConfiguration` para detectar servicios que no deberían estar globalmente mockeados (ej. servicios cuya lógica interna debe probarse en integración).
2. Añadir test(s) que validen el patrón de overrides (por ejemplo, test que valida que `MetabaseEmbeddingService` pueda inicializarse en profile `test`).
3. Documentar en `docs/INFORME_TESTS_FALLIDOS.md` (esta sección añadida) y en `.gemini/docs/TESTING_STRATEGY.md` (se agregó una nota) la decisión y ejemplo para futuros desarrolladores.

INFORME: fallo en test AdvisoryLockSerializationTest y solucion aplicada

Fecha: 2025-11-11
Autor: (automatizado) - cambios aplicados en repo de pruebas

Resumen:
- Test afectado: src/test/java/com/cambiaso/ioc/service/AdvisoryLockSerializationTest
- Sintoma: al cargar el contexto de Spring durante el test se producia una excepcion UnsatisfiedDependencyException que bloqueaba el arranque del ApplicationContext.
- Error visible en logs: fallo al crear el bean 'supabaseAuthService' por inyeccion de dependencias (falta de valores para @Value)

Diagnostico:
- La clase com.cambiaso.ioc.service.SupabaseAuthService tiene dos propiedades inyectadas con @Value:
  - supabase.url
  - supabase.service-role-key
- El test usa el perfil 'pgtest' (@ActiveProfiles("pgtest")), que carga src/test/resources/application-pgtest.properties.
- El fichero `application-pgtest.properties` no contenia las propiedades `supabase.url` y `supabase.service-role-key`, por lo que Spring no podia resolver esas @Value y el bean no se inicializaba.

Accion tomada (solucion aplicada):
- Se agregaron valores de prueba (mock) para Supabase al fichero:
  - src/test/resources/application-pgtest.properties
  Valores añadidos:
    supabase.url=http://localhost:54321
    supabase.service-role-key=test-service-role-key-for-testing-only
- Razon: proporcionar valores de prueba permite que el bean `SupabaseAuthService` sea construido por Spring y evita que la falta de configuracion externa bloquee los tests de integracion que no interactuan realmente con Supabase.

Archivos modificados:
- src/test/resources/application-pgtest.properties  (añadidas propiedades supabase.url y supabase.service-role-key)

Estado actual:
- El test `AdvisoryLockSerializationTest` pasa (verificacion manual por el desarrollador). El ApplicationContext se carga correctamente con el perfil 'pgtest'.

Recomendaciones y pasos siguientes:
1. Centralizar propiedades comunes de testing en `src/test/resources/application-test.properties` y mantener perfiles específicos (como pgtest) solo con diferencias necesarias.
2. Para tests que no necesitan el comportamiento de Supabase, considerar usar @MockBean en las clases de test para reemplazar `SupabaseAuthService` o crear una clase de configuracion de test que provea un bean 'stub' de SupabaseAuthService.
3. Añadir validacion automatica en la CI para detectar propiedades faltantes en perfiles de test (por ejemplo, un pequeño test que valida que todas las claves @Value necesarias esten presentes en los properties de test).
4. Documentar en la estrategia de testing (archivo .gemini/docs/TESTING_STRATEGY.md) la necesidad de proveer valores por defecto para servicios externos o mockearlos en los tests de integracion.

Notas:
- La solucion aplicada usa valores de prueba no secretos. No incluir claves reales en el repositorio.
- Si se prefiere no incluir esas propiedades en `application-pgtest.properties`, alternativa: anadir un bean de test que proporcione SupabaseAuthService como stub o usar @MockBean en los tests que cargan todo el contexto.

Fin del informe.
