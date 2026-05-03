package fpt.edu.sep490.pilahub.dto.request.injury;

import fpt.edu.sep490.pilahub.enums.InjuryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a personal injury")
public record UpdatePersonalInjuryRequest(
        @Schema(description = "Injury status", example = "RECOVERED")
        InjuryStatus status,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        @Schema(description = "Personal notes about the injury", example = "Feeling better after physical therapy")
        String notes
) {
}
