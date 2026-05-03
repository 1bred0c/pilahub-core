package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to complete a workout session")
public record CompleteWorkoutSessionRequest(
        @Schema(description = "Recording URL from client (required)", example = "https://storage.example.com/recordings/xyz123.mp4", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(max = 1000, message = "Record URL must not exceed 1000 characters")
        String recordUrl
) {
}

