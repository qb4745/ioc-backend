package com.cambiaso.ioc.config;

import com.cambiaso.ioc.service.NotificationService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.Mockito.mock;

/**
 * Configuración global de mocks para todos los tests.
 * 
 * Provee:
 * - Mock de SimpMessagingTemplate (WebSocket)
 * - Mock de NotificationService
 * - MeterRegistry funcional para métricas en tests
 * - Mock JwtDecoder para evitar llamadas a JWKS en tests
 */
@TestConfiguration
public class GlobalTestConfiguration {

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

    /**
     * MeterRegistry para tests.
     * Usa SimpleMeterRegistry para que las métricas funcionen sin necesitar backend.
     */
    @Bean
    @Primary
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

}

