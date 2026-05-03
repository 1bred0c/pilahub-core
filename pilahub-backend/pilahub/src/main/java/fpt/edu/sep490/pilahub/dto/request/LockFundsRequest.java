package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request to lock funds in wallet")
public record LockFundsRequest(
        @Schema(description = "Amount to lock in VND", example = "100000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Amount must not be null")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @Schema(description = "Reason for locking funds", example = "Pending order payment")
        String reason
) {
}
