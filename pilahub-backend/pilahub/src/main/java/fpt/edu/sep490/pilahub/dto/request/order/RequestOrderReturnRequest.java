package fpt.edu.sep490.pilahub.dto.request.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to return an order")
public record RequestOrderReturnRequest(
        @Schema(description = "Reason for returning the order", example = "Products were defective", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Return reason must not be blank") @Size(max = 500, message = "Return reason must not exceed 500 characters") String reason) {
}
