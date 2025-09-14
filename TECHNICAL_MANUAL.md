# Manual Técnico de la Aplicación

Este documento sirve como la guía técnica central para el backend del proyecto "Inteligencia Operacional Cambiaso". Describe la arquitectura, configuraciones clave y flujos de trabajo implementados.

---

## 1. Módulo de Seguridad y Autenticación con Supabase

La seguridad de la API se basa en un modelo **Zero Trust**, donde ninguna petición es confiada por defecto. La autenticación es manejada exclusivamente por Supabase, que actúa como nuestro Proveedor de Identidad (IdP).

### 1.1. Principios de Arquitectura

*   **Stateless (Sin Estado):** El backend no mantiene sesiones de usuario. Cada petición debe incluir un JWT (JSON Web Token) que contiene toda la información necesaria para la autenticación y autorización.
*   **Proveedor de Identidad Externo:** Supabase es la única autoridad para crear usuarios, gestionar credenciales y emitir tokens. El backend solo valida estos tokens.
*   **Algoritmo de Firma Asimétrico:** Se utiliza el algoritmo **`ES256`** (criptografía de curva elíptica). Supabase firma los tokens con una clave privada secreta, y nuestro backend los verifica usando una clave pública que obtiene de Supabase de forma segura.

### 1.2. Flujo de Autenticación

1.  El cliente (frontend) inicia un proceso de login con Supabase usando email/password u otro método.
2.  Supabase valida las credenciales y emite un JWT firmado con `ES256`.
3.  El cliente almacena este JWT y lo envía en el encabezado `Authorization` de cada petición a nuestro backend (Ej: `Authorization: Bearer <jwt>`).
4.  El backend de Spring Boot intercepta la petición.
5.  Valida la firma y la caducidad del JWT usando la clave pública de Supabase.
6.  Si el token es válido, extrae la información del usuario (como el `sub` o ID de usuario) y permite que la petición continúe hacia el controlador.
7.  Si el token es inválido, caducado o no está presente, rechaza la petición con un error `401 Unauthorized`.

### 1.3. Configuración e Implementación

#### Dependencias (`pom.xml`)
Se agregó la dependencia `spring-boot-starter-oauth2-resource-server` para habilitar la funcionalidad de validación de JWT.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

#### Configuración de la Aplicación (`application.properties`)
La comunicación con Supabase se configura mediante una variable de entorno para mantener la portabilidad del código.

```properties
# La URI del emisor de tokens de Supabase
spring.security.oauth2.resourceserver.jwt.issuer-uri=${SUPABASE_ISSUER_URI}
```
*   **Variable de Entorno:** `SUPABASE_ISSUER_URI` debe ser configurada en el entorno de ejecución (ej. en IntelliJ, Docker, etc.) con el valor `https://<project-ref>.supabase.co/auth/v1`.

#### Configuración de Seguridad (`SecurityConfig.java`)
Esta clase es el núcleo de la configuración de seguridad:
*   Establece la política de sesiones como `STATELESS`.
*   Configura CORS para permitir peticiones desde el frontend.
*   Protege todos los endpoints por defecto, requiriendo autenticación.
*   **Lo más importante:** Define un `JwtDecoder` personalizado que especifica el algoritmo `ES256`, que es el que Supabase utiliza en nuestro proyecto.

```java
// src/main/java/com/cambiaso/ioc/security/SecurityConfig.java

@Bean
public JwtDecoder jwtDecoder() {
    String jwkSetUri = this.issuerUri + "/.well-known/jwks.json";

    return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
            .jwsAlgorithm(SignatureAlgorithm.ES256)
            .build();
}
```

#### Manejo de Excepciones (`GlobalExceptionHandler.java`)
Se creó un manejador global para interceptar excepciones de seguridad (`AuthenticationException`, `AccessDeniedException`) y devolver respuestas de error en un formato JSON limpio y consistente, en lugar del HTML por defecto de Spring.

#### Acceso a Datos del Usuario en Controladores
Para obtener el ID del usuario autenticado en un endpoint, se inyecta el objeto `Jwt` en el método del controlador.

```java
// src/main/java/com/cambiaso/ioc/user/controller/UserController.java

@GetMapping("/me")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<String> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getSubject(); // ID de Supabase del usuario
    // ... lógica para buscar el perfil del usuario en nuestra DB ...
    return ResponseEntity.ok("Your Supabase User ID is: " + userId);
}
```
