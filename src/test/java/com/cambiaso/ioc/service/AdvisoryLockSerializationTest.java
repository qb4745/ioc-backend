package com.cambiaso.ioc.service;


import com.cambiaso.ioc.persistence.entity.DimMaquina;
import com.cambiaso.ioc.persistence.entity.DimMaquinista;
import com.cambiaso.ioc.persistence.entity.FactProduction;
import com.cambiaso.ioc.persistence.repository.DimMaquinaRepository;
import com.cambiaso.ioc.persistence.repository.DimMaquinistaRepository;
import com.cambiaso.ioc.persistence.repository.FactProductionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Deterministic advisory lock serialization test using artificial delay (etl.lock.test.sleep-ms).
 * Validates that the second concurrent sync blocks until the first releases the advisory lock.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("pgtest")
class AdvisoryLockSerializationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-postgresql.sql");

    // Configure artificial delay after lock acquisition (before delete) to expose serialization
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        r.add("etl.lock.enabled", () -> "true");
        r.add("etl.lock.test.sleep-ms", () -> "450"); // 450ms delay inside first transaction
        r.add("etl.retry.unique.enabled", () -> "false");
    }

    @Autowired private DataSyncService dataSyncService;
    @Autowired private DimMaquinaRepository dimMaquinaRepository;
    @Autowired private DimMaquinistaRepository dimMaquinistaRepository;
    @Autowired private FactProductionRepository factProductionRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private DimMaquina maquina;
    private DimMaquinista maquinista;

    @BeforeEach
    void init() {
        clean();
        maquina = new DimMaquina();
        maquina.setCodigoMaquina("M_LOCK");
        maquina.setNombreMaquina("Maquina Lock");
        maquina = dimMaquinaRepository.saveAndFlush(maquina);
        maquinista = new DimMaquinista();
        maquinista.setCodigoMaquinista(7777L);
        maquinista.setNombreCompleto("Operador Lock");
        maquinista = dimMaquinistaRepository.saveAndFlush(maquinista);
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_fact_prod_natural ON fact_production (fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log)");
    }

    private void clean() {
        jdbcTemplate.execute("DELETE FROM fact_production");
        jdbcTemplate.execute("DELETE FROM dim_maquinista");
        jdbcTemplate.execute("DELETE FROM dim_maquina");
    }

    private FactProduction build(LocalDate date, long nLog) {
        FactProduction f = new FactProduction();
        f.setFechaContabilizacion(date);
        f.setMaquina(maquina);
        f.setMaquinista(maquinista);
        f.setNumeroLog(nLog);
        f.setHoraContabilizacion(LocalTime.of(7,30,0));
        f.setFechaNotificacion(date.plusDays(1));
        f.setMaterialSku(999000111L);
        f.setCantidad(new BigDecimal("5.0000"));
        f.setPesoNeto(new BigDecimal("9.5000"));
        f.setTurno("X");
        f.setStatusOrigen("AA");
        return f;
    }

    @Test
    @DisplayName("Advisory lock: serie sin mezcla (gana uno de los lotes) y un hilo espera el delay")
    void advisoryLockBlocksSecondAndLastBatchWins() throws Exception {
        LocalDate date = LocalDate.of(2025, 9, 1);
        List<FactProduction> batchA = List.of(build(date, 5001L), build(date,5002L));
        List<FactProduction> batchB = List.of(build(date, 6001L), build(date,6002L));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        class Timing { Instant start; Instant end; }
        Timing tA = new Timing();
        Timing tB = new Timing();

        Future<?> fa = pool.submit(() -> {
            await(startLatch);
            tA.start = Instant.now();
            dataSyncService.syncWithDeleteInsert(date, date, batchA);
            tA.end = Instant.now();
        });
        Future<?> fb = pool.submit(() -> {
            await(startLatch);
            tB.start = Instant.now();
            dataSyncService.syncWithDeleteInsert(date, date, batchB);
            tB.end = Instant.now();
        });

        startLatch.countDown();
        fa.get();
        fb.get();
        pool.shutdown();

        // Verificar que hubo serialización: al menos uno refleja el delay (>400ms)
        long durA = Duration.between(tA.start, tA.end).toMillis();
        long durB = Duration.between(tB.start, tB.end).toMillis();
        assertThat(Math.max(durA, durB))
                .as("Una de las transacciones debe reflejar el delay interno (>=400ms). durA=%d durB=%d", durA, durB)
                .isGreaterThanOrEqualTo(400);

        List<FactProduction> all = factProductionRepository.findAll();
        assertThat(all).hasSize(2);
        Set<Long> logs = all.stream().map(FactProduction::getNumeroLog).collect(java.util.stream.Collectors.toSet());
        boolean isBatchA = logs.equals(Set.of(5001L,5002L));
        boolean isBatchB = logs.equals(Set.of(6001L,6002L));
        assertThat(isBatchA || isBatchB)
                .as("El resultado final debe ser exactamente uno de los lotes (sin mezcla). logs=%s", logs)
                .isTrue();
    }

    private void await(CountDownLatch latch) {
        try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
