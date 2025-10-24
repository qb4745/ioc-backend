package com.cambiaso.ioc.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.cambiaso.ioc.persistence.entity.*;
import com.cambiaso.ioc.persistence.repository.*;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(exclude = { HibernateJpaAutoConfiguration.class })
@ComponentScan(basePackages = {
        "com.cambiaso.ioc.service",
        "com.cambiaso.ioc.mapper"
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

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                       DataSource dataSource) {
        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.hbm2ddl.auto", "none");
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProps.put("hibernate.bytecode.provider", "none");
        jpaProps.put("hibernate.enhance.enableLazyInitialization", "false");
        jpaProps.put("hibernate.enhance.enableDirtyTracking", "false");
        jpaProps.put("hibernate.enhance.enableAssociationManagement", "false");

        return builder
                .dataSource(dataSource)
                .packages(
                        AppUser.class,
                        Role.class,
                        Permission.class,
                        UserRole.class,
                        RolePermission.class
                )
                .persistenceUnit("test")
                .properties(jpaProps)
                .build();
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
