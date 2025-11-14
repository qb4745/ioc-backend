package com.cambiaso.ioc.config;

import com.cambiaso.ioc.exception.DashboardNotFoundException;
import com.cambiaso.ioc.service.NotificationService;
import com.cambiaso.ioc.service.MetabaseEmbeddingService;
import com.cambiaso.ioc.service.SupabaseAuthService;
import com.cambiaso.ioc.service.DashboardAccessService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import org.mockito.Mockito;

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

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        // Mock para evitar que el JwtDecoder real intente resolver JWKS en tests
        return mock(JwtDecoder.class);
    }

    @Bean
    @Primary
    public MetabaseEmbeddingService metabaseEmbeddingServiceMock() {
        // Mock that simulates the behavior of the real service
        MetabaseEmbeddingService svc = mock(MetabaseEmbeddingService.class);
        Mockito.when(svc.getSignedDashboardUrl(Mockito.anyInt(), Mockito.any()))
            .thenAnswer(invocation -> {
                Integer dashboardId = invocation.getArgument(0);
                if (dashboardId != null && dashboardId >= 1 && dashboardId <= 10) {
                    return String.format("http://localhost:3000/embed/dashboard/%s#bordered=true&titled=true", dashboardId);
                } else {
                    throw new DashboardNotFoundException(
                        String.format("Dashboard with ID %d is not configured or does not exist.", dashboardId)
                    );
                }
            });
        return svc;
    }

    @Bean
    @Primary
    public SupabaseAuthService supabaseAuthService() {
        // Mock global de SupabaseAuthService para evitar llamadas externas en tests
        return mock(SupabaseAuthService.class);
    }

    @Bean
    @Primary
    public DashboardAccessService dashboardAccessService() {
        // Provide a mock DashboardAccessService for tests to avoid instantiating
        // the real implementation (which depends on MeterRegistry and other beans)
        DashboardAccessService mock = mock(DashboardAccessService.class);
        // By default allow access in tests
        doNothing().when(mock).checkAccessOrThrow(Mockito.any(), Mockito.anyInt());
        Mockito.when(mock.canAccess(Mockito.any(), Mockito.anyInt())).thenReturn(true);
        return mock;
    }

}
