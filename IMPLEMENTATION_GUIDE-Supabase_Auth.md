# Blueprint: Integrating Supabase Auth with Spring Boot

This document provides the definitive technical plan for securing our REST API. It follows a Zero Trust model where the backend **never trusts a request** and **always verifies** the token via Supabase.

## 1. Guiding Architectural Principles

*   **Stateless by Design:** Our backend will be completely stateless. All necessary authentication and authorization information will be contained within the JWT provided with each request. No server-side sessions will be used.
*   **Clear Separation of Concerns:** Supabase handles **Authentication** (who the user is). Our Spring Boot application handles **Authorization** (what the user is allowed to do).
*   **Single Source of Truth:** The Supabase project is the only authority for user identity and token issuance.

## 2. Dependency Management (`pom.xml`)

*   **Required Dependency:** To function as a stateless Resource Server, the `spring-boot-starter-oauth2-resource-server` dependency is mandatory.
*   **Action:** The `spring-boot-starter-security` dependency is already present, but `spring-boot-starter-oauth2-resource-server` is missing. Add the following snippet to your `pom.xml` inside the `<dependencies>` section.

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    ```

## 3. Application Configuration (`application.properties`)

*   **Recommended Method (JWK Set URI):** This is the most secure method as it doesn't require storing secrets in our backend. It dynamically fetches Supabase's public keys to verify the JWT signature. Add the following line to `src/main/resources/application.properties`.
    *   `spring.security.oauth2.resourceserver.jwt.issuer-uri`: `https://<project-ref>.supabase.co/auth/v1` (Replace `<project-ref>` with your actual Supabase project reference).
*   **Database Schema Rule:** Re-confirm that `spring.jpa.hibernate.ddl-auto` is set to `validate` or `none` for production environments to prevent accidental data loss.

## 4. Code Organization & Structure

*   **Action:** A dedicated `security` package must be created to encapsulate all security-related code.
*   **Path:** `com/cambiaso/ioc/security/`
*   **Contents:** This new package will house the `SecurityConfig` class and the `GlobalExceptionHandler`.

## 5. Core Implementation: Security Configuration

*   **Action:** Create the `SecurityConfig.java` class inside the `com.cambiaso.ioc.security` package.
*   **Annotations:** `@Configuration`, `@EnableWebSecurity`, `@EnableMethodSecurity(prePostEnabled = true)`
*   **SecurityFilterChain Bean:** This configuration establishes a stateless security policy, configures CORS, sets authorization rules, and enables JWT validation.

    ```java
    package com.cambiaso.ioc.security;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.CorsConfigurationSource;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

    import java.util.Arrays;

    @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity(prePostEnabled = true)
    public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                // Disable CSRF protection, as we are using stateless JWT authentication
                .csrf(csrf -> csrf.disable())
                // Configure CORS to allow requests from the frontend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Set session management to stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Define authorization rules for endpoints
                .authorizeHttpRequests(authorize -> authorize
                    // Allow unauthenticated access to public endpoints if any (e.g., actuator health)
                    .requestMatchers("/public/**", "/actuator/health").permitAll()
                    // Require authentication for all other requests
                    .anyRequest().authenticated()
                )
                // Configure the app as an OAuth2 Resource Server to validate JWTs
                .oauth2ResourceServer(oauth2 -> oauth2.jwt());

            return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            // Specify allowed origins (e.g., your frontend application's URL)
            configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
            configuration.setAllowCredentials(true);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
    }
    ```

## 6. Centralized Exception Handling

*   **Problem:** By default, Spring Security exceptions return non-standard HTML or verbose error messages, which is unsuitable for a REST API.
*   **Action:** Create a `GlobalExceptionHandler.java` class inside the `com.cambiaso.ioc.security` package to standardize API error responses.
*   **Implementation:** This class handles security-related exceptions and returns a clean, consistent JSON error response.

    ```java
    package com.cambiaso.ioc.security;

    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.AccessDeniedException;
    import org.springframework.security.core.AuthenticationException;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.annotation.RestControllerAdvice;
    import org.springframework.web.context.request.WebRequest;

    import java.time.LocalDateTime;
    import java.util.LinkedHashMap;
    import java.util.Map;

    @RestControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.UNAUTHORIZED.value());
            body.put("error", "Unauthorized");
            body.put("message", "Authentication failed: " + ex.getMessage());
            body.put("path", request.getDescription(false).replace("uri=", ""));

            return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.FORBIDDEN.value());
            body.put("error", "Forbidden");
            body.put("message", "You do not have permission to access this resource.");
            body.put("path", request.getDescription(false).replace("uri=", ""));

            return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
        }
    }
    ```

## 7. Securing Endpoints & Accessing User Data

*   **Endpoint Protection:** Use `@PreAuthorize` on your controller methods to enforce authorization rules.
    *   Example 1 (Any authenticated user): `@PreAuthorize("isAuthenticated()")`
    *   Example 2 (User with a specific role/scope): `@PreAuthorize("hasAuthority('SCOPE_read:messages')")` or `@PreAuthorize("hasRole('ADMIN')")` (Note: Supabase JWTs use scopes, not roles by default).
*   **Accessing the User:** Access the authenticated user's details directly in your controller methods by injecting the `Jwt` principal. The user's Supabase ID is available via the `getSubject()` method (`sub` claim).

    ```java
    package com.cambiaso.ioc.user.controller; // Assuming a controller package exists

    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.security.oauth2.jwt.Jwt;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    @RestController
    @RequestMapping("/api/users")
    public class UserController {

        @GetMapping("/me")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<String> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
            String userId = jwt.getSubject();
            // You can now use this userId to fetch user details from your database
            // Example: Optional<User> user = userRepository.findBySupabaseId(userId);
            return ResponseEntity.ok("Your Supabase User ID is: " + userId);
        }
    }
    ```

## 8. Verification & Testing Plan

*   **Action:** After implementing the changes, run the application and use `curl` to test the security configuration.
    1.  **Unauthorized Request:** This command should fail as it lacks an `Authorization` header.
        ```bash
        curl -v http://localhost:8080/api/users/me
        ```
        *   **Expected Result:** An **HTTP 401 Unauthorized** response with the JSON error format defined in `GlobalExceptionHandler`.

    2.  **Authorized Request:** Replace `<YOUR_SUPABASE_JWT>` with a valid JWT obtained from your Supabase project (e.g., after a user logs in via your frontend).
        ```bash
        curl -v -H "Authorization: Bearer <YOUR_SUPABASE_JWT>" http://localhost:8080/api/users/me
        ```
        *   **Expected Result:** An **HTTP 200 OK** response with the body: `Your Supabase User ID is: <user-id-from-token>`.
