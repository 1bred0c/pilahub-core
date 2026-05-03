package fpt.edu.sep490.pilahub.dto.request.supplement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request to update a supplement ingredient relationship")
public record UpdateSupplementIngredientRequest(
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
