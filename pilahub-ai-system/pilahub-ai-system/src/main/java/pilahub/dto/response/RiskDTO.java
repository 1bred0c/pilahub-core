package pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pilahub.enums.RiskSeverity;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskDTO {
    private String riskType;
    private RiskSeverity severity;
    private String description;
    private List<String> affectedBodyParts;
}
