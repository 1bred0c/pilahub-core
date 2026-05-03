package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Personal schedule information")
public record PersonalScheduleDto(
        @Schema(description = "Unique schedule identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID personalScheduleId,

        @Schema(description = "Personal stage identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID personalStageId,

        @Schema(description = "Schedule name", example = "Morning Workout")
        String scheduleName,

        @Schema(description = "Schedule description", example = "Complete morning workout routine")
        String description,

        @Schema(description = "Day of week", example = "MONDAY")
        String dayOfWeek,

        @Schema(description = "Scheduled date and time", example = "2026-01-24T07:00:00Z")
        Instant scheduledDate,

        @Schema(description = "Duration in minutes", example = "60")
        Integer durationMinutes,

        @Schema(description = "Whether the schedule is completed", example = "false")
        boolean completed,

        @Schema(description = "Completion timestamp", example = "2026-01-24T08:00:00Z")
        Instant completedAt,

        @Schema(description = "Schedule creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
