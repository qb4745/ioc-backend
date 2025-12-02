package com.cambiaso.ioc.integration;
import com.cambiaso.ioc.AbstractIntegrationTest;
import com.cambiaso.ioc.dto.request.PermissionRequest;
import com.cambiaso.ioc.dto.request.RoleRequest;
import com.cambiaso.ioc.dto.request.UsuarioCreateRequest;
import com.cambiaso.ioc.dto.response.UsuarioResponse;
import com.cambiaso.ioc.exception.ResourceConflictException;
import com.cambiaso.ioc.persistence.entity.Permission;
import com.cambiaso.ioc.persistence.entity.Role;
import com.cambiaso.ioc.service.AssignmentService;
import com.cambiaso.ioc.service.PermissionService;
import com.cambiaso.ioc.service.RoleService;
import com.cambiaso.ioc.service.UserAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for role management using H2 in-memory database.
 * Spring JPA creates schema automatically from entities.
 */

class RoleManagementIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserAdminService userAdminService;

    @Autowired
    private AssignmentService assignmentService;

    // Mock external services not needed for this test


    @Test
    void shouldCreateRoleAndPermission() {
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setName("INTEGRATION_ROLE");
        roleRequest.setDescription("Role created in integration test");
        PermissionRequest permRequest = new PermissionRequest();
        permRequest.setName("READ_DATA");
        permRequest.setDescription("Permission to read data");
        Role role = roleService.create(roleRequest);
        Permission permission = permissionService.create(permRequest);
        assertThat(role).isNotNull();
        assertThat(role.getId()).isNotNull();
        assertThat(role.getName()).isEqualTo("INTEGRATION_ROLE");
        assertThat(permission).isNotNull();
        assertThat(permission.getId()).isNotNull();
        assertThat(permission.getName()).isEqualTo("READ_DATA");
    }
    @Test
    void shouldAssignRoleToUserAndPermissionToRole() {
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setName("USER_ROLE");
        roleRequest.setDescription("User role");
        Role role = roleService.create(roleRequest);
        PermissionRequest permRequest = new PermissionRequest();
        permRequest.setName("WRITE_DATA");
        permRequest.setDescription("Write permission");
        Permission permission = permissionService.create(permRequest);
        UsuarioCreateRequest userRequest = new UsuarioCreateRequest();
        userRequest.setSupabaseUserId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        userRequest.setEmail("integration@test.com");
        userRequest.setPrimerNombre("Integration");
        userRequest.setPrimerApellido("User");
        UsuarioResponse user = userAdminService.create(userRequest);
        assignmentService.assignRoleToUser(user.getId(), role.getId(), 1L);
        assignmentService.assignPermissionToRole(role.getId(), permission.getId());
        assignmentService.assignRoleToUser(user.getId(), role.getId(), 1L);
        assignmentService.assignPermissionToRole(role.getId(), permission.getId());
        assertThatThrownBy(() -> roleService.delete(role.getId()))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("in use");
    }
    @Test
    void shouldPreventDeletionOfRoleInUse() {
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setName("LOCKED_ROLE");
        roleRequest.setDescription("Role that will be locked by assignment");
        Role role = roleService.create(roleRequest);
        UsuarioCreateRequest userRequest = new UsuarioCreateRequest();
        userRequest.setSupabaseUserId(UUID.fromString("223e4567-e89b-12d3-a456-426614174001"));
        userRequest.setEmail("locked@test.com");
        userRequest.setPrimerNombre("Locked");
        userRequest.setPrimerApellido("User");
        UsuarioResponse user = userAdminService.create(userRequest);
        assignmentService.assignRoleToUser(user.getId(), role.getId(), 1L);
        assertThatThrownBy(() -> roleService.delete(role.getId()))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("in use");
    }
    @Test
    void shouldAllowDeletionAfterRemovingAssignments() {
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setName("TEMP_ROLE");
        roleRequest.setDescription("Temporary role");
        Role role = roleService.create(roleRequest);
        UsuarioCreateRequest userRequest = new UsuarioCreateRequest();
        userRequest.setSupabaseUserId(UUID.fromString("323e4567-e89b-12d3-a456-426614174002"));
        userRequest.setEmail("temp@test.com");
        userRequest.setPrimerNombre("Temp");
        userRequest.setPrimerApellido("User");
        UsuarioResponse user = userAdminService.create(userRequest);
        assignmentService.assignRoleToUser(user.getId(), role.getId(), 1L);
        assignmentService.removeRoleFromUser(user.getId(), role.getId());
        roleService.delete(role.getId());
        assertThatThrownBy(() -> roleService.getById(role.getId()))
                .hasMessageContaining("not found");
    }
    @Test
    void shouldEnforceUniqueConstraints() {
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setName("UNIQUE_ROLE");
        roleRequest.setDescription("First role");
        roleService.create(roleRequest);
        RoleRequest duplicateRequest = new RoleRequest();
        duplicateRequest.setName("unique_role");
        duplicateRequest.setDescription("Duplicate role");
        assertThatThrownBy(() -> roleService.create(duplicateRequest))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("already exists");
    }
}
