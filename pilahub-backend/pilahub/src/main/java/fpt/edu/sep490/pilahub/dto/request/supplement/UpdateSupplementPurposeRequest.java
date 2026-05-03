package fpt.edu.sep490.pilahub.dto.request.supplement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a supplement purpose relationship")
public record UpdateSupplementPurposeRequest(
        @Schema(description = "Whether this is a primary purpose", example = "true")
        Boolean primary,

        @Schema(description = "Effectiveness notes", example = "Highly effective for post-workout recovery")
        @Size(max = 500, message = "Effectiveness notes must not exceed 500 characters")
        String effectivenessNotes
) {
}
