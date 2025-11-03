package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    @Query(value = "SELECT * FROM roles r WHERE :search::text IS NULL OR :search = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search::text, '%'))",
           countQuery = "SELECT COUNT(*) FROM roles r WHERE :search::text IS NULL OR :search = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search::text, '%'))",
           nativeQuery = true)
    Page<Role> search(@Param("search") String search, Pageable pageable);

    boolean existsById(Integer id);
}
