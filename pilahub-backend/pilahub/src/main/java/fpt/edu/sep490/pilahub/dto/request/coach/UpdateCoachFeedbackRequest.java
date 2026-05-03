package fpt.edu.sep490.pilahub.dto.request.coach;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request to update coach feedback")
public record UpdateCoachFeedbackRequest(
        @Schema(description = "Rating (1-5)", example = "5")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must not exceed 5")
        Integer rating,

        @Schema(description = "Feedback comment", example = "Excellent coach! Very professional and helpful.")
        @Size(max = 2000, message = "Comment must not exceed 2000 characters")
        String comment
) {
}
