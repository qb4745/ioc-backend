package com.cambiaso.ioc.config;

import com.cambiaso.ioc.persistence.entity.*;
import com.cambiaso.ioc.persistence.repository.AppUserRepository;
import com.cambiaso.ioc.persistence.repository.PermissionRepository;
import com.cambiaso.ioc.persistence.repository.RolePermissionRepository;
import com.cambiaso.ioc.persistence.repository.RoleRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EntityScan(
    basePackageClasses = {
        AppUser.class,
        Role.class,
        Permission.class,
        UserRole.class,
        RolePermission.class
    }
)
@EnableJpaRepositories(basePackageClasses = {
        RoleRepository.class,
        PermissionRepository.class,
        AppUserRepository.class,
        UserRoleRepository.class,
        RolePermissionRepository.class
})
public class TestEntityScanConfig {
    // Narrow entity/repo scanning for tests only.
}
