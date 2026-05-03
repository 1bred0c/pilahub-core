package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a stage")
public record UpdateStageRequest(
        @Schema(description = "Stage name", example = "Beginner Stage")
        @Size(max = 255, message = "Stage name must not exceed 255 characters")
        String name,

        @Schema(description = "Stage description", example = "Initial stage for beginners")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description
) {
}
