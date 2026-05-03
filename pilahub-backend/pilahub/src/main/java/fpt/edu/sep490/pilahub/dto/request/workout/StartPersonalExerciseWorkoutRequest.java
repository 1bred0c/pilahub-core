package fpt.edu.sep490.pilahub.dto.request.workout;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to start workout for a personal exercise")
public record StartPersonalExerciseWorkoutRequest(
        @Schema(description = "Personal exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Personal exercise ID must not be null")
        UUID personalExerciseId,

        @Schema(description = "Whether AI tracking is enabled", example = "false")
        boolean haveAITracking,

        @Schema(description = "Whether IoT device tracking is enabled", example = "false")
        boolean haveIOTDeviceTracking
) {
}

