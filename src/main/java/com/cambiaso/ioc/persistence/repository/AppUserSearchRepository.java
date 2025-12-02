package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppUserSearchRepository {
    Page<AppUser> search(String search, Integer plantaId, Boolean isActive, Pageable pageable);
}

