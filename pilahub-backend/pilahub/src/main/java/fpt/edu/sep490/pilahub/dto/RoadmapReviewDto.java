package fpt.edu.sep490.pilahub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Roadmap review information")
public record RoadmapReviewDto(
        @Schema(description = "Unique roadmap review identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID roadmapReviewId,

        @Schema(description = "Roadmap identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID roadmapId,

        @Schema(description = "Overall effectiveness score", example = "87")
        Integer overallScore,

        @Schema(description = "Sub-scores breakdown")
        Map<String, Integer> subScores,

        @Schema(description = "Delta metrics comparing initial vs final")
        Map<String, DeltaMetric> deltaMetrics,

        @Schema(description = "Narrative summary and explanation")
        String narrativeSummary,

        @Schema(description = "Prioritized recommendations")
        List<PrioritizedRecommendation> prioritizedRecommendations,

        @Schema(description = "Confidence level (0-100)", example = "85")
        Integer confidenceLevel,

        @Schema(description = "Review creation timestamp", example = "2026-04-29T10:30:00Z")
        Instant createdAt
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
            @Schema(description = "Recommendation text")
            String recommendation,

            @Schema(description = "Rationale for recommendation")
            String rationale
    ) {}
}
