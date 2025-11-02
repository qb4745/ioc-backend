package com.cambiaso.ioc.security;

import com.cambiaso.ioc.persistence.repository.AppUserRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${app.cors.allowed-origins:}")
    private String appCorsAllowedOrigins; // comma-separated patterns, optional

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection, as we are using stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)
                // Configure CORS to allow requests from the frontend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Set session management to stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Define authorization rules for endpoints
                .authorizeHttpRequests(authorize -> authorize
                        // Allow unauthenticated access to public endpoints and API docs
                        .requestMatchers(
                            "/public/**", 
                            "/actuator/health",
                            "/v3/api-docs/**",
                            "/swagger-ui.html",
                            "/swagger-ui/**"
                        ).permitAll()
                        // Require authentication for all other requests
                        .anyRequest().authenticated()
                )
                // Configure the app as an OAuth2 Resource Server to validate JWTs
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()).jwtAuthenticationConverter(jwtAuthenticationConverter())))
                // Add security headers for embedding protection
                .headers(headers -> headers
                    // Disables the default X-Frame-Options header which is DENY
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                    // Sets the Content-Security-Policy header to allow embedding only from the same origin
                    .contentSecurityPolicy(csp -> csp
                        .policyDirectives("frame-ancestors 'self' " +
                                "https://treated-paste-eos-memo.trycloudflare.com; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "default-src 'self'")
                    )
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Build origin patterns from property if provided; otherwise use safe defaults.
        List<String> originPatterns;
        if (appCorsAllowedOrigins != null && !appCorsAllowedOrigins.isBlank()) {
            originPatterns = List.of(appCorsAllowedOrigins.split("\s*,\s*"));
        } else {
            originPatterns = List.of(
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "https://*.vercel.app"
            );
        }

        // Use allowedOriginPatterns to accept wildcard preview domains (e.g. *.vercel.app)
        configuration.setAllowedOriginPatterns(originPatterns);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Retry-After", "RateLimit-Limit", "RateLimit-Remaining", "RateLimit-Reset", "Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Construct the JWK Set URI from the issuer URI
        String jwkSetUri = this.issuerUri + "/.well-known/jwks.json";

        // Create a NimbusJwtDecoder that is configured to use the ES256 algorithm
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.ES256)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this.jwtGrantedAuthoritiesConverter());
        return converter;
    }

    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();

            // 1) Extract roles from realm_access.roles (Keycloak-style)
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof Map) {
                Object rolesObj = ((Map<?, ?>) realmAccess).get("roles");
                if (rolesObj instanceof List) {
                    List<?> roles = (List<?>) rolesObj;
                    authorities.addAll(roles.stream()
                            .map(Object::toString)
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .collect(Collectors.toList()));
                }
            }

            // 2) Extract roles claim if present (simple list claim)
            List<String> directRoles = jwt.getClaimAsStringList("roles");
            if (directRoles != null) {
                authorities.addAll(directRoles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList()));
            }

            // 🔥 3) NUEVO: Enriquecer desde PostgreSQL (SOLUCIÓN PARA FREE TIER)
            String supabaseUserIdStr = jwt.getSubject(); // "sub" del JWT
            if (supabaseUserIdStr != null) {
                try {
                    java.util.UUID supabaseUserId = java.util.UUID.fromString(supabaseUserIdStr);

                    // Buscar usuario en la BD local
                    appUserRepository.findBySupabaseUserId(supabaseUserId).ifPresent(appUser -> {
                        // Obtener roles desde user_roles
                        List<String> dbRoles = userRoleRepository.findRoleNamesByUserId(appUser.getId());
                        dbRoles.forEach(roleName ->
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()))
                        );
                    });
                } catch (IllegalArgumentException e) {
                    // UUID inválido, ignorar
                }
            }

            // 4) Also include scope-based authorities (default converter) to support scopes
            JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
            Collection<GrantedAuthority> scopeAuthorities = scopesConverter.convert(jwt);
            if (scopeAuthorities != null) authorities.addAll(scopeAuthorities);

            return authorities;
        };
    }
}
