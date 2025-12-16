package com.halolight.service;

import com.halolight.domain.entity.Notification;
import com.halolight.domain.entity.User;
import com.halolight.domain.repository.NotificationRepository;
import com.halolight.domain.repository.UserRepository;
import com.halolight.web.dto.notification.CreateNotificationRequest;
import com.halolight.web.dto.notification.NotificationCountResponse;
import com.halolight.web.dto.notification.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing user notifications.
 * Provides functionality for creating, reading, and managing notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Get paginated list of notifications for a user.
     *
     * @param userId   ID of the user
     * @param pageable Pagination parameters
     * @return Page of notification responses
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(String userId, Pageable pageable) {
        log.debug("Fetching notifications for user: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toNotificationResponse);
    }

    /**
     * Get all notifications for a user (without pagination).
     *
     * @param userId ID of the user
     * @return List of notification responses
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllUserNotifications(String userId) {
        log.debug("Fetching all notifications for user: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get count of unread notifications for a user.
     *
     * @param userId ID of the user
     * @return Count response with unread notification count
     */
    @Transactional(readOnly = true)
    public NotificationCountResponse getUnreadCount(String userId) {
        log.debug("Fetching unread count for user: {}", userId);
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return NotificationCountResponse.builder()
                .count(count)
                .build();
    }

    /**
     * Get only unread notifications for a user.
     *
     * @param userId ID of the user
     * @return List of unread notification responses
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(String userId) {
        log.debug("Fetching unread notifications for user: {}", userId);
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get notifications by type for a user.
     *
     * @param userId   ID of the user
     * @param type     Type of notifications to filter
     * @param pageable Pagination parameters
     * @return Page of notification responses
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByType(String userId, String type, Pageable pageable) {
        log.debug("Fetching notifications of type '{}' for user: {}", type, userId);
        return notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable)
                .map(this::toNotificationResponse);
    }

    /**
     * Mark a notification as read.
     *
     * @param notificationId ID of the notification
     * @param userId         ID of the user (for authorization check)
     * @return Updated notification response
     */
    @Transactional
    public NotificationResponse markAsRead(String notificationId, String userId) {
        log.info("Marking notification {} as read for user: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        // Verify the notification belongs to the user
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Notification does not belong to user");
        }

        // Only update if not already read
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
            log.debug("Notification {} marked as read", notificationId);
        }

        return toNotificationResponse(notification);
    }

    /**
     * Mark all notifications as read for a user.
     *
     * @param userId ID of the user
     * @return Number of notifications marked as read
     */
    @Transactional
    public int markAllAsRead(String userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        int count = notificationRepository.markAllAsReadByUserId(userId, Instant.now());
        log.debug("Marked {} notifications as read", count);
        return count;
    }

    /**
     * Delete a notification.
     *
     * @param notificationId ID of the notification
     * @param userId         ID of the user (for authorization check)
     */
    @Transactional
    public void deleteNotification(String notificationId, String userId) {
        log.info("Deleting notification {} for user: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        // Verify the notification belongs to the user
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Notification does not belong to user");
        }

        notificationRepository.delete(notification);
        log.debug("Notification {} deleted successfully", notificationId);
    }

    /**
     * Delete all notifications for a user.
     *
     * @param userId ID of the user
     */
    @Transactional
    public void deleteAllUserNotifications(String userId) {
        log.info("Deleting all notifications for user: {}", userId);
        notificationRepository.deleteByUserId(userId);
        log.debug("All notifications deleted for user: {}", userId);
    }

    /**
     * Create a new notification (internal use).
     * This method is typically called by other services to notify users.
     *
     * @param request Create notification request
     * @return Created notification response
     */
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        log.info("Creating notification for user: {}", request.getUserId());

        // Validate user exists
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .actionUrl(request.getActionUrl())
                .isRead(request.getIsRead() != null ? request.getIsRead() : false)
                .build();

        notification = notificationRepository.save(notification);
        log.debug("Notification {} created successfully", notification.getId());

        return toNotificationResponse(notification);
    }

    /**
     * Batch create notifications for multiple users.
     *
     * @param userIds List of user IDs
     * @param title   Notification title
     * @param message Notification message
     * @param type    Notification type
     * @return List of created notification responses
     */
    @Transactional
    public List<NotificationResponse> createBatchNotifications(
            List<String> userIds,
            String title,
            String message,
            String type
    ) {
        log.info("Creating batch notifications for {} users", userIds.size());

        return userIds.stream()
                .map(userId -> createNotification(CreateNotificationRequest.builder()
                        .userId(userId)
                        .title(title)
                        .message(message)
                        .type(type)
                        .build()))
                .collect(Collectors.toList());
    }

    /**
     * Convert Notification entity to NotificationResponse DTO.
     *
     * @param notification Notification entity
     * @return NotificationResponse DTO
     */
    private NotificationResponse toNotificationResponse(Notification notification) {
        NotificationResponse.NotificationResponseBuilder builder = NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .actionUrl(notification.getActionUrl())
                .createdAt(notification.getCreatedAt());

        // Optionally include sender information if user relationship is loaded
        if (notification.getUser() != null) {
            User user = notification.getUser();
            builder.sender(NotificationResponse.SenderInfo.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .avatar(user.getAvatar())
                    .build());
        }

        return builder.build();
    }
}
