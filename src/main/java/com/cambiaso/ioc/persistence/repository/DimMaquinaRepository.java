package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.DimMaquina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DimMaquinaRepository extends JpaRepository<DimMaquina, Long> {
    Optional<DimMaquina> findByCodigoMaquina(String codigoMaquina);
}
