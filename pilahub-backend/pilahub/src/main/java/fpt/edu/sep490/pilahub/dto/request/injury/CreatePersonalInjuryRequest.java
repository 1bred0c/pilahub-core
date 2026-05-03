package fpt.edu.sep490.pilahub.dto.request.injury;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to create a personal injury")
public record CreatePersonalInjuryRequest(
        @NotNull(message = "Injury ID must not be null")
        @Schema(description = "Injury ID from injury library", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID injuryId,

        @Schema(description = "Personal notes about the injury", example = "Got injured during basketball game")
        String notes
) {
}
