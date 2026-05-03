package fpt.edu.sep490.pilahub.service.implement;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.edu.sep490.pilahub.config.properties.MoMoProperties;
import fpt.edu.sep490.pilahub.dto.request.CreateDepositRequest;
import fpt.edu.sep490.pilahub.dto.response.MoMoDepositResponse;
import fpt.edu.sep490.pilahub.enums.TransactionType;
 import fpt.edu.sep490.pilahub.exception.WalletNotFoundException;
import fpt.edu.sep490.pilahub.pojo.Transaction;
import fpt.edu.sep490.pilahub.pojo.Wallet;
import fpt.edu.sep490.pilahub.repository.TransactionRepository;
import fpt.edu.sep490.pilahub.repository.WalletRepository;
import fpt.edu.sep490.pilahub.service.MoMoService;
import fpt.edu.sep490.pilahub.util.MoMoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoMoServiceImpl implements MoMoService {

    private final MoMoProperties moMoProperties;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public MoMoDepositResponse createDepositPayment(UUID accountId, CreateDepositRequest request) {
        log.info("Creating MoMo payment for account: {}, amount: {}", accountId, request.amount());

        walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for account ID: " + accountId));

        UUID orderTrackingId = UUID.randomUUID();
        String orderCode = orderTrackingId.toString().replace("-", "");

        try {
            String amount = request.amount().stripTrailingZeros().toPlainString();
            String requestId = orderCode;
            String orderInfo = "Thanh toan don hang " + orderCode;
            // Keep account id in signed callback payload so we can create transaction only when payment succeeds.
            String extraData = accountId.toString();

            Map<String, String> signatureInput = new LinkedHashMap<>();
            signatureInput.put("accessKey", moMoProperties.getAccessKey());
            signatureInput.put("amount", amount);
            signatureInput.put("extraData", extraData);
            signatureInput.put("ipnUrl", moMoProperties.getIpnUrl());
            signatureInput.put("orderId", orderCode);
            signatureInput.put("orderInfo", orderInfo);
            signatureInput.put("partnerCode", moMoProperties.getPartnerCode());
            signatureInput.put("redirectUrl", moMoProperties.getRedirectUrl());
            signatureInput.put("requestId", requestId);
            signatureInput.put("requestType", moMoProperties.getRequestType());

            String rawSignature = MoMoUtil.buildCreateSignatureRaw(signatureInput);
            String signature = MoMoUtil.hmacSHA256(moMoProperties.getSecretKey(), rawSignature);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("partnerCode", moMoProperties.getPartnerCode());
            payload.put("partnerName", "Pilahub");
            payload.put("storeId", "PilahubStore");
            payload.put("requestId", requestId);
            payload.put("amount", amount);
            payload.put("orderId", orderCode);
            payload.put("orderInfo", orderInfo);
            payload.put("redirectUrl", moMoProperties.getRedirectUrl());
            payload.put("ipnUrl", moMoProperties.getIpnUrl());
            payload.put("lang", moMoProperties.getLang());
            payload.put("requestType", moMoProperties.getRequestType());
            payload.put("autoCapture", true);
            payload.put("extraData", extraData);
            payload.put("signature", signature);

            String requestJson = objectMapper.writeValueAsString(payload);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(moMoProperties.getEndpoint()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            Map<String, Object> responseBody = objectMapper.readValue(
                    response.body(),
                    new TypeReference<Map<String, Object>>() {
                    }
            );

            Integer resultCode = toInteger(responseBody.get("resultCode"));
            if (resultCode == null || resultCode != 0) {
                throw new IllegalStateException("MoMo create payment failed: " + responseBody.get("message"));
            }

            return MoMoDepositResponse.builder()
                    .deeplink((String) responseBody.get("deeplink"))
                    .payUrl((String) responseBody.get("payUrl"))
                    .transactionId(orderTrackingId)
                    .orderCode(orderCode)
                    .build();

        } catch (Exception ex) {
            log.error("Error creating MoMo payment for orderCode {}", orderCode, ex);
            throw new RuntimeException("Failed to create MoMo payment", ex);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> handlePaymentCallback(Map<String, String> params) {
        log.info("========== MoMo IPN Callback START ==========");
        log.info("Received params: {}", params);

        String partnerCode = params.get("partnerCode");
        String requestId = params.get("requestId");
        String orderId = params.get("orderId");

        if (!verifyPaymentCallback(params)) {
            log.error("Invalid MoMo callback signature");
            return buildIpnResponse(partnerCode, requestId, orderId, 13, "Invalid signature");
        }

        try {
            UUID accountId = extractAccountId(params.get("extraData"));
            BigDecimal amount = new BigDecimal(params.getOrDefault("amount", "0"));
            Integer resultCode = toInteger(params.get("resultCode"));

            if (resultCode != null && resultCode == 0) {
                String transId = params.get("transId");
                if (transId == null || transId.isBlank()) {
                    log.error("Missing transId in successful MoMo callback");
                    return buildIpnResponse(partnerCode, requestId, orderId, 99, "Missing transaction reference");
                }

                UUID paymentReferenceId = UUID.nameUUIDFromBytes(transId.getBytes(StandardCharsets.UTF_8));
                if (transactionRepository.existsByReferenceIdAndTransactionType(paymentReferenceId, TransactionType.WALLET_TOP_UP)) {
                    log.warn("MoMo callback already processed for transId: {}", transId);
                    return buildIpnResponse(partnerCode, requestId, orderId, 0, "Already processed");
                }

                Wallet wallet = walletRepository.findByAccountId(accountId)
                        .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

                wallet.setBalanceVND(wallet.getBalanceVND().add(amount));
                wallet.setAvailableVND(wallet.getAvailableVND().add(amount));
                walletRepository.save(wallet);

                transactionRepository.save(Transaction.builder()
                        .transactionType(TransactionType.WALLET_TOP_UP)
                        .amount(amount)
                        .accountId(accountId)
                        .referenceId(paymentReferenceId)
                        .description("Nạp tiền vào ví qua MoMo")
                        .build());

                log.info("MoMo deposit processed successfully. OrderId: {}", orderId);
                return buildIpnResponse(partnerCode, requestId, orderId, 0, "Success");
            }

            log.warn("MoMo payment failed for orderId {} with resultCode {}", orderId, resultCode);
            return buildIpnResponse(partnerCode, requestId, orderId, 0, "Acknowledged");

        } catch (Exception ex) {
            log.error("Error processing MoMo callback", ex);
            return buildIpnResponse(partnerCode, requestId, orderId, 99, "Unknown error");
        } finally {
            log.info("========== MoMo IPN Callback END ==========");
        }
    }

    @Override
    public boolean verifyPaymentCallback(Map<String, String> params) {
        String signature = params.get("signature");
        if (signature == null || signature.isBlank()) {
            return false;
        }

        Map<String, String> signatureData = new LinkedHashMap<>();
        signatureData.put("accessKey", moMoProperties.getAccessKey());
        signatureData.put("amount", params.getOrDefault("amount", ""));
        signatureData.put("extraData", params.getOrDefault("extraData", ""));
        signatureData.put("message", params.getOrDefault("message", ""));
        signatureData.put("orderId", params.getOrDefault("orderId", ""));
        signatureData.put("orderInfo", params.getOrDefault("orderInfo", ""));
        signatureData.put("orderType", params.getOrDefault("orderType", ""));
        signatureData.put("partnerCode", params.getOrDefault("partnerCode", ""));
        signatureData.put("payType", params.getOrDefault("payType", ""));
        signatureData.put("requestId", params.getOrDefault("requestId", ""));
        signatureData.put("responseTime", params.getOrDefault("responseTime", ""));
        signatureData.put("resultCode", params.getOrDefault("resultCode", ""));
        signatureData.put("transId", params.getOrDefault("transId", ""));

        String rawSignature = MoMoUtil.buildIpnSignatureRaw(signatureData);
        String calculatedSignature = MoMoUtil.hmacSHA256(moMoProperties.getSecretKey(), rawSignature);
        return calculatedSignature.equals(signature);
    }

    private Map<String, Object> buildIpnResponse(String partnerCode, String requestId, String orderId, int resultCode, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("partnerCode", partnerCode);
        response.put("requestId", requestId);
        response.put("orderId", orderId);
        response.put("resultCode", resultCode);
        response.put("message", message);
        return response;
    }

    private UUID extractAccountId(String extraData) {
        if (extraData == null || extraData.isBlank()) {
            throw new IllegalArgumentException("Missing extraData in MoMo callback");
        }
        return UUID.fromString(extraData.trim());
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return Integer.parseInt(String.valueOf(value));
    }
}

