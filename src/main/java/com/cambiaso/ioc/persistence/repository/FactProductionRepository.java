package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.FactProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Repository
public interface FactProductionRepository extends JpaRepository<FactProduction, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM FactProduction fp WHERE fp.fechaContabilizacion BETWEEN :minDate AND :maxDate")
    int deleteByFechaContabilizacionBetween(@Param("minDate") LocalDate minDate, @Param("maxDate") LocalDate maxDate);
}