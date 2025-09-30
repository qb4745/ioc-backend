package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.FactProduction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que el parser elimine duplicados lógicos (misma clave natural) usando la deduplicación temprana.
 */
@SpringBootTest
@ActiveProfiles("test")
class ParserServiceDedupTest {

    @Autowired
    private ParserService parserService;

    @Test
    void givenTwoIdenticalLines_whenParse_thenOnlyOneRecordRemains() throws Exception {
        // Header minimal que el parser reconoce (contiene las columnas clave)
        String header = "| Status|Fecha Cont. |Hora    |Fecha Notif |Numero Log.|Material |Maquina |Maquinista|Cantidad |Peso Neto|Turno  |";
        // Dos líneas idénticas (misma fecha, maquina, maquinista, numero log) deben producir un único registro
        String line = "| @08@  |30.08.2025  |08:00:00|31.08.2025  |    1234567|6000123456|M001    |9001      |10,000    |15,500   |A      |";
        String duplicated = header + "\n" + line + "\n" + line + "\n"; // duplicado
        InputStream is = new ByteArrayInputStream(duplicated.getBytes(StandardCharsets.ISO_8859_1));

        List<FactProduction> records = parserService.parse(is);

        assertThat(records)
                .as("Debe existir solo un registro tras deduplicación")
                .hasSize(1);

        FactProduction fp = records.getFirst();
        assertThat(fp.getNumeroLog()).isEqualTo(1234567L);
        assertThat(fp.getMaquina()).isNotNull();
    }
}

