package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Supplement ingredient relationship information")
public record SupplementIngredientDto(
        @Schema(description = "Unique supplement ingredient identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID supplementIngredientId,

        @Schema(description = "Supplement ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID supplementId,

        @Schema(description = "Ingredient information")
        IngredientDto ingredient,

        @Schema(description = "Amount of ingredient", example = "25.5")
        BigDecimal amount,

        @Schema(description = "Unit of measurement", example = "g")
        String unit,

        @Schema(description = "Additional notes", example = "Main protein source")
        String notes,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
