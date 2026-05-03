package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to resolve a live session report (Admin only)")
public record ResolveLiveSessionReportRequest(
        @Schema(
                description = "Internal admin note/conclusion about the report resolution",
                example = "Confirmed coach did not show up. Account suspended for 3 days.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Internal note must not be blank")
        @Size(max = 2000, message = "Internal note must not exceed 2000 characters")
        String internalNote
) {
}

