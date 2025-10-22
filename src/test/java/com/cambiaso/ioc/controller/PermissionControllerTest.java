package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.controller.admin.PermissionController;
import com.cambiaso.ioc.dto.response.PermissionResponse;
import com.cambiaso.ioc.mapper.PermissionMapper;
import com.cambiaso.ioc.persistence.entity.Permission;
import com.cambiaso.ioc.service.PermissionService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PermissionService permissionService;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionController permissionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(permissionController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void search_ok_returns200() throws Exception {
        Permission p = new Permission();
        p.setId(1);
        p.setName("READ");
        Page<Permission> page = new PageImpl<>(List.of(p));
        Mockito.when(permissionService.search(eq("read"), any(PageRequest.class))).thenReturn(page);
        Mockito.when(permissionMapper.toResponse(any(Permission.class))).thenReturn(new PermissionResponse());

        mockMvc.perform(get("/api/admin/permissions").param("search", "read"))
                .andExpect(status().isOk());
    }
}
