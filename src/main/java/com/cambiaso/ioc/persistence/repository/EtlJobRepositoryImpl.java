package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.EtlJob;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Custom implementation to avoid JPQL UPDATE type issues with OffsetDateTime in some dialects.
 * Performs a select then per-row update ensuring finishedAt and details are set consistently.
 */
public class EtlJobRepositoryImpl implements EtlJobRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public int markStuckAsFailed(OffsetDateTime cutoff) {
        // Selecciona jobs creados estrictamente antes del cutoff
        List<EtlJob> stuck = em.createQuery("SELECT j FROM EtlJob j WHERE j.status IN ('INICIADO','PROCESANDO','SINCRONIZANDO') AND j.finishedAt IS NULL AND j.createdAt < :cutoff", EtlJob.class)
                .setParameter("cutoff", cutoff)
                .getResultList();

        if (stuck.isEmpty()) return 0;
        OffsetDateTime now = OffsetDateTime.now();
        for (EtlJob j : stuck) {
            j.setStatus("FALLO");
            String details = j.getDetails();
            if (details == null || details.isBlank()) {
                j.setDetails("Watchdog timeout");
            } else if (!details.contains("Watchdog timeout")) {
                j.setDetails(details + "|Watchdog timeout");
            }
            j.setFinishedAt(now);
        }
        return stuck.size();
    }
}
