package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Notification data")
public record NotificationDto(

        @Schema(description = "Notification identifier")
        UUID notificationId,

        @Schema(description = "Recipient account identifier")
        UUID recipientId,

        @Schema(description = "Notification type", example = "BOOKING_CONFIRMED")
        NotificationType type,

        @Schema(description = "Short notification title", example = "Booking Confirmed")
        String title,

        @Schema(description = "Notification body message")
        String message,

        @Schema(description = "ID of the related entity (booking, course, etc.)")
        UUID referenceId,

        @Schema(description = "Type of the related entity", example = "BOOKING")
        String referenceType,

        @Schema(description = "Whether this notification has been read", example = "false")
        boolean read,

        @Schema(description = "When the notification was created")
        Instant createdAt
) {
}
