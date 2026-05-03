package pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InBodyExtractionDataResponse {
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private BigDecimal bmi;
    private BigDecimal bodyFatPercentage;
    private BigDecimal muscleMassKg;
    private BigDecimal waistCm;
    private BigDecimal hipCm;
    private String source;
    private String metadata;
}

