package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.DimMaquina;
import com.cambiaso.ioc.persistence.entity.DimMaquinista;
import com.cambiaso.ioc.persistence.entity.FactProduction;
import com.cambiaso.ioc.persistence.entity.FactProductionId;
import com.cambiaso.ioc.persistence.repository.DimMaquinaRepository;
import com.cambiaso.ioc.persistence.repository.DimMaquinistaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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

        // --- Performance Optimization: In-memory cache for dimensions ---
        Map<String, DimMaquina> maquinaCache = maquinaRepository.findAll().stream()
                .collect(Collectors.toMap(DimMaquina::getCodigoMaquina, maquina -> maquina));
        
        Map<Long, DimMaquinista> maquinistaCache = maquinistaRepository.findAll().stream()
                .collect(Collectors.toMap(DimMaquinista::getCodigoMaquinista, maquinista -> maquinista));

        List<DimMaquina> newMaquinas = new ArrayList<>();
        List<DimMaquinista> newMaquinistas = new ArrayList<>();
        // ----------------------------------------------------------------

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "Windows-1252"))) {
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
                    continue;
                }

                try {
                    FactProduction record = parseDataLine(line, headerMap, maquinaCache, maquinistaCache, newMaquinas, newMaquinistas);
                    records.add(record);
                } catch (Exception e) {
                    log.warn("Skipping malformed line #{}: '{}'. Reason: {}", lineNumber, line, e.getMessage());
                }
            }
        }

        // --- Performance Optimization: Batch save new dimensions ---
        if (!newMaquinas.isEmpty()) {
            maquinaRepository.saveAll(newMaquinas);
            log.info("Saved {} new DimMaquina entities.", newMaquinas.size());
        }
        if (!newMaquinistas.isEmpty()) {
            maquinistaRepository.saveAll(newMaquinistas);
            log.info("Saved {} new DimMaquinista entities.", newMaquinistas.size());
        }
        // ---------------------------------------------------------

        return records;
    }

    private Map<String, Integer> parseHeader(String line) {
        Map<String, Integer> map = new HashMap<>();
        String[] headers = line.split("\\|", -1);
        for (int i = 0; i < headers.length; i++) {
            String cleanedHeader = headers[i].trim()
                                             .replace("á", "a").replace("é", "e")
                                             .replace("í", "i").replace("ó", "o")
                                             .replace("ú", "u").replace("°", "o"); // Normaliza caracteres comunes
            map.put(cleanedHeader, i);
        }
        return map;
    }

    private FactProduction parseDataLine(String line, Map<String, Integer> headerMap,
                                         Map<String, DimMaquina> maquinaCache, Map<Long, DimMaquinista> maquinistaCache,
                                         List<DimMaquina> newMaquinas, List<DimMaquinista> newMaquinistas) {
        String[] fields = line.split("\\|", -1);
        FactProduction record = new FactProduction();

        Map<String, BiConsumer<FactProduction, String>> setterMap = getSetterMap(maquinaCache, maquinistaCache, newMaquinas, newMaquinistas);

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

        if (record.getFechaContabilizacion() == null) {
            throw new IllegalArgumentException("Fecha Contabilizacion is required and could not be parsed.");
        }
        if (record.getNumeroLog() == null) {
            throw new IllegalArgumentException("Numero Log is required and could not be parsed.");
        }

        return record;
    }

    private Map<String, BiConsumer<FactProduction, String>> getSetterMap(
            Map<String, DimMaquina> maquinaCache, Map<Long, DimMaquinista> maquinistaCache,
            List<DimMaquina> newMaquinas, List<DimMaquinista> newMaquinistas) {
        
        Map<String, BiConsumer<FactProduction, String>> map = new HashMap<>();

        map.put("Status", (r, v) -> r.setStatusOrigen(v.replace("@", "").trim()));
        map.put("Fecha Cont.", (r, v) -> r.setFechaContabilizacion(LocalDate.parse(v, DATE_FORMATTER)));
        map.put("Hora", (r, v) -> r.setHoraContabilizacion(LocalTime.parse(v, TIME_FORMATTER)));
        map.put("Fecha Notif", (r, v) -> r.setFechaNotificacion(LocalDate.parse(v, DATE_FORMATTER)));
        map.put("Numero Log.", (r, v) -> r.setNumeroLog(safeParseLong(v)));
        map.put("Documento", (r, v) -> r.setDocumento(safeParseLong(v)));
        map.put("Material", (r, v) -> r.setMaterialSku(safeParseLong(v)));
        map.put("Descripcion", FactProduction::setMaterialDescripcion);
        map.put("No Pallet", (r, v) -> r.setNumeroPallet(safeParseInt(v)));
        map.put("Cantidad", (r, v) -> r.setCantidad(safeParseBigDecimal(v.replace(",", "."))));
        map.put("Peso Neto", (r, v) -> r.setPesoNeto(safeParseBigDecimal(v.replace(",", "."))));
        map.put("Lista", FactProduction::setLista);
        map.put("Version", FactProduction::setVersionProduccion);
        map.put("Maquina", (r, v) -> r.setMaquina(findOrCreateMaquina(v, maquinaCache, newMaquinas)));
        map.put("Maquinista", (r, v) -> r.setMaquinista(findOrCreateMaquinista(v, maquinistaCache, newMaquinistas)));
        map.put("Ctro.Ctos.", (r, v) -> r.setCentroCostos(safeParseLong(v)));
        map.put("Turno", FactProduction::setTurno);
        map.put("Jornada", FactProduction::setJornada);
        map.put("Usuario", FactProduction::setUsuarioSap);
        map.put("Bodeguero", (r, v) -> r.setBodeguero("NaN".equalsIgnoreCase(v) ? null : v));

        return map;
    }

    private DimMaquina findOrCreateMaquina(String codigoMaquina, Map<String, DimMaquina> cache, List<DimMaquina> newMaquinas) {
        if (codigoMaquina == null || codigoMaquina.trim().isEmpty()) {
            return null;
        }
        String codigo = codigoMaquina.trim();
        
        if (cache.containsKey(codigo)) {
            return cache.get(codigo);
        }
        
        DimMaquina nuevaMaquina = new DimMaquina();
        nuevaMaquina.setCodigoMaquina(codigo);
        nuevaMaquina.setNombreMaquina("Máquina " + codigo);
        
        cache.put(codigo, nuevaMaquina);
        newMaquinas.add(nuevaMaquina);
        
        return nuevaMaquina;
    }

    private DimMaquinista findOrCreateMaquinista(String codigoMaquinistaStr, Map<Long, DimMaquinista> cache, List<DimMaquinista> newMaquinistas) {
        if (codigoMaquinistaStr == null || codigoMaquinistaStr.trim().isEmpty()) {
            return null;
        }
        Long codigo = safeParseLong(codigoMaquinistaStr.trim());
        if (codigo == null) {
            return null;
        }

        if (cache.containsKey(codigo)) {
            return cache.get(codigo);
        }

        DimMaquinista nuevoMaquinista = new DimMaquinista();
        nuevoMaquinista.setCodigoMaquinista(codigo);
        nuevoMaquinista.setNombreCompleto("Maquinista " + codigo);

        cache.put(codigo, nuevoMaquinista);
        newMaquinistas.add(nuevoMaquinista);

        return nuevoMaquinista;
    }

    // safeParse methods remain the same...
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
