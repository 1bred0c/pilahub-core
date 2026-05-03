package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Equipment information")
public record EquipmentDto(
        @Schema(description = "Unique equipment identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID equipmentId,

        @Schema(description = "Equipment name", example = "Pilates Mat")
        String name,

        @Schema(description = "Equipment description", example = "A cushioned mat for floor exercises")
        String description,

        @Schema(description = "Image URL", example = "https://example.com/mat.jpg")
        String imageUrl,

        @Schema(description = "Equipment creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
