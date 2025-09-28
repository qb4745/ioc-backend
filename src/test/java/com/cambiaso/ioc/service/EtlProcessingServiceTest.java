package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.NotificationPayload;
import com.cambiaso.ioc.persistence.entity.FactProduction;
import com.cambiaso.ioc.persistence.entity.FactProductionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EtlProcessingService Unit Tests")
class EtlProcessingServiceTest {

    @Mock
    private EtlJobService etlJobService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DataSyncService dataSyncService;

    @Mock
    private ParserService parserService;

    @InjectMocks
    private EtlProcessingService etlProcessingService;

    private UUID testJobId;
    private String testUserId;
    private MockMultipartFile testFile;
    private List<FactProduction> mockRecords;

    @BeforeEach
    void setUp() {
        testJobId = UUID.randomUUID();
        testUserId = "test-user";
        testFile = new MockMultipartFile("test.txt", "test.txt", "text/plain",
                "| @08@  |30.08.2025  |08:29:15|01.09.2025  |2922290|6760161400|48,000|105,6|".getBytes());

        // Create mock FactProduction records for tests
        FactProduction mockRecord = new FactProduction();
        // Provide a dummy 'id' for the composite key in the test setup
        FactProductionId mockId = new FactProductionId(1L, LocalDate.of(2025, 8, 30));
        mockRecord.setId(mockId);
        mockRecord.setNumeroLog(2922290L);
        mockRecord.setHoraContabilizacion(LocalTime.of(8, 29, 15));
        mockRecord.setFechaNotificacion(LocalDate.of(2025, 9, 1));
        mockRecord.setMaterialSku(6760161400L);
        mockRecord.setCantidad(new BigDecimal("48.000"));
        mockRecord.setPesoNeto(new BigDecimal("105.6"));
        mockRecord.setTurno("A");

        mockRecords = List.of(mockRecord);
    }

    @Nested
    @DisplayName("File Hash Calculation Tests")
    class FileHashTests {

