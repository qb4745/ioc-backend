package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.AppUser;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AppUserSearchRepositoryImpl implements AppUserSearchRepository {

    private final EntityManager em;

    @Override
    public Page<AppUser> search(String search, Integer plantaId, Boolean isActive, Pageable pageable) {
        StringBuilder jpql = new StringBuilder("SELECT u FROM AppUser u WHERE u.deletedAt IS NULL");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(u) FROM AppUser u WHERE u.deletedAt IS NULL");

        if (search != null && !search.isBlank()) {
            jpql.append(" AND (LOWER(u.email) LIKE :q OR LOWER(u.primerNombre) LIKE :q OR LOWER(u.primerApellido) LIKE :q)");
            countJpql.append(" AND (LOWER(u.email) LIKE :q OR LOWER(u.primerNombre) LIKE :q OR LOWER(u.primerApellido) LIKE :q)");
        }
        if (plantaId != null) {
            jpql.append(" AND u.planta.id = :plantaId");
            countJpql.append(" AND u.planta.id = :plantaId");
        }
        if (isActive != null) {
            // entity field is 'active' (boolean)
            jpql.append(" AND u.active = :active");
            countJpql.append(" AND u.active = :active");
        }

        var query = em.createQuery(jpql.toString(), AppUser.class);
        var countQuery = em.createQuery(countJpql.toString(), Long.class);

        if (search != null && !search.isBlank()) {
            String q = "%" + search.toLowerCase() + "%";
            query.setParameter("q", q);
            countQuery.setParameter("q", q);
        }
        if (plantaId != null) {
            query.setParameter("plantaId", plantaId);
            countQuery.setParameter("plantaId", plantaId);
        }
        if (isActive != null) {
            query.setParameter("active", isActive);
            countQuery.setParameter("active", isActive);
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        var items = query.getResultList();
        var total = countQuery.getSingleResult();
        return new PageImpl<>(items, pageable, total);
    }
}

