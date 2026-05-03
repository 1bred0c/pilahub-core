package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Course information")
public record CourseDto(
        @Schema(description = "Unique course identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID courseId,

        @Schema(description = "Course name", example = "Beginner Pilates Program")
        String name,

        @Schema(description = "Course description", example = "A comprehensive 8-week pilates course")
        String description,

        @Schema(description = "Course image URL", example = "https://example.com/images/course.jpg")
        String imageUrl,

        @Schema(description = "Difficulty level", example = "BEGINNER")
        String level,

        @Schema(description = "Course price", example = "99.99")
        Double price,

        @Schema(description = "Whether the course is active", example = "true")
        boolean active,

        @Schema(description = "Total number of lessons in the course", example = "12")
        Integer totalLesson,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
