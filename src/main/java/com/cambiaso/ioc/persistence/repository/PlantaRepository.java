package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.Planta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlantaRepository extends JpaRepository<Planta, Integer> {
    Optional<Planta> findByCodeIgnoreCase(String code);
}

