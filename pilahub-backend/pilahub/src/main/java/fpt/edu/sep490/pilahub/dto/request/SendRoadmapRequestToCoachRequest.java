package fpt.edu.sep490.pilahub.dto.request;

import fpt.edu.sep490.pilahub.enums.WorkoutLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request payload for a trainee to ask a coach to create their AI roadmap")
public record SendRoadmapRequestToCoachRequest(

                @Schema(description = "ID of the coach to send the request to", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Coach ID must not be null") UUID coachId,

                @Schema(description = "Primary fitness goal ID", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Primary goal must not be null") UUID primaryGoalId,

                @Schema(description = "Secondary fitness goal IDs (optional, max 4)") @Size(max = 4, message = "Maximum 4 secondary goals allowed") List<UUID> secondaryGoalIds,

                @Schema(description = "Desired workout level", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Workout level must not be null") WorkoutLevel workoutLevel,

                @Schema(description = "Desired training day schedules", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty(message = "Training day schedules must not be empty") @Size(min = 1, max = 7, message = "Training day schedules must have between 1 and 7 days") List<@Valid TrainingDayScheduleRequest> trainingDaySchedules,

                @Schema(description = "Desired roadmap duration in weeks (1–52)", example = "12") @Min(value = 1, message = "Duration must be at least 1 week") @Max(value = 52, message = "Duration must not exceed 52 weeks") Integer durationWeeks,

                @Schema(description = "Optional message to the coach (max 1000 characters)") @Size(max = 1000, message = "Message must not exceed 1000 characters") String traineeMessage) {
}
