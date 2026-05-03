package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MoMo payment response for wallet deposit")
public class MoMoDepositResponse {

    @Schema(description = "MoMo deeplink for app-to-app payment", example = "momo://app?action=payWithApp&isScanQR=true&serviceType=...")
    private String deeplink;

    @Schema(description = "MoMo pay URL fallback", example = "https://test-payment.momo.vn/v2/gateway/pay?t=...")
    private String payUrl;

    @Schema(description = "Transaction ID for tracking", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID transactionId;

    @Schema(description = "Order code sent to MoMo", example = "550e8400e29b41d4a716446655440000")
    private String orderCode;
}

