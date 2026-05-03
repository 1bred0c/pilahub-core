package fpt.edu.sep490.pilahub.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Utility class for VNPay integration
 */
public class VNPayUtil {

    /**
     * Generate HMAC SHA512 signature
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(result);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }

    /**
     * Convert byte array to hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Build query string from parameters map (with URL encoding)
     * Used for building payment URL
     */
    public static String buildQueryString(Map<String, String> params) {
        // Sort parameters by key
        TreeMap<String, String> sortedParams = new TreeMap<>(params);

        return sortedParams.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry -> {
                    try {
                        return URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString())
                                + "="
                                + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException("Error encoding URL parameter", e);
                    }
                })
                .collect(Collectors.joining("&"));
    }

    /**
     * Build hash data string from parameters map (WITH URL encoding)
     * Used for creating signature - data MUST be URL encoded according to VNPay specs
     */
    public static String buildHashData(Map<String, String> params) {
        // Sort parameters by key
        TreeMap<String, String> sortedParams = new TreeMap<>(params);

        StringBuilder hashData = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (!first) {
                    hashData.append("&");
                }
                try {
                    hashData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()))
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Error encoding URL parameter", e);
                }
                first = false;
            }
        }

        return hashData.toString();
    }
}
