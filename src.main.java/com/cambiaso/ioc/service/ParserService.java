package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.FactProduction;
import com.cambiaso.ioc.persistence.entity.FactProductionId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@Service
public class ParserService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public List<FactProduction> parse(InputStream inputStream) throws IOException {
        List<FactProduction> records = new ArrayList<>();
        Map<String, Integer> headerMap = new HashMap<>();
        boolean headerFound = false;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.contains("Fecha Cont.")) {
                    headerFound = true;
                    headerMap = parseHeader(line);
                    continue;
                }

                if (!headerFound || line.trim().startsWith("-") || line.trim().isEmpty() || line.contains("Cantidad")) {
                    continue; // Skip non-data lines
                }

                try {
                    FactProduction record = parseDataLine(line, headerMap);
                    records.add(record);
                } catch (Exception e) {
                    log.warn("Skipping malformed line #{}: '{}'. Reason: {}", lineNumber, line, e.getMessage());
                }
            }
        }
        return records;
    }

    private Map<String, Integer> parseHeader(String line) {
        Map<String, Integer> map = new HashMap<>();
        String[] headers = line.split("\|", -1);
        for (int i = 0; i < headers.length; i++) {
            map.put(headers[i].trim(), i);
        }
        return map;
    }

    private FactProduction parseDataLine(String line, Map<String, Integer> headerMap) {
        String[] fields = line.split("\|", -1);
        FactProduction record = new FactProduction();
        FactProductionId id = new FactProductionId();
        record.setId(id);

        Map<String, BiConsumer<FactProduction, String>> setterMap = getSetterMap();

        headerMap.forEach((headerName, index) -> {
            if (index < fields.length) {
                String value = fields[index].trim();
                if (!value.isEmpty() && setterMap.containsKey(headerName)) {
                    try {
                        setterMap.get(headerName).accept(record, value);
                    } catch (Exception e) {
                        log.trace("Could not parse field '{}' with value '{}'. Error: {}", headerName, value, e.getMessage());
                    }
                }
            }
        });

        if (record.getId().getFechaContabilizacion() == null) {
            throw new IllegalArgumentException("Fecha Contabilizacion is required and could not be parsed.");
        }
        return record;
    }

    private Map<String, BiConsumer<FactProduction, String>> getSetterMap() {
        Map<String, BiConsumer<FactProduction, String>> map = new HashMap<>();
        map.put("Status", (r, v) -> r.setStatusOrigen(v.replace("@", "")));
        map.put("Fecha Cont.", (r, v) -> r.getId().setFechaContabilizacion(LocalDate.parse(v, DATE_FORMATTER)));
        map.put("Hora", (r, v) -> r.setHoraContabilizacion(LocalTime.parse(v, TIME_FORMATTER)));
        map.put("Fecha Notif", (r, v) -> r.setFechaNotificacion(LocalDate.parse(v, DATE_FORMATTER)));
        map.put("Número Log.", (r, v) -> r.setNumeroLog(safeParseLong(v)));
        map.put("Documento", (r, v) -> r.setDocumento(safeParseLong(v)));
        map.put("Material", (r, v) -> r.setMaterialSku(safeParseLong(v)));
        map.put("Descripción", FactProduction::setMaterialDescripcion);
        map.put("N° Pallet", (r, v) -> r.setNumeroPallet(safeParseInt(v)));
        map.put("Cantidad", (r, v) -> r.setCantidad(safeParseBigDecimal(v.replace(",", "."))));
        map.put("Peso Neto", (r, v) -> r.setPesoNeto(safeParseBigDecimal(v.replace(",", "."))));
        map.put("Lista", FactProduction::setLista);
        map.put("Versión", FactProduction::setVersionProduccion);
        map.put("Ctro.Ctos.", (r, v) -> r.setCentroCostos(safeParseLong(v)));
        map.put("Turno", FactProduction::setTurno);
        map.put("Jornada", FactProduction::setJornada);
        map.put("Usuario", FactProduction::setUsuarioSap);
        map.put("Bodeguero", (r, v) -> r.setBodeguero("NaN".equalsIgnoreCase(v) ? null : v));
        return map;
    }

    private Long safeParseLong(String s) {
        try {
            // Handle scientific notation from the text file
            return new BigDecimal(s).longValue();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer safeParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal safeParseBigDecimal(String s) {
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
