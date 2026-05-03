package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Course lesson relationship information")
public record CourseLessonDto(
        @Schema(description = "Unique course lesson identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID courseLessonId,

        @Schema(description = "Course ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID courseId,

        @Schema(description = "Course name", example = "Beginner Fitness Program")
        String courseName,

        @Schema(description = "Lesson ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID lessonId,

        @Schema(description = "Lesson name", example = "Morning Stretching Routine")
        String lessonName,

        @Schema(description = "Display order", example = "1")
        Integer displayOrder,

        @Schema(description = "Additional notes", example = "Complete before moving to next lesson")
        String notes,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
