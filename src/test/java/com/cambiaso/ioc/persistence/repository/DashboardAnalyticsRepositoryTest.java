package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.dto.analytics.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para DashboardAnalyticsRepository.
 *
 * BSS-001: DashboardAnalyticsRepository Test Suite
 * Feature: FP-001A - Dashboard AI Explanation
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DashboardAnalyticsRepositoryTest {

    @Autowired
    private DashboardAnalyticsRepository repository;

    @Test
    @DisplayName("fetchTotals debe retornar totales para rango válido")
    void fetchTotals_withValidRange_returnsTotals() {
        // Given
        LocalDate inicio = LocalDate.of(2025, 6, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);

        // When
        TotalsDto result = repository.fetchTotals(inicio, fin);

        // Then
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.totalRegistros(), "Total registros should not be null");
        assertNotNull(result.totalUnidades(), "Total unidades should not be null");
        assertNotNull(result.pesoNetoTotal(), "Peso neto total should not be null");
        assertTrue(result.totalRegistros() >= 0, "Total registros should be non-negative");
    }

    @Test
    @DisplayName("fetchTotals debe lanzar excepción si rango excede 12 meses")
    void fetchTotals_exceedsMaxRange_throwsException() {
        // Given
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);

        // When/Then
        InvalidDataAccessApiUsageException exception = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> repository.fetchTotals(inicio, fin)
        );

        // Verify wrapper message and root cause
        assertTrue(exception.getMessage().contains("12 meses"),
            "Error message should mention 12 months limit");
        assertNotNull(exception.getCause(), "Should have a cause");
        assertTrue(exception.getCause() instanceof IllegalArgumentException,
            "Root cause should be IllegalArgumentException");
    }

    @Test
    @DisplayName("fetchTotals debe lanzar excepción si fecha inicio es posterior a fecha fin")
    void fetchTotals_startDateAfterEndDate_throwsException() {
        // Given
        LocalDate inicio = LocalDate.of(2025, 6, 30);
        LocalDate fin = LocalDate.of(2025, 6, 1);

        // When/Then
        InvalidDataAccessApiUsageException exception = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> repository.fetchTotals(inicio, fin)
        );

        assertTrue(exception.getMessage().contains("fechaInicio debe ser"),
            "Error message should mention date order");
        assertNotNull(exception.getCause(), "Should have a cause");
        assertTrue(exception.getCause() instanceof IllegalArgumentException,
            "Root cause should be IllegalArgumentException");
    }

    @Test
    @DisplayName("fetchTotals debe lanzar excepción si fechas son nulas")
    void fetchTotals_withNullDates_throwsException() {
        // When/Then - wrap each call and assert the wrapper is thrown
        InvalidDataAccessApiUsageException ex1 = assertThrows(InvalidDataAccessApiUsageException.class,
            () -> repository.fetchTotals(null, LocalDate.now()));
        assertTrue(ex1.getCause() instanceof IllegalArgumentException);

        InvalidDataAccessApiUsageException ex2 = assertThrows(InvalidDataAccessApiUsageException.class,
            () -> repository.fetchTotals(LocalDate.now(), null));
        assertTrue(ex2.getCause() instanceof IllegalArgumentException);

        InvalidDataAccessApiUsageException ex3 = assertThrows(InvalidDataAccessApiUsageException.class,
            () -> repository.fetchTotals(null, null));
        assertTrue(ex3.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("fetchTopOperarios debe retornar máximo 10 resultados")
    void fetchTopOperarios_returnsMax10() {
        // Given
        LocalDate inicio = LocalDate.of(2025, 6, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);

        // When
        List<TopOperarioDto> result = repository.fetchTopOperarios(inicio, fin);

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() <= 10, "Should return maximum 10 results");

        // Verify DESC order by totalUnidades
        for (int i = 1; i < result.size(); i++) {
            assertTrue(
                result.get(i - 1).totalUnidades().compareTo(result.get(i).totalUnidades()) >= 0,
                "Results should be ordered by totalUnidades DESC"
            );
        }
    }

    @Test
    @DisplayName("fetchTopOperarios debe validar rango de fechas")
    void fetchTopOperarios_validatesDateRange() {
        // Given
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);

        // When/Then
        InvalidDataAccessApiUsageException exception = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> repository.fetchTopOperarios(inicio, fin));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("fetchDistribucionTurno debe agrupar por turno")
    void fetchDistribucionTurno_groupsByTurno() {
        // Given
        LocalDate inicio = LocalDate.of(2025, 6, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);

        // When
        List<TurnoDistributionDto> result = repository.fetchDistribucionTurno(inicio, fin);

        // Then
        assertNotNull(result, "Result should not be null");

        // Verify each item has required fields
        result.forEach(dto -> {
            assertNotNull(dto.turno(), "Turno should not be null");
            assertNotNull(dto.totalUnidades(), "Total unidades should not be null");
            assertNotNull(dto.numRegistros(), "Num registros should not be null");
        });

        // Verify DESC order by totalUnidades
        for (int i = 1; i < result.size(); i++) {
            assertTrue(
                result.get(i - 1).totalUnidades().compareTo(result.get(i).totalUnidades()) >= 0,
                "Results should be ordered by totalUnidades DESC"
            );
        }
    }

    @Test
    @DisplayName("fetchDistribucionTurno debe validar rango de fechas")
    void fetchDistribucionTurno_validatesDateRange() {
        // Given
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);

        // When/Then
        InvalidDataAccessApiUsageException exception = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> repository.fetchDistribucionTurno(inicio, fin));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("fetchTopMaquinas debe retornar máximo 10 resultados")
    void fetchTopMaquinas_returnsMax10() {
        // Given
        LocalDate inicio = LocalDate.of(2025, 6, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);

        // When
        List<TopMachineDto> result = repository.fetchTopMaquinas(inicio, fin);

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() <= 10, "Should return maximum 10 results");

        // Verify DESC order by totalUnidades
        for (int i = 1; i < result.size(); i++) {
            assertTrue(
                result.get(i - 1).totalUnidades().compareTo(result.get(i).totalUnidades()) >= 0,
                "Results should be ordered by totalUnidades DESC"
            );
        }
    }

    @Test
    @DisplayName("fetchTopMaquinas debe incluir nombre y código de máquina")
    void fetchTopMaquinas_includesMachineDetails() {
        // Given
        LocalDate inicio = LocalDate.of(2025, 6, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);

        // When
        List<TopMachineDto> result = repository.fetchTopMaquinas(inicio, fin);

        // Then
        result.forEach(dto -> {
            assertNotNull(dto.maquinaNombre(), "Maquina nombre should not be null");
            assertNotNull(dto.maquinaCodigo(), "Maquina codigo should not be null");
            assertNotNull(dto.totalUnidades(), "Total unidades should not be null");
            assertNotNull(dto.numRegistros(), "Num registros should not be null");
        });
    }

    @Test
    @DisplayName("fetchTendenciaDiaria debe retornar datos ordenados por fecha ASC")
    void fetchTendenciaDiaria_returnsOrderedByDateAsc() {
        // Given
        LocalDate inicio = LocalDate.of(2025, 6, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);

        // When
        List<DailyTrendPoint> result = repository.fetchTendenciaDiaria(inicio, fin);

        // Then
        assertNotNull(result, "Result should not be null");

        // Verify ASC order by fecha
        for (int i = 1; i < result.size(); i++) {
            assertTrue(
                result.get(i - 1).fecha().isBefore(result.get(i).fecha()) ||
                result.get(i - 1).fecha().isEqual(result.get(i).fecha()),
                "Results should be ordered by fecha ASC"
            );
        }
    }

    @Test
    @DisplayName("fetchTendenciaDiaria debe incluir todos los campos requeridos")
    void fetchTendenciaDiaria_includesAllFields() {
        // Given
        LocalDate inicio = LocalDate.of(2025, 6, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);

        // When
        List<DailyTrendPoint> result = repository.fetchTendenciaDiaria(inicio, fin);

        // Then
        result.forEach(point -> {
            assertNotNull(point.fecha(), "Fecha should not be null");
            assertNotNull(point.totalUnidades(), "Total unidades should not be null");
            assertNotNull(point.numRegistros(), "Num registros should not be null");
            assertTrue(
                point.fecha().compareTo(inicio) >= 0 && point.fecha().compareTo(fin) <= 0,
                "Fecha should be within requested range"
            );
        });
    }

    @Test
    @DisplayName("fetchTendenciaDiaria debe validar rango de fechas")
    void fetchTendenciaDiaria_validatesDateRange() {
        // Given
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);

        // When/Then
        InvalidDataAccessApiUsageException exception = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> repository.fetchTendenciaDiaria(inicio, fin));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("Todos los métodos deben manejar rangos de un solo día")
    void allMethods_handleSingleDayRange() {
        // Given
        LocalDate fecha = LocalDate.of(2025, 6, 15);

        // When/Then - no exceptions should be thrown
        assertDoesNotThrow(() -> {
            repository.fetchTotals(fecha, fecha);
            repository.fetchTopOperarios(fecha, fecha);
            repository.fetchDistribucionTurno(fecha, fecha);
            repository.fetchTopMaquinas(fecha, fecha);
            repository.fetchTendenciaDiaria(fecha, fecha);
        });
    }

    @Test
    @DisplayName("Todos los métodos deben manejar rangos exactamente de 12 meses")
    void allMethods_handle12MonthRange() {
        // Given
        LocalDate inicio = LocalDate.of(2024, 6, 1);
        LocalDate fin = LocalDate.of(2025, 6, 1); // Exactly 12 months

        // When/Then - no exceptions should be thrown
        assertDoesNotThrow(() -> {
            repository.fetchTotals(inicio, fin);
            repository.fetchTopOperarios(inicio, fin);
            repository.fetchDistribucionTurno(inicio, fin);
            repository.fetchTopMaquinas(inicio, fin);
            repository.fetchTendenciaDiaria(inicio, fin);
        });
    }
}
