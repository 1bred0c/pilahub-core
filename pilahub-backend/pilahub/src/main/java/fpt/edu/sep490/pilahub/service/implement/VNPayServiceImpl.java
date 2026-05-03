package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.config.properties.VNPayProperties;
import fpt.edu.sep490.pilahub.enums.NotificationType;
import fpt.edu.sep490.pilahub.event.NotificationEvent;
import fpt.edu.sep490.pilahub.dto.request.CreateDepositRequest;
import fpt.edu.sep490.pilahub.dto.response.VNPayDepositResponse;
import fpt.edu.sep490.pilahub.enums.TransactionType;
import fpt.edu.sep490.pilahub.exception.WalletNotFoundException;
import fpt.edu.sep490.pilahub.pojo.Transaction;
import fpt.edu.sep490.pilahub.pojo.Wallet;
import fpt.edu.sep490.pilahub.repository.TransactionRepository;
import fpt.edu.sep490.pilahub.repository.WalletRepository;
import fpt.edu.sep490.pilahub.service.VNPayService;
import fpt.edu.sep490.pilahub.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    private final VNPayProperties vnPayProperties;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public VNPayDepositResponse createDepositPaymentUrl(UUID accountId, CreateDepositRequest request,
            HttpServletRequest httpRequest) {
        log.info("Creating VNPay payment URL for account: {}, amount: {}", accountId, request.amount());

        // Verify wallet exists
        walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for account ID: " + accountId));

        // Generate order code for gateway request. Transaction is created only after
        // successful callback.
        UUID orderTrackingId = UUID.randomUUID();

        // Build VNPay parameters - using TreeMap to maintain sorted order
        Map<String, String> vnpParams = new TreeMap<>();

        // Order code - use UUID without hyphens
        String orderCode = orderTrackingId.toString().replace("-", "");

        // Get client IP
        String clientIp = getClientIp(httpRequest);

        // Create date format: yyyyMMddHHmmss (timezone: GMT+7)
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnpCreateDate = formatter.format(new Date());

        // Expiry time: 15 minutes from now
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        calendar.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(calendar.getTime());

        // Amount in smallest unit (VND * 100)
        long amountInSmallestUnit = request.amount().multiply(new BigDecimal("100")).longValue();

        // Required parameters according to VNPay specification
        vnpParams.put("vnp_Version", vnPayProperties.getVersion());
        vnpParams.put("vnp_Command", vnPayProperties.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amountInSmallestUnit));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderCode);
        // Keep account id in signed callback payload so we can create transaction after
        // successful payment.
        vnpParams.put("vnp_OrderInfo", "WALLET_TOP_UP|" + accountId);
        vnpParams.put("vnp_OrderType", vnPayProperties.getOrderType());
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        vnpParams.put("vnp_IpAddr", clientIp);
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Build hash data (NOT URL encoded) and generate secure hash
        String hashData = VNPayUtil.buildHashData(vnpParams);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), hashData);

        // Log for debugging
        log.info("VNPay TmnCode: {}", vnPayProperties.getTmnCode());
        log.info("Hash data: {}", hashData);
        log.info("Secure hash: {}", vnpSecureHash);

        // Build payment URL with URL encoded params
        String queryString = VNPayUtil.buildQueryString(vnpParams);
        String paymentUrl = vnPayProperties.getUrl() + "?" + queryString + "&vnp_SecureHash=" + vnpSecureHash;

        log.info("Payment URL generated for orderCode: {}", orderCode);

        return VNPayDepositResponse.builder()
                .paymentUrl(paymentUrl)
                .transactionId(orderTrackingId)
                .orderCode(orderCode)
                .build();
    }

    @Override
    @Transactional
    public String handlePaymentCallback(Map<String, String> params) {
        log.info("========== VNPay IPN Callback START ==========");
        log.info("Received params: {}", params);

        // Log important params
        log.info("vnp_TxnRef: {}", params.get("vnp_TxnRef"));
        log.info("vnp_ResponseCode: {}", params.get("vnp_ResponseCode"));
        log.info("vnp_TransactionStatus: {}", params.get("vnp_TransactionStatus"));
        log.info("vnp_Amount: {}", params.get("vnp_Amount"));
        log.info("vnp_SecureHash: {}", params.get("vnp_SecureHash"));

        // Verify signature first
        if (!verifyPaymentCallback(params)) {
            log.error("❌ Invalid VNPay callback signature - Payment will NOT be processed");
            log.error("========== VNPay IPN Callback END (SIGNATURE FAILED) ==========");
            return "97"; // Invalid signature
        }

        log.info("✅ Signature verified successfully");

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo"); // VNPay transaction ID
        String amountStr = params.get("vnp_Amount");
        String orderInfo = params.get("vnp_OrderInfo");

        try {
            BigDecimal vnpAmount = new BigDecimal(amountStr).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            UUID accountId = extractAccountIdFromOrderInfo(orderInfo);

            // Process based on response code
            log.info("Processing payment with responseCode: {}", responseCode);
            if ("00".equals(responseCode)) {
                log.info("✅ Payment successful - Updating wallet and creating transaction");

                UUID paymentReferenceId = UUID.nameUUIDFromBytes(transactionNo.getBytes());
                if (transactionRepository.existsByReferenceIdAndTransactionType(paymentReferenceId,
                        TransactionType.WALLET_TOP_UP)) {
                    log.warn("⚠️ Callback already processed for VNPay reference: {}", transactionNo);
                    log.info("========== VNPay IPN Callback END (ALREADY PROCESSED) ==========");
                    return "02";
                }

                Wallet wallet = walletRepository.findByAccountId(accountId)
                        .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

                log.info("Current wallet balance: BalanceVND={}, AvailableVND={}",
                        wallet.getBalanceVND(), wallet.getAvailableVND());

                wallet.setBalanceVND(wallet.getBalanceVND().add(vnpAmount));
                wallet.setAvailableVND(wallet.getAvailableVND().add(vnpAmount));
                walletRepository.save(wallet);

                log.info("💰 Updated wallet balance: BalanceVND={}, AvailableVND={}",
                        wallet.getBalanceVND(), wallet.getAvailableVND());

                Transaction savedTransaction = transactionRepository.save(Transaction.builder()
                        .transactionType(TransactionType.WALLET_TOP_UP)
                        .amount(vnpAmount)
                        .accountId(accountId)
                        .referenceId(paymentReferenceId)
                        .description("Nạp tiền vào ví qua VNPay")
                        .build());

                eventPublisher.publishEvent(new NotificationEvent(
                        this,
                        accountId,
                        NotificationType.WALLET_DEPOSIT_SUCCESS,
                        "Nạp Tiền Thành Công",
                        "Bạn đã nạp thành công " + vnpAmount.toPlainString() + " VND vào ví của mình.",
                        savedTransaction.getTransactionId(), "TRANSACTION"));

                log.info("✅ Successfully processed deposit. OrderCode: {}, Amount: {}", txnRef, vnpAmount);
                log.info("========== VNPay IPN Callback END (SUCCESS) ==========");
                return "00";
            }

            log.warn("⚠️ Payment failed for orderCode: {}. Response code: {}", txnRef, responseCode);
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    accountId,
                    NotificationType.WALLET_DEPOSIT_FAILED,
                    "Nạp Tiền Thất Bại",
                    "Việc nạp " + vnpAmount.toPlainString() + " VND không thành công. Vui lòng thử lại.",
                    null, null));

            log.info("========== VNPay IPN Callback END (PAYMENT FAILED) ==========");
            return "00"; // Acknowledge receipt

        } catch (Exception e) {
            log.error("❌ Error processing VNPay callback", e);
            log.info("========== VNPay IPN Callback END (ERROR) ==========");
            return "99"; // Unknown error
        }
    }

    @Override
    public boolean verifyPaymentCallback(Map<String, String> params) {
        log.info("--- Verifying VNPay signature ---");
        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null) {
            log.error("❌ Missing vnp_SecureHash in callback");
            return false;
        }
        log.info("Received vnp_SecureHash: {}", vnpSecureHash);

        // Remove hash parameters before verification
        Map<String, String> paramsToVerify = new HashMap<>(params);
        paramsToVerify.remove("vnp_SecureHash");
        paramsToVerify.remove("vnp_SecureHashType");

        log.info("Parameters to verify (after removing hash): {}", paramsToVerify);

        // Build hash data and verify
        String hashData = VNPayUtil.buildHashData(paramsToVerify);
        log.info("Built hash data: {}", hashData);

        String calculatedHash = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), hashData);
        log.info("Calculated hash: {}", calculatedHash);

        boolean isValid = calculatedHash.equals(vnpSecureHash);
        log.info("Signature verification result: {}", isValid ? "✅ VALID" : "❌ INVALID");

        if (!isValid) {
            log.error("Expected hash: {}", calculatedHash);
            log.error("Received hash: {}", vnpSecureHash);
        }

        return isValid;
    }

    @Override
    public String getVNPayUrl() {
        return vnPayProperties.getUrl();
    }

    @Override
    public String getReturnUrl() {
        return vnPayProperties.getReturnUrl();
    }

    @Override
    public String getIpnUrl() {
        return vnPayProperties.getIpnUrl();
    }

    @Override
    public String getTmnCode() {
        return vnPayProperties.getTmnCode();
    }

    @Override
    public String getHashSecret() {
        return vnPayProperties.getHashSecret();
    }

    private UUID extractAccountIdFromOrderInfo(String orderInfo) {
        if (orderInfo == null || orderInfo.isBlank()) {
            throw new IllegalArgumentException("Missing vnp_OrderInfo");
        }

        String[] parts = orderInfo.split("\\|");
        if (parts.length != 2 || !"WALLET_TOP_UP".equals(parts[0])) {
            throw new IllegalArgumentException("Invalid vnp_OrderInfo format");
        }

        return UUID.fromString(parts[1]);
    }

    /**
     * Get client IP address from HTTP request
     */
    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // In case of multiple IPs in X-Forwarded-For, take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress != null ? ipAddress : "0.0.0.0";
    }
}
