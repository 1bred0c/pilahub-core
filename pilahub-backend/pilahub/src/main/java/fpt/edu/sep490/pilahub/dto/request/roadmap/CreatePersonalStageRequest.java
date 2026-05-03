package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request to create a new personal stage")
public record CreatePersonalStageRequest(
        @Schema(description = "Roadmap identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Roadmap ID must not be null")
        UUID roadmapId,

        @Schema(description = "Stage identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID stageId,

        @Schema(description = "Stage order number", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Stage order must not be null")
        @Min(value = 1, message = "Stage order must be at least 1")
        Integer stageOrder,

        @Schema(description = "Stage start date", example = "2026-01-01T00:00:00Z")
        Instant startDate,

        @Schema(description = "Stage end date", example = "2026-03-31T23:59:59Z")
        Instant endDate
) {
}
