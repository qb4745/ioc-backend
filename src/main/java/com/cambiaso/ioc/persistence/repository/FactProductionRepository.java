package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.FactProduction;
import com.cambiaso.ioc.persistence.entity.FactProductionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactProductionRepository extends JpaRepository<FactProduction, FactProductionId> {
}
