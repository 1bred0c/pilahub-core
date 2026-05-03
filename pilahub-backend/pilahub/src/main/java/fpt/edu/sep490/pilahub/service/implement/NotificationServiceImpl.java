package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.NotificationDto;
import fpt.edu.sep490.pilahub.enums.NotificationType;
import fpt.edu.sep490.pilahub.event.NotificationEvent;
import fpt.edu.sep490.pilahub.exception.AccountNotFoundException;
import fpt.edu.sep490.pilahub.exception.NotificationNotFoundException;
import fpt.edu.sep490.pilahub.mapper.NotificationMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.Notification;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.NotificationRepository;
import fpt.edu.sep490.pilahub.service.NotificationService;
import fpt.edu.sep490.pilahub.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    /**
     * STOMP user-specific destination – clients subscribe to
     * /user/queue/notifications
     */
    private static final String USER_NOTIFICATION_DESTINATION = "/queue/notifications";

    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final PushNotificationService pushNotificationService;

    @Value("${app.notification.push.default-image-url:https://storage.googleapis.com/pilahub-assets/default-notification.png}")
    private String defaultPushImageUrl;

    // ─── Core creation ────────────────────────────────────────────────────────

    @Override
    public NotificationDto sendNotification(UUID recipientId,
            NotificationType type,
            String title,
            String message,
            UUID referenceId,
            String referenceType) {
        log.debug("Sending notification [{}] to account {}", type, recipientId);

        Account recipient = accountRepository.findById(recipientId)
                .orElseThrow(() -> new AccountNotFoundException("Recipient account not found: " + recipientId));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();

        notification = notificationRepository.save(notification);
        NotificationDto dto = notificationMapper.toDto(notification);

        // Push to connected WebSocket client (disabled: using FCM push only)
        // pushToWebSocket(recipient.getEmail(), dto);

        // Push to mobile device if FCM token exists (best-effort)
        pushToDevice(recipient.getFcmToken(), title, message);

        return dto;
    }

    @Override
    public NotificationDto sendNotification(UUID recipientId,
            NotificationType type,
            String title,
            String message) {
        return sendNotification(recipientId, type, title, message, null, null);
    }

    @Override
    public int broadcastAdminNotification(String title,
            String message,
            UUID referenceId,
            String referenceType) {
        List<UUID> recipientIds = accountRepository.findActiveAccountIds();
        int sentCount = 0;

        for (UUID recipientId : recipientIds) {
            sendNotification(
                    recipientId,
                    NotificationType.SYSTEM,
                    title,
                    message,
                    referenceId,
                    referenceType);
            sentCount++;
        }

        log.info("Admin broadcast notification sent to {} active users", sentCount);
        return sentCount;
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotifications(UUID accountId, Pageable pageable) {
        return notificationRepository
                .findByRecipient_AccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(notificationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID accountId) {
        return notificationRepository.countByRecipient_AccountIdAndReadFalse(accountId);
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    @Override
    public NotificationDto markAsRead(UUID notificationId, UUID accountId) {
        Notification notification = getOwnedNotification(notificationId, accountId);
        notification.setRead(true);
        return notificationMapper.toDto(notificationRepository.save(notification));
    }

    @Override
    public void markAllAsRead(UUID accountId) {
        int updated = notificationRepository.markAllAsReadByAccountId(accountId);
        log.debug("Marked {} notifications as read for account {}", updated, accountId);
    }

    @Override
    public void deleteNotification(UUID notificationId, UUID accountId) {
        Notification notification = getOwnedNotification(notificationId, accountId);
        notificationRepository.delete(notification);
        log.debug("Deleted notification {} for account {}", notificationId, accountId);
    }

    // ─── Spring Event listener (decoupled trigger) ────────────────────────────

    /**
     * Handles {@link NotificationEvent} published by any Spring component.
     * This allows other services to trigger notifications without a direct
     * dependency on {@link NotificationService}.
     */
    @Async
    @EventListener
    public void onNotificationEvent(NotificationEvent event) {
        try {
            sendNotification(
                    event.getRecipientId(),
                    event.getType(),
                    event.getTitle(),
                    event.getMessage(),
                    event.getReferenceId(),
                    event.getReferenceType());
        } catch (Exception ex) {
            log.error("Failed to process NotificationEvent for recipient {}: {}",
                    event.getRecipientId(), ex.getMessage(), ex);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void pushToWebSocket(String recipientEmail, NotificationDto dto) {
        try {
            // convertAndSendToUser resolves to /user/{principal.name}/queue/notifications
            // principal.name is the email set by WebSocketAuthChannelInterceptor
            messagingTemplate.convertAndSendToUser(
                    recipientEmail,
                    USER_NOTIFICATION_DESTINATION,
                    dto);
            log.debug("Pushed notification {} to WebSocket user {}", dto.notificationId(), recipientEmail);
        } catch (Exception ex) {
            // Not critical – the notification is already persisted
            log.warn("WebSocket push failed for user {}: {}", recipientEmail, ex.getMessage());
        }
    }

    private void pushToDevice(String fcmToken, String title, String body) {
        if (!StringUtils.hasText(fcmToken)) {
            return;
        }

        try {
            pushNotificationService.sendPush(fcmToken, title, body, defaultPushImageUrl);
            log.debug("Pushed FCM notification to token prefix {}",
                    fcmToken.substring(0, Math.min(10, fcmToken.length())));
        } catch (Exception ex) {
            // Not critical – notification is already persisted
            log.warn("FCM push failed: {}", ex.getMessage());
        }
    }

    private Notification getOwnedNotification(UUID notificationId, UUID accountId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found: " + notificationId));

        if (!notification.getRecipient().getAccountId().equals(accountId)) {
            throw new NotificationNotFoundException(
                    "Notification not found: " + notificationId);
        }
        return notification;
    }
}
