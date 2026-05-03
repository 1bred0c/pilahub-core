package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Subscription information")
public record SubscriptionDto(
        @Schema(description = "Unique subscription identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID subscriptionId,

        @Schema(description = "Trainee information")
        TraineeDto trainee,

        @Schema(description = "Package information")
        PackageDto subscribedPackage,

        @Schema(description = "Subscription status", example = "ACTIVE")
        SubscriptionStatus status,

        @Schema(description = "Subscription start date", example = "2026-01-23T10:30:00Z")
        Instant startDate,

        @Schema(description = "Subscription end date", example = "2026-02-22T10:30:00Z")
        Instant endDate,

        @Schema(description = "Subscription creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Subscription last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
