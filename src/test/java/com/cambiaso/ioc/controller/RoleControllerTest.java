package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.controller.admin.RoleController;
import com.cambiaso.ioc.dto.response.RoleResponse;
import com.cambiaso.ioc.mapper.RoleMapper;
import com.cambiaso.ioc.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleController roleController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void search_ok_returns200WithEnrichedData() throws Exception {
        RoleResponse resp = new RoleResponse();
        resp.setId(1);
        resp.setName("ADMIN");
        resp.setUsersCount(10);
        resp.setPermissions(new java.util.ArrayList<>(List.of("READ", "WRITE")));

        Page<RoleResponse> page = new PageImpl<>(List.of(resp), PageRequest.of(0, 1), 1);
        Mockito.when(roleService.searchWithDetails(eq("adm"), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/roles").param("search", "adm"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("ADMIN"))
                .andExpect(jsonPath("$.content[0].usersCount").value(10))
                .andExpect(jsonPath("$.content[0].permissions[0]").value("READ"));
    }

    @Test
    void getById_ok_returnsEnrichedResponse() throws Exception {
        RoleResponse resp = new RoleResponse();
        resp.setId(5);
        resp.setName("OPERATOR");
        resp.setUsersCount(3);
        resp.setPermissions(new java.util.ArrayList<>(List.of("VIEW")));

        Mockito.when(roleService.getByIdWithDetails(5)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/admin/roles/5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("OPERATOR"))
                .andExpect(jsonPath("$.usersCount").value(3))
                .andExpect(jsonPath("$.permissions[0]").value("VIEW"));
    }
}
