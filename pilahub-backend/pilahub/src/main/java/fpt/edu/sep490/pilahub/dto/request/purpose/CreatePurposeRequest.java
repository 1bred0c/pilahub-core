package fpt.edu.sep490.pilahub.dto.request.purpose;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new purpose")
public record CreatePurposeRequest(
        @Schema(description = "Purpose name", example = "Muscle Gain", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Purpose name must not be blank")
        @Size(max = 255, message = "Purpose name must not exceed 255 characters")
        String name,

        @Schema(description = "Purpose code", example = "MUSCLE_GAIN", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Purpose code must not be blank")
        @Size(max = 100, message = "Purpose code must not exceed 100 characters")
        String code,

        @Schema(description = "Purpose description", example = "Supplements designed to support muscle growth and development")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {
}
