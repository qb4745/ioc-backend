package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.DimMaquinista;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DimMaquinistaRepository extends JpaRepository<DimMaquinista, Long> {
}
