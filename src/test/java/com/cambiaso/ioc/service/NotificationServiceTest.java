package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.NotificationPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        // Reset the active users set for each test
        notificationService.removeActiveUser("test-user");
        notificationService.removeActiveUser("user1");
        notificationService.removeActiveUser("user2");
    }

    @Nested
    @DisplayName("User Notifications")
    class UserNotificationTests {

        @Test
        @DisplayName("Should send notification to specific user with correct destination")
        void whenNotifyUser_thenSendsMessageToCorrectDestination() {
            // Arrange
            String userId = "test-user";
            UUID jobId = UUID.randomUUID();
            NotificationPayload payload = new NotificationPayload("PROCESANDO", "El archivo se está procesando.");
            String expectedDestination = String.format("/topic/etl-jobs/%s", jobId);

            // Act
            notificationService.notifyUser(userId, jobId, payload);

            // Assert
            verify(messagingTemplate).convertAndSendToUser(eq(userId), eq(expectedDestination), eq(payload));
            assertThat(notificationService.getActiveUserCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should not send notification with null parameters")
        void whenNotifyUserWithNullParams_thenDoesNotSendMessage() {
            // Act
            notificationService.notifyUser(null, UUID.randomUUID(), new NotificationPayload("STATUS", "MSG"));
            notificationService.notifyUser("user", null, new NotificationPayload("STATUS", "MSG"));
            notificationService.notifyUser("user", UUID.randomUUID(), null);

            // Assert
            verifyNoInteractions(messagingTemplate);
            assertThat(notificationService.getActiveUserCount()).isZero();
        }

        @Test
        @DisplayName("Should not send notification with empty or whitespace userId")
        void whenNotifyUserWithInvalidUserId_thenDoesNotSendMessage() {
            // Arrange
            UUID jobId = UUID.randomUUID();
            NotificationPayload payload = new NotificationPayload("STATUS", "MSG");

            // Act
            notificationService.notifyUser("", jobId, payload);
            notificationService.notifyUser("   ", jobId, payload);

            // Assert
            verifyNoInteractions(messagingTemplate);
            assertThat(notificationService.getActiveUserCount()).isZero();
        }

        @Test
        @DisplayName("Should not send notification with invalid payload")
        void whenNotifyUserWithInvalidPayload_thenDoesNotSendMessage() {
            // Arrange
            String userId = "test-user";
            UUID jobId = UUID.randomUUID();

            // Act - Test empty or null status
            notificationService.notifyUser(userId, jobId, new NotificationPayload("", "Message"));
            notificationService.notifyUser(userId, jobId, new NotificationPayload("   ", "Message"));
            notificationService.notifyUser(userId, jobId, new NotificationPayload(null, "Message"));

            // Assert
            verifyNoInteractions(messagingTemplate);
            assertThat(notificationService.getActiveUserCount()).isZero();
        }

        @Test
        @DisplayName("Should throw NotificationException when messaging template fails")
        void whenMessagingTemplateFails_thenThrowsNotificationException() {
            // Arrange
            String userId = "test-user";
            UUID jobId = UUID.randomUUID();
            NotificationPayload payload = new NotificationPayload("PROCESANDO", "Processing");

            doThrow(new RuntimeException("WebSocket connection failed"))
                    .when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

            // Act & Assert
            assertThatThrownBy(() -> notificationService.notifyUser(userId, jobId, payload))
                    .isInstanceOf(NotificationService.NotificationException.class)
                    .hasMessage("Failed to send notification")
                    .hasCauseInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Broadcast Notifications")
    class BroadcastNotificationTests {

        @Test
        @DisplayName("Should broadcast job update to all subscribers")
        void whenBroadcastJobUpdate_thenSendsToCorrectTopic() {
            // Arrange
            UUID jobId = UUID.randomUUID();
            NotificationPayload payload = new NotificationPayload("COMPLETADO", "Job finished successfully");
            String expectedDestination = String.format("/topic/etl-jobs/%s", jobId);

            // Act
            notificationService.broadcastJobUpdate(jobId, payload);

            // Assert
            verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(payload));
        }

        @Test
        @DisplayName("Should not broadcast with null parameters")
        void whenBroadcastJobUpdateWithNullParams_thenDoesNotSend() {
            // Act
            notificationService.broadcastJobUpdate(null, new NotificationPayload("STATUS", "MSG"));
            notificationService.broadcastJobUpdate(UUID.randomUUID(), null);

            // Assert
            verifyNoInteractions(messagingTemplate);
        }

        @Test
        @DisplayName("Should throw NotificationException when broadcast fails")
        void whenBroadcastJobUpdateFails_thenThrowsNotificationException() {
            // Arrange
            UUID jobId = UUID.randomUUID();
            NotificationPayload payload = new NotificationPayload("ERROR", "Job failed");

            doThrow(new RuntimeException("Broadcast failed"))
                    .when(messagingTemplate).convertAndSend(anyString(), any(NotificationPayload.class));

            // Act & Assert
            assertThatThrownBy(() -> notificationService.broadcastJobUpdate(jobId, payload))
                    .isInstanceOf(NotificationService.NotificationException.class)
                    .hasMessage("Failed to broadcast notification");
        }
    }

    @Nested
    @DisplayName("System Messages")
    class SystemMessageTests {

        @Test
        @DisplayName("Should broadcast system message to all users")
        void whenBroadcastSystemMessage_thenSendsToSystemTopic() {
            // Arrange
            String message = "System maintenance scheduled for tonight";
            NotificationPayload expectedPayload = new NotificationPayload("SYSTEM", message);

            // Act
            notificationService.broadcastSystemMessage(message);

            // Assert
            verify(messagingTemplate).convertAndSend(eq("/topic/system"), eq(expectedPayload));
        }

        @Test
        @DisplayName("Should not broadcast empty or null system messages")
        void whenBroadcastEmptySystemMessage_thenDoesNotSend() {
            // Act
            notificationService.broadcastSystemMessage(null);
            notificationService.broadcastSystemMessage("");
            notificationService.broadcastSystemMessage("   ");

            // Assert
            verifyNoInteractions(messagingTemplate);
        }

        @Test
        @DisplayName("Should throw NotificationException when system broadcast fails")
        void whenBroadcastSystemMessageFails_thenThrowsNotificationException() {
            // Arrange
            String message = "Important system update";
            NotificationPayload expectedPayload = new NotificationPayload("SYSTEM", message);
            doThrow(new RuntimeException("System broadcast failed"))
                    .when(messagingTemplate).convertAndSend(eq("/topic/system"), eq(expectedPayload));

            // Act & Assert
            assertThatThrownBy(() -> notificationService.broadcastSystemMessage(message))
                    .isInstanceOf(NotificationService.NotificationException.class)
                    .hasMessage("Failed to broadcast system message");
        }
    }

    @Nested
    @DisplayName("User Management")
    class UserManagementTests {

        @Test
        @DisplayName("Should track active users correctly")
        void whenMultipleUsersNotified_thenTracksActiveUsers() {
            // Arrange
            UUID jobId = UUID.randomUUID();
            NotificationPayload payload = new NotificationPayload("COMPLETADO", "Job finished");

            // Act
            notificationService.notifyUser("user1", jobId, payload);
            notificationService.notifyUser("user2", jobId, payload);
            notificationService.notifyUser("user1", jobId, payload); // Duplicate user

            // Assert
            assertThat(notificationService.getActiveUserCount()).isEqualTo(2); // Should not duplicate
            verify(messagingTemplate, times(3)).convertAndSendToUser(anyString(), anyString(), eq(payload));
        }

        @Test
        @DisplayName("Should remove active user correctly")
        void whenRemoveActiveUser_thenUserCountDecreases() {
            // Arrange
            UUID jobId = UUID.randomUUID();
            NotificationPayload payload = new NotificationPayload("STARTED", "Job started");

            notificationService.notifyUser("user1", jobId, payload);
            notificationService.notifyUser("user2", jobId, payload);

            // Act
            notificationService.removeActiveUser("user1");

            // Assert
            assertThat(notificationService.getActiveUserCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle null user removal gracefully")
        void whenRemoveNullUser_thenNoException() {
            // Act & Assert - Should not throw exception
            notificationService.removeActiveUser(null);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Performance")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle concurrent notifications correctly")
        void whenConcurrentNotifications_thenAllProcessed() {
            // Arrange
            UUID jobId = UUID.randomUUID();
            NotificationPayload payload = new NotificationPayload("PROCESSING", "Processing multiple files");

            // Act - Simulate concurrent calls
            for (int i = 0; i < 10; i++) {
                notificationService.notifyUser("user" + i, jobId, payload);
            }

            // Assert
            verify(messagingTemplate, times(10)).convertAndSendToUser(anyString(), anyString(), eq(payload));
            assertThat(notificationService.getActiveUserCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should handle different ETL job statuses correctly")
        void whenDifferentJobStatuses_thenAllSentCorrectly() {
            // Arrange
            String userId = "test-user";
            UUID jobId = UUID.randomUUID();

            NotificationPayload[] payloads = {
                new NotificationPayload("INICIADO", "ETL job started"),
                new NotificationPayload("PROCESANDO", "Processing file"),
                new NotificationPayload("VALIDANDO", "Validating data"),
                new NotificationPayload("COMPLETADO", "Job completed successfully"),
                new NotificationPayload("FALLIDO", "Job failed with errors")
            };

            // Act
            for (NotificationPayload payload : payloads) {
                notificationService.notifyUser(userId, jobId, payload);
            }

            // Assert
            for (NotificationPayload payload : payloads) {
                verify(messagingTemplate).convertAndSendToUser(eq(userId), anyString(), eq(payload));
            }
        }
    }
}
