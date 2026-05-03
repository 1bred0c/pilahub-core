package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to update final health profile for a roadmap")
public record UpdateFinalHealthProfileRequest(
        @Schema(description = "Final health profile ID", example = "123e4567-e89b-12d3-a456-426614174003", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Final health profile ID must not be null")
        UUID finalHealthProfileId
) {
}

