package com.cambiaso.ioc.service;

import com.cambiaso.ioc.persistence.entity.EtlJob;
import com.cambiaso.ioc.persistence.repository.EtlJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("EtlJobService Tests")
class EtlJobServiceTest {

    @Autowired
    private EtlJobService etlJobService;

    @Autowired
    private EtlJobRepository etlJobRepository;

    @BeforeEach
    void setUp() {
        etlJobRepository.deleteAll();
    }

    @Nested
    @DisplayName("Job Creation")
    class JobCreationTests {

        @Test
        @DisplayName("Should create job with initial status")
        void createJob_shouldCreateJobWithInitialStatus() {
            EtlJob job = etlJobService.createJob("test.csv", "hash123", "user1");

            assertThat(job).isNotNull();
            assertThat(job.getJobId()).isNotNull();
            assertThat(job.getStatus()).isEqualTo("INICIADO");
            assertThat(job.getFileName()).isEqualTo("test.csv");
            assertThat(job.getFileHash()).isEqualTo("hash123");
            assertThat(job.getUserId()).isEqualTo("user1");
            assertThat(job.getCreatedAt()).isNotNull();
            assertThat(etlJobRepository.findById(job.getJobId())).isPresent();
        }

        @Test
        @DisplayName("Should find job by file hash")
        void findByFileHash_shouldReturnJobIfExists() {
            EtlJob created = etlJobService.createJob("test.csv", "hash123", "user1");

            Optional<EtlJob> found = etlJobService.findByFileHash("hash123");

            assertThat(found).isPresent();
            assertThat(found.get().getJobId()).isEqualTo(created.getJobId());
            assertThat(found.get().getFileName()).isEqualTo("test.csv");
        }

