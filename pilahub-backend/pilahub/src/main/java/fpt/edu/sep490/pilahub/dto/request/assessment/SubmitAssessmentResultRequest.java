package fpt.edu.sep490.pilahub.dto.request.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "One criterion score in assessment submission")
public record SubmitAssessmentResultRequest(
        @Schema(description = "Criterion ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Criterion ID must not be null")
        UUID criterionId,

        @Schema(description = "Score from 0 to 10", example = "8.5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Score must not be null")
        @DecimalMin(value = "0.0", message = "Score must be at least 0")
        @DecimalMax(value = "10.0", message = "Score must not exceed 10")
        BigDecimal score
) {
}

