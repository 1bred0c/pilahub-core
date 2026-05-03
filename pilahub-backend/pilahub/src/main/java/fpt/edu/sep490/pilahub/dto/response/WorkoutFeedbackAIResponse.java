package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI-generated workout feedback response")
public record WorkoutFeedbackAIResponse(
        @Schema(description = "Total mistakes detected", example = "5")
        Integer totalMistakes,

        @Schema(description = "Form score (0-100)", example = "85.5")
        Double formScore,

        @Schema(description = "Endurance score (0-100)", example = "78.0")
        Double enduranceScore,

        @Schema(description = "Overall performance score (0-100)", example = "82.0")
        Double overallScore,

        @Schema(description = "Identified strengths", example = "Good core engagement throughout the session. Breathing rhythm was consistent.")
        String strengths,

        @Schema(description = "Identified weaknesses", example = "Tendency to rush through transitions. Occasional loss of hip alignment in later reps.")
        String weaknesses,

        @Schema(description = "Recommendations for improvement", example = "Focus on slower, controlled transitions. Maintain awareness of hip position throughout the movement.")
        String recommendations,

        @Schema(description = "AI model identifier", example = "pilahub-workout-analyzer-v1.0")
        String aiModel
) {
}

