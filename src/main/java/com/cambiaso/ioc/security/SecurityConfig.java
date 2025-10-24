package com.cambiaso.ioc.security;

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
import org.springframework.security.core.Authentication;
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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public OncePerRequestFilter jwtAuthoritiesAugmentor(Converter<Jwt, Collection<GrantedAuthority>> jwtAuthoritiesConverter) {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
                Authentication authentication = context.getAuthentication();
                if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                    Jwt jwt = jwtAuth.getToken();
                    log.debug("jwtAuthoritiesAugmentor: before={}, claims roles={}, direct roles={}",
                            jwtAuth.getAuthorities(),
                            (jwt.getClaim("realm_access") instanceof java.util.Map ra) ? ra.get("roles") : null,
                            jwt.getClaimAsStringList("roles"));
                    Collection<GrantedAuthority> extracted = jwtAuthoritiesConverter.convert(jwt);
                    Set<GrantedAuthority> merged = new HashSet<>(jwtAuth.getAuthorities());
                    boolean changed = false;
                    if (extracted != null && !extracted.isEmpty()) {
                        changed = merged.addAll(extracted);
                    }
                    if (changed) {
                        // Preserve the name/principal and details
                        JwtAuthenticationToken replacement = new JwtAuthenticationToken(jwt, merged, jwtAuth.getName());
                        replacement.setDetails(jwtAuth.getDetails());
                        context.setAuthentication(replacement);
                        log.debug("jwtAuthoritiesAugmentor: after={} (added={})", merged, extracted);
                    } else {
                        log.debug("jwtAuthoritiesAugmentor: no changes to authorities");
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
    }

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
                        // Allow unauthenticated access to public endpoints if any (e.g., actuator health)
                        .requestMatchers("/public/**", "/actuator/health").permitAll()
                        // Require authentication for all other requests
                        .anyRequest().authenticated()
                )
                // Configure the app as an OAuth2 Resource Server to validate JWTs
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()).jwtAuthenticationConverter(jwtAuthenticationConverter())))
                // Ensure Jwt set by tests (MockMvc jwt()) gets enriched with ROLE_ authorities from claims
                .addFilterAfter(jwtAuthoritiesAugmentor(jwtGrantedAuthoritiesConverter()), SecurityContextPersistenceFilter.class)
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
        // Specify allowed origins (e.g., your frontend application's URL)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "https://ioc-frontend-git-chore-configmetabase-a-8166e5-qb4745s-projects.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Retry-After", "RateLimit-Limit", "RateLimit-Remaining", "RateLimit-Reset"));
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
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return converter;
    }

    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return jwt -> {
            // Use a Set to avoid duplicate authorities
            Set<GrantedAuthority> authorities = new HashSet<>();

            // 1) Preserve default scope conversion (SCOPE_... authorities)
            JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
            Collection<GrantedAuthority> scopeAuthorities = scopesConverter.convert(jwt);
            if (scopeAuthorities != null) authorities.addAll(scopeAuthorities);

            // Helper to normalize a role name to have a single ROLE_ prefix
            java.util.function.Function<String, String> normalizeRole = r -> r == null ? null : (r.startsWith("ROLE_") ? r : "ROLE_" + r);

            // 2) Extract roles from realm_access.roles (Keycloak-style)
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof Map) {
                Object rolesObj = ((Map<?, ?>) realmAccess).get("roles");
                if (rolesObj instanceof List) {
                    List<?> roles = (List<?>) rolesObj;
                    roles.stream()
                            .map(Object::toString)
                            .map(normalizeRole)
                            .filter(java.util.Objects::nonNull)
                            .map(SimpleGrantedAuthority::new)
                            .forEach(authorities::add);
                }
            }

            // 3) Extract roles from a simple "roles" claim if present
            List<String> directRoles = jwt.getClaimAsStringList("roles");
            if (directRoles != null) {
                directRoles.stream()
                        .map(normalizeRole)
                        .filter(java.util.Objects::nonNull)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }

            return authorities;
        };
    }
}
