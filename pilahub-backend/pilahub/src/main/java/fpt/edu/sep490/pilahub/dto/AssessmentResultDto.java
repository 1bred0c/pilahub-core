package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Assessment result for one criterion")
public record AssessmentResultDto(
        @Schema(description = "Criterion ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID criterionId,

        @Schema(description = "Criterion name", example = "Ky thuat dong tac")
        String criterionName,

        @Schema(description = "Display order", example = "1")
        Integer displayOrder,

        @Schema(description = "Score", example = "8.5")
        BigDecimal score
) {
}

