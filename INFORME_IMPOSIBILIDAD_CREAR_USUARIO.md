# Informe: Imposibilidad de crear usuarios vía `/api/v1/admin/users`

Fecha: 2025-10-30

Resumen
-------
Este informe documenta por qué una llamada al endpoint administrativo `POST /api/v1/admin/users` devuelve 401 (Unauthorized) y qué se necesita en la aplicación Spring para que la creación automática de usuarios funcione correctamente.

Checklist
---------
- [x] Explicar por qué devuelve 401
- [x] Enumerar propiedades de Spring necesarias
- [x] Describir beans de seguridad relevantes (JwtDecoder, JwtAuthenticationConverter)
- [x] Indicar validaciones y payload esperado
- [x] Mostrar flujo para obtener token admin y probar la API
- [x] Recomendaciones y notas de seguridad

1) Por qué la llamada devuelve 401
---------------------------------
- El `AdminUserController` que expone `/api/v1/admin/users` está anotado con `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`. Solo un cliente con JWT válido que incluya la autoridad `ROLE_ADMIN` puede invocar este endpoint.
- `SecurityConfig` configura la app como OAuth2 Resource Server: valida JWTs contra el issuer configurado (`spring.security.oauth2.resourceserver.jwt.issuer-uri`) usando un `JwtDecoder` (JWKs). Si la petición no incluye `Authorization: Bearer <token>` con un token aceptado por el decodificador y con los claims apropiados, Spring Security responde 401.
- Por tanto, la petición desde Swagger sin un token válido se bloquea con 401 y el backend nunca ejecuta la lógica que crea el usuario en Supabase.

2) Propiedades de `application-*.properties` (mínimas) que deben existir
------------------------------------------------------------------------
Asegúrate de definir al menos las siguientes propiedades (ejemplos):

```properties
# JWT/OAuth2 Resource Server
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://<tu-proyecto>.supabase.co/auth/v1

# Integración con Supabase (creación automática de usuarios)
supabase.url=https://<tu-proyecto>.supabase.co
supabase.service-role-key=${SUPABASE_SERVICE_ROLE_KEY}
supabase.anon-key=${SUPABASE_ANON_KEY}

# Opcional: fijar dialecto para evitar problemas si la BD está inaccesible
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

- `SUPABASE_SERVICE_ROLE_KEY` debe inyectarse desde variables de entorno (no en el repositorio).

3) Beans y clases Spring relevantes (qué debe existir y qué esperan)
--------------------------------------------------------------------
- `JwtDecoder` bean: la app ya construye un `NimbusJwtDecoder` apuntando a `${issuer-uri}/.well-known/jwks.json` y configurado con `ES256` en este proyecto. Esto requiere que el issuer (Supabase auth v1) tenga disponible el JWK set en la URL correspondiente.

- `JwtAuthenticationConverter` (y `jwtGrantedAuthoritiesConverter`): la conversión de claims a autoridades en `SecurityConfig` busca:
  - `realm_access.roles` (estilo Keycloak) — extrae lista y la mapea a `ROLE_<rol>`.
  - claim `roles` como lista de strings — mapea a `ROLE_<rol>`.
  - además añade las authorities derivadas de scopes (default converter).

  Esto significa que el JWT debe incluir, preferiblemente, un claim `roles` (por ejemplo `"roles": ["ADMIN"]`) o que la metadata del issuer incluya `realm_access.roles` con `ADMIN` para que la autoridad `ROLE_ADMIN` aparezca en el `Authentication` de Spring.

- `UserAdminService` y `SupabaseAuthService`: `UserAdminService.create(...)` delega en `SupabaseAuthService.createSupabaseUser(email,password)` para crear la cuenta en Supabase. `SupabaseAuthService` usa `WebClient` y añade headers:
  - `apikey: <service-role-key>`
  - `Authorization: Bearer <service-role-key>`

  Por tanto, el backend necesita la variable `supabase.service-role-key` para realizar la creación vía API admin de Supabase.

4) Validaciones y modelo del request esperado
--------------------------------------------
El DTO `UsuarioCreateRequest` impone validaciones:
- `email` — `@NotBlank`, `@Email`, además hay validación personalizada `@UniqueEmail`.
- `password` — `@Size(min = 6)` (campo opcional en algunos flujos, pero necesario para la creación en Supabase si el backend debe pasar una password).
- `primerNombre` — `@NotBlank`
- `primerApellido` — `@NotBlank`
- `plantaId`, `centroCosto`, `fechaContrato`, `roles` — opcionales o con límites (`centroCosto` max 50)

Payload JSON de ejemplo (copiar a Swagger/postman):

```json
{
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
}
```

Notas:
- Si no proporcionas `password`, el backend podría intentar crear el usuario en Supabase con contraseña generada o fallar según la implementación; en este repo `UsuarioCreateRequest` incluye el campo `password` y `SupabaseAuthService` necesita un password para crear el usuario.

5) Flujo recomendado para probar y verificar (paso a paso)
---------------------------------------------------------
Requisitos previos: tener `SUPABASE_SERVICE_ROLE_KEY` y `SUPABASE_ANON_KEY` disponibles en tu entorno.

A) Crear un usuario admin en Supabase (usando service role key)

```bash
export SUPABASE_URL="https://<tu-proyecto>.supabase.co"
export SUPABASE_SERVICE_ROLE_KEY="eyJ...SERVICE_ROLE_KEY..."

