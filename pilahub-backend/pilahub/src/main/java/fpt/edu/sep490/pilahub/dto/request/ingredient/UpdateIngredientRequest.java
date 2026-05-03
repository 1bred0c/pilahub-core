package fpt.edu.sep490.pilahub.dto.request.ingredient;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request to update an ingredient with its rules (full update)")
public record UpdateIngredientRequest(
        @Schema(description = "Ingredient name", example = "Creatine Monohydrate", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 255, message = "Ingredient name must not exceed 255 characters")
        String name,

        @Schema(description = "Ingredient description", example = "A naturally occurring compound that improves strength")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(description = "List of ingredient rules (full update)")
        @NotNull
        List<@Valid UpdateIngredientRuleRequest> ingredientRules
) {}
