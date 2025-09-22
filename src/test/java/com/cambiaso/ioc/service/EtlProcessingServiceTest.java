package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.NotificationPayload;
import com.cambiaso.ioc.exception.JobConflictException;
import com.cambiaso.ioc.persistence.entity.EtlJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.UUID;

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

    @InjectMocks
    private EtlProcessingService etlProcessingService;

    private UUID testJobId;
    private String testUserId;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        testJobId = UUID.randomUUID();
        testUserId = "test-user";
        testFile = new MockMultipartFile("test.csv", "test.csv", "text/csv", "data,value\n1,test".getBytes());
    }

    @Nested
    @DisplayName("File Hash Calculation Tests")
    class FileHashTests {

        @Test
        @DisplayName("Should calculate consistent hash for same file content")
        void calculateFileHash_sameContent_shouldReturnSameHash() {
            MockMultipartFile file1 = new MockMultipartFile("file1.csv", "test.csv", "text/csv", "same,content\n1,test".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("file2.csv", "different-name.csv", "text/csv", "same,content\n1,test".getBytes());

            String hash1 = etlProcessingService.calculateFileHash(file1);
            String hash2 = etlProcessingService.calculateFileHash(file2);

            assertThat(hash1).isEqualTo(hash2);
            assertThat(hash1).hasSize(64); // SHA-256 produces 64-character hex string
        }

        @Test
        @DisplayName("Should calculate different hashes for different content")
        void calculateFileHash_differentContent_shouldReturnDifferentHashes() {
            MockMultipartFile file1 = new MockMultipartFile("file1.csv", "test.csv", "text/csv", "content1\n1,test".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("file2.csv", "test.csv", "text/csv", "content2\n2,test".getBytes());

            String hash1 = etlProcessingService.calculateFileHash(file1);
            String hash2 = etlProcessingService.calculateFileHash(file2);

            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("Should handle empty files gracefully")
        void calculateFileHash_emptyFile_shouldNotThrow() {
            MockMultipartFile emptyFile = new MockMultipartFile("empty.csv", "empty.csv", "text/csv", new byte[0]);

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
        void processFile_validFile_shouldCompleteSuccessfully() throws InterruptedException {
            // Arrange
            when(etlJobService.isWindowLocked(any(LocalDate.class), any(LocalDate.class))).thenReturn(false);

            // Act
            etlProcessingService.processFile(testFile, testUserId, testJobId);

            // Allow async processing to complete
            Thread.sleep(100);

            // Assert
            verify(etlJobService).updateJobDateRange(eq(testJobId), any(LocalDate.class), any(LocalDate.class));
            verify(etlJobService).isWindowLocked(any(LocalDate.class), any(LocalDate.class));
            verify(etlJobService).updateJobStatus(testJobId, "EXITO", "ETL process completed successfully.");

            verify(notificationService, times(3)).notifyUser(eq(testUserId), eq(testJobId), any(NotificationPayload.class));
        }

        @Test
        @DisplayName("Should handle window lock conflict gracefully")
        void processFile_windowLocked_shouldFailGracefully() throws InterruptedException {
            // Arrange
            when(etlJobService.isWindowLocked(any(LocalDate.class), any(LocalDate.class))).thenReturn(true);

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
        void processFile_unexpectedError_shouldFailGracefully() throws InterruptedException {
            // Arrange
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
        void processFile_shouldSendNotificationsAtEachStage() throws InterruptedException {
            // Arrange
            when(etlJobService.isWindowLocked(any(LocalDate.class), any(LocalDate.class))).thenReturn(false);

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
        void processFile_onError_shouldSendFailureNotification() throws InterruptedException {
            // Arrange
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
        void processFile_largeFile_shouldProcess() throws InterruptedException {
            // Arrange - Create a large file (1MB)
            byte[] largeContent = new byte[1024 * 1024];
            MockMultipartFile largeFile = new MockMultipartFile("large.csv", "large.csv", "text/csv", largeContent);
            when(etlJobService.isWindowLocked(any(LocalDate.class), any(LocalDate.class))).thenReturn(false);

            // Act
            etlProcessingService.processFile(largeFile, testUserId, testJobId);

            // Allow async processing to complete
            Thread.sleep(200);

            // Assert
            verify(etlJobService).updateJobStatus(testJobId, "EXITO", "ETL process completed successfully.");
        }
    }
}
