package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.EtlJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EtlJobRepository extends JpaRepository<EtlJob, UUID> {
}
