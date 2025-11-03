package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@SuppressWarnings("SqlNoDataSourceInspection")
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    Optional<Permission> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    //noinspection SqlNoDataSourceInspection
    @SuppressWarnings("SqlNoDataSourceInspection")
    @Query(value = "SELECT * FROM permissions p WHERE CAST(:search AS TEXT) IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%'))",
           countQuery = "SELECT COUNT(*) FROM permissions p WHERE CAST(:search AS TEXT) IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%'))",
           nativeQuery = true)
    Page<Permission> search(@Param("search") String search, Pageable pageable);
}
