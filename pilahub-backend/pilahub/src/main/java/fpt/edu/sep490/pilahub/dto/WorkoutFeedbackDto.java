package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Workout feedback information")
public record WorkoutFeedbackDto(
        @Schema(description = "Unique workout feedback identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID workoutFeedbackId,

        @Schema(description = "Workout session identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID workoutSessionId,

        @Schema(description = "Total number of mistakes detected", example = "5")
        Integer totalMistakes,

        @Schema(description = "Form score (0-100)", example = "85.5")
        Double formScore,

        @Schema(description = "Endurance score based on heart rate (0-100)", example = "78.0")
        Double enduranceScore,

        @Schema(description = "Overall performance score (0-100)", example = "82.0")
        Double overallScore,

        @Schema(description = "AI-identified strengths (max 5000 chars)", example = "Good core engagement, proper breathing rhythm")
        String strengths,

        @Schema(description = "AI-identified weaknesses (max 5000 chars)", example = "Tendency to rush transitions, occasional hip misalignment")
        String weaknesses,

        @Schema(description = "AI recommendations for improvement (max 5000 chars)", example = "Focus on slower transitions, maintain neutral spine throughout")
        String recommendations,

        @Schema(description = "AI model used for assessment", example = "pilahub-v1.0")
        String aiModel,

        @Schema(description = "Feedback generation timestamp", example = "2026-01-23T10:30:00Z")
        Instant generatedAt
) {
}

