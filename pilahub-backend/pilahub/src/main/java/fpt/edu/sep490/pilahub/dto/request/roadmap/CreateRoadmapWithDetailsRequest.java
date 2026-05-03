package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request to create a roadmap with all its nested stages, schedules, and exercises")
public record CreateRoadmapWithDetailsRequest(
                @Schema(description = "Roadmap title", example = "Full Stack Developer Path", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Title must not be blank") @Size(max = 255, message = "Title must not exceed 255 characters") String title,

                @Schema(description = "Roadmap description", example = "Complete roadmap to become a full stack developer") @Size(max = 1000, message = "Description must not exceed 1000 characters") String description,

                @Schema(description = "Roadmap start date", example = "2026-01-01T00:00:00Z") Instant startDate,

                @Schema(description = "Roadmap end date", example = "2026-12-31T23:59:59Z") Instant endDate,

                @Schema(description = "Source of the roadmap", example = "roadmap.sh") @Size(max = 255, message = "Source must not exceed 255 characters") String source,

                @Schema(description = "Trainee ID (optional - defaults to current user if trainee, required for coaches/admins)", example = "123e4567-e89b-12d3-a456-426614174000") UUID traineeId,

                @Schema(description = "Primary fitness goal ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Primary goal must not be null") UUID primaryGoalId,

                @Schema(description = "Secondary fitness goal IDs (optional)", example = "[\"uuid1\", \"uuid2\"]") @Size(max = 4, message = "Maximum 4 secondary goals allowed") List<UUID> secondaryGoalIds,

                @Schema(description = "List of personal stages for this roadmap") @Valid List<PersonalStageDetails> stages) {
        @Schema(description = "Personal stage details with nested schedules")
        public record PersonalStageDetails(
                        @Schema(description = "Stage name", example = "Foundation Phase", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Stage name must not be blank") @Size(max = 255, message = "Stage name must not exceed 255 characters") String stageName,

                        @Schema(description = "Stage description", example = "Learn the fundamentals of programming") @Size(max = 1000, message = "Description must not exceed 1000 characters") String description,

                        @Schema(description = "Stage order number", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Integer stageOrder,

                        @Schema(description = "Stage start date", example = "2026-01-01T00:00:00Z") Instant startDate,

                        @Schema(description = "Stage end date", example = "2026-03-31T23:59:59Z") Instant endDate,

                        @Schema(description = "List of personal schedules for this stage") @Valid List<PersonalScheduleDetails> schedules) {
        }

        @Schema(description = "Personal schedule details with nested exercises")
        public record PersonalScheduleDetails(
                        @Schema(description = "Schedule name", example = "Morning Workout", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Schedule name must not be blank") @Size(max = 255, message = "Schedule name must not exceed 255 characters") String scheduleName,

                        @Schema(description = "Schedule description", example = "Complete morning workout routine") @Size(max = 1000, message = "Description must not exceed 1000 characters") String description,

                        @Schema(description = "Scheduled date and time", example = "2026-01-24T07:00:00Z") Instant scheduledDate,

                        @Schema(description = "Duration in minutes", example = "60") Integer durationMinutes,

                        @Schema(description = "List of personal exercises for this schedule") @Valid List<PersonalExerciseDetails> exercises) {
        }

        @Schema(description = "Personal exercise details")
        public record PersonalExerciseDetails(
                        @Schema(description = "Exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED) java.util.UUID exerciseId,

                        @Schema(description = "Exercise order number", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Integer exerciseOrder,

                        @Schema(description = "Number of sets", example = "3") Integer sets,

                        @Schema(description = "Number of reps", example = "15") Integer reps,

                        @Schema(description = "Duration in seconds", example = "120") Integer durationSeconds,

                        @Schema(description = "Rest time between sets in seconds", example = "60") Integer restSeconds,

                        @Schema(description = "Personal notes", example = "Focus on form") @Size(max = 500, message = "Notes must not exceed 500 characters") String notes) {
        }
}
