package fpt.edu.sep490.pilahub.dto.request.exercise;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new body part")
public record CreateBodyPartRequest(
        @Schema(description = "Body part name", example = "Chest", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Body part name must not be blank")
        @Size(max = 100, message = "Body part name must not exceed 100 characters")
        String name,

        @Schema(description = "Body part description", example = "The chest muscles including pectoralis major and minor")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}

