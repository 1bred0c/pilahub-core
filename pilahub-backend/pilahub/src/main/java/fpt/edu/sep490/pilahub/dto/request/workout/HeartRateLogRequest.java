package fpt.edu.sep490.pilahub.dto.request.workout;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Heart rate log data for a workout session")
public record HeartRateLogRequest(
        @Schema(description = "Heart rate in bpm", example = "120", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Heart rate must not be null")
        @Min(value = 1, message = "Heart rate must be at least 1")
        Integer heartRate,

        @Schema(description = "Recorded time in seconds from session start", example = "300")
        Integer recordedAt
) {
}

