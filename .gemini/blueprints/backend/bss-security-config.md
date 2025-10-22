# Backend Service Specification: Security Config (JWT → Authorities)

## Metadata
- ID: BSS-IOC-004-22
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `SecurityConfig`
- Tipo: Config
- Package: `com.cambiaso.ioc.config`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Configurar Spring Security para autenticar con JWT (Supabase) y mapear claims/roles a GrantedAuthorities (`ROLE_*`).

---

## 2. Puntos clave
- Resource Server (JWT) con la clave pública de Supabase.
- `JwtAuthenticationConverter` que transforma claims a authorities.
- Endpoints admin (`/api/v1/admin/**`) requieren `ROLE_ADMIN`.

---

## 3. Esqueleto propuesto

```java
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(reg -> reg
        .requestMatchers("/actuator/**").permitAll()
        .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
        .anyRequest().authenticated()
      )
      .oauth2ResourceServer(oauth -> oauth
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
      );
    return http.build();
  }

  @Bean
  Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter() {
    return jwt -> {
      Collection<GrantedAuthority> authorities = new ArrayList<>();
      // Map from custom claim (e.g., roles) to ROLE_*
      List<String> roles = jwt.getClaimAsStringList("roles");
      if (roles != null) {
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r.toUpperCase())));
      }
      return new JwtAuthenticationToken(jwt, authorities);
    };
  }
}
```

---

## 4. Tests
- JWT sin roles → 403 en `/api/v1/admin/**`
- JWT con role ADMIN → 200 en endpoints admin

