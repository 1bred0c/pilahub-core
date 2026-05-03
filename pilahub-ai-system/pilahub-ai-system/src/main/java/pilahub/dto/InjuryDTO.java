package pilahub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pilahub.enums.InjuryStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InjuryDTO {
    @NotBlank(message = "Injury name must not be blank")
    private String name;

    private String description;
    private String symptoms;
    private String causes;
    private String treatmentSuggestions;
    private String preventionTips;
    private List<AffectedBodyPart> affectedBodyParts;
    private InjuryStatus status;
}
