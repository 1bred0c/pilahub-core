package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Assessment criterion information")
public record AssessmentCriterionDto(
        @Schema(description = "Criterion ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID assessmentCriterionId,

        @Schema(description = "Criterion name", example = "Ky thuat dong tac")
        String name,

        @Schema(description = "Criterion description", example = "Danh gia do chuan cua dong tac")
        String description,

        @Schema(description = "Display order", example = "1")
        Integer displayOrder,

        @Schema(description = "Whether criterion is active", example = "true")
        boolean isActive,

        @Schema(description = "Creation timestamp", example = "2026-04-22T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-04-22T10:30:00Z")
        Instant updatedAt
) {
}

