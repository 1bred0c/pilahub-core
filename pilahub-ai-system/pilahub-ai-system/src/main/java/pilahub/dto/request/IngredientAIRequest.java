package pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Ingredient information for AI request")
public record IngredientAIRequest(
        @Schema(description = "Ingredient name", example = "Whey Protein Isolate")
        String name,

        @Schema(description = "Amount", example = "25.0")
        BigDecimal amount,

        @Schema(description = "Unit", example = "g")
        String unit,

        @Schema(description = "Associated rules for this ingredient")
        List<IngredientRuleAIRequest> rules
) {
}
