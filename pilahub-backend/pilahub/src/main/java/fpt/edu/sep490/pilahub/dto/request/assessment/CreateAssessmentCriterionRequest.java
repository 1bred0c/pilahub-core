package fpt.edu.sep490.pilahub.dto.request.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create an assessment criterion")
public record CreateAssessmentCriterionRequest(
        @Schema(description = "Criterion name", example = "Ky thuat dong tac", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Criterion name must not be blank")
        @Size(max = 255, message = "Criterion name must not exceed 255 characters")
        String name,

        @Schema(description = "Criterion description", example = "Danh gia do chuan cua dong tac")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(description = "Display order", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Display order must not be null")
        Integer displayOrder
) {
}

