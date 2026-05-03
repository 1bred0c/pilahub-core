package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for admin to broadcast a notification to all users")
public record AdminBroadcastNotificationRequest(
        @NotBlank(message = "Title must not be blank") @Size(max = 255, message = "Title must not exceed 255 characters") @Schema(description = "Notification title", example = "System Maintenance") String title,

        @NotBlank(message = "Message must not be blank") @Schema(description = "Notification message body", example = "Pilahub will be under maintenance from 23:00 to 01:00.") String message) {
}