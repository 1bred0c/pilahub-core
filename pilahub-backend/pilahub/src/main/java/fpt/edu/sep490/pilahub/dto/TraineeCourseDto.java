package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Trainee course enrollment information")
public record TraineeCourseDto(
        @Schema(description = "Unique trainee course identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID traineeCourseId,

        @Schema(description = "Trainee information")
        TraineeDto trainee,

        @Schema(description = "Course information")
        CourseDto course,

        @Schema(description = "Enrollment timestamp", example = "2026-01-23T10:30:00Z")
        Instant enrolledAt,

        @Schema(description = "Progress percentage (0-100)", example = "45")
        Integer progressPercentage,

        @Schema(description = "Active status", example = "false")
        Boolean active,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
