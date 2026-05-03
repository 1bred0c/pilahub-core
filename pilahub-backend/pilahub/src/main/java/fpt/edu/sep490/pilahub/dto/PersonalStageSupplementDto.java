package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Personal stage supplement information")
public record PersonalStageSupplementDto(
        @Schema(description = "Unique personal stage supplement identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID personalStageSupplementId,

        @Schema(description = "Personal stage ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID personalStageId,

        @Schema(description = "Supplement ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID supplementId,

        @Schema(description = "Supplement name", example = "Whey Protein")
        String supplementName,

        @Schema(description = "Image URL of the supplement", example = "https://example.com/supplements/whey-protein.jpg")
        String supplementImageUrl,

        @Schema(description = "Recommended timing", example = "Post-workout")
        String recommendedTiming,

        @Schema(description = "Dosage", example = "25-30g per serving")
        String dosage,

        @Schema(description = "Reason for recommendation", example = "Supports muscle recovery and growth")
        String reason,

        @Schema(description = "Priority level", example = "HIGH")
        String priority,

        @Schema(description = "Additional notes")
        String notes,

        @Schema(description = "Whether the supplement is optional", example = "false")
        boolean optional,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
