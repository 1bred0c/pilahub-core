package fpt.edu.sep490.pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Equipment information aggregated for a roadmap")
public record EquipmentRoadmapResponse(
        @Schema(description = "Equipment name", example = "Barbell", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("equipmentName")
        String equipmentName,

        @Schema(description = "Equipment description", example = "Standard Olympic barbell")
        String description,

        @Schema(description = "Whether this equipment is required for at least one exercise in the roadmap")
        @JsonProperty("isRequired")
        Boolean isRequired,

        @Schema(description = "Whether this equipment is listed as alternative for some exercises")
        @JsonProperty("isAlternative")
        Boolean isAlternative,

        @Schema(description = "Total quantity needed (maximum across all exercises)", example = "2")
        Integer quantity,

        @Schema(description = "Image URL for the equipment")
        @JsonProperty("imageUrl")
        String imageUrl,

        @Schema(description = "List of exercise names that use this equipment")
        @JsonProperty("usedInExercises")
        List<String> usedInExercises
) {
}
