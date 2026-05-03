package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request to create a new personal exercise")
public record CreatePersonalExerciseRequest(
                @Schema(description = "Personal schedule identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Personal schedule ID must not be null") UUID personalScheduleId,

                @Schema(description = "Exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Exercise ID must not be null") UUID exerciseId,

                @Schema(description = "Exercise order number", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Exercise order must not be null") @Min(value = 1, message = "Exercise order must be at least 1") Integer exerciseOrder,

                @Schema(description = "Number of sets", example = "3") @Min(value = 1, message = "Sets must be at least 1") Integer sets,

                @Schema(description = "Number of reps", example = "15") @Min(value = 1, message = "Reps must be at least 1") Integer reps,

                @Schema(description = "Duration in seconds", example = "120") @Min(value = 1, message = "Duration must be at least 1 second") Integer durationSeconds,

                @Schema(description = "Rest time between sets in seconds", example = "60") @Min(value = 1, message = "Rest time must be at least 1 second") Integer restSeconds,

                @Schema(description = "Personal notes", example = "Focus on form") @Size(max = 500, message = "Notes must not exceed 500 characters") String notes) {
}
