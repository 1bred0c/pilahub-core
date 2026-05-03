package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Purpose information")
public record PurposeDto(
        @Schema(description = "Unique purpose identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID purposeId,

        @Schema(description = "Purpose name", example = "Muscle Gain")
        String name,

        @Schema(description = "Purpose code", example = "MUSCLE_GAIN")
        String code,

        @Schema(description = "Purpose description", example = "Supplements designed to support muscle growth and development")
        String description,

        @Schema(description = "Whether the purpose is active", example = "true")
        boolean active,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
