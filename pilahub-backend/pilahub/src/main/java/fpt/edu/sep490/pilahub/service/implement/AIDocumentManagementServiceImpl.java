package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.response.*;
import fpt.edu.sep490.pilahub.service.AIDocumentManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIDocumentManagementServiceImpl implements AIDocumentManagementService {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    @Override
    public DocumentUploadResponse uploadScoringGuideline(MultipartFile file) {
        log.info("Uploading scoring guideline: {}", file.getOriginalFilename());

        validateFile(file);

        String url = aiServerUrl + "/api/v1/admin/documents/upload-scoring-guideline";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<DocumentUploadResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    DocumentUploadResponse.class
            );

            DocumentUploadResponse responseBody = response.getBody();
            if (responseBody != null) {
                log.info("Upload scoring guideline successful: {}", responseBody.getFileName());
            }
            return responseBody;

        } catch (IOException e) {
            log.error("Failed to read file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentUploadResponse uploadFile(MultipartFile file, String displayName) {
        log.info("Uploading file: {} with displayName: {}", file.getOriginalFilename(), displayName);

        validateFile(file);

        String url = aiServerUrl + "/api/v1/admin/documents/upload";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            if (displayName != null && !displayName.isBlank()) {
                body.add("displayName", displayName);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<DocumentUploadResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    DocumentUploadResponse.class
            );

            DocumentUploadResponse responseBody = response.getBody();
            if (responseBody != null) {
                log.info("Upload file successful: {}", responseBody.getFileName());
            }
            return responseBody;

        } catch (IOException e) {
            log.error("Failed to read file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public FileListResponse listFiles(Integer pageSize, String pageToken) {
        log.info("Listing files with pageSize: {}, pageToken: {}", pageSize, pageToken);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(aiServerUrl + "/api/v1/admin/documents/list");

        if (pageSize != null) {
            uriBuilder.queryParam("pageSize", pageSize);
        }
        if (pageToken != null && !pageToken.isBlank()) {
            uriBuilder.queryParam("pageToken", pageToken);
        }

        try {
            ResponseEntity<FileListResponse> response = restTemplate.getForEntity(
                    uriBuilder.toUriString(),
                    FileListResponse.class
            );

            FileListResponse responseBody = response.getBody();
            int fileCount = responseBody != null && responseBody.getFiles() != null ?
                    responseBody.getFiles().size() : 0;
            log.info("List files successful, found {} file(s)", fileCount);
            return responseBody;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public GeminiFileInfo getFileInfo(String fileName) {
        log.info("Getting file info for: {}", fileName);

        String url = aiServerUrl + "/api/v1/admin/documents/file/" + fileName;

        try {
            ResponseEntity<GeminiFileInfo> response = restTemplate.getForEntity(
                    url,
                    GeminiFileInfo.class
            );

            log.info("Get file info successful: {}", fileName);
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentManagementResponse deleteFile(String fileName) {
        log.info("Deleting file: {}", fileName);

        // Build URL with proper encoding for fileName
        String url = UriComponentsBuilder
                .fromUriString(aiServerUrl)
                .path("/api/v1/admin/documents/file/{fileName}")
                .buildAndExpand(fileName)
                .toUriString();

        try {
            ResponseEntity<DocumentManagementResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    null,
                    DocumentManagementResponse.class
            );

            log.info("Delete file successful: {}", fileName);
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public GuidelineStatusResponse checkGuidelineStatus() {
        log.info("Checking scoring guideline status");

        String url = aiServerUrl + "/api/v1/admin/documents/scoring-guideline/status";

        try {
            ResponseEntity<GuidelineStatusResponse> response = restTemplate.getForEntity(
                    url,
                    GuidelineStatusResponse.class
            );

            GuidelineStatusResponse responseBody = response.getBody();
            if (responseBody != null) {
                log.info("Check guideline status successful: hasActive={}",
                        responseBody.isHasActiveGuideline());
            }
            return responseBody;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadScoringGuideline() {
        log.info("Downloading scoring guideline from local storage");

        // AI System endpoint: GET /api/v1/admin/documents/download-guideline
        // Endpoint này download quy định chấm điểm từ local storage
        String url = aiServerUrl + "/api/v1/admin/documents/download-guideline";

        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(
                    url,
                    byte[].class
            );

            byte[] fileContent = response.getBody();
            if (fileContent != null) {
                log.info("Download scoring guideline successful: {} bytes", fileContent.length);
            }
            return fileContent;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadFileWithHeaders(String fileName) {
        log.info("Downloading file from local storage: {}", fileName);

        // Build URL with proper encoding for fileName (có thể chứa '/', ký tự đặc biệt)
        // AI System endpoint: GET /api/v1/admin/documents/download/{fileName}
        String url = UriComponentsBuilder
                .fromUriString(aiServerUrl)
                .path("/api/v1/admin/documents/download/{fileName}")
                .buildAndExpand(fileName)
                .toUriString();

        try {
            // Get full response with headers
            ResponseEntity<byte[]> response = restTemplate.getForEntity(
                    url,
                    byte[].class
            );

            byte[] fileContent = response.getBody();
            if (fileContent != null) {
                log.info("Download file successful: {} bytes", fileContent.length);

                // Log headers from AI System for debugging
                HttpHeaders headers = response.getHeaders();
                log.info("AI System headers - Content-Type: {}, Content-Disposition: {}",
                        headers.getContentType(),
                        headers.getContentDisposition());
            }

            // Return full response with AI System's headers
            return response;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    /**
     * Validate file trước khi upload
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Check file size (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File vượt quá giới hạn 10MB");
        }

        // Optional: Check file type
        String contentType = file.getContentType();
        if (contentType != null &&
            !contentType.startsWith("text/") &&
            !contentType.equals("application/pdf")) {
            log.warn("Uploading file with content type: {}", contentType);
        }
    }

    // ======================== ROADMAP REFERENCE DOCUMENT ========================

    @Override
    public DocumentUploadResponse uploadRoadmapReferenceDocument(MultipartFile file) {
        log.info("Uploading roadmap reference document: {}", file.getOriginalFilename());

        validateFile(file);

        String url = aiServerUrl + "/api/v1/admin/documents/upload-roadmap-reference";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<DocumentUploadResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    DocumentUploadResponse.class
            );

            DocumentUploadResponse responseBody = response.getBody();
            if (responseBody != null) {
                log.info("Upload roadmap reference document successful: {}", responseBody.getFileName());
            }
            return responseBody;

        } catch (IOException e) {
            log.error("Failed to read file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public RoadmapReferenceStatusResponse checkRoadmapReferenceStatus() {
        log.info("Checking roadmap reference document status");

        String url = aiServerUrl + "/api/v1/admin/documents/roadmap-reference/status";

        try {
            ResponseEntity<RoadmapReferenceStatusResponse> response = restTemplate.getForEntity(
                    url,
                    RoadmapReferenceStatusResponse.class
            );

            RoadmapReferenceStatusResponse responseBody = response.getBody();
            if (responseBody != null) {
                log.info("Check roadmap reference status successful: hasActive={}",
                        responseBody.isHasActiveDocument());
            }
            return responseBody;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadRoadmapReference() {
        log.info("Downloading roadmap reference document from local storage");

        String url = aiServerUrl + "/api/v1/admin/documents/download-roadmap-reference";

        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(
                    url,
                    byte[].class
            );

            byte[] fileContent = response.getBody();
            if (fileContent != null) {
                log.info("Download roadmap reference document successful: {} bytes", fileContent.length);
            }
            return fileContent;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentManagementResponse deleteRoadmapDocument(String fileName) {
        log.info("Deleting roadmap reference document: {}", fileName);

        String url = UriComponentsBuilder
                .fromUriString(aiServerUrl)
                .path("/api/v1/admin/documents/roadmap-reference/{fileName}")
                .buildAndExpand(fileName)
                .toUriString();

        try {
            ResponseEntity<DocumentManagementResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    null,
                    DocumentManagementResponse.class
            );

            log.info("Delete roadmap reference document successful: {}", fileName);
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    // ======================== WORKOUT FEEDBACK REFERENCE DOCUMENT ========================

    @Override
    public DocumentUploadResponse uploadWorkoutFeedbackReferenceDocument(MultipartFile file) {
        log.info("Uploading workout feedback reference document: {}", file.getOriginalFilename());

        validateFile(file);

        String url = aiServerUrl + "/api/v1/admin/documents/upload-workout-feedback-reference";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<DocumentUploadResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    DocumentUploadResponse.class
            );

            DocumentUploadResponse responseBody = response.getBody();
            if (responseBody != null) {
                log.info("Upload workout feedback reference document successful: {}", responseBody.getFileName());
            }
            return responseBody;

        } catch (IOException e) {
            log.error("Failed to read file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public WorkoutFeedbackReferenceStatusResponse checkWorkoutFeedbackReferenceStatus() {
        log.info("Checking workout feedback reference document status");

        String url = aiServerUrl + "/api/v1/admin/documents/workout-feedback-reference/status";

        try {
            ResponseEntity<WorkoutFeedbackReferenceStatusResponse> response = restTemplate.getForEntity(
                    url,
                    WorkoutFeedbackReferenceStatusResponse.class
            );

            WorkoutFeedbackReferenceStatusResponse responseBody = response.getBody();
            if (responseBody != null) {
                log.info("Check workout feedback reference status successful: hasActive={}",
                        responseBody.isHasActiveDocument());
            }
            return responseBody;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadWorkoutFeedbackReference() {
        log.info("Downloading workout feedback reference document from local storage");

        String url = aiServerUrl + "/api/v1/admin/documents/download-workout-feedback-reference";

        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(
                    url,
                    byte[].class
            );

            byte[] fileContent = response.getBody();
            if (fileContent != null) {
                log.info("Download workout feedback reference document successful: {} bytes", fileContent.length);
            }
            return fileContent;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentManagementResponse deleteWorkoutFeedbackDocument(String fileName) {
        log.info("Deleting workout feedback reference document: {}", fileName);

        String url = UriComponentsBuilder
                .fromUriString(aiServerUrl)
                .path("/api/v1/admin/documents/workout-feedback-reference/{fileName}")
                .buildAndExpand(fileName)
                .toUriString();

        try {
            ResponseEntity<DocumentManagementResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    null,
                    DocumentManagementResponse.class
            );

            log.info("Delete workout feedback reference document successful: {}", fileName);
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    // ======================== ROADMAP REVIEW REFERENCE DOCUMENT ========================

    @Override
    public DocumentUploadResponse uploadRoadmapReviewReferenceDocument(MultipartFile file) {
        log.info("Uploading roadmap review reference document: {}", file.getOriginalFilename());

        validateFile(file);

        String url = aiServerUrl + "/api/v1/admin/documents/upload-roadmap-review-reference";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<DocumentUploadResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    DocumentUploadResponse.class
            );

            DocumentUploadResponse responseBody = response.getBody();
            if (responseBody != null) {
                log.info("Upload roadmap review reference document successful: {}", responseBody.getFileName());
            }
            return responseBody;

        } catch (IOException e) {
            log.error("Failed to read file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public RoadmapReviewReferenceStatusResponse checkRoadmapReviewReferenceStatus() {
        log.info("Checking roadmap review reference document status");

        String url = aiServerUrl + "/api/v1/admin/documents/roadmap-review-reference/status";

        try {
            ResponseEntity<RoadmapReviewReferenceStatusResponse> response = restTemplate.getForEntity(
                    url,
                    RoadmapReviewReferenceStatusResponse.class
            );

            RoadmapReviewReferenceStatusResponse responseBody = response.getBody();
            if (responseBody != null) {
                log.info("Check roadmap review reference status successful: hasActive={}",
                        responseBody.isHasActiveDocument());
            }
            return responseBody;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadRoadmapReviewReference() {
        log.info("Downloading roadmap review reference document from local storage");

        String url = aiServerUrl + "/api/v1/admin/documents/download-roadmap-review-reference";

        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(
                    url,
                    byte[].class
            );

            byte[] fileContent = response.getBody();
            if (fileContent != null) {
                log.info("Download roadmap review reference document successful: {} bytes", fileContent.length);
            }
            return fileContent;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentManagementResponse deleteRoadmapReviewDocument(String fileName) {
        log.info("Deleting roadmap review reference document: {}", fileName);

        String url = UriComponentsBuilder
                .fromUriString(aiServerUrl)
                .path("/api/v1/admin/documents/roadmap-review-reference/{fileName}")
                .buildAndExpand(fileName)
                .toUriString();

        try {
            ResponseEntity<DocumentManagementResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    null,
                    DocumentManagementResponse.class
            );

            log.info("Delete roadmap review reference document successful: {}", fileName);
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI System error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI System error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI System: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI System: " + e.getMessage(), e);
        }
    }
}
