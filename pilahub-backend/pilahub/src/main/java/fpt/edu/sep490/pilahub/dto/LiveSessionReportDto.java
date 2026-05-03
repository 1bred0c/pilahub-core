package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Live session report information")
public record LiveSessionReportDto(
        @Schema(description = "Live session ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID liveSessionId,

        @Schema(description = "Reporter (Trainee) ID", example = "223e4567-e89b-12d3-a456-426614174000")
        UUID reporterId,

        @Schema(description = "Reported (Coach) user ID", example = "323e4567-e89b-12d3-a456-426614174000")
        UUID reportedUserId,

        @Schema(description = "Report reason code", example = "COACH_NO_SHOW")
        String reason,

        @Schema(description = "Report reason name", example = "Coach did not join")
        String reasonName,

        @Schema(description = "Detailed description", example = "Coach did not show up for the session")
        String description,

        @Schema(description = "Report creation timestamp", example = "2026-03-14T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Report resolution timestamp (null if unresolved)", example = "2026-03-14T14:30:00Z")
        Instant resolvedAt,

        @Schema(description = "Admin who resolved the report", example = "423e4567-e89b-12d3-a456-426614174000")
        UUID resolvedBy,

        @Schema(description = "Internal admin notes (only visible to admin)", example = "Confirmed coach did not show. Account suspended for 3 days.")
        String internalNote
) {
}

