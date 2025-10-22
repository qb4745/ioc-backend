package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.controller.admin.RoleController;
import com.cambiaso.ioc.dto.response.RoleResponse;
import com.cambiaso.ioc.mapper.RoleMapper;
import com.cambiaso.ioc.persistence.entity.Role;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void search_ok_returns200() throws Exception {
        Role r = new Role();
        r.setId(1);
        r.setName("ADMIN");
        Page<Role> page = new PageImpl<>(List.of(r));
        Mockito.when(roleService.search(eq("adm"), any(PageRequest.class))).thenReturn(page);
        Mockito.when(roleMapper.toResponse(any(Role.class))).thenReturn(new RoleResponse());

        mockMvc.perform(get("/api/admin/roles").param("search", "adm"))
                .andExpect(status().isOk());
    }
}
