package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<AppUser> findBySupabaseUserId(UUID supabaseUserId);
    boolean existsBySupabaseUserId(UUID supabaseUserId);
}
