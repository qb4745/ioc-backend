package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@SuppressWarnings("SqlNoDataSourceInspection")
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    // IntelliJ sometimes warns "No data sources are configured to run this SQL" for
    // native queries. Add both inline inspection suppression and @SuppressWarnings to silence the inspection.
    //noinspection SqlNoDataSourceInspection
    @SuppressWarnings("SqlNoDataSourceInspection")
    @Query(value = "SELECT * FROM roles r WHERE CAST(:search AS TEXT) IS NULL OR :search = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%'))",
           countQuery = "SELECT COUNT(*) FROM roles r WHERE CAST(:search AS TEXT) IS NULL OR :search = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%'))",
           nativeQuery = true)
    Page<Role> search(@Param("search") String search, Pageable pageable);
}
