package com.halolight.web.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for notification data.
 * Contains all information about a notification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    /**
     * Unique identifier for the notification
     */
    private String id;

    /**
     * ID of the user who receives this notification
     */
    private String userId;

    /**
     * Title of the notification
     */
    private String title;

    /**
     * Detailed message content
     */
    private String message;

    /**
     * Type/category of the notification
     * Examples: system, task, message, alert, user
     */
    private String type;

    /**
     * Whether the notification has been read
     */
    private Boolean isRead;

    /**
     * Timestamp when the notification was read (null if unread)
     */
    private Instant readAt;

    /**
     * Optional action URL (e.g., link to related task or document)
     */
    private String actionUrl;

    /**
     * Timestamp when the notification was created
     */
    private Instant createdAt;

    /**
     * Optional sender information (for user-generated notifications)
     */
    private SenderInfo sender;

    /**
     * Nested class for sender information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SenderInfo {
        private String id;
        private String name;
        private String username;
        private String email;
        private String avatar;
    }
}
