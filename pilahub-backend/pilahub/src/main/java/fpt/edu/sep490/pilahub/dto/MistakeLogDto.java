package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Mistake log information")
public record MistakeLogDto(
        @Schema(description = "Unique mistake log identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID mistakeLogId,

        @Schema(description = "Workout session identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID workoutSessionId,

        @Schema(description = "Body part identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID bodyPartId,

        @Schema(description = "Body part name", example = "Lower Back")
        String bodyPartName,

        @Schema(description = "Mistake details", example = "Form error detected")
        String details,

        @Schema(description = "Screenshot URL", example = "https://example.com/mistake.jpg")
        String imageUrl,

        @Schema(description = "Recorded time in seconds from session start", example = "120.5")
        Double recordedAtSecond,

        @Schema(description = "Duration of mistake in seconds", example = "15.0")
        Double duration,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt
) {
}

