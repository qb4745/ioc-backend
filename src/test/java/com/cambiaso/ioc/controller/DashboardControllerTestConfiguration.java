package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.service.NotificationService;
import com.cambiaso.ioc.service.SupabaseAuthService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.Mockito.mock;

/**
 * Configuración específica para DashboardControllerIntegrationTest.
 * NO mockea MetabaseEmbeddingService para permitir que el test use el servicio real.
 */
@TestConfiguration
public class DashboardControllerTestConfiguration {

    @Bean
    @Primary
    public SimpMessagingTemplate simpMessagingTemplate() {
        return mock(SimpMessagingTemplate.class);
    }

    @Bean
    @Primary
    public NotificationService notificationService() {
        return mock(NotificationService.class);
    }

    @Bean
    @Primary
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }

    @Bean
    @Primary
    public SupabaseAuthService supabaseAuthService() {
        return mock(SupabaseAuthService.class);
    }
}
