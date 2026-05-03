package pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import pilahub.enums.AIModel;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response containing workout feedback analysis")
public class WorkoutFeedbackAnalysisResponse {

    @Schema(description = "Total number of mistakes detected", example = "5")
    @JsonProperty("totalMistakes")
    private Integer totalMistakes;

    @Schema(description = "Form quality score (0-100)", example = "85.5")
    @JsonProperty("formScore")
    private Double formScore;

    @Schema(description = "Endurance score based on heart rate (0-100, nullable)", example = "78.0")
    @JsonProperty("enduranceScore")
    private Double enduranceScore;

    @Schema(description = "Overall performance score (0-100)", example = "82.5")
    @JsonProperty("overallScore")
    private Double overallScore;

    @Schema(description = "Positive aspects of the workout (in Vietnamese)", example = "Tư thế lõi rất tốt...")
    @JsonProperty("strengths")
    private String strengths;

    @Schema(description = "Areas needing improvement (in Vietnamese)", example = "Xu hướng vội vàng...")
    @JsonProperty("weaknesses")
    private String weaknesses;

    @Schema(description = "Actionable recommendations for improvement (in Vietnamese)", example = "Hãy tập trung vào...")
    @JsonProperty("recommendations")
    private String recommendations;

    @Schema(description = "AI model identifier", example = "gemini-2.0-flash")
    @JsonProperty("aiModel")
    private String aiModel;

    @Schema(description = "Timestamp of analysis")
    @JsonProperty("analyzedAt")
    private Instant analyzedAt;
}

