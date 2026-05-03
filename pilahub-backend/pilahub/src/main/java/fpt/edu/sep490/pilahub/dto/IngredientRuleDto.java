package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Ingredient rule information")
public record IngredientRuleDto(
        @Schema(description = "Unique ingredient rule identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID ingredientRuleId,

        @Schema(description = "Ingredient ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID ingredientId,

        @Schema(description = "Rule type", example = "AGE")
        String ruleType,

        @Schema(description = "Rule description", example = "Not recommended for individuals under 18 years old")
        String ruleDescription,

        @Schema(description = "Operator for checking", example = "LESS_THAN")
        String operator,

        @Schema(description = "Value to check against", example = "18")
        String value,

        @Schema(description = "Severity level", example = "MEDIUM")
        String severity,

        @Schema(description = "Action to take", example = "WARN")
        String action,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
