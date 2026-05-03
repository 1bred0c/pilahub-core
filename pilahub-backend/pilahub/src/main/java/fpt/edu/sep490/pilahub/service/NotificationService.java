package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.NotificationDto;
import fpt.edu.sep490.pilahub.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    /**
     * Persist a notification and immediately push it to the recipient via
     * WebSocket.
     *
     * @param recipientId   target account UUID
     * @param type          notification type
     * @param title         short title shown in UI
     * @param message       full notification body
     * @param referenceId   optional – UUID of the related entity (booking, course,
     *                      etc.)
     * @param referenceType optional – human-readable type name ("BOOKING",
     *                      "COURSE", …)
     * @return the persisted {@link NotificationDto}
     */
    NotificationDto sendNotification(UUID recipientId,
            NotificationType type,
            String title,
            String message,
            UUID referenceId,
            String referenceType);

    /**
     * Convenience overload – no reference entity.
     */
    NotificationDto sendNotification(UUID recipientId,
            NotificationType type,
            String title,
            String message);

    /**
     * Broadcast an admin-created notification to all active users.
     * Returns the number of persisted notifications.
     */
    int broadcastAdminNotification(String title,
            String message,
            UUID referenceId,
            String referenceType);

    /**
     * Paginated list of notifications for the authenticated user, newest first.
     */
    Page<NotificationDto> getNotifications(UUID accountId, Pageable pageable);

    /**
     * Count of unread notifications for the authenticated user.
     */
    long getUnreadCount(UUID accountId);

    /**
     * Mark a single notification as read. Throws
     * {@link fpt.edu.sep490.pilahub.exception.NotificationNotFoundException}
     * if the notification does not belong to the given account.
     */
    NotificationDto markAsRead(UUID notificationId, UUID accountId);

    /**
     * Mark all notifications of the authenticated user as read.
     */
    void markAllAsRead(UUID accountId);

    /**
     * Delete a notification. Throws
     * {@link fpt.edu.sep490.pilahub.exception.NotificationNotFoundException}
     * if the notification does not belong to the given account.
     */
    void deleteNotification(UUID notificationId, UUID accountId);
}
