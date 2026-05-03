package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request to adjust wallet balance (admin)")
public record AdjustBalanceRequest(
        @Schema(description = "Amount to add (positive) or deduct (negative) in VND", example = "100000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Amount must not be null")
        BigDecimal amount,

        @Schema(description = "Reason for adjustment", example = "Refund for order #12345", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Reason must not be null")
        String reason
) {
}
