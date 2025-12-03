package com.halolight.web.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new notification.
 * Used internally by the system to create notifications for users.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    /**
     * ID of the user who will receive this notification
     */
    @NotBlank(message = "User ID is required")
    @Size(max = 40, message = "User ID must not exceed 40 characters")
    private String userId;

    /**
     * Title of the notification (short summary)
     */
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    /**
     * Detailed message content
     */
    @NotBlank(message = "Message is required")
    private String message;

    /**
     * Type/category of the notification
     * Common types: system, task, message, alert, user
     */
    @NotBlank(message = "Type is required")
    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;

    /**
     * Optional URL for action button (e.g., /tasks/123)
     */
    @Size(max = 500, message = "Action URL must not exceed 500 characters")
    private String actionUrl;

    /**
     * Whether the notification should be marked as read immediately
     * Default: false
     */
    @Builder.Default
    private Boolean isRead = false;
}
