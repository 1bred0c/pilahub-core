package fpt.edu.sep490.pilahub.dto.request.ingredient;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request to create a new ingredient")
public record CreateIngredientRequest(
        @Schema(description = "Ingredient name", example = "Creatine Monohydrate", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Ingredient name must not be blank")
        @Size(max = 255, message = "Ingredient name must not exceed 255 characters")
        String name,

        @Schema(description = "Ingredient description", example = "A naturally occurring compound that improves strength")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(description = "List of ingredient rules to create together with this ingredient")
        List<@Valid CreateIngredientRuleRequest> ingredientRules
) {
}
