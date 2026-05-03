package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Ingredient information")
public record IngredientDto(
        @Schema(description = "Unique ingredient identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID ingredientId,

        @Schema(description = "Ingredient name", example = "Creatine Monohydrate")
        String name,

        @Schema(description = "Whether the ingredient is active", example = "true")
        boolean active,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
