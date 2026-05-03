package fpt.edu.sep490.pilahub.event;

import fpt.edu.sep490.pilahub.enums.NotificationType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Spring Application Event for decoupled notification triggering.
 * Any service can publish this via ApplicationEventPublisher without
 * depending on NotificationService directly.
 *
 * <pre>
 * // Example usage in another service:
 * eventPublisher.publishEvent(new NotificationEvent(this,
 *     recipientId, NotificationType.BOOKING_CONFIRMED,
 *     "Booking Confirmed", "Your session on 2026-03-10 is confirmed.",
 *     bookingId, "BOOKING"));
 * </pre>
 */
@Getter
public class NotificationEvent extends ApplicationEvent {

    private final UUID recipientId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final UUID referenceId;
    private final String referenceType;

    public NotificationEvent(Object source,
                             UUID recipientId,
                             NotificationType type,
                             String title,
                             String message,
                             UUID referenceId,
                             String referenceType) {
        super(source);
        this.recipientId = recipientId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
    }

    /** Convenience constructor with no reference. */
    public NotificationEvent(Object source, UUID recipientId, NotificationType type,
                             String title, String message) {
        this(source, recipientId, type, title, message, null, null);
    }
}
