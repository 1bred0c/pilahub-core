package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Tutorial information")
public record TutorialDto(
        @Schema(description = "Unique tutorial identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID tutorialId,

        @Schema(description = "Related exercise ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID exerciseId,

        @Schema(description = "Practice video URL", example = "https://example.com/practice.mp4")
        String practiceVideoUrl,

        @Schema(description = "Theory video URL", example = "https://example.com/theory.mp4")
        String theoryVideoUrl,

        @Schema(description = "Common mistakes", example = "Not engaging core, incorrect alignment")
        String commonMistakes,

        @Schema(description = "Guidelines", example = "Keep movements slow and controlled")
        String guidelines,

        @Schema(description = "Breathing technique", example = "Exhale on exertion, inhale on release")
        String breathingTechnique,

        @Schema(description = "Whether the tutorial is published", example = "true")
        boolean published,

        @Schema(description = "Tutorial creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
