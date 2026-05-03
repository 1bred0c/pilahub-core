package fpt.edu.sep490.pilahub.dto.request.lesson;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a lesson exercise relationship")
public record UpdateLessonExerciseRequest(
        @Schema(description = "Display order", example = "1")
        @Min(value = 1, message = "Order must be at least 1")
        Integer displayOrder,

        @Schema(description = "Number of sets", example = "3")
        @Min(value = 1, message = "Sets must be at least 1")
        Integer sets,

        @Schema(description = "Number of reps", example = "12")
        @Min(value = 1, message = "Reps must be at least 1")
        Integer reps,

        @Schema(description = "Duration in seconds", example = "60")
        @Min(value = 0, message = "Duration must be at least 0")
        Integer durationSeconds,

        @Schema(description = "Rest time in seconds", example = "30")
        @Min(value = 0, message = "Rest time must be at least 0")
        Integer restSeconds,

        @Schema(description = "Additional notes", example = "Focus on form")
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes,

        @Schema(description = "Whether the exercise is optional", example = "false")
        Boolean optional
) {
}
