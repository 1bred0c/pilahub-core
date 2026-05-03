package fpt.edu.sep490.pilahub.service.implement;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import fpt.edu.sep490.pilahub.config.properties.AgoraProperties;
import fpt.edu.sep490.pilahub.exception.InvalidRequestException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.pojo.LiveSession;
import fpt.edu.sep490.pilahub.repository.LiveSessionRepository;
import fpt.edu.sep490.pilahub.service.AgoraRecordingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Implementation of Agora Cloud Recording Service
 * Uses Agora RESTful API to manage cloud recordings
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AgoraRecordingServiceImpl implements AgoraRecordingService {

    private final LiveSessionRepository liveSessionRepository;
    private final AgoraProperties agoraProperties;
    private final RestTemplate restTemplate;

    private static final String AGORA_RECORDING_BASE_URL = "https://api.sd-rtn.com/v1/apps";
    private static final int RECORDING_RETENTION_DAYS = 7;
    private static final int RECORDING_BOT_UID = 999999;

    @Override
    public void startRecording(UUID liveSessionId) {
        LiveSession session = findSessionOrThrow(liveSessionId);

        if (session.getAgoraResourceId() != null) {
            log.warn("Recording already started for session: {}", liveSessionId);
            return;
        }

        try {
            log.info("Starting Agora Cloud Recording for session: {}", liveSessionId);
            log.info("Channel: {}, Recording UID: {}", session.getChannelName(), RECORDING_BOT_UID);

            String resourceId = acquireResource(session);
            session.setAgoraResourceId(resourceId);
            log.info("Acquired resource ID: {}", resourceId);

            String sid = startRecordingWithResource(session, resourceId);
            session.setAgoraRecordingSid(sid);

            liveSessionRepository.save(session);
            log.info("Started recording with SID: {}", sid);

            verifyRecordingStarted(session);

        } catch (Exception e) {
            log.error("Failed to start recording for session {}: {}", liveSessionId, e.getMessage(), e);
            session.setErrorMessage("Recording start failed: " + e.getMessage());
            liveSessionRepository.save(session);
            throw new InvalidRequestException("Failed to start recording: " + e.getMessage());
        }
    }

    @Override
    public void stopRecording(UUID liveSessionId) {
        LiveSession session = findSessionOrThrow(liveSessionId);

        if (session.getAgoraResourceId() == null || session.getAgoraRecordingSid() == null) {
            log.warn("No active recording found for session: {}", liveSessionId);
            return;
        }

        try {
            stopRecordingWithResource(session);

            // Wait for Agora to upload file to storage
            String status;
            Map<String, Object> queryResponse;
            int maxRetries = 15;
            int attempts = 0;

            log.info("Waiting for Agora to upload recording to storage...");
            do {
                Thread.sleep(2000);
                queryResponse = queryRecordingDetails(session);
                status = queryRecordingStatus(session);
                attempts++;
                log.info("Polling status... Attempt {}: {}", attempts, status);
            } while (!"6".equals(status) && attempts < maxRetries);

            if (!"6".equals(status)) {
                log.warn("Upload timeout reached but status is not 6. Files may be incomplete.");
            } else {
                log.info("Upload completed successfully (Status 6)");
            }

            String recordingUrl = buildRecordingUrlFromQueryResponse(session, queryResponse);
            session.setRecordingUrl(recordingUrl);
            session.setRecordingExpiresAt(Instant.now().plus(RECORDING_RETENTION_DAYS, ChronoUnit.DAYS));

            liveSessionRepository.save(session);
            log.info("Stopped recording for session: {}", liveSessionId);
            log.info("Recording URL: {}", recordingUrl);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidRequestException("Failed to stop recording: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to stop recording for session {}: {}", liveSessionId, e.getMessage(), e);
            throw new InvalidRequestException("Failed to stop recording: " + e.getMessage());
        }
    }

    @Override
    public String getRecordingStatus(UUID liveSessionId) {
        LiveSession session = findSessionOrThrow(liveSessionId);

        if (session.getAgoraResourceId() == null || session.getAgoraRecordingSid() == null) {
            return "NO_RECORDING";
        }

        try {
            return queryRecordingStatus(session);
        } catch (Exception e) {
            log.error("Failed to query recording status for session {}: {}", liveSessionId, e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    public String getRecordingUrl(UUID liveSessionId) {
        LiveSession session = findSessionOrThrow(liveSessionId);

        if (session.getRecordingUrl() == null || session.getRecordingUrl().isEmpty()) {
            throw new InvalidRequestException("Recording URL not available for this session");
        }

        String recordingUrl = session.getRecordingUrl();

        // Check if direct URL (no signature), generate presigned URL
        if (!recordingUrl.contains("X-Amz-Signature")) {
            try {
                String[] parts = recordingUrl.split("/file/" + agoraProperties.getRecording().getBucket() + "/");
                if (parts.length > 1) {
                    String objectKey = parts[1];
                    log.info("Generating fresh presigned URL for: {}", objectKey);
                    return generateSignedDownloadUrl(objectKey);
                }
            } catch (Exception e) {
                log.error("Failed to parse objectKey from URL: {}", e.getMessage());
            }
        }

        // If already presigned, generate fresh URL to avoid expiration
        try {
            String baseUrl = recordingUrl.split("\\?")[0];
            String[] parts = baseUrl.split("/");
            int recordingsIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if ("recordings".equals(parts[i])) {
                    recordingsIndex = i;
                    break;
                }
            }

            if (recordingsIndex >= 0 && recordingsIndex + 2 < parts.length) {
                String objectKey = String.join("/",
                    Arrays.copyOfRange(parts, recordingsIndex, parts.length));

                log.info("Refreshing presigned URL for: {}", objectKey);
                return generateSignedDownloadUrl(objectKey);
            }
        } catch (Exception e) {
            log.error("Failed to refresh presigned URL: {}", e.getMessage());
        }

        return recordingUrl;
    }

    private LiveSession findSessionOrThrow(UUID liveSessionId) {
        return liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("LiveSession", "id", liveSessionId));
    }

    private String acquireResource(LiveSession session) {
        String url = String.format("%s/%s/cloud_recording/acquire",
                AGORA_RECORDING_BASE_URL, agoraProperties.getAppId());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cname", session.getChannelName());
        requestBody.put("uid", String.valueOf(RECORDING_BOT_UID));
        requestBody.put("clientRequest", Map.of(
                "resourceExpiredHour", 24,
                "scene", 0
        ));

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new InvalidRequestException("Failed to acquire recording resource");
        }

        return (String) response.getBody().get("resourceId");
    }

    private String startRecordingWithResource(LiveSession session, String resourceId) {
        String url = String.format("%s/%s/cloud_recording/resourceid/%s/mode/mix/start",
                AGORA_RECORDING_BASE_URL, agoraProperties.getAppId(), resourceId);

        // Recording configuration
        Map<String, Object> recordingConfig = new HashMap<>();
        recordingConfig.put("channelType", 0);
        recordingConfig.put("streamTypes", 2);
        recordingConfig.put("maxIdleTime", 120);
        recordingConfig.put("transcodingConfig", Map.of(
                "width", 1280,
                "height", 720,
                "fps", 30,
                "bitrate", 2000,
                "mixedVideoLayout", 1
        ));

        // Recording file configuration for MP4 output
        Map<String, Object> recordingFileConfig = new HashMap<>();
        recordingFileConfig.put("avFileType", Arrays.asList("hls", "mp4"));

        // Storage configuration for Backblaze B2
        Map<String, Object> storageConfig = new HashMap<>();
        storageConfig.put("vendor", 11);
        storageConfig.put("region", 0);
        storageConfig.put("bucket", agoraProperties.getRecording().getBucket());
        storageConfig.put("accessKey", agoraProperties.getRecording().getAccessKey());
        storageConfig.put("secretKey", agoraProperties.getRecording().getSecretKey());

        Map<String, Object> extParams = new HashMap<>();
        String endpoint = agoraProperties.getRecording().getEndpoint();
        if (endpoint != null && !endpoint.isEmpty()) {
            if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
                endpoint = "https://" + endpoint;
            }
            extParams.put("endpoint", endpoint);
        }
        storageConfig.put("extensionParams", extParams);
        storageConfig.put("fileNamePrefix", List.of("recordings", session.getLiveSessionId().toString()));

        String recordingToken = generateRecordingToken(session.getChannelName(), RECORDING_BOT_UID);

        // Build client request
        Map<String, Object> clientRequest = new HashMap<>();
        clientRequest.put("token", recordingToken);
        clientRequest.put("recordingConfig", recordingConfig);
        clientRequest.put("recordingFileConfig", recordingFileConfig);
        clientRequest.put("storageConfig", storageConfig);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cname", session.getChannelName());
        requestBody.put("uid", String.valueOf(RECORDING_BOT_UID));
        requestBody.put("clientRequest", clientRequest);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        log.info("Starting recording with MP4 output - Channel: {}, UID: {}, ResourceID: {}",
                session.getChannelName(), RECORDING_BOT_UID, resourceId);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new InvalidRequestException("Failed to start recording");
        }

        return (String) response.getBody().get("sid");
    }

    private Map<String, Object> stopRecordingWithResource(LiveSession session) {
        String url = String.format("%s/%s/cloud_recording/resourceid/%s/sid/%s/mode/mix/stop",
                AGORA_RECORDING_BASE_URL,
                agoraProperties.getAppId(),
                session.getAgoraResourceId(),
                session.getAgoraRecordingSid());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cname", session.getChannelName());
        requestBody.put("uid", String.valueOf(RECORDING_BOT_UID));
        requestBody.put("clientRequest", Collections.emptyMap());

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new InvalidRequestException("Failed to stop recording");
        }

        log.info("Recording stopped successfully for session: {}", session.getLiveSessionId());

        // Return the full response for file list extraction
        return response.getBody();
    }

    /**
     * Query recording status (status code only)
     * GET /v1/apps/{appId}/cloud_recording/resourceid/{resourceId}/sid/{sid}/mode/mix/query
     */
    private String queryRecordingStatus(LiveSession session) {
        Map<String, Object> queryResponse = queryRecordingDetails(session);

        if (queryResponse != null) {
            Map<String, Object> serverResponse = (Map<String, Object>) queryResponse.get("serverResponse");
            if (serverResponse != null && serverResponse.get("status") != null) {
                Object statusObj = serverResponse.get("status");
                return String.valueOf(statusObj);
            }
        }
        return "UNKNOWN";
    }

    /**
     * Query recording details (full response with fileList)
     * GET /v1/apps/{appId}/cloud_recording/resourceid/{resourceId}/sid/{sid}/mode/mix/query
     */
    private Map<String, Object> queryRecordingDetails(LiveSession session) {
        String url = String.format("%s/%s/cloud_recording/resourceid/%s/sid/%s/mode/mix/query",
                AGORA_RECORDING_BASE_URL,
                agoraProperties.getAppId(),
                session.getAgoraResourceId(),
                session.getAgoraRecordingSid());

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to query recording details: {}", e.getMessage());
        }

        return null;
    }

    private HttpHeaders createAuthHeaders() {
        String credentials = agoraProperties.getCustomerId() + ":" + agoraProperties.getCustomerSecret();
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedCredentials);
        return headers;
    }

    private String generateRecordingToken(String channelName, int uid) {
        try {
            io.agora.media.RtcTokenBuilder2 tokenBuilder = new io.agora.media.RtcTokenBuilder2();
            int expirationTs = (int) (Instant.now().getEpochSecond() + 3600);

            return tokenBuilder.buildTokenWithUid(
                    agoraProperties.getAppId(),
                    agoraProperties.getAppCertificate(),
                    channelName,
                    uid,
                    io.agora.media.RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                    expirationTs,
                    expirationTs
            );
        } catch (Exception e) {
            log.error("Failed to generate recording token: {}", e.getMessage(), e);
            throw new InvalidRequestException("Failed to generate recording token");
        }
    }

    /**
     * Build recording URL using Agora's naming convention
     * Format: {sid}_{channelName}_0.mp4
     */
    private String buildRecordingUrlFromQueryResponse(LiveSession session, Map<String, Object> queryResponse) {
        try {
            String bucket = agoraProperties.getRecording().getBucket();
            String sessionId = session.getLiveSessionId().toString();

            // Agora saves MP4 files with format: {sid}_{channelName}_0.mp4
            String targetFileName = String.format("%s_%s_0.mp4",
                    session.getAgoraRecordingSid(),
                    session.getChannelName());

            String fullUrl = String.format("https://f005.backblazeb2.com/file/%s/recordings/%s/%s",
                    bucket, sessionId, targetFileName);

            log.info("Built recording URL: {}", fullUrl);
            return fullUrl;

        } catch (Exception e) {
            log.error("Failed to build recording URL: {}", e.getMessage(), e);
            return buildFolderUrl(session);
        }
    }

    /**
     * Build folder URL (fallback)
     */
    private String buildFolderUrl(LiveSession session) {
        String bucket = agoraProperties.getRecording().getBucket();
        String sessionId = session.getLiveSessionId().toString();
        return String.format("https://f005.backblazeb2.com/file/%s/recordings/%s/", bucket, sessionId);
    }

    /**
     * Generate Backblaze B2 Presigned Download URL
     * Creates secure download link valid for 2 hours
     *
     * @param objectKey - File path in bucket (e.g., "recordings/{sessionId}/file.mp4")
     * @return Presigned URL string for direct use in video tag
     */
    private String generateSignedDownloadUrl(String objectKey) {
        try {
            String accessKey = agoraProperties.getRecording().getAccessKey();
            String secretKey = agoraProperties.getRecording().getSecretKey();
            String bucketName = agoraProperties.getRecording().getBucket();
            String endpoint = "https://" + agoraProperties.getRecording().getEndpoint();
            String region = "us-east-1";

            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                    .withPathStyleAccessEnabled(true)
                    .build();

            Date expiration = new Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 60 * 2; // 2 hours
            expiration.setTime(expTimeMillis);

            URL presignedUrl = s3Client.generatePresignedUrl(
                    bucketName,
                    objectKey,
                    expiration,
                    com.amazonaws.HttpMethod.GET
            );

            String urlString = presignedUrl.toString();
            log.info("Generated presigned URL for: {}", objectKey);
            log.info("URL expires at: {}", expiration);

            return urlString;

        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}", e.getMessage(), e);
            throw new InvalidRequestException("Failed to generate download URL: " + e.getMessage());
        }
    }

    private void verifyRecordingStarted(LiveSession session) {
        try {
            Thread.sleep(5000);
            String status = queryRecordingStatus(session);
            log.info("Recording status verification for session {}: {}", session.getLiveSessionId(), status);

            if ("2".equals(status)) {
                throw new InvalidRequestException("Recording start failed - likely storage configuration issue");
            } else if ("6".equals(status)) {
                log.warn("Status 6 immediately after start. Recording may have stopped prematurely. Check Backblaze B2.");
            } else if ("5".equals(status)) {
                log.info("Recording verified successfully - recording in progress");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}



















