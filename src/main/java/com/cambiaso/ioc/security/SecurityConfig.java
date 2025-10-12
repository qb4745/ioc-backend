package com.cambiaso.ioc.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

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
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())))
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
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000", 
                "http://localhost:5173",
                "https://ioc-frontend-git-chore-configmetabase-a-8166e5-qb4745s-projects.vercel.app"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(Arrays.asList("Retry-After", "RateLimit-Limit", "RateLimit-Remaining", "RateLimit-Reset"));
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
}
