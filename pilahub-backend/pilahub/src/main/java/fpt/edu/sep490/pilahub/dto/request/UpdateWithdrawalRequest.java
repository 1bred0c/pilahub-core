package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Request to update a pending wallet withdrawal")
public record UpdateWithdrawalRequest(
        @Size(max = 255, message = "Recipient name must not exceed 255 characters")
        @Schema(description = "Recipient name", example = "Nguyen Van A")
        String recipientName,

        @Size(max = 50, message = "Bank account number must not exceed 50 characters")
        @Schema(description = "Bank account number", example = "1234567890")
        String bankAccountNumber,

        @Size(max = 50, message = "Bank code must not exceed 50 characters")
        @Schema(description = "Bank code", example = "bidv")
        String bankCode,

        @Size(max = 255, message = "Bank name must not exceed 255 characters")
        @Schema(description = "Bank name", example = "Ngân hàng TMCP Đầu tư và Phát triển Việt Nam")
        String bankName,

        @Size(max = 500, message = "Bank logo URL must not exceed 500 characters")
        @Schema(description = "Bank logo URL", example = "https://example.com/logo.png")
        String bankLogo,

        @DecimalMin(value = "10000.0", message = "Minimum withdrawal amount is 10,000 VND")
        @Schema(description = "Withdrawal amount in VND", example = "500000.00")
        BigDecimal amount,

        @Size(max = 1000, message = "Note must not exceed 1000 characters")
        @Schema(description = "User's note", example = "Urgent withdrawal")
        String note
) {
}
