package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.DimMaquina;
import com.cambiaso.ioc.persistence.entity.DimMaquinista;
import com.cambiaso.ioc.persistence.entity.FactProduction;
import com.cambiaso.ioc.persistence.repository.DimMaquinaRepository;
import com.cambiaso.ioc.persistence.repository.DimMaquinistaRepository;
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

    private final DimMaquinaRepository maquinaRepository;
    private final DimMaquinistaRepository maquinistaRepository;

    public ParserService(DimMaquinaRepository maquinaRepository, DimMaquinistaRepository maquinistaRepository) {
        this.maquinaRepository = maquinaRepository;
        this.maquinistaRepository = maquinistaRepository;
    }

    public List<FactProduction> parse(InputStream inputStream) throws IOException {
        List<FactProduction> records = new ArrayList<>();
        Map<String, Integer> headerMap = new HashMap<>();
        boolean headerFound = false;

        // Use "Windows-1252" charset for files originating from Windows systems
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "Windows-1252"))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.contains("Fecha Cont.")) {
                    headerFound = true;
                    headerMap = parseHeader(line);
                    continue; // Skip header line after processing
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
        String[] headers = line.split("\\|", -1);
        for (int i = 0; i < headers.length; i++) {
            String trimmed = headers[i].trim();
            map.put(trimmed, i);
        }
        return map;
    }

    private FactProduction parseDataLine(String line, Map<String, Integer> headerMap) {
        String[] fields = line.split("\\|", -1);
        FactProduction record = new FactProduction();

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

        // --- VALIDATION MOVED TO THE END ---
        if (record.getFechaContabilizacion() == null) {
            throw new IllegalArgumentException("Fecha Contabilizacion is required and could not be parsed.");
        }
        if (record.getNumeroLog() == null) {
            throw new IllegalArgumentException("Numero Log is required and could not be parsed.");
        }

        return record;
    }

    private Map<String, BiConsumer<FactProduction, String>> getSetterMap() {
        Map<String, BiConsumer<FactProduction, String>> map = new HashMap<>();

        // Claves exactas como aparecen en los diferentes tests
        map.put("Status", (r, v) -> r.setStatusOrigen(v.replace("@", "").trim()));
        map.put("Fecha Cont.", (r, v) -> r.setFechaContabilizacion(LocalDate.parse(v, DATE_FORMATTER)));
        map.put("Hora", (r, v) -> r.setHoraContabilizacion(LocalTime.parse(v, TIME_FORMATTER)));
        map.put("Fecha Notif", (r, v) -> r.setFechaNotificacion(LocalDate.parse(v, DATE_FORMATTER)));

        // Ambas variantes del header Número Log
        map.put("Número Log.", (r, v) -> r.setNumeroLog(safeParseLong(v)));
        map.put("NÃºmero Log.", (r, v) -> r.setNumeroLog(safeParseLong(v))); // Versión con codificación incorrecta

        map.put("Documento", (r, v) -> r.setDocumento(safeParseLong(v)));
        map.put("Material", (r, v) -> r.setMaterialSku(safeParseLong(v)));

        // Ambas variantes de Descripción
        map.put("Descripción", FactProduction::setMaterialDescripcion);
        map.put("DescripciÃ³n", FactProduction::setMaterialDescripcion); // Versión con codificación incorrecta

        // Ambas variantes de N° Pallet
        map.put("N° Pallet", (r, v) -> r.setNumeroPallet(safeParseInt(v)));
        map.put("NÂ° Pallet", (r, v) -> r.setNumeroPallet(safeParseInt(v))); // Versión con codificación incorrecta

        map.put("Cantidad", (r, v) -> r.setCantidad(safeParseBigDecimal(v.replace(",", "."))));
        map.put("Peso Neto", (r, v) -> r.setPesoNeto(safeParseBigDecimal(v.replace(",", "."))));
        map.put("Lista", FactProduction::setLista);

        // Ambas variantes de Versión
        map.put("Versión", FactProduction::setVersionProduccion);
        map.put("VersiÃ³n", FactProduction::setVersionProduccion); // Versión con codificación incorrecta

        // Máquina y Maquinista - ahora con lógica para buscar/crear entidades
        map.put("Máquina", (r, v) -> r.setMaquina(findOrCreateMaquina(v)));
        map.put("MÃ¡quina", (r, v) -> r.setMaquina(findOrCreateMaquina(v))); // Versión con codificación incorrecta

        map.put("Maquinista", (r, v) -> r.setMaquinista(findOrCreateMaquinista(v)));
        map.put("MÃ¡quinista", (r, v) -> r.setMaquinista(findOrCreateMaquinista(v))); // Versión con codificación incorrecta

        map.put("Ctro.Ctos.", (r, v) -> r.setCentroCostos(safeParseLong(v)));
        map.put("Turno", FactProduction::setTurno);
        map.put("Jornada", FactProduction::setJornada);
        map.put("Usuario", FactProduction::setUsuarioSap);
        map.put("Bodeguero", (r, v) -> r.setBodeguero("NaN".equalsIgnoreCase(v) ? null : v));

        return map;
    }

    /**
     * Busca una máquina por código, si no existe la crea
     */
    private DimMaquina findOrCreateMaquina(String codigoMaquina) {
        if (codigoMaquina == null || codigoMaquina.trim().isEmpty()) {
            return null;
        }

        String codigo = codigoMaquina.trim();
        return maquinaRepository.findByCodigoMaquina(codigo)
                .orElseGet(() -> {
                    DimMaquina nuevaMaquina = new DimMaquina();
                    nuevaMaquina.setCodigoMaquina(codigo);
                    nuevaMaquina.setNombreMaquina("Máquina " + codigo); // Nombre por defecto
                    DimMaquina savedMaquina = maquinaRepository.save(nuevaMaquina);
                    log.debug("Created new DimMaquina with codigo: {}", codigo);
                    return savedMaquina;
                });
    }

    /**
     * Busca un maquinista por código, si no existe lo crea
     */
    private DimMaquinista findOrCreateMaquinista(String codigoMaquinistaStr) {
        if (codigoMaquinistaStr == null || codigoMaquinistaStr.trim().isEmpty()) {
            return null;
        }

        Long codigoMaquinista = safeParseLong(codigoMaquinistaStr.trim());
        if (codigoMaquinista == null) {
            log.warn("Invalid maquinista code: {}", codigoMaquinistaStr);
            return null;
        }

        return maquinistaRepository.findByCodigoMaquinista(codigoMaquinista)
                .orElseGet(() -> {
                    DimMaquinista nuevoMaquinista = new DimMaquinista();
                    nuevoMaquinista.setCodigoMaquinista(codigoMaquinista);
                    nuevoMaquinista.setNombreCompleto("Maquinista " + codigoMaquinista); // Nombre por defecto
                    DimMaquinista savedMaquinista = maquinistaRepository.save(nuevoMaquinista);
                    log.debug("Created new DimMaquinista with codigo: {}", codigoMaquinista);
                    return savedMaquinista;
                });
    }

    private Long safeParseLong(String s) {
        try {
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
