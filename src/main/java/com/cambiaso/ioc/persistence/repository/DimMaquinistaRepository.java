package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.DimMaquinista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DimMaquinistaRepository extends JpaRepository<DimMaquinista, Long> {
    Optional<DimMaquinista> findByCodigoMaquinista(Long codigoMaquinista);
}
