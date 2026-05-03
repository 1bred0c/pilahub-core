package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Request to create a new fitness goal")
public record CreateFitnessGoalRequest(

        @Schema(description = "Fitness goal code (must be unique)", example = "BACK_PAIN_RELIEF",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Fitness goal code must not be blank")
        @Size(max = 100, message = "Fitness goal code must not exceed 100 characters")
        String code,

        @Schema(description = "Vietnamese name", example = "Giảm đau lưng",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Vietnamese name must not be blank")
        @Size(max = 255, message = "Vietnamese name must not exceed 255 characters")
        String vietnameseName,

        @Schema(description = "English description", example = "Relieve back pain",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Description must not be blank")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Schema(description = "Related purpose IDs (UUIDs of existing purposes)")
        Set<UUID> relatedPurposeIds
) {
}
