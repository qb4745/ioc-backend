# Informe: Fix de Conexión y Creación Automática de Usuarios

Fecha: 2025-10-30

Autor: Equipo de desarrollo (generado automáticamente)

Resumen ejecutivo
-----------------
Se detectó un fallo que impedía el arranque de la aplicación y también afectaba la capacidad de creación automática de usuarios en Supabase. La aplicación fallaba con errores de conexión a la base de datos y con errores derivados de la ausencia de configuración de dialecto de Hibernate. Además, la creación automática de usuarios está protegida por mecanismos de autenticación/autoridad: el endpoint `/api/v1/admin/users` requiere un JWT con `ROLE_ADMIN`.

Objetivo del informe
--------------------
Documentar el problema, su causa raíz, las acciones correctivas aplicadas, pasos de verificación y las recomendaciones/seguimiento para evitar regressiones y asegurar un flujo seguro de creación de usuarios.

Checklist (resumen de entregables)
---------------------------------
- [x] Diagnóstico del problema
- [x] Causa raíz identificada
- [x] Cambios aplicados documentados (configuración y script de entorno)
- [x] Guía para obtener un token admin y probar el endpoint protegido
- [x] Notas de seguridad y siguientes pasos

Detalles del problema
---------------------
Síntomas observados:
- Excepción inicial: `java.net.UnknownHostException: aws-1-sa-east-1.pooler.supabase.com` al arrancar.
- Error secundario: `Unable to determine Dialect without JDBC metadata` (Hibernate no podía determinar el dialecto porque la DB estaba inalcanzable).
- Petición POST a `/api/v1/admin/users` desde Swagger devolvía 401 Unauthorized (sin creación de usuario).

Archivos y lugares afectados
----------------------------
- `src/main/resources/application.properties` (se ajustó para fijar el dialecto de Hibernate)
- `env-setup.sh` (nuevo, script de ayuda para exportar variables de entorno)
- Lógica de creación de usuario: `SupabaseAuthService` (usa `supabase.service-role-key` internamente para crear usuarios en Supabase)
- Seguridad: `SecurityConfig` y `AdminUserController` (endpoints admin protegidos por JWT y por `ROLE_ADMIN`)

Causa raíz
----------
1. Variables de entorno críticas no estaban definidas en el entorno de ejecución (p. ej. `SUPABASE_DB_PASSWORD`, `SUPABASE_SERVICE_ROLE_KEY`). Esto provocó que la aplicación no se pudiera conectar a la base de datos.
2. La propiedad de dialecto de Hibernate estaba comentada en la configuración base, lo que llevó a que Hibernate intentara inferir el dialecto desde la metadata JDBC — fallando cuando la DB era inaccesible.
3. El endpoint de administración para crear usuarios requiere autorización con JWT que contenga la autoridad `ROLE_ADMIN`. La petición desde Swagger no incluía un token válido por lo que el backend devolvió 401 y no ejecutó la lógica de creación.

Acciones aplicadas
------------------
1. Se agregó/descendió el dialecto de Hibernate a `application.properties`:

