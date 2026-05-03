package pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ingredient rule information for AI request")
public record IngredientRuleAIRequest(
        @Schema(description = "Rule type", example = "CONDITION")
        String ruleType,

        @Schema(description = "Rule description", example = "Not recommended for people with lactose intolerance")
        String ruleDescription,

        @Schema(description = "Rule operator", example = "EQUALS")
        String operator,

        @Schema(description = "Rule value", example = "LACTOSE_INTOLERANCE")
        String value,

        @Schema(description = "Severity", example = "WARNING")
        String severity,

        @Schema(description = "Action", example = "WARN")
        String action
) {
}
