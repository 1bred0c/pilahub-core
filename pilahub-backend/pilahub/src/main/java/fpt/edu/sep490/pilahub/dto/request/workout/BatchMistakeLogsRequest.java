package fpt.edu.sep490.pilahub.dto.request.workout;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request to add multiple mistake logs to a workout session")
public record BatchMistakeLogsRequest(
        @Schema(description = "Workout session identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Workout session ID must not be null")
        UUID workoutSessionId,

        @Schema(description = "List of mistake logs", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "Mistake logs list must not be empty")
        @Valid
        List<MistakeLogRequest> mistakeLogs
) {
}

