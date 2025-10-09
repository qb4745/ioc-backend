package com.cambiaso.ioc.service;

import com.cambiaso.ioc.config.MetabaseProperties;
import com.cambiaso.ioc.exception.DashboardAccessDeniedException;
import com.cambiaso.ioc.exception.DashboardNotFoundException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MetabaseEmbeddingService Unit Tests")
class MetabaseEmbeddingServiceTest {

    @Mock
    private DashboardAuditService auditService;

    @Mock
    private Authentication authentication;

    private MetabaseEmbeddingService service;
    private MetabaseProperties properties;

    @BeforeEach
    void setUp() {
        properties = new MetabaseProperties();
        properties.setSiteUrl("http://localhost:3000");
        // Usar una clave hexadecimal válida de 64 caracteres
        properties.setSecretKey("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");
        properties.setTokenExpirationMinutes(10);
        
        MetabaseProperties.DashboardConfig dashboard = new MetabaseProperties.DashboardConfig();
        dashboard.setId(5);
        dashboard.setName("Test Dashboard");
        // Aunque la lógica de roles está desactivada, la configuración debe estar presente
        dashboard.setAllowedRoles(Set.of("ROLE_ADMIN", "ROLE_USER"));
        
        properties.setDashboards(List.of(dashboard));
        
        service = new MetabaseEmbeddingService(
            properties, 
            auditService, 
            new SimpleMeterRegistry()
        );
    }

    @Test
    @DisplayName("Should generate signed URL for any authenticated user (roles disabled)")
    void shouldGenerateSignedUrlForAuthenticatedUser() {
        // Given: Un usuario autenticado (sin roles específicos)
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);

        // When
        String url = service.getSignedDashboardUrl(5, authentication);

        // Then
        assertThat(url).isNotNull();
        assertThat(url).startsWith("http://localhost:3000/embed/dashboard/");
        
        // Verifica que se audita el acceso como concedido
        verify(auditService).logDashboardAccess("testuser", 5, "Test Dashboard", true);
    }

    @Test
    @DisplayName("Should throw DashboardAccessDeniedException for unauthenticated user")
    void shouldThrowExceptionForUnauthenticatedUser() {
        // Given: Un usuario no autenticado
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> service.getSignedDashboardUrl(5, authentication))
            .isInstanceOf(DashboardAccessDeniedException.class)
            .hasMessage("You must be authenticated to view this dashboard.");
            
        // Verifica que se audita el acceso como denegado con el usuario "unknown"
        verify(auditService).logDashboardAccess("unknown", 5, "UNKNOWN", false);
    }

    @Test
    @DisplayName("Should throw DashboardNotFoundException for non-existent dashboard")
    void shouldThrowExceptionForNonExistentDashboard() {
        // Given: Un usuario autenticado (no necesitamos mockearlo para este camino de error)
        
        // When & Then
        assertThatThrownBy(() -> service.getSignedDashboardUrl(999, authentication))
            .isInstanceOf(DashboardNotFoundException.class)
            .hasMessageContaining("Dashboard with ID 999 is not configured");
    }

    @Test
    @DisplayName("Should throw IllegalStateException at startup if secret key is too short")
    void shouldValidateSecretKeyAtStartup_TooShort() {
        // Given
        MetabaseProperties invalidProps = new MetabaseProperties();
        invalidProps.setSecretKey("tooshort");

        // When & Then
        assertThatThrownBy(() -> new MetabaseEmbeddingService(invalidProps, auditService, new SimpleMeterRegistry()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("too short");
    }
    
    @Test
    @DisplayName("Should throw IllegalStateException at startup if secret key is not hex")
    void shouldValidateSecretKeyAtStartup_NotHex() {
        // Given
        MetabaseProperties invalidProps = new MetabaseProperties();
        // Clave de 64 caracteres pero con un carácter no hexadecimal ('g')
        invalidProps.setSecretKey("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdeg");

        // When & Then
        assertThatThrownBy(() -> new MetabaseEmbeddingService(invalidProps, auditService, new SimpleMeterRegistry()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("must be hexadecimal");
    }
}
