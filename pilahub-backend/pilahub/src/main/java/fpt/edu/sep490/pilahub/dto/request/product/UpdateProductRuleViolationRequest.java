package fpt.edu.sep490.pilahub.dto.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to update product rule violation flag")
public record UpdateProductRuleViolationRequest(
        @Schema(description = "Rule violation flag", example = "true", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "ruleViolation must not be null") Boolean ruleViolation) {
}
