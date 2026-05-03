package fpt.edu.sep490.pilahub.dto.request.reportreason;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a report reason")
public record CreateReportReasonRequest(
        @Schema(description = "Display name", example = "Coach did not join", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Report reason name must not be blank")
        @Size(max = 255, message = "Report reason name must not exceed 255 characters")
        String name,

        @Schema(description = "Reason code", example = "COACH_NO_SHOW", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Report reason code must not be blank")
        @Size(max = 100, message = "Report reason code must not exceed 100 characters")
        String code,

        @Schema(description = "Description", example = "Coach did not show up for the session")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(description = "Whether detailed description is mandatory", example = "false")
        boolean requiresDescription
) {
}

