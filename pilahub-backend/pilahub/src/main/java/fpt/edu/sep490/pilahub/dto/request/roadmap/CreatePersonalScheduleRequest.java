package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request to create a new personal schedule")
public record CreatePersonalScheduleRequest(
        @Schema(description = "Personal stage identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Personal stage ID must not be null")
        UUID personalStageId,

        @Schema(description = "Schedule name", example = "Morning Workout", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Schedule name must not be blank")
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
