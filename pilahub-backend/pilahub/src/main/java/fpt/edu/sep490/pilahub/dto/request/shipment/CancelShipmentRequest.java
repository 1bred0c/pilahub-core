package fpt.edu.sep490.pilahub.dto.request.shipment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to cancel a shipment")
public record CancelShipmentRequest(

        @Schema(description = "Reason for cancellation", example = "Vendor out of stock",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Cancellation reason must not be blank")
        @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
        String cancellationReason
) {
}
