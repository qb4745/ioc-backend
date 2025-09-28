package com.cambiaso.ioc.persistence;

import com.cambiaso.ioc.persistence.entity.*;
import com.cambiaso.ioc.persistence.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class PersistenceLayerTest {

    @Autowired
    private DimMaquinaRepository dimMaquinaRepository;
    @Autowired
    private DimMaquinistaRepository dimMaquinistaRepository;
    @Autowired
    private EtlJobRepository etlJobRepository;
    @Autowired
    private QuarantinedRecordRepository quarantinedRecordRepository;
    @Autowired
    private FactProductionRepository factProductionRepository;

    // Helper method to create a valid EtlJob for tests
    private EtlJob createAndSaveTestEtlJob() {
        EtlJob job = new EtlJob();
        job.setJobId(UUID.randomUUID());
        job.setFileName("test.txt");
        job.setFileHash("hash-" + job.getJobId()); // Ensure hash is unique for each test run
        job.setUserId("test-user");
        job.setStatus("INICIADO");
        return etlJobRepository.saveAndFlush(job); // Use saveAndFlush
    }

    @Test
    void whenSaveAndFindEtlJob_thenCorrect() {
        EtlJob job = createAndSaveTestEtlJob();

        EtlJob found = etlJobRepository.findById(job.getJobId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getFileName()).isEqualTo("test.txt");
        assertThat(found.getCreatedAt()).isNotNull(); // Verify @CreationTimestamp
    }

    @Test
    void whenSaveAndFindDimMaquinista_thenCorrect() {
        DimMaquinista maquinista = new DimMaquinista();
        maquinista.setCodigoMaquinista(12345L);
        maquinista.setNombreCompleto("Juan Perez");
        dimMaquinistaRepository.saveAndFlush(maquinista); // Use saveAndFlush

        DimMaquinista found = dimMaquinistaRepository.findById(maquinista.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getCodigoMaquinista()).isEqualTo(12345L);
        assertThat(found.getCreatedAt()).isNotNull(); // Verify @CreationTimestamp
        assertThat(found.getUpdatedAt()).isNotNull(); // Verify @UpdateTimestamp
    }

    @Test
    void whenSaveAndFindQuarantinedRecord_thenCorrect() {
        EtlJob job = createAndSaveTestEtlJob();

        QuarantinedRecord record = new QuarantinedRecord();
        record.setEtlJob(job);
        record.setFileName("error.csv");
        record.setLineNumber(42);
        record.setRawLine("invalid,data,format");
        record.setErrorDetails("Invalid date format");
        quarantinedRecordRepository.saveAndFlush(record); // Use saveAndFlush

        QuarantinedRecord found = quarantinedRecordRepository.findById(record.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getId()).isPositive();
        assertThat(found.getEtlJob().getJobId()).isEqualTo(job.getJobId());
        assertThat(found.getRawLine()).isEqualTo("invalid,data,format");
        assertThat(found.getCreatedAt()).isNotNull(); // Verify @CreationTimestamp
    }

    @Test
    void whenSaveAndFindFactProduction_thenCorrect() {
        DimMaquina maquina = new DimMaquina();
        maquina.setCodigoMaquina("M01");
        maquina.setNombreMaquina("Maquina 1");
        dimMaquinaRepository.saveAndFlush(maquina);

        // Create FactProduction with simple ID auto-generation
        FactProduction fact = new FactProduction();
        fact.setFechaContabilizacion(LocalDate.of(2025, 1, 15));
        fact.setMaquina(maquina);
        fact.setNumeroLog(12345L);
        fact.setHoraContabilizacion(LocalTime.now());
        fact.setFechaNotificacion(LocalDate.now());
        fact.setMaterialSku(98765L);
        fact.setCantidad(new BigDecimal("100.50"));
        fact.setPesoNeto(new BigDecimal("200.75"));
        fact.setTurno("A");

        FactProduction savedFact = factProductionRepository.saveAndFlush(fact);

        // Verify that the ID was auto-generated
        assertThat(savedFact.getId()).isNotNull();
        assertThat(savedFact.getId()).isPositive();
        assertThat(savedFact.getFechaContabilizacion()).isEqualTo(LocalDate.of(2025, 1, 15));

        FactProduction found = factProductionRepository.findById(savedFact.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getMaquina().getCodigoMaquina()).isEqualTo("M01");
        assertThat(found.getTurno()).isEqualTo("A");
    }

    @Test
    void whenSaveEtlJobWithNullFileName_thenThrowsException() {
        EtlJob job = new EtlJob();
        job.setJobId(UUID.randomUUID());
        job.setFileHash("unique-hash-for-null-test");
        job.setUserId("test-user");
        job.setStatus("FALLIDO");
        // fileName is intentionally left null

        // We expect a DataIntegrityViolationException due to the "nullable = false" constraint
        assertThatThrownBy(() -> {
            etlJobRepository.saveAndFlush(job); // saveAndFlush forces immediate DB interaction
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void whenSaveDimMaquinaWithDuplicateCode_thenThrowsException() {
        DimMaquina maquina1 = new DimMaquina();
        maquina1.setCodigoMaquina("M02");
        maquina1.setNombreMaquina("Maquina Duplicada 1");
        dimMaquinaRepository.save(maquina1);

        DimMaquina maquina2 = new DimMaquina();
        maquina2.setCodigoMaquina("M02"); // Same unique code
        maquina2.setNombreMaquina("Maquina Duplicada 2");

        // We expect a DataIntegrityViolationException due to the "unique = true" constraint
        assertThatThrownBy(() -> dimMaquinaRepository.saveAndFlush(maquina2)).isInstanceOf(DataIntegrityViolationException.class);
    }
}
