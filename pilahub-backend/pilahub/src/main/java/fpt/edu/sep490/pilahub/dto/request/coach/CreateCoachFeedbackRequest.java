package fpt.edu.sep490.pilahub.dto.request.coach;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Schema(description = "Request to create feedback for a coach")
public record CreateCoachFeedbackRequest(
        @Schema(description = "Coach ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Coach ID must not be null")
        UUID coachId,

        @Schema(description = "Rating (1-5)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Rating must not be null")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must not exceed 5")
        Integer rating,

        @Schema(description = "Feedback comment", example = "Excellent coach! Very professional and helpful.")
        @Size(max = 2000, message = "Comment must not exceed 2000 characters")
        String comment
) {
}
