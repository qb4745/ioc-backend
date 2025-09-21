package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.DimMaquina;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DimMaquinaRepository extends JpaRepository<DimMaquina, Long> {
}
