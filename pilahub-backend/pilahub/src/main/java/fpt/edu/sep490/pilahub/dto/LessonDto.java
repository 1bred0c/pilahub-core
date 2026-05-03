package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Lesson information")
public record LessonDto(
        @Schema(description = "Unique lesson identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID lessonId,

        @Schema(description = "Lesson name", example = "Morning Stretching Routine")
        String name,

        @Schema(description = "Lesson description", example = "A comprehensive morning stretching session")
        String description,

        @Schema(description = "Whether the lesson is active", example = "true")
        boolean active,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
