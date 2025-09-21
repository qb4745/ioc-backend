package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.NotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    // Track active user sessions for monitoring
    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    /**
     * Sends a notification to a specific user about an ETL job status change
     */
    public void notifyUser(String userId, UUID jobId, NotificationPayload payload) {
        if (!isValidInput(userId, jobId, payload)) {
            log.warn("Invalid notification parameters: userId={}, jobId={}, payload={}", userId, jobId, payload);
            return;
        }

        String destination = String.format("/topic/etl-jobs/%s", jobId);
        try {
            messagingTemplate.convertAndSendToUser(userId, destination, payload);
            activeUsers.add(userId);
            log.debug("Notification sent to user {} for job {}: {}", userId, jobId, payload.status());
        } catch (Exception e) {
            log.error("Failed to send notification to user {} for job {}: {}", userId, jobId, e.getMessage(), e);
            throw new NotificationException("Failed to send notification", e);
        }
    }

    /**
     * Broadcasts a notification to all users subscribed to a specific job
     */
    public void broadcastJobUpdate(UUID jobId, NotificationPayload payload) {
        if (jobId == null || payload == null) {
            log.warn("Invalid broadcast parameters: jobId={}, payload={}", jobId, payload);
            return;
        }

        String destination = String.format("/topic/etl-jobs/%s", jobId);
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.info("Broadcast sent for job {}: {}", jobId, payload.status());
        } catch (Exception e) {
            log.error("Failed to broadcast for job {}: {}", jobId, e.getMessage(), e);
            throw new NotificationException("Failed to broadcast notification", e);
        }
    }

    /**
     * Sends a system-wide notification to all connected users
     */
    public void broadcastSystemMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            log.warn("Cannot broadcast empty system message");
            return;
        }

        NotificationPayload payload = new NotificationPayload("SYSTEM", message);
        try {
            messagingTemplate.convertAndSend("/topic/system", payload);
            log.info("System message broadcasted: {}", message);
        } catch (Exception e) {
            log.error("Failed to broadcast system message: {}", e.getMessage(), e);
            throw new NotificationException("Failed to broadcast system message", e);
        }
    }

    /**
     * Gets the count of active users who have received notifications
     */
    public int getActiveUserCount() {
        return activeUsers.size();
    }

    /**
     * Removes a user from the active users set (for cleanup)
     */
    public void removeActiveUser(String userId) {
        if (userId != null) {
            activeUsers.remove(userId);
            log.debug("User {} removed from active users", userId);
        }
    }

    private boolean isValidInput(String userId, UUID jobId, NotificationPayload payload) {
        return userId != null && !userId.trim().isEmpty()
               && jobId != null
               && payload != null
               && payload.status() != null
               && !payload.status().trim().isEmpty();
    }

    // Custom exception for notification failures
    public static class NotificationException extends RuntimeException {
        public NotificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
