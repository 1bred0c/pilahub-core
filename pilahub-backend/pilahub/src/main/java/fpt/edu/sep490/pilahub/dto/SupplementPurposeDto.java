package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Supplement purpose relationship information")
public record SupplementPurposeDto(
        @Schema(description = "Unique supplement purpose identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID supplementPurposeId,

        @Schema(description = "Supplement ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID supplementId,

        @Schema(description = "Purpose information")
        PurposeDto purpose,

        @Schema(description = "Whether this is a primary purpose", example = "true")
        boolean primary,

        @Schema(description = "Effectiveness notes", example = "Highly effective for post-workout recovery")
        String effectivenessNotes,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
