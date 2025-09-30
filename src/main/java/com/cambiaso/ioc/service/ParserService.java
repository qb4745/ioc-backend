package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.DimMaquina;
import com.cambiaso.ioc.persistence.entity.DimMaquinista;
import com.cambiaso.ioc.persistence.entity.FactProduction;
import com.cambiaso.ioc.persistence.repository.DimMaquinaRepository;
import com.cambiaso.ioc.persistence.repository.DimMaquinistaRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParserService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DimMaquinaRepository maquinaRepository;
    private final DimMaquinistaRepository maquinistaRepository;
    private final MeterRegistry meterRegistry;

    public ParserService(DimMaquinaRepository maquinaRepository,
                         DimMaquinistaRepository maquinistaRepository,
                         MeterRegistry meterRegistry) {
        this.maquinaRepository = maquinaRepository;
        this.maquinistaRepository = maquinistaRepository;
        this.meterRegistry = meterRegistry;
    }

    // Metrics helpers
    private Counter rowsParsedCounter() { return meterRegistry.counter("etl.rows.parsed"); }
    private Counter rowsDuplicateSkippedCounter() { return meterRegistry.counter("etl.rows.duplicate.skipped"); }
    private Counter malformedLinesCounter() { return meterRegistry.counter("etl.rows.malformed"); }
    private Timer parseDurationTimer() { return meterRegistry.timer("etl.parse.duration"); }

    public List<FactProduction> parse(InputStream inputStream) throws IOException {
        long startNanos = System.nanoTime();
        List<FactProduction> records = new ArrayList<>();
        Map<String, Integer> headerMap = new HashMap<>();
        boolean headerFound = false;
        int duplicatesSkipped = 0;
        int logicalParsed = 0; // registros válidos (post validación)
        int lineNumber = 0;
        int malformedLines = 0;

        // Preload dimension caches
        Map<String, DimMaquina> maquinaCache = maquinaRepository.findAll().stream()
                .collect(Collectors.toMap(DimMaquina::getCodigoMaquina, m -> m));
        Map<Long, DimMaquinista> maquinistaCache = maquinistaRepository.findAll().stream()
                .collect(Collectors.toMap(DimMaquinista::getCodigoMaquinista, m -> m));
        List<DimMaquina> newMaquinas = new ArrayList<>();
        List<DimMaquinista> newMaquinistas = new ArrayList<>();

        // De-dup key set
        Set<String> seenKeys = new HashSet<>();
        Map<String, BiConsumer<FactProduction, String>> setterMap = buildSetterMap(maquinaCache, maquinistaCache, newMaquinas, newMaquinistas);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "Windows-1252"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.contains("Fecha Cont.")) { headerFound = true; headerMap = parseHeader(line); continue; }
                if (!headerFound || line.trim().startsWith("-") || line.trim().isEmpty() || line.contains("Cantidad")) { continue; }

                String earlyKey = null;
                try {
                    earlyKey = buildEarlyKeyCanonical(line, headerMap); // normalizado
                } catch (Exception ex) {
                    if (log.isTraceEnabled()) log.trace("Early key build failed line #{}: {}", lineNumber, ex.getMessage());
                }
                if (earlyKey != null && seenKeys.contains(earlyKey)) {
                    duplicatesSkipped++; rowsDuplicateSkippedCounter().increment();
                    if (log.isTraceEnabled()) log.trace("Early skip duplicate logical fact key={}", earlyKey);
                    continue;
                }

                try {
                    FactProduction record = parseDataLine(line, headerMap, setterMap);
                    if (!isRecordValid(record)) {
                        malformedLines++; malformedLinesCounter().increment();
                        if (log.isDebugEnabled()) log.debug("Skipping invalid record line #{} (missing required fields)", lineNumber);
                        continue; // no cuenta como parsed
                    }
                    String key = earlyKey != null ? earlyKey : buildDedupKey(record);
                    if (key == null) { // fallback: si no se puede construir clave canónica, descartar
                        malformedLines++; malformedLinesCounter().increment();
                        if (log.isDebugEnabled()) log.debug("Skipping record without canonical key line #{}", lineNumber);
                        continue;
                    }
                    if (!seenKeys.add(key)) {
                        duplicatesSkipped++; rowsDuplicateSkippedCounter().increment();
                        if (log.isTraceEnabled()) log.trace("Skipping duplicate logical fact key={} (late)", key);
                        continue;
                    }
                    logicalParsed++; rowsParsedCounter().increment();
                    records.add(record);
                } catch (Exception e) {
                    malformedLines++; malformedLinesCounter().increment();
                    log.warn("Skipping malformed line #{}: '{}'. Reason: {}", lineNumber, line, e.getMessage());
                }
            }
        } finally {
            long elapsed = System.nanoTime() - startNanos;
            parseDurationTimer().record(Duration.ofNanos(elapsed));
            log.info("Parse summary: linesRead={}, recordsParsedValid={}, duplicatesSkipped(early+late)={}, malformedLines={}, newMaquinas={}, newMaquinistas={}, finalRecords={}, elapsedMs={}",
                    lineNumber, logicalParsed, duplicatesSkipped, malformedLines, newMaquinas.size(), newMaquinistas.size(), records.size(), elapsed / 1_000_000);
        }

        // Persist new dimensions once
        if (!newMaquinas.isEmpty()) {
            maquinaRepository.saveAll(newMaquinas);
            log.info("Saved {} new DimMaquina entities.", newMaquinas.size());
        }
        if (!newMaquinistas.isEmpty()) {
            maquinistaRepository.saveAll(newMaquinistas);
            log.info("Saved {} new DimMaquinista entities.", newMaquinistas.size());
        }
        return records;
    }

    private String buildDedupKey(FactProduction r) {
        return canonicalKey(r.getFechaContabilizacion(),
                r.getMaquina() != null ? r.getMaquina().getCodigoMaquina() : null,
                r.getMaquinista() != null ? r.getMaquinista().getCodigoMaquinista() : null,
                r.getNumeroLog());
    }

    // Nuevo helper: genera clave canónica ISO + números normalizados
    private String canonicalKey(LocalDate fecha, String maquinaCodigo, Long maquinistaCodigo, Long numeroLog) {
        if (fecha == null || maquinaCodigo == null || numeroLog == null) return null;
        String maq = maquinaCodigo.trim();
        if (maq.isEmpty()) return null;
        long maqnis = (maquinistaCodigo == null) ? 0L : maquinistaCodigo;
        return fecha.toString() + '|' + maq + '|' + maqnis + '|' + numeroLog;
    }

    private Map<String, Integer> parseHeader(String line) {
        Map<String, Integer> map = new HashMap<>();
        String[] headers = line.split("\\|", -1);
        for (int i = 0; i < headers.length; i++) {
            String cleanedHeader = headers[i].trim()
                    .replace("á", "a").replace("é", "e")
                    .replace("í", "i").replace("ó", "o")
                    .replace("ú", "u").replace("°", "o");
            map.put(cleanedHeader, i);
        }
        return map;
    }

    private FactProduction parseDataLine(String line, Map<String, Integer> headerMap,
                                         Map<String, BiConsumer<FactProduction, String>> setterMap) {
        String[] fields = line.split("\\|", -1);
        FactProduction record = new FactProduction();
        headerMap.forEach((headerName, index) -> {
            if (index < fields.length) {
                String value = fields[index].trim();
                if (!value.isEmpty()) {
                    BiConsumer<FactProduction, String> consumer = setterMap.get(headerName);
                    if (consumer != null) {
                        try { consumer.accept(record, value); } catch (Exception e) {
                            log.trace("Could not parse field '{}' with value '{}'. Error: {}", headerName, value, e.getMessage());
                        }
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

    private Map<String, BiConsumer<FactProduction, String>> buildSetterMap(Map<String, DimMaquina> maquinaCache,
                                                                           Map<Long, DimMaquinista> maquinistaCache,
                                                                           List<DimMaquina> newMaquinas,
                                                                           List<DimMaquinista> newMaquinistas) {
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
        if (codigoMaquina == null || codigoMaquina.trim().isEmpty()) return null;
        String codigo = codigoMaquina.trim();
        DimMaquina existing = cache.get(codigo);
        if (existing != null) return existing;
        DimMaquina nueva = new DimMaquina();
        nueva.setCodigoMaquina(codigo);
        nueva.setNombreMaquina("Máquina " + codigo);
        cache.put(codigo, nueva);
        newMaquinas.add(nueva);
        return nueva;
    }

    private DimMaquinista findOrCreateMaquinista(String codigoStr, Map<Long, DimMaquinista> cache, List<DimMaquinista> newMaquinistas) {
        if (codigoStr == null || codigoStr.trim().isEmpty()) return null;
        Long codigo = safeParseLong(codigoStr.trim());
        if (codigo == null) return null;
        DimMaquinista existing = cache.get(codigo);
        if (existing != null) return existing;
        DimMaquinista nuevo = new DimMaquinista();
        nuevo.setCodigoMaquinista(codigo);
        nuevo.setNombreCompleto("Maquinista " + codigo);
        cache.put(codigo, nuevo);
        newMaquinistas.add(nuevo);
        return nuevo;
    }

    private Long safeParseLong(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return null;
        // Ruta rápida: sólo dígitos
        if (trimmed.chars().allMatch(Character::isDigit)) {
            try { return Long.parseLong(trimmed); } catch (NumberFormatException ex) { return null; }
        }
        // Reemplazar coma decimal si viene en valores que deberían ser enteros (defensivo)
        String normalized = trimmed.replace(",", "");
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException e) {
            try { return new java.math.BigDecimal(trimmed.replace(",", ".")).longValue(); } catch (Exception ex) { return null; }
        }
    }
    private Integer safeParseInt(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }
    private BigDecimal safeParseBigDecimal(String s) {
        try { return new BigDecimal(s); } catch (NumberFormatException e) { return null; }
    }

    // Validación básica de campos NOT NULL esenciales de la entidad
    private boolean isRecordValid(FactProduction r) {
        return r.getFechaContabilizacion() != null &&
                r.getMaquina() != null &&
                r.getNumeroLog() != null &&
                r.getHoraContabilizacion() != null &&
                r.getFechaNotificacion() != null &&
                r.getMaterialSku() != null &&
                r.getCantidad() != null &&
                r.getPesoNeto() != null &&
                r.getTurno() != null;
    }

    // Nuevo early key canónico: parse mínimo para construir clave final consistente
    private String buildEarlyKeyCanonical(String line, Map<String, Integer> headerMap) {
        if (headerMap.isEmpty()) return null;
        String[] fields = line.split("\\|", -1);
        Integer idxFecha = headerMap.get("Fecha Cont.");
        Integer idxMaquina = headerMap.get("Maquina");
        Integer idxMaquinista = headerMap.get("Maquinista");
        Integer idxLog = headerMap.get("Numero Log.");
        if (idxFecha == null || idxMaquina == null || idxLog == null) return null;
        if (idxFecha >= fields.length || idxMaquina >= fields.length || idxLog >= fields.length) return null;
        String rawFecha = fields[idxFecha].trim();
        String rawMaquina = fields[idxMaquina].trim();
        String rawLog = fields[idxLog].trim().replace(" ", "");
        String rawMaqnis = (idxMaquinista != null && idxMaquinista < fields.length) ? fields[idxMaquinista].trim() : "";
        if (rawFecha.isEmpty() || rawMaquina.isEmpty() || rawLog.isEmpty()) return null;
        LocalDate fecha;
        try { fecha = LocalDate.parse(rawFecha, DATE_FORMATTER); } catch (Exception ex) { return null; }
        Long numeroLog;
        try { numeroLog = Long.parseLong(rawLog); } catch (Exception ex) { return null; }
        Long maquinista = null;
        if (!rawMaqnis.isEmpty()) {
            try { maquinista = Long.parseLong(rawMaqnis); } catch (Exception ignore) { maquinista = null; }
        }
        return canonicalKey(fecha, rawMaquina, maquinista, numeroLog);
    }
}
