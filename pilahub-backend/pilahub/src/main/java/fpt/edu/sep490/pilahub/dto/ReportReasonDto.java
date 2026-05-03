package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Report reason information")
public record ReportReasonDto(
        @Schema(description = "Unique report reason identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID reportReasonId,

        @Schema(description = "Display name", example = "Coach did not join")
        String name,

        @Schema(description = "Reason code", example = "COACH_NO_SHOW")
        String code,

        @Schema(description = "Description", example = "Coach did not show up for the session")
        String description,

        @Schema(description = "Whether description is mandatory when reporting", example = "false")
        boolean requiresDescription,

        @Schema(description = "Whether this reason is active", example = "true")
        boolean active,

        @Schema(description = "Creation timestamp", example = "2026-03-14T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-03-14T10:30:00Z")
        Instant updatedAt
) {
}

