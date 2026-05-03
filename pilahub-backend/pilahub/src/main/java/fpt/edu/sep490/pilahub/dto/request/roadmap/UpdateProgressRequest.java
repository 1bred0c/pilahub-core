package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to update roadmap progress")
public record UpdateProgressRequest(
        @Schema(description = "Progress percentage (0-100)", example = "45", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Progress percent must not be null")
        @Min(value = 0, message = "Progress percent must be at least 0")
        @Max(value = 100, message = "Progress percent must not exceed 100")
        Integer progressPercent
) {
}
