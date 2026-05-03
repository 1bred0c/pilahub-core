package fpt.edu.sep490.pilahub.dto.request.exercise;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a tutorial")
public record UpdateTutorialRequest(
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

        @Schema(description = "Whether the tutorial is published", example = "true")
        Boolean published
) {
}
