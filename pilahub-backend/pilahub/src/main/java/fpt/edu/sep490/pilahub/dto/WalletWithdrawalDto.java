package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.WalletWithdrawalStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Wallet withdrawal information")
public record WalletWithdrawalDto(
        @Schema(description = "Withdrawal ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID walletWithdrawalId,

        @Schema(description = "Account ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID accountId,

        @Schema(description = "Recipient name", example = "Nguyen Van A")
        String recipientName,

        @Schema(description = "Bank account number", example = "1234567890")
        String bankAccountNumber,

        @Schema(description = "Bank code", example = "bidv")
        String bankCode,

        @Schema(description = "Bank name", example = "Ngân hàng TMCP Đầu tư và Phát triển Việt Nam")
        String bankName,

        @Schema(description = "Bank logo URL", example = "https://example.com/logo.png")
        String bankLogo,

        @Schema(description = "Withdrawal amount in VND", example = "500000.00")
        BigDecimal amount,

        @Schema(description = "Withdrawal status", example = "PENDING")
        WalletWithdrawalStatus status,

        @Schema(description = "User's note", example = "Urgent withdrawal")
        String note,

        @Schema(description = "Admin's note", example = "Approved by admin")
        String adminNote,

        @Schema(description = "Transfer receipt URL", example = "https://example.com/withdrawals/receipt-123.png")
        String receiptUrl,

        @Schema(description = "Admin who processed this withdrawal", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID processedBy,

        @Schema(description = "Request timestamp", example = "2026-01-23T10:30:00Z")
        Instant requestedAt,

        @Schema(description = "Processed timestamp", example = "2026-01-23T11:00:00Z")
        Instant processedAt,

        @Schema(description = "Completed timestamp", example = "2026-01-23T12:00:00Z")
        Instant completedAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
