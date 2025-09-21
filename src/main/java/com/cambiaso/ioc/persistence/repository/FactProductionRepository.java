package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.FactProduction;
import com.cambiaso.ioc.persistence.entity.FactProductionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface FactProductionRepository extends JpaRepository<FactProduction, FactProductionId> {

    @Modifying
    @Query("DELETE FROM FactProduction fp WHERE fp.id.fechaContabilizacion BETWEEN :minDate AND :maxDate")
    void deleteByFechaContabilizacionBetween(@Param("minDate") LocalDate minDate, @Param("maxDate") LocalDate maxDate);
}