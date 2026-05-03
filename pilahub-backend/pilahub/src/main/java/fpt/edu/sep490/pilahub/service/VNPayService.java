package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.request.CreateDepositRequest;
import fpt.edu.sep490.pilahub.dto.response.VNPayDepositResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.UUID;

public interface VNPayService {

    /**
     * Create VNPay payment URL for wallet deposit
     * @param accountId Account ID making the deposit
     * @param request Deposit request with amount and description
     * @param httpRequest HTTP request to get client IP
     * @return VNPayDepositResponse containing payment URL and transaction info
     */
    VNPayDepositResponse createDepositPaymentUrl(UUID accountId, CreateDepositRequest request, HttpServletRequest httpRequest);

    /**
     * Handle VNPay IPN (Instant Payment Notification) callback
     * @param params VNPay callback parameters
     * @return Response code for VNPay (00 = success, others = error)
     */
    String handlePaymentCallback(Map<String, String> params);

    /**
     * Verify VNPay callback signature
     * @param params VNPay callback parameters
     * @return true if signature is valid
     */
    boolean verifyPaymentCallback(Map<String, String> params);

    /**
     * Get VNPay payment gateway URL
     * @return VNPay URL
     */
    String getVNPayUrl();

    /**
     * Get configured return URL
     * @return Return URL
     */
    String getReturnUrl();

    /**
     * Get configured IPN URL
     * @return IPN URL
     */
    String getIpnUrl();

    /**
     * Get configured TMN Code
     * @return TMN Code
     */
    String getTmnCode();

    /**
     * Get configured Hash Secret (for debugging)
     * @return Hash Secret
     */
    String getHashSecret();
}