```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

2. Se creó `env-setup.sh` (script de ayuda) para facilitar la exportación de variables de entorno necesarias:

- `SUPABASE_DB_PASSWORD`
- `SUPABASE_SERVICE_ROLE_KEY` (service role - **no** compartir)
- `SUPABASE_URL`

3. Se documentó el flujo de creación de usuarios y la dependencia en el `SUPABASE_SERVICE_ROLE_KEY` en `CONNECTION_FIX.md` y `USER_CREATION_FIX.md`.

Verificación realizada
----------------------
- Comprobación de que `application.properties` contiene el dialecto explícito para evitar la dependencia de metadatos JDBC.
- Confirmación de que `SecurityConfig` requiere JWTs emitidos por el issuer configurado (`spring.security.oauth2.resourceserver.jwt.issuer-uri`) y que `AdminUserController` requiere `ROLE_ADMIN`.
- Prueba desde Swagger: la petición POST sin token devolvió 401 (comportamiento esperado). No se creó el usuario.

Cómo reproducir (local)
-----------------------
1. Asegúrate de exportar las variables de entorno necesarias. Ejemplo (rellenar con tus valores reales):

```bash
# Bash
export SUPABASE_DB_PASSWORD="your-db-password"
export SUPABASE_SERVICE_ROLE_KEY="eyJ...<SERVICE_ROLE_KEY>..."
export SUPABASE_URL="https://<tu-proyecto>.supabase.co"
# Opcional: clave anon si la necesitas para pruebas de login
export SUPABASE_ANON_KEY="eyJ...<ANON_KEY>..."
```

2. Levanta la aplicación con el perfil local:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

3. Desde Swagger intenta crear un usuario en `/api/v1/admin/users`. Si no añades un header `Authorization: Bearer <token>` obtendrás 401.

Cómo obtener un token admin (guía rápida)
----------------------------------------
A continuación se detalla una secuencia para crear un usuario administrador en Supabase (usando la Admin API con `service_role`), obtener su access token y usarlo contra la API del backend.

1) Crear usuario admin en Supabase (ADMIN role en metadata)

```bash
# Rellenar SUPABASE_URL y SUPABASE_SERVICE_ROLE_KEY antes
curl -X POST "$SUPABASE_URL/auth/v1/admin/users" \
  -H "apikey: $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Authorization: Bearer $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "AdminP@ssw0rd!",
    "email_confirm": true,
    "app_metadata": {"role": "ADMIN", "roles": ["ADMIN"]}
  }'
```

La respuesta debería incluir `id` del usuario creado.

2) Obtener access token con grant_type=password

```bash
curl -X POST "$SUPABASE_URL/auth/v1/token?grant_type=password" \
  -H "apikey: $SUPABASE_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{ "email": "admin@example.com", "password": "AdminP@ssw0rd!" }'
```

Copiar el campo `access_token` del JSON de respuesta.

3) Llamar al endpoint protegido del backend usando el token

```bash
curl -X POST 'http://localhost:8080/api/v1/admin/users' \
  -H "Authorization: Bearer <ACCESS_TOKEN_AQUI>" \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "test.user+1@example.com",
    "password": "Str0ngP@ssw0rd!",
    "primerNombre": "Test",
    "segundoNombre": "Usuario",
    "primerApellido": "Prueba",
    "segundoApellido": "Ejemplo",
    "plantaId": 1,
    "centroCosto": "CC-123",
    "fechaContrato": "2025-10-30",
    "roles": ["USER"]
  }'
```

Si el token contiene la autoridad `ROLE_ADMIN` (según la conversión de claims definida en `SecurityConfig`), la petición debería devolver 201 Created y el backend ejecutará la creación del usuario en Supabase.

Notas importantes de seguridad
-----------------------------
- `SUPABASE_SERVICE_ROLE_KEY` es sensible y tiene privilegios administrativos. NUNCA lo publiques ni lo subas al repositorio.
- En entornos de producción usa un gestor de secretos (Render, AWS Secrets Manager, Vault, etc.) y no variables de entorno en texto plano.
- El backend confía en los claims del JWT para mapear roles. Asegúrate de que la configuración de Supabase (o de cualquier issuer) incluya los claims `roles` o `realm_access.roles` si quieres aprovechar directamente `ROLE_<role>`.

Recomendaciones y siguientes pasos
---------------------------------
1. Añadir tests de integración que validen el comportamiento del endpoint admin cuando el JWT tiene/ carece de `ROLE_ADMIN`.
2. Añadir un script o documentación de CI/CD para inyectar `SUPABASE_SERVICE_ROLE_KEY` de forma segura en entornos de staging/production.
3. Revisar la política de app_metadata en Supabase para normalizar cómo se exponen roles en el JWT.
4. Considerar un endpoint de administración interno (solo accesible desde la red privada) para tareas que requieren service-role-key, reduciendo la necesidad de tokens admin de usuario.

Anexos: referencias rápidas
-------------------------
- `CONNECTION_FIX.md` — instrucciones y script de entorno (ya incluido en el repo)
- `USER_CREATION_FIX.md` — documentación complementaria sobre creación automática de usuarios
- `SecurityConfig.java` — conversión de claims → authorities (usa `realm_access.roles` y `roles` claim)
- `SupabaseAuthService.java` — integración con Supabase Admin API para crear usuarios

Fin del informe

