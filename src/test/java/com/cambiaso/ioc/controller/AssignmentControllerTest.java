package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.controller.admin.AssignmentController;
import com.cambiaso.ioc.service.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AssignmentService assignmentService;

    @BeforeEach
    void setup() {
        AssignmentController controller = new AssignmentController(assignmentService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void assignRoleToUser_returns200() throws Exception {
        doNothing().when(assignmentService).assignRoleToUser(1L, 2, null);
        mockMvc.perform(post("/api/admin/assignments/users/1/roles/2"))
                .andExpect(status().isOk());
    }

    @Test
    void removeRoleFromUser_returns204() throws Exception {
        doNothing().when(assignmentService).removeRoleFromUser(1L, 2);
        mockMvc.perform(delete("/api/admin/assignments/users/1/roles/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    void assignPermissionToRole_returns200() throws Exception {
        doNothing().when(assignmentService).assignPermissionToRole(3, 5);
        mockMvc.perform(post("/api/admin/assignments/roles/3/permissions/5"))
                .andExpect(status().isOk());
    }

    @Test
    void removePermissionFromRole_returns204() throws Exception {
        doNothing().when(assignmentService).removePermissionFromRole(3, 5);
        mockMvc.perform(delete("/api/admin/assignments/roles/3/permissions/5"))
                .andExpect(status().isNoContent());
    }
}
