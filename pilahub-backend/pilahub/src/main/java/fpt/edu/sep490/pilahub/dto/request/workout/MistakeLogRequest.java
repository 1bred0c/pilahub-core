package fpt.edu.sep490.pilahub.dto.request.workout;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Mistake log data for a workout session")
public record MistakeLogRequest(
        @Schema(description = "Body part identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID bodyPartId,

        @Schema(description = "Mistake details", example = "Form error detected")
        @Size(max = 1000, message = "Details must not exceed 1000 characters")
        String details,

        @Schema(description = "Screenshot URL", example = "https://example.com/mistake.jpg")
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String imageUrl,

        @Schema(description = "Recorded time in seconds from session start", example = "120.5")
        Double recordedAtSecond,

        @Schema(description = "Duration of mistake in seconds", example = "15.0")
        Double duration
) {
}

