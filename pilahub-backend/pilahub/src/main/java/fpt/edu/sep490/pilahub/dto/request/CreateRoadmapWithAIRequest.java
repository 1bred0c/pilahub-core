package fpt.edu.sep490.pilahub.dto.request;

import fpt.edu.sep490.pilahub.enums.WorkoutLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request to create AI-generated roadmap")
public record CreateRoadmapWithAIRequest(
                @Schema(description = "Trainee ID (optional - defaults to current user if trainee, required for coaches)", example = "123e4567-e89b-12d3-a456-426614174000") UUID traineeId,

                @Schema(description = "Primary fitness goal ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Primary goal must not be null") UUID primaryGoalId,

                @Schema(description = "Secondary fitness goal IDs (optional)", example = "[\"uuid1\", \"uuid2\"]") @Size(max = 4, message = "Maximum 4 secondary goals allowed") List<UUID> secondaryGoalIds,

                @Schema(description = "Workout level", example = "INTERMEDIATE", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Workout level must not be null") WorkoutLevel workoutLevel,

                @Schema(description = "Training days of week", example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty(message = "Training days must not be empty") @Size(min = 1, max = 7, message = "Training days must have between 1 and 7 days") List<DayOfWeek> trainingDays,

                @Schema(description = "Start date used to anchor generated schedule dates", example = "2026-04-20", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Start date must not be null") LocalDate startDate,

                @Schema(description = "Duration in weeks", example = "12") @Min(value = 1, message = "Duration must be at least 1 week") @Max(value = 52, message = "Duration must not exceed 52 weeks") Integer durationWeeks) {
}
