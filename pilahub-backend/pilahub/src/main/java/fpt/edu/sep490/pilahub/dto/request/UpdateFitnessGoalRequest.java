package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Request to update an existing fitness goal")
public record UpdateFitnessGoalRequest(

        @Schema(description = "Vietnamese name", example = "Giảm đau lưng")
        @Size(max = 255, message = "Vietnamese name must not exceed 255 characters")
        String vietnameseName,

        @Schema(description = "English description", example = "Relieve back pain")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Schema(description = "Related purpose IDs (UUIDs of existing purposes)")
        Set<UUID> relatedPurposeIds,

        @Schema(description = "Active status", example = "true")
        Boolean active
) {
}
