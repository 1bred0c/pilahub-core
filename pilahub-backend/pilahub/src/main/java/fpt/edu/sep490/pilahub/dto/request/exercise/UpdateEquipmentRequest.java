package fpt.edu.sep490.pilahub.dto.request.exercise;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update an equipment")
public record UpdateEquipmentRequest(
        @Schema(description = "Equipment name", example = "Pilates Mat")
        @Size(max = 255, message = "Equipment name must not exceed 255 characters")
        String name,

        @Schema(description = "Equipment description", example = "A cushioned mat for floor exercises")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @Schema(description = "Image URL", example = "https://example.com/mat.jpg")
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String imageUrl
) {
}
