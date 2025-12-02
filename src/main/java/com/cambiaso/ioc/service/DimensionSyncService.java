package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.DimMaquina;
import com.cambiaso.ioc.persistence.entity.DimMaquinista;
import com.cambiaso.ioc.persistence.repository.DimMaquinaRepository;
import com.cambiaso.ioc.persistence.repository.DimMaquinistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DimensionSyncService {

    private final DimMaquinaRepository maquinaRepository;
    private final DimMaquinistaRepository maquinistaRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<DimMaquina> saveNewMaquinas(List<DimMaquina> newMaquinas) {
        if (newMaquinas == null || newMaquinas.isEmpty()) {
            return List.of();
        }
        List<DimMaquina> savedMaquinas = maquinaRepository.saveAll(newMaquinas);
        log.info("Saved {} new DimMaquina entities in a new transaction.", savedMaquinas.size());
        return savedMaquinas;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<DimMaquinista> saveNewMaquinistas(List<DimMaquinista> newMaquinistas) {
        if (newMaquinistas == null || newMaquinistas.isEmpty()) {
            return List.of();
        }
        List<DimMaquinista> savedMaquinistas = maquinistaRepository.saveAll(newMaquinistas);
        log.info("Saved {} new DimMaquinista entities in a new transaction.", savedMaquinistas.size());
        return savedMaquinistas;
    }
}
