package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a live session report")
public record CreateLiveSessionReportRequest(
        @Schema(
                description = "Report reason code",
                example = "COACH_NO_SHOW",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Reason must not be blank")
        @Size(max = 100, message = "Reason must not exceed 100 characters")
        String reason,

        @Schema(
                description = "Detailed description of the issue (required if selected reason requires it)",
                example = "Coach did not show up for the session and did not respond to messages",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {
}

