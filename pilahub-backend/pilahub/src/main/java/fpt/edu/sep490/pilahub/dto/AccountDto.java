package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.PackageType;
import fpt.edu.sep490.pilahub.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "User account information")
public record AccountDto(
                @Schema(description = "Unique account identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID accountId,

                @Schema(description = "User's email address", example = "user@example.com") String email,

                @Schema(description = "User's phone number", example = "+1234567890") String phoneNumber,

                @Schema(description = "User's role in the system", example = "TRAINEE") Role role,

                @Schema(description = "Whether the account is active", example = "true") boolean active,

                @Schema(description = "Whether the email has been verified", example = "true") boolean emailVerified,

                @Schema(description = "Last seen timestamp", example = "2026-01-23T10:30:00Z") Instant lastSeenAt,

                @Schema(description = "FCM token for push notifications", example = "fcm_device_token_example") String fcmToken,

                @Schema(description = "Whether reminder notifications are enabled", example = "true") Boolean isReminded,

                @Schema(description = "Account creation timestamp", example = "2026-01-23T10:30:00Z")
                Instant createdAt,

                @Schema(description = "Active package type for the user (null if no active subscription)", example = "VIP_MEMBER")
                PackageType activePackageType
) {
}
