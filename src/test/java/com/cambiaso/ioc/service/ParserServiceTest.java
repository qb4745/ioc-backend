package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.FactProduction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ParserServiceTest {

    @Autowired
    private ParserService parserService;

    @Test
    void whenParseValidFile_thenReturnsCorrectRecords() throws IOException {
        // Arrange
        //InputStream inputStream = new ClassPathResource("sample-data.txt").getInputStream();
        InputStream inputStream = new ClassPathResource("real-acortado.txt").getInputStream();
        // Act
        List<FactProduction> records = parserService.parse(inputStream);

        // Assert
        assertThat(records).hasSize(8);

        // --- Imprimir los registros en la consola ---
        System.out.println("--- Registros Parseados ---");
        records.forEach(System.out::println);
        System.out.println("---------------------------");

        // Verify first record in detail
        FactProduction firstRecord = records.getFirst();
        assertThat(firstRecord.getId().getFechaContabilizacion()).isEqualTo(LocalDate.of(2025, 8, 30));
        assertThat(firstRecord.getHoraContabilizacion()).isEqualTo(LocalTime.of(8, 29, 15));
        assertThat(firstRecord.getNumeroLog()).isEqualTo(2922290L);
        assertThat(firstRecord.getMaterialSku()).isEqualTo(6760161400L);
        assertThat(firstRecord.getCantidad()).isEqualByComparingTo(new BigDecimal("48.000"));
        assertThat(firstRecord.getPesoNeto()).isEqualByComparingTo(new BigDecimal("105.6"));
        assertThat(firstRecord.getStatusOrigen()).isEqualTo("08");
        assertThat(firstRecord.getBodeguero()).isNull();

        // Verify second record
        FactProduction secondRecord = records.get(1);
        assertThat(secondRecord.getNumeroLog()).isEqualTo(2922281L);
        assertThat(secondRecord.getBodeguero()).isNull();
    }

    @Test
    void whenLineHasMalformedNumber_shouldSkipAndContinue() throws IOException {
        // Arrange
        String badData = "| @09@  |18.08.2025  |20:19:33|18.08.2025  |    NOT_A_NUMBER|4906173152|...|";
        String header = "| Status|Fecha Cont. |Hora    |Fecha Notif |Número Log.|Documento |...|";
        String content = header + "\n" + badData;
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.ISO_8859_1));

        // Act
        List<FactProduction> records = parserService.parse(inputStream);

        // Assert
        assertThat(records).isEmpty(); // The line is skipped due to the critical error
    }

    @Test
    @Disabled("Disabled by default: for manual validation with large, real-world files only.")
    void whenParseLargeRealFile_thenCountsEntries() throws IOException {
        // --- INSTRUCTIONS ---
        // 1. Place your large test file in the path below.
        // 2. Remove or comment out the @Disabled annotation above.
        // 3. Run this specific test from your IDE.
        // --------------------
        //String realFilePath = "/path/to/your/large/real-data.txt"; // <--- CHANGE THIS PATH
        String realFilePath = "/mnt/ssd-480/repos/captone/datos-cambiaso/EXPORTADOS/ANUAL_2025/EXPORTADO2025.txt";
        // Arrange
        System.out.println("--- Parsing large file: " + realFilePath + " ---");
        InputStream inputStream = java.nio.file.Files.newInputStream(java.nio.file.Paths.get(realFilePath));

        // Act
        long startTime = System.currentTimeMillis();
        List<FactProduction> records = parserService.parse(inputStream);
        long endTime = System.currentTimeMillis();

        // Assert
        int recordCount = records.size();
        long duration = endTime - startTime;

        System.out.println("-------------------------------------------------");
        System.out.println("Successfully parsed " + recordCount + " records.");
        System.out.println("Processing time: " + duration + " ms.");
        System.out.println("-------------------------------------------------");

        assertThat(recordCount).isPositive();
    }
}