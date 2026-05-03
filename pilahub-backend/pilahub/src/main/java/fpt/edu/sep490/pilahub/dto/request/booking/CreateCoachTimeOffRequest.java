package fpt.edu.sep490.pilahub.dto.request.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Request to create coach time off")
public record CreateCoachTimeOffRequest(
        @Schema(description = "Time off start time (must be at least 24 hours in advance)", example = "2026-03-05T10:00:00Z", required = true)
        @NotNull(message = "Start time must not be null")
        Instant startTime,

        @Schema(description = "Time off end time", example = "2026-03-05T12:00:00Z", required = true)
        @NotNull(message = "End time must not be null")
        Instant endTime,

        @Schema(description = "Reason for time off", example = "Personal appointment")
        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}

