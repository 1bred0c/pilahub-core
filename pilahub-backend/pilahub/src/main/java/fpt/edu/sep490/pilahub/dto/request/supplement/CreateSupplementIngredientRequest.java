package fpt.edu.sep490.pilahub.dto.request.supplement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request to create a new supplement ingredient relationship")
public record CreateSupplementIngredientRequest(
        @Schema(description = "Supplement ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Supplement ID must not be null")
        UUID supplementId,

        @Schema(description = "Ingredient ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Ingredient ID must not be null")
        UUID ingredientId,

        @Schema(description = "Amount of ingredient", example = "25.5")
        BigDecimal amount,

        @Schema(description = "Unit of measurement", example = "g")
        @Size(max = 50, message = "Unit must not exceed 50 characters")
        String unit,

        @Schema(description = "Additional notes", example = "Main protein source")
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}
