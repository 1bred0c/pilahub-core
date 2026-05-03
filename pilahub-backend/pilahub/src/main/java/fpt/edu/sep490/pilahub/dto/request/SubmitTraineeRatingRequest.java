package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request to submit trainee rating for a completed live session")
public record SubmitTraineeRatingRequest(
        @Schema(
                description = "Rating score from trainee (0.5 to 5.0 in 0.5 increments)",
                example = "4.5",
                minimum = "0.5",
                maximum = "5.0"
        )
        @NotNull(message = "Rating must not be null")
        @DecimalMin(value = "0.5", message = "Rating must be at least 0.5")
        @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
        BigDecimal rating
) {
}

