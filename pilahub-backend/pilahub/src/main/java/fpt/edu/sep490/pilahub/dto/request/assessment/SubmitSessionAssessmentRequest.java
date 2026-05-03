package fpt.edu.sep490.pilahub.dto.request.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request to submit session assessment form")
public record SubmitSessionAssessmentRequest(
        @Schema(description = "List of criterion scores", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "Assessment results must not be empty")
        @Valid
        List<SubmitAssessmentResultRequest> results
) {
}

