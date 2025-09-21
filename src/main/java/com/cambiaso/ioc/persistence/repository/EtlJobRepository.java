package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.EtlJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EtlJobRepository extends JpaRepository<EtlJob, UUID> {
    Optional<EtlJob> findByFileHash(String fileHash);

    boolean existsByStatusInAndMaxDateGreaterThanEqualAndMinDateLessThanEqual(
            List<String> statuses,
            LocalDate minDate,
            LocalDate maxDate
    );
}