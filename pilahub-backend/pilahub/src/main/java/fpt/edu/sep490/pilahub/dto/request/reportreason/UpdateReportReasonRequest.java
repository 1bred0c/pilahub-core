package fpt.edu.sep490.pilahub.dto.request.reportreason;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a report reason")
public record UpdateReportReasonRequest(
        @Schema(description = "Display name", example = "Coach did not join")
        @Size(max = 255, message = "Report reason name must not exceed 255 characters")
        String name,

        @Schema(description = "Reason code", example = "COACH_NO_SHOW")
        @Size(max = 100, message = "Report reason code must not exceed 100 characters")
        String code,

        @Schema(description = "Description", example = "Coach did not show up for the session")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(description = "Whether detailed description is mandatory", example = "true")
        Boolean requiresDescription,

        @Schema(description = "Whether this reason is active", example = "true")
        Boolean active
) {
}

