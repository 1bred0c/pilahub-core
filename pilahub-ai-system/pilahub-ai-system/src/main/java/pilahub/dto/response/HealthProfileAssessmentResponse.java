package pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import pilahub.enums.AIModel;
import pilahub.enums.HealthProfileLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class HealthProfileAssessmentResponse {
    private Integer score;
    private HealthProfileLevel healthProfileLevel;
    private List<HighlightDTO> highlights;
    private List<RiskDTO> risks;
    private JsonNode explanations;
    private RecommendationsDTO recommendations;
    private BigDecimal confidenceScore;
    private AIModel aiModel;
    private Instant assessedAt;
}
