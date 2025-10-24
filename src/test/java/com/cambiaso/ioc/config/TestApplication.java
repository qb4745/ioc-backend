package com.cambiaso.ioc.config;

import com.cambiaso.ioc.persistence.entity.AppUser;
import com.cambiaso.ioc.persistence.entity.Permission;
import com.cambiaso.ioc.persistence.entity.Role;
import com.cambiaso.ioc.persistence.entity.RolePermission;
import com.cambiaso.ioc.persistence.entity.UserRole;
import com.cambiaso.ioc.persistence.repository.AppUserRepository;
import com.cambiaso.ioc.persistence.repository.PermissionRepository;
import com.cambiaso.ioc.persistence.repository.RolePermissionRepository;
import com.cambiaso.ioc.persistence.repository.RoleRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * ⚠️ CONFIGURACIÓN MAESTRA DE TESTS ⚠️
 * Esta es la ÚNICA clase que debe definir @EnableJpaRepositories y @EntityScan
 * para tests de integración.
 * Carga:
 * - ✅ Services y Mappers (capa de negocio)
 * - ✅ Repositories y Entities (capa de persistencia)
 * NO carga:
 * - ❌ Controllers (capa web)
 * - ❌ Security (configuración de seguridad)
 * - ❌ Configuraciones de com.cambiaso.ioc.config (evita descubrir otras configs)
 *
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
    "com.cambiaso.ioc.service",
    "com.cambiaso.ioc.mapper"
    // Deliberadamente NO incluye "com.cambiaso.ioc.config" para evitar conflictos
})
@EntityScan(basePackageClasses = {
    AppUser.class,
    Role.class,
    Permission.class,
    UserRole.class,
    RolePermission.class
})
@EnableJpaRepositories(basePackageClasses = {
    RoleRepository.class,
    PermissionRepository.class,
    AppUserRepository.class,
    UserRoleRepository.class,
    RolePermissionRepository.class
})
public class TestApplication {
    // Configuración declarativa - no necesita código
}

