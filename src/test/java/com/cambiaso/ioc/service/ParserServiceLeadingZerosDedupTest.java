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
 * Verifica que dos líneas que difieren solo por ceros a la izquierda en Numero Log
 * se deduplican correctamente tras la normalización canónica.
 */
@SpringBootTest
@ActiveProfiles("test")
class ParserServiceLeadingZerosDedupTest {

    @Autowired
    private ParserService parserService;

    @Test
    void givenLeadingZerosInNumeroLog_whenParse_thenSingleLogicalRecord() throws Exception {
        String header = "| Status|Fecha Cont. |Hora    |Fecha Notif |Numero Log.|Material |Maquina |Maquinista|Cantidad |Peso Neto|Turno  |";
        String lineA = "| @08@  |30.08.2025  |08:00:00|31.08.2025  |    0000123|6000123456|M001    |9001      |10,000    |15,500   |A      |";
        String lineB = "| @08@  |30.08.2025  |08:00:00|31.08.2025  |    123     |6000123456|M001    |9001      |10,000    |15,500   |A      |";
        String content = header + "\n" + lineA + "\n" + lineB + "\n";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.ISO_8859_1));

        List<FactProduction> records = parserService.parse(is);

        assertThat(records)
                .as("Las dos lineas representan el mismo Numero Log lógico tras normalización")
                .hasSize(1);
        assertThat(records.getFirst().getNumeroLog()).isEqualTo(123L);
    }
}

