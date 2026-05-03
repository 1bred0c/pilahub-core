package fpt.edu.sep490.pilahub.dto.request.workout;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to start workout for a lesson exercise in course lesson progress")
public record StartLessonExerciseWorkoutRequest(
        @Schema(description = "Course lesson progress identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Course lesson progress ID must not be null")
        UUID courseLessonProgressId,

        @Schema(description = "Lesson exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Lesson exercise ID must not be null")
        UUID lessonExerciseId,

        @Schema(description = "Whether AI tracking is enabled", example = "false")
        boolean haveAITracking,

        @Schema(description = "Whether IoT device tracking is enabled", example = "false")
        boolean haveIOTDeviceTracking
) {
}