        @Test
        @DisplayName("Should return empty when file hash not found")
        void findByFileHash_shouldReturnEmptyWhenNotFound() {
            Optional<EtlJob> found = etlJobService.findByFileHash("nonexistent");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Job Status Updates")
    class JobStatusUpdateTests {

        @Test
        @DisplayName("Should update job status and details")
        void updateJobStatus_shouldUpdateStatusAndDetails() {
            EtlJob job = etlJobService.createJob("test.csv", "hash123", "user1");

            etlJobService.updateJobStatus(job.getJobId(), "PROCESANDO", "Processing file");

            EtlJob updated = etlJobRepository.findById(job.getJobId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo("PROCESANDO");
            assertThat(updated.getDetails()).isEqualTo("Processing file");
            assertThat(updated.getFinishedAt()).isNull(); // Should not set finished time for in-progress status
        }

        @Test
        @DisplayName("Should set finished time for completion statuses")
        void updateJobStatus_shouldSetFinishedTimeForCompletionStatuses() {
            EtlJob job = etlJobService.createJob("test.csv", "hash123", "user1");
            OffsetDateTime beforeUpdate = OffsetDateTime.now();

            etlJobService.updateJobStatus(job.getJobId(), "EXITO", "Completed successfully");

            EtlJob updated = etlJobRepository.findById(job.getJobId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo("EXITO");
            assertThat(updated.getDetails()).isEqualTo("Completed successfully");
            assertThat(updated.getFinishedAt()).isNotNull();
            assertThat(updated.getFinishedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("Should set finished time for failure statuses")
        void updateJobStatus_shouldSetFinishedTimeForFailureStatuses() {
            EtlJob job = etlJobService.createJob("test.csv", "hash123", "user1");

            etlJobService.updateJobStatus(job.getJobId(), "FALLO", "Process failed");

            EtlJob updated = etlJobRepository.findById(job.getJobId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo("FALLO");
            assertThat(updated.getFinishedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when job not found")
        void updateJobStatus_shouldThrowExceptionWhenJobNotFound() {
            UUID nonExistentId = UUID.randomUUID();

            assertThatThrownBy(() -> etlJobService.updateJobStatus(nonExistentId, "PROCESANDO", "test")).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Job not found with ID: " + nonExistentId);
        }
    }

    @Nested
    @DisplayName("Date Range Updates")
    class DateRangeUpdateTests {

        @Test
        @DisplayName("Should update job date range")
        void updateJobDateRange_shouldUpdateDates() {
            EtlJob job = etlJobService.createJob("test.csv", "hash123", "user1");
            LocalDate minDate = LocalDate.of(2025, 1, 1);
            LocalDate maxDate = LocalDate.of(2025, 1, 31);

            etlJobService.updateJobDateRange(job.getJobId(), minDate, maxDate);

            EtlJob updated = etlJobRepository.findById(job.getJobId()).orElseThrow();
            assertThat(updated.getMinDate()).isEqualTo(minDate);
            assertThat(updated.getMaxDate()).isEqualTo(maxDate);
        }

        @Test
        @DisplayName("Should throw exception when job not found for date range update")
        void updateJobDateRange_shouldThrowExceptionWhenJobNotFound() {
            UUID nonExistentId = UUID.randomUUID();

            assertThatThrownBy(() -> etlJobService.updateJobDateRange(nonExistentId, LocalDate.now(), LocalDate.now().plusDays(1))).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Job not found with ID: " + nonExistentId);
        }
    }

    @Nested
    class WindowLockGuardTests {

        @Test
        void isWindowLocked_shouldReturnFalseWhenNoActiveJobs() {
            boolean isLocked = etlJobService.isWindowLocked(UUID.randomUUID(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
            assertThat(isLocked).isFalse();
        }

        @Test
        void isWindowLocked_shouldReturnTrueForOverlappingDateRange() {
            // Arrange: an active job in January
            EtlJob activeJob = etlJobService.createJob("active.csv", "active_hash", "user1");
            etlJobService.updateJobDateRange(activeJob.getJobId(), LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 20));
            etlJobService.updateJobStatus(activeJob.getJobId(), "PROCESANDO", null);

            // Act & Assert: check for a new job that overlaps (must use a different jobId)
            boolean isLocked = etlJobService.isWindowLocked(UUID.randomUUID(), LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 25));
            assertThat(isLocked).isTrue();
        }

        @Test
        void isWindowLocked_shouldReturnFalseForNonOverlappingDateRange() {
            // Arrange: an active job in January
            EtlJob activeJob = etlJobService.createJob("active.csv", "active_hash", "user1");
            etlJobService.updateJobDateRange(activeJob.getJobId(), LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 20));
            etlJobService.updateJobStatus(activeJob.getJobId(), "PROCESANDO", null);

            // Act & Assert: check for a new job in February
            boolean isLocked = etlJobService.isWindowLocked(UUID.randomUUID(), LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28));
            assertThat(isLocked).isFalse();
        }

        @Test
        void isWindowLocked_shouldReturnFalseForJobWithFinishedStatus() {
            // Arrange: a finished job in January
            EtlJob finishedJob = etlJobService.createJob("finished.csv", "finished_hash", "user1");
            etlJobService.updateJobDateRange(finishedJob.getJobId(), LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 20));
            etlJobService.updateJobStatus(finishedJob.getJobId(), "EXITO", "Completed");

            // Act & Assert: check for a new job in the same range
            boolean isLocked = etlJobService.isWindowLocked(UUID.randomUUID(), LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 25));
            assertThat(isLocked).isFalse();
        }

        @Test
        void isWindowLocked_shouldNotBlockItself() {
            // Arrange: an active job in January
            EtlJob activeJob = etlJobService.createJob("active.csv", "active_hash", "user1");
            etlJobService.updateJobDateRange(activeJob.getJobId(), LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 20));
            etlJobService.updateJobStatus(activeJob.getJobId(), "PROCESANDO", null);

            // Act & Assert: check if the job blocks itself (it shouldn't)
            boolean isLocked = etlJobService.isWindowLocked(activeJob.getJobId(), LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 25));
            assertThat(isLocked).isFalse();
        }
    }
}
