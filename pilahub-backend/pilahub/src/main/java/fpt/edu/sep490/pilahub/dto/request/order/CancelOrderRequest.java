package fpt.edu.sep490.pilahub.dto.request.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request to cancel an order")
public record CancelOrderRequest(
        @Schema(description = "Cancellation reason", example = "Changed my mind", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Cancellation reason must not be blank")
        @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
        String cancellationReason
) {
}
