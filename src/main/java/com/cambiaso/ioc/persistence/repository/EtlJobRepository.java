package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.EtlJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EtlJobRepository extends JpaRepository<EtlJob, UUID> {
    Optional<EtlJob> findByFileHash(String fileHash);

    boolean existsByStatusInAndMaxDateGreaterThanEqualAndMinDateLessThanEqual(
            List<String> statuses,
            LocalDate minDate,
            LocalDate maxDate
    );

    @Query("SELECT CASE WHEN COUNT(j) > 0 THEN true ELSE false END " +
           "FROM EtlJob j " +
           "WHERE j.status IN ('INICIADO', 'PROCESANDO', 'SINCRONIZANDO') " +
           "AND j.jobId <> :jobId " + // Exclude the current job from the check
           "AND j.minDate IS NOT NULL AND j.maxDate IS NOT NULL " +
           "AND NOT (j.maxDate < :minDate OR j.minDate > :maxDate)")
    boolean existsActiveJobInDateRange(
            @Param("jobId") UUID jobId,
            @Param("minDate") LocalDate minDate,
            @Param("maxDate") LocalDate maxDate
    );
}
