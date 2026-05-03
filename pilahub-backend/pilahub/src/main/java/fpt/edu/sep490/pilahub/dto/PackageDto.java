package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.PackageType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Package information")
public record PackageDto(
        @Schema(description = "Unique package identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID packageId,

        @Schema(description = "Package name", example = "Premium Package")
        String packageName,

        @Schema(description = "Package description", example = "Access to all features for 30 days")
        String description,

        @Schema(description = "Package price", example = "99.99")
        BigDecimal price,

        @Schema(description = "Duration in days", example = "30")
        Integer durationInDays,

        @Schema(description = "Package type", example = "MEMBER")
        PackageType packageType,

        @Schema(description = "Whether the package is active", example = "true")
        Boolean isActive,

        @Schema(description = "Package creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Package last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
