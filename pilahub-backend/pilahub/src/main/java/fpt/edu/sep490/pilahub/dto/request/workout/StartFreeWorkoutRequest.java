package fpt.edu.sep490.pilahub.dto.request.workout;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to start a free workout session (not linked to personal exercise or lesson)")
public record StartFreeWorkoutRequest(
        @Schema(description = "Exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Exercise ID must not be null")
        UUID exerciseId,

        @Schema(description = "Whether AI tracking is enabled", example = "false")
        boolean haveAITracking,

        @Schema(description = "Whether IoT device tracking is enabled", example = "false")
        boolean haveIOTDeviceTracking
) {
}

