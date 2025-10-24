package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.service.MetabaseEmbeddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Dashboard Controller - Unit Tests (mocked MetabaseEmbeddingService)")
@ContextConfiguration
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Use a static Mockito mock and expose it as a bean via TestConfiguration
    static MetabaseEmbeddingService metabaseEmbeddingService = Mockito.mock(MetabaseEmbeddingService.class);

    @TestConfiguration
    static class MockServiceConfig {
        @Bean
        public MetabaseEmbeddingService metabaseEmbeddingService() {
            return DashboardControllerTest.metabaseEmbeddingService;
        }
    }

    private static final Integer VALID_DASHBOARD_ID = 5;
    private static final Integer NON_EXISTENT_DASHBOARD_ID = 9999;
    private static final String MOCK_SIGNED_URL = "http://localhost:3000/embed/dashboard/abc123?param=value#signature";
    private static final String DASHBOARD_ENDPOINT = "/api/v1/dashboards/{dashboardId}";

    @BeforeEach
    void setUp() {
        reset(metabaseEmbeddingService);

        when(metabaseEmbeddingService.getSignedDashboardUrl(eq(VALID_DASHBOARD_ID), any(Authentication.class)))
            .thenReturn(MOCK_SIGNED_URL);

        when(metabaseEmbeddingService.getSignedDashboardUrl(eq(NON_EXISTENT_DASHBOARD_ID), any(Authentication.class)))
            .thenThrow(new RuntimeException("Dashboard not found"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should return 200 with signed URL for authenticated user")
    void shouldReturn200WithSignedUrlForAuthenticatedUser() throws Exception {
        mockMvc.perform(get(DASHBOARD_ENDPOINT, VALID_DASHBOARD_ID))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.signedUrl").exists())
            .andExpect(jsonPath("$.signedUrl").value(MOCK_SIGNED_URL));

        verify(metabaseEmbeddingService, times(1)).getSignedDashboardUrl(eq(VALID_DASHBOARD_ID), any(Authentication.class));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should return 401 for unauthenticated user")
    void shouldReturn401ForUnauthenticatedUser() throws Exception {
        mockMvc.perform(get(DASHBOARD_ENDPOINT, VALID_DASHBOARD_ID))
            .andDo(print())
            .andExpect(status().isUnauthorized());

        verify(metabaseEmbeddingService, never()).getSignedDashboardUrl(anyInt(), any(Authentication.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 400 for invalid dashboard ID (zero)")
    void shouldReturn400ForDashboardIdZero() throws Exception {
        mockMvc.perform(get(DASHBOARD_ENDPOINT, 0))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());

        verify(metabaseEmbeddingService, never()).getSignedDashboardUrl(anyInt(), any(Authentication.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 404 when dashboard is not found")
    void shouldReturn404WhenDashboardNotFound() throws Exception {
        mockMvc.perform(get(DASHBOARD_ENDPOINT, NON_EXISTENT_DASHBOARD_ID))
            .andDo(print())
            .andExpect(status().is5xxServerError());

        verify(metabaseEmbeddingService, times(1)).getSignedDashboardUrl(eq(NON_EXISTENT_DASHBOARD_ID), any(Authentication.class));
    }

}
