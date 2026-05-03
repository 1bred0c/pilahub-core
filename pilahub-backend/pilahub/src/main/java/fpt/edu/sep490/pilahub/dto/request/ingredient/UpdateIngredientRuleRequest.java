package fpt.edu.sep490.pilahub.dto.request.ingredient;

import fpt.edu.sep490.pilahub.enums.RuleAction;
import fpt.edu.sep490.pilahub.enums.RuleOperator;
import fpt.edu.sep490.pilahub.enums.RuleSeverity;
import fpt.edu.sep490.pilahub.enums.RuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request to update an existing ingredient rule")
public record UpdateIngredientRuleRequest(

        @Schema(description = "Ingredient Rule ID", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID ruleId,

        @Schema(description = "Rule type", example = "AGE")
        RuleType ruleType,

        @Schema(description = "Rule description", example = "Not recommended for individuals under 18 years old")
        @Size(max = 1000, message = "Rule description must not exceed 1000 characters")
        String ruleDescription,

        @Schema(description = "Operator for checking", example = "LESS_THAN")
        RuleOperator operator,

        @Schema(description = "Value to check against", example = "18")
        @Size(max = 500, message = "Value must not exceed 500 characters")
        String value,

        @Schema(description = "Severity level", example = "MEDIUM")
        RuleSeverity severity,

        @Schema(description = "Action to take", example = "WARN")
        RuleAction action
) {}
