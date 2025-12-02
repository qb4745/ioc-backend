package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.AbstractIntegrationTest;
import com.cambiaso.ioc.dto.request.UsuarioCreateRequest;
import com.cambiaso.ioc.dto.request.UsuarioUpdateRequest;
import com.cambiaso.ioc.dto.response.UsuarioResponse;
import com.cambiaso.ioc.service.SupabaseAuthService;
import com.cambiaso.ioc.service.UserAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AdminUserControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserAdminService userAdminService;

    @MockBean
    private SupabaseAuthService supabaseAuthService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_returns200() throws Exception {
        UsuarioResponse u = new UsuarioResponse();
        u.setId(1);
        when(userAdminService.search(any(), any(), any(), any()))
            .thenReturn(new PageImpl<>(List.of(u), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_returns200() throws Exception {
        UsuarioResponse u = new UsuarioResponse();
        u.setId(2);
        when(userAdminService.getById(2L)).thenReturn(u);

        mockMvc.perform(get("/api/v1/admin/users/2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200() throws Exception {
        UsuarioCreateRequest req = new UsuarioCreateRequest();
        req.setEmail("a@b.com");
        req.setPrimerNombre("X");
        req.setPrimerApellido("Y");
        req.setPassword("temporal123");  // Nuevo flujo con password

        // Mock del servicio Supabase para que no falle
        when(supabaseAuthService.createSupabaseUser(any(), any()))
            .thenReturn(UUID.randomUUID());

        UsuarioResponse resp = new UsuarioResponse();
        resp.setId(3);
        when(userAdminService.create(any(UsuarioCreateRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/admin/users")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());  // 201 Created es el código correcto
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_returns200() throws Exception {
        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setPrimerNombre("N");
        req.setPrimerApellido("P");
        UsuarioResponse resp = new UsuarioResponse();
        resp.setId(4);
        when(userAdminService.update(eq(4L), any(UsuarioUpdateRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/api/v1/admin/users/4")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_returns204() throws Exception {
        doNothing().when(userAdminService).delete(5L);

        mockMvc.perform(delete("/api/v1/admin/users/5"))
                .andExpect(status().isNoContent());
    }
}
