package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.BreathingRule;
import fpt.edu.sep490.pilahub.enums.DifficultyLevel;
import fpt.edu.sep490.pilahub.enums.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Exercise information")
public record ExerciseDto(
                @Schema(description = "Unique exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID exerciseId,

                @Schema(description = "Exercise name", example = "Push-ups") String name,

                @Schema(description = "Exercise description", example = "A basic upper body exercise") String description,

                @Schema(description = "Exercise duration in seconds", example = "300") Integer duration,

                @Schema(description = "Exercise type", example = "STRENGTH") String exerciseType,

                @Schema(description = "Difficulty level", example = "BEGINNER") String difficultyLevel,

                @Schema(description = "Body parts targeted") List<BodyPartDto> bodyParts,

                @Schema(description = "Whether equipment is required", example = "false") boolean equipmentRequired,

                @Schema(description = "Exercise image URL", example = "https://example.com/images/exercise.jpg") String imageUrl,

                @Schema(description = "Exercise benefits", example = "Builds upper body strength") String benefits,

                @Schema(description = "Prerequisites", example = "Basic plank hold") String prerequisites,

                @Schema(description = "Contraindications", example = "Shoulder injuries") String contraindications,

                @Schema(description = "Whether the exercise is active", example = "true") boolean active,

                @Schema(description = "Whether AI support is available for this exercise", example = "false") boolean haveAIsupported,

                @Schema(description = "Name used to identify this exercise in the AI model", example = "push_up") String nameInModelAI,

                @Schema(description = "Breathing rule for this exercise", example = "EXHALE_ON_EFFORT") BreathingRule breathingRule,

                @Schema(description = "Whether the exercise is already practiced by current trainee", example = "true") boolean havePracticed,

                @Schema(description = "Exercise creation timestamp", example = "2026-01-23T10:30:00Z") Instant createdAt,

                @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z") Instant updatedAt) {
}
