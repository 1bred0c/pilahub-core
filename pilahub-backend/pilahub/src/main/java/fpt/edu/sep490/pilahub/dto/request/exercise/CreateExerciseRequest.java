package fpt.edu.sep490.pilahub.dto.request.exercise;

import fpt.edu.sep490.pilahub.enums.BreathingRule;
import fpt.edu.sep490.pilahub.enums.DifficultyLevel;
import fpt.edu.sep490.pilahub.enums.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request to create a new exercise")
public record CreateExerciseRequest(
                @Schema(description = "Exercise name", example = "Push-ups", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Exercise name must not be blank") @Size(max = 255, message = "Exercise name must not exceed 255 characters") String name,

                @Schema(description = "Exercise description", example = "A basic upper body exercise") @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,

                @Schema(description = "Exercise duration in seconds", example = "300") Integer duration,

                @Schema(description = "Exercise type", example = "CORE_STRENGTHENING") ExerciseType exerciseType,

                @Schema(description = "Difficulty level", example = "BEGINNER") DifficultyLevel difficultyLevel,

                @Schema(description = "Body parts targeted", example = "[\"CHEST\", \"ARMS\"]") List<String> bodyParts,

                @Schema(description = "Whether equipment is required", example = "false") boolean equipmentRequired,

                @Schema(description = "Exercise image URL", example = "https://example.com/images/exercise.jpg") @Size(max = 500, message = "Image URL must not exceed 500 characters") String imageUrl,

                @Schema(description = "Exercise benefits", example = "Builds upper body strength") @Size(max = 1000, message = "Benefits must not exceed 1000 characters") String benefits,

                @Schema(description = "Prerequisites", example = "Basic plank hold") @Size(max = 500, message = "Prerequisites must not exceed 500 characters") String prerequisites,

                @Schema(description = "Contraindications", example = "Shoulder injuries") @Size(max = 500, message = "Contraindications must not exceed 500 characters") String contraindications,

                @Schema(description = "Whether AI support is available for this exercise", example = "false") boolean haveAIsupported,

                @Schema(description = "Name used to identify this exercise in the AI model", example = "push_up") @Size(max = 255, message = "Name in AI model must not exceed 255 characters") String nameInModelAI,

                @Schema(description = "Breathing rule for this exercise", example = "EXHALE_ON_EFFORT") BreathingRule breathingRule) {
}
