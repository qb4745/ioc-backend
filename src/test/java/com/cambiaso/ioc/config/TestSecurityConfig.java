package com.cambiaso.ioc.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http, OncePerRequestFilter jwtAuthoritiesAugmentor) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // Ensure the Jwt set by tests (MockMvc jwt()) gets enriched with ROLE_ authorities from claims
            .addFilterAfter(jwtAuthoritiesAugmentor, SecurityContextPersistenceFilter.class);
        return http.build();
    }
}
