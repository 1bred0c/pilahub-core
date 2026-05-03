package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Request to create a wallet withdrawal")
public record CreateWithdrawalRequest(
        @NotBlank(message = "Recipient name is required")
        @Size(max = 255, message = "Recipient name must not exceed 255 characters")
        @Schema(description = "Recipient name", example = "Nguyen Van A", requiredMode = Schema.RequiredMode.REQUIRED)
        String recipientName,

        @NotBlank(message = "Bank account number is required")
        @Size(max = 50, message = "Bank account number must not exceed 50 characters")
        @Schema(description = "Bank account number", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
        String bankAccountNumber,

        @NotBlank(message = "Bank code is required")
        @Size(max = 50, message = "Bank code must not exceed 50 characters")
        @Schema(description = "Bank code (appId from VietQR API)", example = "bidv", requiredMode = Schema.RequiredMode.REQUIRED)
        String bankCode,

        @NotBlank(message = "Bank name is required")
        @Size(max = 255, message = "Bank name must not exceed 255 characters")
        @Schema(description = "Bank name", example = "Ngân hàng TMCP Đầu tư và Phát triển Việt Nam", requiredMode = Schema.RequiredMode.REQUIRED)
        String bankName,

        @Size(max = 500, message = "Bank logo URL must not exceed 500 characters")
        @Schema(description = "Bank logo URL", example = "https://example.com/logo.png")
        String bankLogo,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "10000.0", message = "Minimum withdrawal amount is 10,000 VND")
        @Schema(description = "Withdrawal amount in VND", example = "500000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal amount,

        @Size(max = 1000, message = "Note must not exceed 1000 characters")
        @Schema(description = "User's note", example = "Urgent withdrawal")
        String note
) {
}
