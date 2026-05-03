package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.config.properties.AgoraProperties;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Public configuration endpoint for frontend
 * Exposes non-sensitive configuration like Agora App ID
 */
@RestController
@RequestMapping("/api/public/config")
@RequiredArgsConstructor
@Tag(name = "Public Config", description = "Public configuration endpoints")
public class PublicConfigController {

    private final AgoraProperties agoraProperties;

    @GetMapping("/agora")
    @Operation(
            summary = "Get Agora public configuration",
            description = "Returns Agora App ID (non-sensitive) for client-side SDK initialization. " +
                    "App Certificate is NOT exposed for security reasons."
    )
    public ResponseEntity<APIResponse<Map<String, String>>> getAgoraConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("appId", agoraProperties.getAppId());

        return ResponseEntity.ok(APIResponse.success(
                "Agora configuration retrieved successfully",
                config
        ));
    }

    @GetMapping("/agora/health")
    @Operation(
            summary = "Check Agora configuration health",
            description = "Validates that all required Agora credentials are configured properly. " +
                    "Useful for debugging setup issues."
    )
    public ResponseEntity<APIResponse<Map<String, Object>>> checkAgoraHealth() {
        Map<String, Object> health = new HashMap<>();

        boolean appIdConfigured = agoraProperties.getAppId() != null && !agoraProperties.getAppId().isEmpty();
        boolean appCertConfigured = agoraProperties.getAppCertificate() != null && !agoraProperties.getAppCertificate().isEmpty();
        boolean customerIdConfigured = agoraProperties.getCustomerId() != null && !agoraProperties.getCustomerId().isEmpty();
        boolean customerSecretConfigured = agoraProperties.getCustomerSecret() != null && !agoraProperties.getCustomerSecret().isEmpty();

        health.put("appIdConfigured", appIdConfigured);
        health.put("appCertificateConfigured", appCertConfigured);
        health.put("customerIdConfigured", customerIdConfigured);
        health.put("customerSecretConfigured", customerSecretConfigured);
        health.put("tokenExpirationSeconds", agoraProperties.getTokenExpirationSeconds());

        // Show first 8 chars of App ID for verification (not sensitive)
        if (appIdConfigured) {
            String appId = agoraProperties.getAppId();
            health.put("appIdPreview", appId.substring(0, Math.min(8, appId.length())) + "...");
        } else {
            health.put("appIdPreview", "NOT_CONFIGURED");
        }

        boolean isHealthy = appIdConfigured && appCertConfigured && customerIdConfigured && customerSecretConfigured;
        health.put("status", isHealthy ? "HEALTHY" : "UNHEALTHY");

        if (!isHealthy) {
            health.put("message", "Some Agora credentials are missing. Check environment variables or application.yml");
            health.put("help", "See CHECK_AGORA_SETUP.md for detailed instructions");
        }

        return ResponseEntity.ok(APIResponse.success(
                isHealthy ? "Agora configuration is healthy" : "Agora configuration has issues",
                health
        ));
    }
}

