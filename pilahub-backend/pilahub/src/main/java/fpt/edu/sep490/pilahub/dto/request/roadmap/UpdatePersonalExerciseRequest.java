package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a personal exercise")
public record UpdatePersonalExerciseRequest(
                @Schema(description = "Exercise order number", example = "1") @Min(value = 1, message = "Exercise order must be at least 1") Integer exerciseOrder,

                @Schema(description = "Number of sets", example = "3") @Min(value = 1, message = "Sets must be at least 1") Integer sets,

                @Schema(description = "Number of reps", example = "15") @Min(value = 1, message = "Reps must be at least 1") Integer reps,

                @Schema(description = "Duration in seconds", example = "120") @Min(value = 1, message = "Duration must be at least 1 second") Integer durationSeconds,

                @Schema(description = "Rest time between sets in seconds", example = "60") @Min(value = 1, message = "Rest time must be at least 1 second") Integer restSeconds,

                @Schema(description = "Personal notes", example = "Focus on form") @Size(max = 500, message = "Notes must not exceed 500 characters") String notes) {
}
