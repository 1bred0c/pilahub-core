package fpt.edu.sep490.pilahub.dto.request.ingredient;

import fpt.edu.sep490.pilahub.enums.RuleAction;
import fpt.edu.sep490.pilahub.enums.RuleOperator;
import fpt.edu.sep490.pilahub.enums.RuleSeverity;
import fpt.edu.sep490.pilahub.enums.RuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new ingredient rule")
public record CreateIngredientRuleRequest(
                @Schema(description = "Rule type", example = "AGE", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Rule type must not be null") RuleType ruleType,

                @Schema(description = "Rule description", example = "Not recommended for individuals under 18 years old", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Rule description must not be blank") @Size(max = 1000, message = "Rule description must not exceed 1000 characters") String ruleDescription,

                @Schema(description = "Operator for checking", example = "LESS_THAN", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Operator must not be null") RuleOperator operator,

                @Schema(description = "Value to check against", example = "18") @Size(max = 500, message = "Value must not exceed 500 characters") String value,

                @Schema(description = "Severity level", example = "MEDIUM", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Severity must not be null") RuleSeverity severity,

                @Schema(description = "Action to take", example = "WARN", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Action must not be null") RuleAction action) {
}
