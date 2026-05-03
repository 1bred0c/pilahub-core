package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Personal stage information")
public record PersonalStageDto(
        @Schema(description = "Unique stage identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID personalStageId,

        @Schema(description = "Roadmap identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID roadmapId,

        @Schema(description = "Stage identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID stageId,

        @Schema(description = "Stage name", example = "Foundation Phase")
        String stageName,

        @Schema(description = "Stage description", example = "Build foundational strength and mobility")
        String stageDescription,

        @Schema(description = "Stage order number", example = "1")
        Integer stageOrder,

        @Schema(description = "Duration in weeks", example = "4")
        Integer durationWeeks,

        @Schema(description = "Stage start date", example = "2026-01-01T00:00:00Z")
        Instant startDate,

        @Schema(description = "Stage end date", example = "2026-03-31T23:59:59Z")
        Instant endDate,

        @Schema(description = "Whether the stage is completed", example = "false")
        boolean completed,

        @Schema(description = "Stage creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
