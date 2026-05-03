package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for admin to complete a withdrawal")
public record CompleteWithdrawalRequest(
        @NotBlank(message = "Receipt URL is required when completing withdrawal")
        @Size(max = 500, message = "Receipt URL must not exceed 500 characters")
        @Schema(description = "Transfer receipt URL", example = "https://example.com/withdrawals/receipt-123.png", requiredMode = Schema.RequiredMode.REQUIRED)
        String receiptUrl
) {
}

