package fpt.edu.sep490.pilahub.dto.request.exercise;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update an exercise equipment relationship")
public record UpdateExerciseEquipmentRequest(
        @Schema(description = "Whether the equipment is required", example = "true")
        Boolean required,

        @Schema(description = "Whether the equipment is an alternative", example = "false")
        Boolean alternative,

        @Schema(description = "Quantity needed", example = "1")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @Schema(description = "Usage notes", example = "Place mat on flat surface")
        @Size(max = 500, message = "Usage notes must not exceed 500 characters")
        String usageNotes
) {
}
