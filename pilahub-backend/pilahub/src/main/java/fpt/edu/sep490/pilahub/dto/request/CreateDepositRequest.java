package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request to create VNPay payment URL for wallet deposit")
public record CreateDepositRequest(

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "10000", message = "Minimum deposit amount is 10,000 VND")
        @Schema(description = "Deposit amount in VND", example = "100000", minimum = "10000")
        BigDecimal amount,

        @Schema(description = "Deposit description/note", example = "Nạp tiền vào ví")
        String description
) {
}
