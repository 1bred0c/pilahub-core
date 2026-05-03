package fpt.edu.sep490.pilahub.dto.request.exercise;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request to create a new exercise equipment relationship")
public record CreateExerciseEquipmentRequest(
        @Schema(description = "Exercise ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Exercise ID must not be null")
        UUID exerciseId,

        @Schema(description = "Equipment ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Equipment ID must not be null")
        UUID equipmentId,

        @Schema(description = "Whether the equipment is required", example = "true")
        boolean required,

        @Schema(description = "Whether the equipment is an alternative", example = "false")
        boolean alternative,

        @Schema(description = "Quantity needed", example = "1")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @Schema(description = "Usage notes", example = "Place mat on flat surface")
        @Size(max = 500, message = "Usage notes must not exceed 500 characters")
        String usageNotes
) {
}