curl -s -X POST "$SUPABASE_URL/auth/v1/admin/users" \
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

Respuesta esperada: JSON con `id` del usuario creado.

B) Obtener un access_token (grant_type=password) para ese admin

```bash
export SUPABASE_ANON_KEY="eyJ...ANON_KEY..."

curl -s -X POST "$SUPABASE_URL/auth/v1/token?grant_type=password" \
  -H "apikey: $SUPABASE_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{ "email": "admin@example.com", "password": "AdminP@ssw0rd!" }'
```

Respuesta esperada: JSON con `access_token`. Copiar `access_token`.

C) Llamar al backend protegido incluyendo el header Authorization

```bash
curl -i -X POST 'http://localhost:8080/api/v1/admin/users' \
  -H "Authorization: Bearer <ACCESS_TOKEN_AQUI>" \
  -H 'Content-Type: application/json' \
  -d '@payload.json'
```

- Si el token trae `roles: ["ADMIN"]` o el claim `realm_access.roles` incluye `ADMIN`, la conversión en `SecurityConfig` añadirá `ROLE_ADMIN` y la petición debería retornar `201 Created` con el recurso resultante.
- Si el token no incluye el claim `roles` ni `realm_access.roles`, aparecerá 403 o 401 según la validez del token.

6) Comprobaciones técnicas/depuración si sigue fallando
--------------------------------------------------------
- Verificar que `spring.security.oauth2.resourceserver.jwt.issuer-uri` coincide exactamente con el issuer del JWT (ej. `https://<tu-proyecto>.supabase.co/auth/v1`).
- Verificar que la app puede descargar `/.well-known/jwks.json` desde ese issuer (problemas de red, TLS o bloqueo de salida pueden impedir la validación del token).
- Log level: aumentar a DEBUG para `org.springframework.security` y `org.springframework.security.oauth2` para ver por qué falla la validación del token.
- Revisar el JWT con jwt.io o `jq` para comprobar qué claims trae (`roles`, `realm_access`, `exp`, `iss`).

7) Notas de seguridad
---------------------
- `SUPABASE_SERVICE_ROLE_KEY` es altamente privilegiada: no subirla al repo ni exponerla. Usar Secret Manager.
- Evitar crear usuarios admin programáticamente sin controles adicionales.
- Limitar el acceso a los endpoints administrativos (ej.: solo desde la red privada o con scopes adicionales).

8) Dependencias Maven (si necesitas confirmar)
---------------------------------------------
Si tu `pom.xml` no incluye ya estos starters, necesitarás al menos:

```xml
<!-- Resource server JWT support -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- WebClient -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

9) Resumen y acción inmediata
-----------------------------
- Causa del 401: falta de token con `ROLE_ADMIN` o token inválido.
- Acciones inmediatas: crear un usuario admin en Supabase (con `service_role_key`), obtener su `access_token` y llamar al backend con `Authorization: Bearer <token>`; confirmar que el JWT contiene `roles` o `realm_access.roles` con `ADMIN`.

Archivo creado:
- `INFORME_IMPOSIBILIDAD_CREAR_USUARIO.md` en la raíz del repo (puedes abrirlo para revisión).

¿Quieres que además haga alguna de las siguientes acciones ahora?
- 1) Crear un branch y commitear el informe.
- 2) Generar un payload/test de integración (JUnit) que pruebe la ruta con un token `MockJwt` que incluya `roles: ["ADMIN"]`.
- 3) Incluir un fragmento para validar en logs (configuración de logging DEBUG para Spring Security).

Indica la opción y la implemento.  
