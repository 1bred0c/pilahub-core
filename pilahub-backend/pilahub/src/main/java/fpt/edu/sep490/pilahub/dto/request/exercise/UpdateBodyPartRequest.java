package fpt.edu.sep490.pilahub.dto.request.exercise;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a body part")
public record UpdateBodyPartRequest(
        @Schema(description = "Body part name", example = "Chest")
        @Size(max = 100, message = "Body part name must not exceed 100 characters")
        String name,

        @Schema(description = "Body part description", example = "The chest muscles including pectoralis major and minor")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}

