package com.cambiaso.ioc.controller.admin;

import com.cambiaso.ioc.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> assignRoleToUser(
            @PathVariable Long userId,
            @PathVariable Integer roleId,
            @RequestParam(value = "assignedBy", required = false) Long assignedBy) {
        assignmentService.assignRoleToUser(userId, roleId, assignedBy);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable Long userId, @PathVariable Integer roleId) {
        assignmentService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> assignPermissionToRole(@PathVariable Integer roleId, @PathVariable Integer permissionId) {
        assignmentService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> removePermissionFromRole(@PathVariable Integer roleId, @PathVariable Integer permissionId) {
        assignmentService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }
}

