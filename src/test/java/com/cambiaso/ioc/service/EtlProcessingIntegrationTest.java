package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.EtlJob;
import com.cambiaso.ioc.persistence.repository.EtlJobRepository;
import com.cambiaso.ioc.persistence.repository.FactProductionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Punto 4 del checklist (validaciones post-cambio) a nivel end-to-end ETL:
 *  - 4.1 Idempotencia: dos jobs secuenciales con el mismo archivo no generan duplicados y mantienen el mismo conteo.
 *  - 4.2 Conflicto de ventana: dos jobs concurrentes sobre mismo rango -> segundo falla con conflicto.
 *  - 4.3 Deduplicación intra-archivo: archivo con duplicados lógicos produce inserción única de cada clave.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("pgtest")
class EtlProcessingIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        // Asegura flags relevantes
        registry.add("etl.lock.enabled", () -> "true");
        registry.add("etl.retry.unique.enabled", () -> "true");
        registry.add("etl.retry.unique.max-attempts", () -> "3");
        // El retardo ya no es necesario para el test de conflicto determinístico
        registry.add("etl.lock.test.sleep-ms", () -> "0");
    }

    @Autowired
    private EtlProcessingService etlProcessingService;
    @Autowired
    private EtlJobService etlJobService;
    @Autowired
    private EtlJobRepository etlJobRepository;
    @Autowired
    private FactProductionRepository factProductionRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Charset WIN1252 = Charset.forName("windows-1252");

    private String buildFileContentWithDuplicates(LocalDate date, boolean variant) {
        // Cabecera mínima reconocida por ParserService (orden flexible, usamos subset necesario)
        // Campos requeridos para record válido: Fecha Cont.|Hora|Fecha Notif|Numero Log.|Maquina|Maquinista|Material|Cantidad|Peso Neto|Turno
        String header = "Fecha Cont.|Hora|Fecha Notif|Numero Log.|Maquina|Maquinista|Material|Cantidad|Peso Neto|Turno";
        String d = date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        // Dos líneas idénticas (duplicado lógico) y una distinta
        String line1 = d + "|08:00:00|" + d + "|1001|M001|9001|6000123456|10,00|15,50|A";
        String line1dup = line1; // duplicado
        String line2 = d + "|08:05:00|" + d + "|1002|M001|9001|6000123456|20,00|30,00|A";
        String extra = variant ? "# comentario ignorado\n" : ""; // línea extra que el parser ignora (no contiene campos requeridos)
        return header + "\n" + line1 + "\n" + line1dup + "\n" + line2 + "\n" + extra; // cambia hash pero no datos lógicos
    }

    private String buildFileContentWithDuplicates(LocalDate date) {
        return buildFileContentWithDuplicates(date, false);
    }

    private MockMultipartFile buildMultipart(String name, String content) {
        return new MockMultipartFile("file", name, "text/plain", content.getBytes(WIN1252));
    }

    private UUID submitJob(MockMultipartFile file, String user) {
        String fileHash = etlProcessingService.calculateFileHash(file); // Usa el mismo método que el controller
        EtlJob job = etlJobService.createJob(file.getOriginalFilename(), fileHash, user);
        etlProcessingService.processFile(file, user, job.getJobId());
        return job.getJobId();
    }

    private EtlJob waitForFinalStatus(UUID jobId) {
        long deadline = System.nanoTime() + Duration.ofSeconds(15).toNanos();
        EtlJob job;
        do {
            job = etlJobRepository.findById(jobId).orElseThrow();
            if ("EXITO".equals(job.getStatus()) || "FALLO".equals(job.getStatus())) {
                return job;
            }
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } while (System.nanoTime() < deadline);
        throw new IllegalStateException("Timeout esperando estado final del job " + jobId + " (ultimo=" + job.getStatus() + ")");
    }

    @BeforeEach
    void prepare() {
        // Limpieza para aislar conteos entre tests
        jdbcTemplate.execute("DELETE FROM fact_production");
        jdbcTemplate.execute("DELETE FROM dim_maquinista");
        jdbcTemplate.execute("DELETE FROM dim_maquina");
        // Crear índice UNIQUE natural si falta (idempotente)
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_fact_prod_natural ON fact_production (fecha_contabilizacion, maquina_fk, COALESCE(maquinista_fk,0), numero_log)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_fact_production_fecha ON fact_production (fecha_contabilizacion)");
    }

    private void waitUntilDateRangeSet(UUID jobId, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            var opt = etlJobRepository.findById(jobId);
            if (opt.isPresent() && opt.get().getMinDate() != null && opt.get().getMaxDate() != null) {
                return;
            }
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }
        throw new IllegalStateException("Timeout esperando min/max para job " + jobId);
    }

    @Test
    @DisplayName("4.3 Deduplicación intra-archivo: duplicado se omite y se insertan solo claves únicas")
    void duplicateLinesWithinFileAreDeduplicated() {
        LocalDate date = LocalDate.of(2025, 9, 1);
        MockMultipartFile file = buildMultipart("etl_dup.txt", buildFileContentWithDuplicates(date));
        UUID jobId = submitJob(file, "userA");
        EtlJob job = waitForFinalStatus(jobId);
        assertThat(job.getStatus()).isEqualTo("EXITO");
        // Deben insertarse 2 registros (numero_log 1001 y 1002) a pesar de haber 3 líneas de datos
        assertThat(factProductionRepository.count()).isEqualTo(2L);
    }

    @Test
    @DisplayName("4.1 Idempotencia end-to-end: dos jobs secuenciales mismo dataset mantienen conteo constante")
    void sequentialJobsSameFileAreIdempotent() {
        LocalDate date = LocalDate.of(2025, 9, 2);
        MockMultipartFile file1 = buildMultipart("etl_idem.txt", buildFileContentWithDuplicates(date, false));
        UUID firstJob = submitJob(file1, "userB");
        EtlJob job1 = waitForFinalStatus(firstJob);
        assertThat(job1.getStatus()).isEqualTo("EXITO");
        long afterFirst = factProductionRepository.count();
        assertThat(afterFirst).isEqualTo(2L);
        // Segundo job con variante de contenido (hash diferente, mismos registros lógicos)
        MockMultipartFile file2 = buildMultipart("etl_idem_v2.txt", buildFileContentWithDuplicates(date, true));
        UUID secondJob = submitJob(file2, "userB");
        EtlJob job2 = waitForFinalStatus(secondJob);
        assertThat(job2.getStatus()).isEqualTo("EXITO");
        long afterSecond = factProductionRepository.count();
        assertThat(afterSecond).isEqualTo(2L);
    }

    @Test
    @DisplayName("4.2 Conflicto de ventana: segundo job encuentra job activo y falla")
    void concurrentWindowConflictSecondJobFails() {
        LocalDate date = LocalDate.of(2025, 9, 3);
        // Crear job stub (activo) con rango establecido sin lanzar procesamiento real
        String stubHash = "stub-" + UUID.randomUUID();
        EtlJob stub = etlJobService.createJob("stub.txt", stubHash, "userC");
        etlJobService.updateJobDateRange(stub.getJobId(), date, date); // status permanece INICIADO

        // Ahora lanzar un job real que procesa archivo con mismo rango -> debe detectar conflicto
        MockMultipartFile file = buildMultipart("etl_conflict_real.txt", buildFileContentWithDuplicates(date, false));
        UUID jobRealId = submitJob(file, "userC");
        EtlJob jobReal = waitForFinalStatus(jobRealId);

        assertThat(jobReal.getStatus()).isEqualTo("FALLO");
        assertThat(Optional.ofNullable(jobReal.getDetails()).orElse("").toLowerCase())
                .contains("another etl job is processing this date range".toLowerCase());
    }
}
