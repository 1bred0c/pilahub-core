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
@Schema(description = "VNPay payment URL response")
public class VNPayDepositResponse {

    @Schema(description = "VNPay payment URL to redirect user", example = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...")
    private String paymentUrl;

    @Schema(description = "Transaction ID for tracking", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID transactionId;

    @Schema(description = "Order code (VNPay TxnRef)", example = "550e8400e29b41d4a716446655440000")
    private String orderCode;
}
