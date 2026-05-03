package fpt.edu.sep490.pilahub.dto.request.supplement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request to create a new supplement purpose relationship")
public record CreateSupplementPurposeRequest(
        @Schema(description = "Supplement ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Supplement ID must not be null")
        UUID supplementId,

        @Schema(description = "Purpose ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Purpose ID must not be null")
        UUID purposeId,

        @Schema(description = "Whether this is a primary purpose", example = "true")
        boolean primary,

        @Schema(description = "Effectiveness notes", example = "Highly effective for post-workout recovery")
        @Size(max = 500, message = "Effectiveness notes must not exceed 500 characters")
        String effectivenessNotes
) {
}
