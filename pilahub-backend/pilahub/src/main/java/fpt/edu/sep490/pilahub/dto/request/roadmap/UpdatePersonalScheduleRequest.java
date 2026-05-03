package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Request to update a personal schedule")
public record UpdatePersonalScheduleRequest(
        @Schema(description = "Schedule name", example = "Morning Workout")
        @Size(max = 255, message = "Schedule name must not exceed 255 characters")
        String scheduleName,

        @Schema(description = "Schedule description", example = "Complete morning workout routine")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(description = "Scheduled date and time", example = "2026-01-24T07:00:00Z")
        Instant scheduledDate,

        @Schema(description = "Duration in minutes", example = "60")
        @Min(value = 1, message = "Duration must be at least 1 minute")
        Integer durationMinutes
) {
}
