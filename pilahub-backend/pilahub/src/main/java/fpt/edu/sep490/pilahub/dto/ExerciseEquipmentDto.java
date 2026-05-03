package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Exercise equipment relationship information")
public record ExerciseEquipmentDto(
        @Schema(description = "Unique exercise equipment identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID exerciseEquipmentId,

        @Schema(description = "Exercise ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID exerciseId,

        @Schema(description = "Equipment ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID equipmentId,

        @Schema(description = "Equipment name", example = "Pilates Mat")
        String equipmentName,

        @Schema(description = "Whether the equipment is required", example = "true")
        boolean required,

        @Schema(description = "Whether the equipment is an alternative", example = "false")
        boolean alternative,

        @Schema(description = "Quantity needed", example = "1")
        Integer quantity,

        @Schema(description = "Usage notes", example = "Place mat on flat surface")
        String usageNotes,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
