package fpt.edu.sep490.pilahub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Exercise data sent to AI server for roadmap generation")
public record ExerciseAIRequest(
        @Schema(description = "Exercise name", example = "Push-up") String name,

        @Schema(description = "Exercise description", example = "A bodyweight upper-body exercise") String description,

        @Schema(description = "Exercise duration in seconds", example = "60") Integer duration,

        @Schema(description = "Exercise type", example = "CORE_STRENGTHENING") @JsonProperty("exerciseType") String exerciseType,

        @Schema(description = "Difficulty level", example = "BEGINNER") @JsonProperty("difficultyLevel") String difficultyLevel,

        @Schema(description = "Body parts targeted", example = "[\"Chest\", \"Triceps\"]") @JsonProperty("bodyParts") List<String> bodyParts,

        @Schema(description = "Whether equipment is required", example = "false") @JsonProperty("equipmentRequired") boolean equipmentRequired,

        @Schema(description = "Exercise benefits", example = "Builds upper body strength") String benefits,

        @Schema(description = "Exercise prerequisites", example = "Basic plank form") String prerequisites,

        @Schema(description = "Exercise contraindications", example = "Acute shoulder injury") String contraindications,

        @Schema(description = "Name used by AI model", example = "push_up") @JsonProperty("nameInModelAI") String nameInModelAI,

        @Schema(description = "Breathing rule", example = "EXHALE_ON_EFFORT") @JsonProperty("breathingRule") String breathingRule) {
}
