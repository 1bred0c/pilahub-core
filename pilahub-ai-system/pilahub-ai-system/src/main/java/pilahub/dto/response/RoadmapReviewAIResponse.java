package pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response containing AI roadmap review analysis")
public class RoadmapReviewAIResponse {

    @Schema(description = "Overall score (0-100)", example = "87")
    @JsonProperty("overallScore")
    private Integer overallScore;

    @Schema(description = "Sub scores by criteria")
    @JsonProperty("subScores")
    private SubScores subScores;

    @Schema(description = "Delta metrics between initial and final profiles")
    @JsonProperty("deltaMetrics")
    private DeltaMetrics deltaMetrics;

    @Schema(description = "Narrative summary in Vietnamese")
    @JsonProperty("narrativeSummary")
    private String narrativeSummary;

    @Schema(description = "Prioritized recommendations list")
    @JsonProperty("prioritizedRecommendations")
    private List<PrioritizedRecommendation> prioritizedRecommendations;

    @Schema(description = "Confidence level (0-100)", example = "85")
    @JsonProperty("confidenceLevel")
    private Integer confidenceLevel;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubScores {
        @JsonProperty("effectiveness")
        private Integer effectiveness;

        @JsonProperty("adherence")
        private Integer adherence;

        @JsonProperty("bodyCompositionChange")
        private Integer bodyCompositionChange;

        @JsonProperty("muscleChange")
        private Integer muscleChange;

        @JsonProperty("waistChange")
        private Integer waistChange;

        @JsonProperty("goalAchievement")
        private Integer goalAchievement;

        @JsonProperty("safetyRisk")
        private Integer safetyRisk;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeltaMetrics {
        @JsonProperty("weightKg")
        private MetricDelta weightKg;

        @JsonProperty("bodyFat%")
        private MetricDelta bodyFatPercent;

        @JsonProperty("muscleMassKg")
        private MetricDelta muscleMassKg;

        @JsonProperty("waistCm")
        private MetricDelta waistCm;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetricDelta {
        @JsonProperty("baseline")
        private BigDecimal baseline;

        @JsonProperty("final")
        private BigDecimal finalValue;

        @JsonProperty("percent")
        private BigDecimal percent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrioritizedRecommendation {
        @JsonProperty("recommendation")
        private String recommendation;

        @JsonProperty("rationale")
        private String rationale;
    }
}

