package com.halolight.controller;

import com.halolight.dto.ApiResponse;
import com.halolight.service.NotificationService;
import com.halolight.web.dto.notification.NotificationCountResponse;
import com.halolight.web.dto.notification.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing user notifications.
 * Provides endpoints for retrieving, reading, and deleting notifications.
 */
@Slf4j
@Tag(name = "Notifications", description = "User notification management API endpoints")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for the current user.
     * Supports pagination and returns notifications in descending order by creation time.
     *
     * @param authentication Current user authentication
     * @param pageable       Pagination parameters
     * @return Paginated list of notifications
     */
    @Operation(
            summary = "Get user notifications",
            description = "Retrieve all notifications for the authenticated user with pagination support"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Notifications retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing authentication token"
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            Authentication authentication,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String userId = getUserId(authentication);
        log.debug("Fetching notifications for user: {}", userId);

        // For now, return all notifications without pagination
        // In production, you might want to return Page<NotificationResponse>
        List<NotificationResponse> notifications = notificationService.getAllUserNotifications(userId);

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Get notifications with pagination support.
     *
     * @param authentication Current user authentication
     * @param pageable       Pagination parameters
     * @return Paginated notifications
     */
    @Operation(
            summary = "Get paginated notifications",
            description = "Retrieve paginated notifications for the authenticated user"
    )
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getPaginatedNotifications(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String userId = getUserId(authentication);
        log.debug("Fetching paginated notifications for user: {}", userId);

        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Get count of unread notifications.
     *
     * @param authentication Current user authentication
     * @return Unread notification count
     */
    @Operation(
            summary = "Get unread notification count",
            description = "Retrieve the count of unread notifications for the authenticated user"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Unread count retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing authentication token"
            )
    })
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<NotificationCountResponse>> getUnreadCount(
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        log.debug("Fetching unread count for user: {}", userId);

        NotificationCountResponse count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Get only unread notifications.
     *
     * @param authentication Current user authentication
     * @return List of unread notifications
     */
    @Operation(
            summary = "Get unread notifications",
            description = "Retrieve only unread notifications for the authenticated user"
    )
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        log.debug("Fetching unread notifications for user: {}", userId);

        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Get notifications by type.
     *
     * @param authentication Current user authentication
     * @param type           Notification type filter
     * @param pageable       Pagination parameters
     * @return Paginated notifications of specified type
     */
    @Operation(
            summary = "Get notifications by type",
            description = "Retrieve notifications filtered by type (e.g., system, task, message, alert, user)"
    )
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotificationsByType(
            Authentication authentication,
            @Parameter(description = "Notification type (system, task, message, alert, user)")
            @PathVariable String type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String userId = getUserId(authentication);
        log.debug("Fetching notifications of type '{}' for user: {}", type, userId);

        Page<NotificationResponse> notifications = notificationService.getNotificationsByType(userId, type, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Mark a notification as read.
     *
     * @param id             Notification ID
     * @param authentication Current user authentication
     * @return Updated notification
     */
    @Operation(
            summary = "Mark notification as read",
            description = "Mark a specific notification as read and update the readAt timestamp"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Notification marked as read successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Notification not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - notification does not belong to user"
            )
    })
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @Parameter(description = "Notification ID")
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        log.info("Marking notification {} as read for user: {}", id, userId);

        NotificationResponse notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    /**
     * Mark all notifications as read.
     *
     * @param authentication Current user authentication
     * @return Success response with count of updated notifications
     */
    @Operation(
            summary = "Mark all notifications as read",
            description = "Mark all unread notifications as read for the authenticated user"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "All notifications marked as read successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing authentication token"
            )
    })
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        log.info("Marking all notifications as read for user: {}", userId);

        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "All notifications marked as read",
                        Map.of("updatedCount", count)
                )
        );
    }

    /**
     * Delete a notification.
     *
     * @param id             Notification ID
     * @param authentication Current user authentication
     * @return Success response
     */
    @Operation(
            summary = "Delete notification",
            description = "Delete a specific notification for the authenticated user"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Notification deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Notification not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - notification does not belong to user"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @Parameter(description = "Notification ID")
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        log.info("Deleting notification {} for user: {}", id, userId);

        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }

    /**
     * Delete all notifications for the current user.
     *
     * @param authentication Current user authentication
     * @return Success response
     */
    @Operation(
            summary = "Delete all notifications",
            description = "Delete all notifications for the authenticated user"
    )
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Void>> deleteAllNotifications(
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        log.info("Deleting all notifications for user: {}", userId);

        notificationService.deleteAllUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications deleted successfully", null));
    }

    /**
     * Extract user ID from authentication.
     * This assumes the authentication principal contains user information.
     *
     * @param authentication Spring Security authentication object
     * @return User ID
     */
    private String getUserId(Authentication authentication) {
        // For now, we'll use the username/name as ID
        // TODO: Update this based on your actual UserPrincipal implementation
        // If UserPrincipal has been updated to use String id, use:
        // return ((UserPrincipal) authentication.getPrincipal()).getId();

        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }

        // Using authentication name as fallback
        return authentication.getName();
    }
}
