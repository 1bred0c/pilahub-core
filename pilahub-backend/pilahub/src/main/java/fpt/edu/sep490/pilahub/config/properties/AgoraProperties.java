package fpt.edu.sep490.pilahub.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "agora")
@Getter
@Setter
@Slf4j
public class AgoraProperties {

    private String appId;
    private String appCertificate;
    private String customerId;
    private String customerSecret;
    private Integer tokenExpirationSeconds;

    private Recording recording = new Recording();

    @PostConstruct
    public void validateConfig() {
        log.info("=== AGORA CONFIGURATION ===");
        log.info("App ID: {}", appId);
        log.info("App Certificate: {}...", appCertificate != null && appCertificate.length() > 10
                ? appCertificate.substring(0, 10) : "NOT SET");
        log.info("Token Expiration: {} seconds", tokenExpirationSeconds);
        log.info("Recording Region: {} (0 = custom endpoint)", recording.getRegion());
        log.info("Recording Endpoint: {}", recording.getEndpoint());
        log.info("Recording Bucket: {}", recording.getBucket());
        log.info("===========================");

        // Validate required fields
        if (appId == null || appId.isEmpty()) {
            log.error("AGORA_APP_ID is not configured!");
        }
        if (appCertificate == null || appCertificate.isEmpty()) {
            log.error("AGORA_APP_CERTIFICATE is not configured!");
        }

        // Validate App Certificate is different from App ID
        if (appId != null && appCertificate != null && appId.equals(appCertificate)) {
            log.error("App Certificate must be DIFFERENT from App ID!");
            log.error("Get the correct App Certificate from Agora Console");
        }
    }

    @Getter
    @Setter
    public static class Recording {
        private Integer region;      // MUST be Integer for Agora API (0 for custom endpoint)
        private String endpoint;      // Backblaze B2 S3-compatible endpoint
        private String bucket;
        private String accessKey;
        private String secretKey;
        private Integer vendor;
        private Integer retentionDays;
    }
}

