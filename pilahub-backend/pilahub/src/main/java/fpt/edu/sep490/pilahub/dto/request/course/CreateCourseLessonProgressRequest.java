package fpt.edu.sep490.pilahub.dto.request.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request to create a course lesson progress record")
public record CreateCourseLessonProgressRequest(

        @Schema(
                description = "Trainee course ID",
                example = "123e4567-e89b-12d3-a456-426614174000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Trainee course ID must not be null")
        UUID traineeCourseId,

        @Schema(
                description = "Start date for scheduling lessons (inclusive)",
                example = "2026-04-20",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        LocalDate startDate,

        @Schema(
                description = "List of training days of week (1=Monday, 7=Sunday)",
                example = "[2, 4, 6]",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        List<Integer> trainingDays
) {
}
