package com.halolight.web.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for unread notification count.
 * Simple wrapper for returning the count of unread notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCountResponse {

    /**
     * Number of unread notifications for the user
     */
    private Long count;
}
