package fpt.edu.sep490.pilahub.dto.request.lesson;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request to create a new lesson exercise relationship")
public record CreateLessonExerciseRequest(
        @Schema(description = "Exercise ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Exercise ID must not be null")
        UUID exerciseId,

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
        boolean optional
) {
}
