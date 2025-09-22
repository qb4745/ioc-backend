package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.FactProduction;
import com.cambiaso.ioc.persistence.repository.FactProductionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {

    private final FactProductionRepository factProductionRepository;

    @Transactional
    public void syncWithDeleteInsert(LocalDate minDate, LocalDate maxDate, @NonNull List<FactProduction> records) {
        try {
            log.info("Starting data sync for date range {} to {} with {} records",
                    minDate, maxDate, records.size());

            // Step 1: Delete existing records in the date range.
            // This operation will be rolled back if the subsequent insertion fails.
            factProductionRepository.deleteByFechaContabilizacionBetween(minDate, maxDate);
            log.debug("Deleted existing records in date range {} to {}", minDate, maxDate);

            // Step 2: Insert the new batch of records.
            if (!records.isEmpty()) {
                factProductionRepository.saveAll(records);
                // We must flush here to force Hibernate to send the INSERT statements to the DB.
                // This will trigger any database-level constraints (like NOT NULL) within the transaction.
                factProductionRepository.flush();
                log.info("Successfully synced {} records for date range {} to {}",
                        records.size(), minDate, maxDate);
            } else {
                log.info("No records to sync for date range {} to {}", minDate, maxDate);
            }
        } catch (Exception e) {
            log.error("Failed to sync data for date range {} to {}: {}",
                    minDate, maxDate, e.getMessage(), e);
            throw new DataSyncException("Failed to sync data for date range " + minDate + " to " + maxDate, e);
        }
    }

    /**
     * Custom exception for data synchronization failures
     */
    public static class DataSyncException extends RuntimeException {
        public DataSyncException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
