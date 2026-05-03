package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a personal stage supplement")
public record UpdatePersonalStageSupplementRequest(
        @Schema(description = "Recommended timing", example = "Post-workout")
        @Size(max = 255, message = "Recommended timing must not exceed 255 characters")
        String recommendedTiming,

        @Schema(description = "Dosage", example = "25-30g per serving")
        @Size(max = 100, message = "Dosage must not exceed 100 characters")
        String dosage,

        @Schema(description = "Reason for recommendation", example = "Supports muscle recovery and growth")
        @Size(max = 1000, message = "Reason must not exceed 1000 characters")
        String reason,

        @Schema(description = "Priority level", example = "HIGH")
        @Size(max = 20, message = "Priority must not exceed 20 characters")
        String priority,

        @Schema(description = "Additional notes")
        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        String notes,

        @Schema(description = "Whether the supplement is optional", example = "false")
        Boolean optional
) {
}
