package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.FactProduction;
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
        InputStream inputStream = new ClassPathResource("sample-data.txt").getInputStream();

        // Act
        List<FactProduction> records = parserService.parse(inputStream);

        // Assert
        assertThat(records).hasSize(3);

        // --- Imprimir los registros en la consola ---
        System.out.println("--- Registros Parseados ---");
        records.forEach(System.out::println);
        System.out.println("---------------------------");

        // Verify first record in detail
        FactProduction firstRecord = records.get(0);
        assertThat(firstRecord.getId().getFechaContabilizacion()).isEqualTo(LocalDate.of(2025, 8, 18));
        assertThat(firstRecord.getHoraContabilizacion()).isEqualTo(LocalTime.of(20, 19, 33));
        assertThat(firstRecord.getNumeroLog()).isEqualTo(2918170L);
        assertThat(firstRecord.getMaterialSku()).isEqualTo(6000700906L);
        assertThat(firstRecord.getCantidad()).isEqualByComparingTo(new BigDecimal("85.000"));
        assertThat(firstRecord.getPesoNeto()).isEqualByComparingTo(new BigDecimal("387.0"));
        assertThat(firstRecord.getStatusOrigen()).isEqualTo("09");
        assertThat(firstRecord.getBodeguero()).isNull();

        // Verify second record (with NaN for Bodeguero)
        FactProduction secondRecord = records.get(1);
        assertThat(secondRecord.getNumeroLog()).isEqualTo(2918169L);
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
}