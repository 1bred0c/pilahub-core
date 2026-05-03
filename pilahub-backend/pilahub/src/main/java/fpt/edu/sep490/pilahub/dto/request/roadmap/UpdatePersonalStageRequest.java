package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request to update a personal stage")
public record UpdatePersonalStageRequest(
        @Schema(description = "Stage identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID stageId,

        @Schema(description = "Stage order number", example = "1")
        @Min(value = 1, message = "Stage order must be at least 1")
        Integer stageOrder,

        @Schema(description = "Stage start date", example = "2026-01-01T00:00:00Z")
        Instant startDate,

        @Schema(description = "Stage end date", example = "2026-03-31T23:59:59Z")
        Instant endDate
) {
}
