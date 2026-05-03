package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Stage information")
public record StageDto(
        @Schema(description = "Unique stage identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID stageId,

        @Schema(description = "Stage name", example = "Beginner Stage")
        String name,

        @Schema(description = "Stage description", example = "Initial stage for beginners")
        String description,

        @Schema(description = "Whether the stage is active", example = "true")
        boolean active,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
