package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.CoachRoadmapRequestStatus;
import fpt.edu.sep490.pilahub.enums.WorkoutLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Coach roadmap request information")
public record CoachRoadmapRequestDto(

        @Schema(description = "Unique request identifier") UUID requestId,

        @Schema(description = "Trainee ID") UUID traineeId,

        @Schema(description = "Trainee's full name") String traineeFullName,

        @Schema(description = "Trainee's avatar URL") String traineeAvatarUrl,

        @Schema(description = "Coach ID") UUID coachId,

        @Schema(description = "Coach's full name") String coachFullName,

        @Schema(description = "Request status", example = "PENDING") CoachRoadmapRequestStatus status,

        @Schema(description = "Primary fitness goal ID") UUID primaryGoalId,

        @Schema(description = "Primary fitness goal Vietnamese name") String primaryGoalName,

        @Schema(description = "Secondary fitness goal IDs") List<UUID> secondaryGoalIds,

        @Schema(description = "Secondary fitness goal Vietnamese names") List<String> secondaryGoalNames,

        @Schema(description = "Desired workout level", example = "INTERMEDIATE") WorkoutLevel workoutLevel,

        @Schema(description = "Desired training days of week", example = "[\"MONDAY\",\"WEDNESDAY\",\"FRIDAY\"]") List<DayOfWeek> trainingDays,

        @Schema(description = "Desired training day schedules with start times") List<TrainingDayScheduleDto> trainingDaySchedules,

        @Schema(description = "Desired roadmap duration in weeks", example = "12") Integer durationWeeks,

        @Schema(description = "Optional message from trainee to coach") String traineeMessage,

        @Schema(description = "Optional note from coach (e.g. rejection reason)") String coachNote,

        @Schema(description = "Request creation timestamp") Instant createdAt,

        @Schema(description = "Request last updated timestamp") Instant updatedAt) {
}
