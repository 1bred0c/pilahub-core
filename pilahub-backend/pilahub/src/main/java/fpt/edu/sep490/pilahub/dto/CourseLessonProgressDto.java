package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Course lesson progress information for a trainee")
public record CourseLessonProgressDto(
        @Schema(description = "Unique progress identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID progressId,

        @Schema(description = "Trainee course information")
        TraineeCourseDto traineeCourse,

        @Schema(description = "Course lesson information")
        CourseLessonDto courseLesson,

        @Schema(description = "Lesson start timestamp", example = "2026-01-24T08:00:00Z")
        Instant startedAt,

        @Schema(description = "Lesson completion timestamp", example = "2026-01-24T09:30:00Z")
        Instant completedAt,

        @Schema(description = "Whether the lesson is completed", example = "false")
        boolean completed
) {
}
