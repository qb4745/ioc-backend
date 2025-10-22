package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.UserRole;
import com.cambiaso.ioc.persistence.entity.UserRoleKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleKey> {

    boolean existsByIdUserIdAndIdRoleId(Long userId, Integer roleId);

    boolean existsByIdRoleId(Integer roleId);

    long countByIdRoleId(Integer roleId);

    @Modifying
    @Transactional
    void deleteByIdUserIdAndIdRoleId(Long userId, Integer roleId);

    @Query("SELECT r.name FROM UserRole ur JOIN ur.role r WHERE ur.id.userId = :userId ORDER BY r.name")
    List<String> findRoleNamesByUserId(@Param("userId") long userId);
}
