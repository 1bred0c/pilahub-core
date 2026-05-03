package fpt.edu.sep490.pilahub.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class MoMoUtil {

    public static String hmacSHA256(String key, String data) {
        try {
            Mac hmac256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac256.init(secretKey);
            byte[] result = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(result);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating HMAC SHA256", e);
        }
    }

    public static String buildCreateSignatureRaw(Map<String, String> data) {
        return "accessKey=" + data.getOrDefault("accessKey", "")
                + "&amount=" + data.getOrDefault("amount", "")
                + "&extraData=" + data.getOrDefault("extraData", "")
                + "&ipnUrl=" + data.getOrDefault("ipnUrl", "")
                + "&orderId=" + data.getOrDefault("orderId", "")
                + "&orderInfo=" + data.getOrDefault("orderInfo", "")
                + "&partnerCode=" + data.getOrDefault("partnerCode", "")
                + "&redirectUrl=" + data.getOrDefault("redirectUrl", "")
                + "&requestId=" + data.getOrDefault("requestId", "")
                + "&requestType=" + data.getOrDefault("requestType", "captureWallet");
    }

    public static String buildIpnSignatureRaw(Map<String, String> data) {
        return "accessKey=" + data.getOrDefault("accessKey", "")
                + "&amount=" + data.getOrDefault("amount", "")
                + "&extraData=" + data.getOrDefault("extraData", "")
                + "&message=" + data.getOrDefault("message", "")
                + "&orderId=" + data.getOrDefault("orderId", "")
                + "&orderInfo=" + data.getOrDefault("orderInfo", "")
                + "&orderType=" + data.getOrDefault("orderType", "")
                + "&partnerCode=" + data.getOrDefault("partnerCode", "")
                + "&payType=" + data.getOrDefault("payType", "")
                + "&requestId=" + data.getOrDefault("requestId", "")
                + "&responseTime=" + data.getOrDefault("responseTime", "")
                + "&resultCode=" + data.getOrDefault("resultCode", "")
                + "&transId=" + data.getOrDefault("transId", "");
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

