package fpt.edu.sep490.pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "AI-generated roadmap review response")
public record RoadmapReviewAIResponse(
        @Schema(description = "Overall score", example = "87")
        Integer overallScore,

        @Schema(description = "Sub-scores breakdown")
        Map<String, Integer> subScores,

        @Schema(description = "Delta metrics")
        Map<String, DeltaMetric> deltaMetrics,

        @Schema(description = "Narrative summary")
        String narrativeSummary,

        @Schema(description = "Prioritized recommendations")
        List<PrioritizedRecommendation> prioritizedRecommendations,

        @Schema(description = "Confidence level", example = "85")
        Integer confidenceLevel
) {
    @Schema(description = "Delta metric details")
    public record DeltaMetric(
            @Schema(description = "Baseline value", example = "78.5")
            Double baseline,

            @Schema(description = "Final value", example = "73.2")
            @JsonProperty("final")
            Double finalValue,

            @Schema(description = "Percent change", example = "-6.8")
            Double percent
    ) {}

    @Schema(description = "Recommendation with rationale")
    public record PrioritizedRecommendation(
            @Schema(description = "Recommendation")
            String recommendation,

            @Schema(description = "Rationale")
            String rationale
    ) {}
}

