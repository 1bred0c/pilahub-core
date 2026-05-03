package pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Supplement information for AI request")
public record SupplementAIRequest(
        @Schema(description = "Supplement name", example = "Whey Protein")
        String name,

        @Schema(description = "Supplement description")
        String description,

        @Schema(description = "Brand", example = "Optimum Nutrition")
        String brand,

        @Schema(description = "Form", example = "Powder")
        String form,

        @Schema(description = "Usage instructions")
        String usageInstructions,

        @Schema(description = "Benefits")
        String benefits,

        @Schema(description = "Side effects")
        String sideEffects,

        @Schema(description = "Contraindications")
        String contraindications,

        @Schema(description = "Warnings")
        String warnings,

        @Schema(description = "List of ingredients")
        List<IngredientAIRequest> ingredients,

        @Schema(description = "Primary purposes")
        List<String> purposes
) {
}
