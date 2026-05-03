package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Transaction information")
public record TransactionDto(
        @Schema(description = "Unique transaction identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID transactionId,

        @Schema(description = "Type of transaction", example = "WALLET_TOP_UP")
        TransactionType transactionType,

        @Schema(description = "Transaction amount", example = "100000.00")
        BigDecimal amount,

        @Schema(description = "Account ID associated with the transaction", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID accountId,

        @Schema(description = "Reference ID to related entity", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID referenceId,

        @Schema(description = "Transaction description", example = "Wallet top-up via bank transfer")
        String description,

        @Schema(description = "Transaction date", example = "2026-01-23T10:30:00Z")
        Instant transactionDate
) {
}
