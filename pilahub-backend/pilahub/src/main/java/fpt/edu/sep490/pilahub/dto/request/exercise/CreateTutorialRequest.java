package fpt.edu.sep490.pilahub.dto.request.exercise;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request to create a new tutorial")
public record CreateTutorialRequest(
        @Schema(description = "Related exercise ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Exercise ID must not be null")
        UUID exerciseId,

        @Schema(description = "Practice video URL", example = "https://example.com/practice.mp4")
        @Size(max = 500, message = "Practice video URL must not exceed 500 characters")
        String practiceVideoUrl,

        @Schema(description = "Theory video URL", example = "https://example.com/theory.mp4")
        @Size(max = 500, message = "Theory video URL must not exceed 500 characters")
        String theoryVideoUrl,

        @Schema(description = "Common mistakes", example = "Not engaging core, incorrect alignment")
        @Size(max = 2000, message = "Common mistakes must not exceed 2000 characters")
        String commonMistakes,

        @Schema(description = "Guidelines", example = "Keep movements slow and controlled")
        @Size(max = 2000, message = "Guidelines must not exceed 2000 characters")
        String guidelines,

        @Schema(description = "Breathing technique", example = "Exhale on exertion, inhale on release")
        @Size(max = 1000, message = "Breathing technique must not exceed 1000 characters")
        String breathingTechnique,

        @Schema(description = "Whether the tutorial is published", example = "false")
        boolean published
) {
}
