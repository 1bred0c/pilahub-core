package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Wallet balance information")
public record WalletDto(
        @Schema(description = "Account ID associated with this wallet", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID accountId,

        @Schema(description = "Total balance in VND", example = "1000000.00")
        BigDecimal balanceVND,

        @Schema(description = "Available balance in VND (can be used for transactions)", example = "800000.00")
        BigDecimal availableVND,

        @Schema(description = "Locked balance in VND (reserved for pending transactions)", example = "200000.00")
        BigDecimal lockedVND,

        @Schema(description = "Whether the wallet is active", example = "true")
        boolean active,

        @Schema(description = "Wallet creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant openAt
) {
}
