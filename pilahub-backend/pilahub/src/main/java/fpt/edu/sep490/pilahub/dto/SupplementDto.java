package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Supplement information")
public record SupplementDto(
        @Schema(description = "Unique supplement identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID supplementId,

        @Schema(description = "Supplement name", example = "Whey Protein")
        String name,

        @Schema(description = "Supplement description", example = "High-quality whey protein for muscle recovery")
        String description,

        @Schema(description = "Brand name", example = "Optimum Nutrition")
        String brand,

        @Schema(description = "Supplement form", example = "Powder")
        String form,

        @Schema(description = "Usage instructions", example = "Mix with water or milk, consume after workout")
        String usageInstructions,

        @Schema(description = "Benefits", example = "Supports muscle growth and recovery")
        String benefits,

        @Schema(description = "Possible side effects", example = "May cause bloating in some individuals")
        String sideEffects,

        @Schema(description = "Contraindications", example = "Not suitable for lactose intolerant individuals")
        String contraindications,

        @Schema(description = "Warnings", example = "Consult physician before use if pregnant or nursing")
        String warnings,

        @Schema(description = "Image URL of the supplement", example = "https://example.com/supplements/whey-protein.jpg")
        String imageUrl,

        @Schema(description = "Whether the supplement is active", example = "true")
        boolean active,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