        @Test
        @DisplayName("Should calculate consistent hash for same file content")
        void calculateFileHash_sameContent_shouldReturnSameHash() {
            MockMultipartFile file1 = new MockMultipartFile("file1.txt", "test.txt", "text/plain",
                "| @08@  |30.08.2025  |08:29:15|01.09.2025  |2922290|6760161400|48,000|105,6|".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("file2.txt", "different-name.txt", "text/plain",
                "| @08@  |30.08.2025  |08:29:15|01.09.2025  |2922290|6760161400|48,000|105,6|".getBytes());

            String hash1 = etlProcessingService.calculateFileHash(file1);
            String hash2 = etlProcessingService.calculateFileHash(file2);

            assertThat(hash1).isEqualTo(hash2);
            assertThat(hash1).hasSize(64); // SHA-256 produces 64-character hex string
        }

        @Test
        @DisplayName("Should calculate different hashes for different content")
        void calculateFileHash_differentContent_shouldReturnDifferentHashes() {
            MockMultipartFile file1 = new MockMultipartFile("file1.txt", "test.txt", "text/plain",
                "| @08@  |30.08.2025  |08:29:15|01.09.2025  |2922290|6760161400|48,000|105,6|".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("file2.txt", "test.txt", "text/plain",
                "| @09@  |30.08.2025  |08:22:36|01.09.2025  |2922278|6602030100|87,000|99,6|".getBytes());

            String hash1 = etlProcessingService.calculateFileHash(file1);
            String hash2 = etlProcessingService.calculateFileHash(file2);

            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("Should handle empty files gracefully")
        void calculateFileHash_emptyFile_shouldNotThrow() {
            MockMultipartFile emptyFile = new MockMultipartFile("empty.txt", "empty.txt", "text/plain", new byte[0]);

            String hash = etlProcessingService.calculateFileHash(emptyFile);

            assertThat(hash).isNotNull();
            assertThat(hash).hasSize(64);
        }
    }

    @Nested
    @DisplayName("Async Processing Tests")
    class AsyncProcessingTests {

        @Test
        @DisplayName("Should successfully process file through all stages")
        void processFile_validFile_shouldCompleteSuccessfully() throws InterruptedException, IOException {
            // Arrange
            when(parserService.parse(any(InputStream.class))).thenReturn(mockRecords);
            when(etlJobService.isWindowLocked(eq(testJobId), any(LocalDate.class), any(LocalDate.class))).thenReturn(false);

            // Act
            etlProcessingService.processFile(testFile, testUserId, testJobId);

            // Allow async processing to complete
            Thread.sleep(100);

            // Assert
            verify(parserService).parse(any(InputStream.class));
            verify(etlJobService).updateJobDateRange(eq(testJobId), any(LocalDate.class), any(LocalDate.class));
            verify(etlJobService).isWindowLocked(eq(testJobId), any(LocalDate.class), any(LocalDate.class));
            verify(dataSyncService).syncWithDeleteInsert(any(LocalDate.class), any(LocalDate.class), eq(mockRecords));
            verify(etlJobService).updateJobStatus(eq(testJobId), eq("EXITO"), contains("ETL process completed successfully"));

            verify(notificationService, times(3)).notifyUser(eq(testUserId), eq(testJobId), any(NotificationPayload.class));
        }

        @Test
        @DisplayName("Should handle window lock conflict gracefully")
        void processFile_windowLocked_shouldFailGracefully() throws InterruptedException, IOException {
            // Arrange
            when(parserService.parse(any(InputStream.class))).thenReturn(mockRecords);
            when(etlJobService.isWindowLocked(eq(testJobId), any(LocalDate.class), any(LocalDate.class))).thenReturn(true);

            // Act
            etlProcessingService.processFile(testFile, testUserId, testJobId);

            // Allow async processing to complete
            Thread.sleep(100);

            // Assert
            verify(etlJobService).updateJobStatus(eq(testJobId), eq("FALLO"), contains("Another ETL job is processing this date range"));
            verify(notificationService).notifyUser(eq(testUserId), eq(testJobId),
                    argThat(payload -> "FALLO".equals(payload.getStatus())));
        }

        @Test
        @DisplayName("Should handle unexpected errors gracefully")
        void processFile_unexpectedError_shouldFailGracefully() throws InterruptedException, IOException {
            // Arrange
            when(parserService.parse(any(InputStream.class))).thenReturn(mockRecords);
            doThrow(new RuntimeException("Database connection failed"))
                    .when(etlJobService).updateJobDateRange(any(UUID.class), any(LocalDate.class), any(LocalDate.class));

            // Act
            etlProcessingService.processFile(testFile, testUserId, testJobId);

            // Allow async processing to complete
            Thread.sleep(100);

            // Assert
            verify(etlJobService).updateJobStatus(eq(testJobId), eq("FALLO"), contains("Database connection failed"));
        }
    }

    @Nested
    @DisplayName("Notification Integration Tests")
    class NotificationTests {

        @Test
        @DisplayName("Should send notifications at each processing stage")
        void processFile_shouldSendNotificationsAtEachStage() throws InterruptedException, IOException {
            // Arrange
            when(parserService.parse(any(InputStream.class))).thenReturn(mockRecords);
            when(etlJobService.isWindowLocked(eq(testJobId), any(LocalDate.class), any(LocalDate.class))).thenReturn(false);

            // Act
            etlProcessingService.processFile(testFile, testUserId, testJobId);

            // Allow async processing to complete
            Thread.sleep(100);

            // Assert - verify notification sequence
            verify(notificationService).notifyUser(testUserId, testJobId,
                    new NotificationPayload("PROCESANDO", "Parsing file content."));
            verify(notificationService).notifyUser(testUserId, testJobId,
                    new NotificationPayload("SINCRONIZANDO", "Writing data to database."));
            verify(notificationService).notifyUser(testUserId, testJobId,
                    new NotificationPayload("EXITO", "Process finished."));
        }

        @Test
        @DisplayName("Should send failure notification on error")
        void processFile_onError_shouldSendFailureNotification() throws InterruptedException, IOException {
            // Arrange
            when(parserService.parse(any(InputStream.class))).thenReturn(mockRecords);
            doThrow(new RuntimeException("Processing failed")).when(etlJobService)
                    .updateJobDateRange(any(UUID.class), any(LocalDate.class), any(LocalDate.class));

            // Act
            etlProcessingService.processFile(testFile, testUserId, testJobId);

            // Allow async processing to complete
            Thread.sleep(100);

            // Assert
            verify(notificationService).notifyUser(eq(testUserId), eq(testJobId),
                    argThat(payload -> "FALLO".equals(payload.getStatus()) &&
                                     payload.getMessage().contains("Processing failed")));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null file gracefully")
        void processFile_nullFile_shouldHandleGracefully() throws InterruptedException {
            // Act
            etlProcessingService.processFile(null, testUserId, testJobId);

            // Allow async processing to complete
            Thread.sleep(100);

            // Assert
            verify(etlJobService).updateJobStatus(eq(testJobId), eq("FALLO"), anyString());
        }

        @Test
        @DisplayName("Should handle very large files")
        void processFile_largeFile_shouldProcess() throws InterruptedException, IOException {
            // Arrange - Create a large file (1MB) with valid TXT format
            String largeContent = "| @08@  |30.08.2025  |08:29:15|01.09.2025  |2922290|6760161400|48,000|105,6|\n".repeat(10000);
            MockMultipartFile largeFile = new MockMultipartFile("large.txt", "large.txt", "text/plain", largeContent.getBytes());
            when(parserService.parse(any(InputStream.class))).thenReturn(mockRecords);
            when(etlJobService.isWindowLocked(eq(testJobId), any(LocalDate.class), any(LocalDate.class))).thenReturn(false);

            // Act
            etlProcessingService.processFile(largeFile, testUserId, testJobId);

            // Allow async processing to complete
            Thread.sleep(200);

            // Assert
            verify(etlJobService).updateJobStatus(eq(testJobId), eq("EXITO"), contains("ETL process completed successfully"));
        }

        @Test
        @DisplayName("Should handle empty file gracefully")
        void processFile_emptyFileContent_shouldFinishAsSuccess() throws InterruptedException, IOException {
            // Arrange - parser returns empty list for empty file
            when(parserService.parse(any(InputStream.class))).thenReturn(List.of());

            // Act
            etlProcessingService.processFile(testFile, testUserId, testJobId);

            // Allow async processing to complete
            Thread.sleep(100);

            // Assert
            verify(etlJobService).updateJobStatus(eq(testJobId), eq("EXITO"), contains("No data rows found to sync"));
            verify(notificationService).notifyUser(eq(testUserId), eq(testJobId),
                    argThat(payload -> "EXITO".equals(payload.getStatus())));
        }
    }
}
