package pilahub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pilahub.dto.response.DocumentManagementResponse;
import pilahub.dto.response.FileListResponse;
import pilahub.dto.response.FileUploadResponse;
import pilahub.service.GeminiFileStoreService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiFileStoreServiceImpl implements GeminiFileStoreService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.file-upload-url:https://generativelanguage.googleapis.com/upload/v1beta/files}")
    private String fileUploadUrl;

    @Value("${gemini.api.file-list-url:https://generativelanguage.googleapis.com/v1beta/files}")
    private String fileListUrl;

    @Value("${file.storage.local-path:uploads/gemini-backups}")
    private String localStoragePath;

    @Value("${file.storage.scoring-guideline-filename:quy-dinh-cham-diem-suc-khoe.md}")
    private String scoringGuidelineFileName;

    // Prefix để phân biệt loại file trên Gemini File Store
    private static final String SCORING_GUIDELINE_PREFIX = "HEALTH_SCORING_GUIDELINE__";
    private static final String ROADMAP_REFERENCE_PREFIX = "ROADMAP_REFERENCE__";
    private static final String WORKOUT_FEEDBACK_PREFIX = "WORKOUT_FEEDBACK_REFERENCE__";
    private static final String ROADMAP_REVIEW_PREFIX = "ROADMAP_REVIEW_REFERENCE__";

    // Thư mục con trong local storage
    private static final String SCORING_GUIDELINE_DIR = "scoring-guidelines";
    private static final String ROADMAP_REFERENCE_DIR = "roadmap-references";
    private static final String WORKOUT_FEEDBACK_DIR = "workout-feedback-references";
    private static final String ROADMAP_REVIEW_DIR = "roadmap-review-references";

    // Cache URI của file quy định chấm điểm hiện tại
    private String cachedScoringGuidelineUri = null;
    private String cachedScoringGuidelineFileName = null;

    // Cache URI của file tài liệu tham khảo roadmap hiện tại
    private String cachedRoadmapDocumentUri = null;
    private String cachedRoadmapDocumentFileName = null;

    // Cache URI của file tài liệu tham khảo workout feedback hiện tại
    private String cachedWorkoutFeedbackDocumentUri = null;
    private String cachedWorkoutFeedbackDocumentFileName = null;

    // Cache URI của file tài liệu tham khảo roadmap review hiện tại
    private String cachedRoadmapReviewDocumentUri = null;
    private String cachedRoadmapReviewDocumentFileName = null;

    /**
     * Khởi tạo thư mục local storage nếu chưa tồn tại
     */
    private void initializeLocalStorage() {
        try {
            Path path = Paths.get(localStoragePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Created local storage directory: {}", localStoragePath);
            }
        } catch (IOException e) {
            log.error("Failed to create local storage directory: {}", e.getMessage(), e);
            throw new RuntimeException("Could not create local storage directory", e);
        }
    }

    /**
     * Lưu file vào local storage
     * @param file MultipartFile to save
     * @param geminiFileName Gemini file ID (e.g., "files/abc123" or just "abc123")
     */
    private void saveFileToLocal(MultipartFile file, String geminiFileName) {
        saveFileToLocal(file, geminiFileName, null);
    }

    /**
     * Lưu file vào local storage với subdirectory cụ thể
     * @param file MultipartFile to save
     * @param geminiFileName Gemini file ID (e.g., "files/abc123" or just "abc123")
     * @param subdirectory Subdirectory trong local storage (null = root)
     */
    private void saveFileToLocal(MultipartFile file, String geminiFileName, String subdirectory) {
        try {
            initializeLocalStorage();

            // Extract just the ID part (remove "files/" prefix if exists)
            String fileId = geminiFileName;
            if (fileId.startsWith("files/")) {
                fileId = fileId.substring(6); // Remove "files/" prefix
            }

            // Add file extension from original filename if needed
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // Final filename: {fileId}{extension}
            String localFileName = fileId + extension;

            // Xác định target path (với subdirectory nếu có)
            Path basePath = Paths.get(localStoragePath);
            Path targetPath;

            if (subdirectory != null && !subdirectory.isEmpty()) {
                Path subPath = basePath.resolve(subdirectory);
                if (!Files.exists(subPath)) {
                    Files.createDirectories(subPath);
                    log.info("Created subdirectory: {}", subPath.toAbsolutePath());
                }
                targetPath = subPath.resolve(localFileName);
            } else {
                targetPath = basePath.resolve(localFileName);
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Saved file to local storage: {} -> {}", geminiFileName, targetPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save file to local storage: {}", e.getMessage(), e);
            // Không throw exception vì upload lên Gemini đã thành công
            // Local storage chỉ là backup
        }
    }

    @Override
    public FileUploadResponse.GeminiFileInfo uploadFile(MultipartFile file, String displayName) {
        try {
            log.info("Uploading file to Gemini File Store: {}", displayName);

            String fileName = displayName != null ? displayName :
                    (file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");

            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // URL format: https://generativelanguage.googleapis.com/upload/v1beta/files?key=API_KEY
            String url = fileUploadUrl + "?key=" + apiKey;

            log.info("Upload URL: {}", url);
            log.info("File size: {} bytes, Display name: {}", file.getSize(), fileName);
            log.info("Content type: {}", contentType);

            // Create multipart/related request (Google yêu cầu định dạng này)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("multipart/related"));
            headers.set("X-Goog-Upload-Protocol", "multipart"); // Header bắt buộc cho upload

            // Part 1: Metadata (JSON đúng cấu trúc bọc bởi "file")
            // Gemini yêu cầu: {"file": {"display_name": "..."}}
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("display_name", fileName);

            Map<String, Object> fileMetadata = new HashMap<>();
            fileMetadata.put("file", fileInfo); // Bọc đối tượng file lại

            HttpHeaders metadataHeaders = new HttpHeaders();
            metadataHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> metadataPart = new HttpEntity<>(fileMetadata, metadataHeaders);

            // Part 2: Media (File bytes)
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return fileName; // Override để Google nhận diện filename
                }
            };

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.valueOf(contentType));
            HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(fileResource, fileHeaders);

            // Build MultiValueMap - Spring sẽ tự tạo boundary
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", metadataPart);
            body.add("file", filePart);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("Sending multipart/related request to Gemini...");

            // Send POST request
            ResponseEntity<FileUploadResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    FileUploadResponse.class
            );

            if (response.getBody() == null || response.getBody().getFile() == null) {
                log.error("Invalid response from Gemini File API");
                throw new RuntimeException("Invalid response from Gemini File API");
            }

            FileUploadResponse.GeminiFileInfo uploadedFile = response.getBody().getFile();
            log.info("Successfully uploaded file: {} (state: {})", uploadedFile.getName(), uploadedFile.getState());

            // Lưu file vào local storage với Gemini file ID (để dễ download sau này)
            saveFileToLocal(file, uploadedFile.getName());

            return uploadedFile;

        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read file", e);
        } catch (HttpClientErrorException e) {
            log.error("Gemini API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to upload file to Gemini: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to Gemini", e);
        }
    }

    @Override
    public DocumentManagementResponse uploadHealthScoringGuideline(MultipartFile file) {
        try {
            log.info("Uploading health scoring guideline: {}", file.getOriginalFilename());

            // Thêm prefix để phân biệt loại file
            String displayName = SCORING_GUIDELINE_PREFIX + "Quy định chấm điểm hồ sơ sức khỏe";
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Upload lên Gemini với display name có prefix
            String url = fileUploadUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("multipart/related"));
            headers.set("X-Goog-Upload-Protocol", "multipart");

            Map<String, Object> fileInfoMap = new HashMap<>();
            fileInfoMap.put("display_name", displayName);

            Map<String, Object> fileMetadata = new HashMap<>();
            fileMetadata.put("file", fileInfoMap);

            HttpHeaders metadataHeaders = new HttpHeaders();
            metadataHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> metadataPart = new HttpEntity<>(fileMetadata, metadataHeaders);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return displayName;
                }
            };

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.valueOf(contentType));
            HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(fileResource, fileHeaders);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", metadataPart);
            body.add("file", filePart);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<FileUploadResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    FileUploadResponse.class
            );

            if (response.getBody() == null || response.getBody().getFile() == null) {
                throw new RuntimeException("Invalid response from Gemini File API");
            }

            FileUploadResponse.GeminiFileInfo fileInfo = response.getBody().getFile();

            // Lưu vào local storage với subdirectory riêng cho scoring guidelines
            saveFileToLocal(file, fileInfo.getName(), SCORING_GUIDELINE_DIR);

            // Cache URI và file name
            cachedScoringGuidelineUri = fileInfo.getUri();
            cachedScoringGuidelineFileName = fileInfo.getName();

            log.info("Successfully uploaded scoring guideline with prefix: {} -> {}",
                    fileInfo.getName(), fileInfo.getUri());

            return DocumentManagementResponse.builder()
                    .success(true)
                    .message("Upload quy định chấm điểm thành công")
                    .fileName(fileInfo.getName())
                    .fileUri(fileInfo.getUri())
                    .state(fileInfo.getState())
                    .displayName(fileInfo.getDisplayName())
                    .sizeBytes(fileInfo.getSizeBytes() != null ? Long.parseLong(fileInfo.getSizeBytes()) : null)
                    .expirationTime(fileInfo.getExpirationTime())
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload health scoring guideline: {}", e.getMessage(), e);
            return DocumentManagementResponse.builder()
                    .success(false)
                    .message("Upload thất bại: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public FileListResponse listFiles(Integer pageSize, String pageToken) {
        try {
            StringBuilder url = new StringBuilder(fileListUrl + "?key=" + apiKey);

            if (pageSize != null) {
                url.append("&pageSize=").append(pageSize);
            }

            if (pageToken != null && !pageToken.isEmpty()) {
                url.append("&pageToken=").append(pageToken);
            }

            log.info("Listing files from Gemini File Store");
            ResponseEntity<FileListResponse> response = restTemplate.getForEntity(
                    url.toString(),
                    FileListResponse.class
            );

            FileListResponse body = response.getBody();
            log.info("Retrieved {} files", body != null && body.getFiles() != null ?
                    body.getFiles().size() : 0);

            return body;

        } catch (HttpClientErrorException e) {
            log.error("Error listing files: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to list files: " + e.getMessage(), e);
        }
    }

    @Override
    public FileUploadResponse.GeminiFileInfo getFile(String fileName) {
        try {
            String url = fileListUrl + "/" + fileName + "?key=" + apiKey;

            log.info("Getting file info: {}", fileName);
            ResponseEntity<FileUploadResponse.GeminiFileInfo> response = restTemplate.getForEntity(
                    url,
                    FileUploadResponse.GeminiFileInfo.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Error getting file: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get file info: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentManagementResponse deleteFile(String fileName) {
        try {
            String url = fileListUrl + "/" + fileName + "?key=" + apiKey;

            log.info("Deleting file: {}", fileName);
            restTemplate.delete(url);

            // Clear cache nếu xóa file quy định chấm điểm
            if (fileName.equals(cachedScoringGuidelineFileName)) {
                cachedScoringGuidelineUri = null;
                cachedScoringGuidelineFileName = null;
            }

            // Clear cache nếu xóa file tài liệu roadmap
            if (fileName.equals(cachedRoadmapDocumentFileName)) {
                cachedRoadmapDocumentUri = null;
                cachedRoadmapDocumentFileName = null;
            }

            // Clear cache nếu xóa file tài liệu workout feedback
            if (fileName.equals(cachedWorkoutFeedbackDocumentFileName)) {
                cachedWorkoutFeedbackDocumentUri = null;
                cachedWorkoutFeedbackDocumentFileName = null;
            }

            // Clear cache nếu xóa file tài liệu roadmap review
            if (fileName.equals(cachedRoadmapReviewDocumentFileName)) {
                cachedRoadmapReviewDocumentUri = null;
                cachedRoadmapReviewDocumentFileName = null;
            }

            return DocumentManagementResponse.builder()
                    .success(true)
                    .message("Xóa file thành công")
                    .fileName(fileName)
                    .build();

        } catch (HttpClientErrorException e) {
            log.error("Error deleting file: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return DocumentManagementResponse.builder()
                    .success(false)
                    .message("Xóa file thất bại: " + e.getMessage())
                    .fileName(fileName)
                    .build();
        }
    }

    @Override
    public String getActiveScoringGuidelineUri() {
        // Nếu đã có cache và vẫn còn hiệu lực
        if (cachedScoringGuidelineUri != null && cachedScoringGuidelineFileName != null) {
            try {
                // Kiểm tra trạng thái file
                String state = checkFileState(cachedScoringGuidelineFileName);
                if ("ACTIVE".equals(state)) {
                    log.info("Using cached scoring guideline URI: {}", cachedScoringGuidelineUri);
                    return cachedScoringGuidelineUri;
                } else {
                    log.warn("Cached scoring guideline is not ACTIVE (state: {}), clearing cache", state);
                    cachedScoringGuidelineUri = null;
                    cachedScoringGuidelineFileName = null;
                }
            } catch (Exception e) {
                log.warn("Error checking cached file state, clearing cache: {}", e.getMessage());
                cachedScoringGuidelineUri = null;
                cachedScoringGuidelineFileName = null;
            }
        }

        // Tìm file quy định chấm điểm trong File Store (CHỈ tìm files có prefix)
        try {
            FileListResponse fileList = listFiles(100, null);

            if (fileList.getFiles() != null) {
                for (FileUploadResponse.GeminiFileInfo file : fileList.getFiles()) {
                    // CHỈ lấy files có prefix HEALTH_SCORING_GUIDELINE và đang ACTIVE
                    if (file.getDisplayName() != null &&
                        file.getDisplayName().startsWith(SCORING_GUIDELINE_PREFIX) &&
                        "ACTIVE".equals(file.getState())) {

                        cachedScoringGuidelineUri = file.getUri();
                        cachedScoringGuidelineFileName = file.getName();
                        log.info("Found active scoring guideline: {} (displayName: {})",
                                file.getName(), file.getDisplayName());
                        return cachedScoringGuidelineUri;
                    }
                }
            }

            log.warn("No active scoring guideline found in File Store");
            return null;

        } catch (Exception e) {
            log.error("Error getting active scoring guideline: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String checkFileState(String fileName) {
        try {
            FileUploadResponse.GeminiFileInfo fileInfo = getFile(fileName);
            return fileInfo.getState();
        } catch (Exception e) {
            log.error("Error checking file state: {}", e.getMessage());
            throw new RuntimeException("Failed to check file state", e);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        return loadFileAsResource(fileName, null);
    }

    /**
     * Load file from local storage with optional subdirectory
     * @param fileName File name or file ID
     * @param subdirectory Subdirectory to search in (null = all directories)
     */
    private Resource loadFileAsResource(String fileName, String subdirectory) {
        try {
            initializeLocalStorage();

            // Remove "files/" prefix if exists (for Gemini file IDs)
            final String fileId = fileName.startsWith("files/")
                ? fileName.substring(6)
                : fileName;

            Path storageDir = Paths.get(localStoragePath);

            // Nếu có subdirectory, search trong đó trước
            if (subdirectory != null && !subdirectory.isEmpty()) {
                Path subDir = storageDir.resolve(subdirectory);
                if (Files.exists(subDir)) {
                    Resource resource = searchFileInDirectory(subDir, fileId, fileName);
                    if (resource != null) {
                        return resource;
                    }
                }
            }

            // Nếu không tìm thấy trong subdirectory hoặc không có subdirectory, search toàn bộ
            // Try exact match first in root
            Path exactMatch = storageDir.resolve(fileName).normalize();
            if (Files.exists(exactMatch) && Files.isReadable(exactMatch)) {
                Resource resource = new UrlResource(exactMatch.toUri());
                log.info("Loaded file from local storage (exact match): {}", fileName);
                return resource;
            }

            // Try with fileId (in case fileName includes extension)
            Path fileIdMatch = storageDir.resolve(fileId).normalize();
            if (Files.exists(fileIdMatch) && Files.isReadable(fileIdMatch)) {
                Resource resource = new UrlResource(fileIdMatch.toUri());
                log.info("Loaded file from local storage (fileId match): {}", fileId);
                return resource;
            }

            // Search for files starting with fileId in root
            Resource resource = searchFileInDirectory(storageDir, fileId, fileName);
            if (resource != null) {
                return resource;
            }

            // Search in all subdirectories
            try (var stream = Files.list(storageDir)) {
                var subdirs = stream.filter(Files::isDirectory).toList();
                for (Path subdir : subdirs) {
                    resource = searchFileInDirectory(subdir, fileId, fileName);
                    if (resource != null) {
                        return resource;
                    }
                }
            }

            log.error("File not found or not readable: {}", fileName);
            throw new RuntimeException("File not found: " + fileName);

        } catch (MalformedURLException e) {
            log.error("Error loading file: {}", e.getMessage(), e);
            throw new RuntimeException("Error loading file: " + fileName, e);
        } catch (IOException e) {
            log.error("Error searching for file: {}", e.getMessage(), e);
            throw new RuntimeException("Error searching for file: " + fileName, e);
        }
    }

    /**
     * Search for file in specific directory
     */
    private Resource searchFileInDirectory(Path directory, String fileId, String fileName) throws IOException {
        try (var stream = Files.list(directory)) {
            var matchingFiles = stream
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        // Match: fileId.ext or fileId (exact)
                        return name.equals(fileId) || name.startsWith(fileId + ".");
                    })
                    .toList();

            if (!matchingFiles.isEmpty()) {
                Path matched = matchingFiles.get(0);
                Resource resource = new UrlResource(matched.toUri());
                log.info("Loaded file from local storage (pattern match): {} -> {}",
                         fileName, matched.toString());
                return resource;
            }
        }
        return null;
    }

    @Override
    public Resource loadScoringGuidelineAsResource() {
        // Try to use cached file name first
        if (cachedScoringGuidelineFileName != null) {
            log.info("Loading scoring guideline from local storage using cached ID: {}",
                     cachedScoringGuidelineFileName);
            try {
                return loadFileAsResource(cachedScoringGuidelineFileName, SCORING_GUIDELINE_DIR);
            } catch (Exception e) {
                log.warn("Failed to load using cached name, trying to find by displayName: {}",
                         e.getMessage());
            }
        }

        // Fallback: Try to find by searching for scoring guideline in list
        try {
            FileListResponse files = listFiles(100, null);
            if (files.getFiles() != null) {
                for (FileUploadResponse.GeminiFileInfo file : files.getFiles()) {
                    // Tìm file có prefix HEALTH_SCORING_GUIDELINE
                    if (file.getDisplayName() != null &&
                        file.getDisplayName().startsWith(SCORING_GUIDELINE_PREFIX)) {
                        log.info("Found scoring guideline by prefix, loading: {}", file.getName());
                        return loadFileAsResource(file.getName(), SCORING_GUIDELINE_DIR);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to find scoring guideline: {}", e.getMessage());
        }

        // Last resort: try with config filename
        log.warn("Using fallback filename from config: {}", scoringGuidelineFileName);
        return loadFileAsResource(scoringGuidelineFileName, SCORING_GUIDELINE_DIR);
    }

    @Override
    public DocumentManagementResponse uploadRoadmapReferenceDocument(MultipartFile file) {
        try {
            log.info("Uploading roadmap reference document: {}", file.getOriginalFilename());

            // Thêm prefix để phân biệt loại file + timestamp để tránh trùng
            String displayName = ROADMAP_REFERENCE_PREFIX + "Roadmap Reference - " + Instant.now().toString();
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Validate file type (accept PDF, markdown, txt)
            if (!contentType.contains("pdf") && 
                !contentType.contains("markdown") && 
                !contentType.contains("text")) {
                log.warn("Unsupported file type for roadmap document: {}", contentType);
                return DocumentManagementResponse.builder()
                        .success(false)
                        .message("Chỉ hỗ trợ file PDF, Markdown hoặc Text")
                        .build();
            }

            // Upload to Gemini với display name có prefix
            String url = fileUploadUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("multipart/related"));
            headers.set("X-Goog-Upload-Protocol", "multipart");

            Map<String, Object> fileInfoMap = new HashMap<>();
            fileInfoMap.put("display_name", displayName);

            Map<String, Object> fileMetadata = new HashMap<>();
            fileMetadata.put("file", fileInfoMap);

            HttpHeaders metadataHeaders = new HttpHeaders();
            metadataHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> metadataPart = new HttpEntity<>(fileMetadata, metadataHeaders);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return displayName;
                }
            };

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.valueOf(contentType));
            HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(fileResource, fileHeaders);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", metadataPart);
            body.add("file", filePart);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<FileUploadResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    FileUploadResponse.class
            );

            if (response.getBody() == null || response.getBody().getFile() == null) {
                throw new RuntimeException("Invalid response from Gemini File API");
            }

            FileUploadResponse.GeminiFileInfo fileInfo = response.getBody().getFile();

            // Lưu vào local storage với subdirectory riêng cho roadmap references
            saveFileToLocal(file, fileInfo.getName(), ROADMAP_REFERENCE_DIR);

            // Cache URI và file name
            cachedRoadmapDocumentUri = fileInfo.getUri();
            cachedRoadmapDocumentFileName = fileInfo.getName();

            log.info("Successfully uploaded roadmap reference with prefix: {} -> {}",
                    fileInfo.getName(), fileInfo.getUri());

            return DocumentManagementResponse.builder()
                    .success(true)
                    .message("Upload tài liệu tham khảo roadmap thành công")
                    .fileName(fileInfo.getName())
                    .fileUri(fileInfo.getUri())
                    .state(fileInfo.getState())
                    .displayName(fileInfo.getDisplayName())
                    .sizeBytes(fileInfo.getSizeBytes() != null ? Long.parseLong(fileInfo.getSizeBytes()) : null)
                    .expirationTime(fileInfo.getExpirationTime())
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload roadmap reference document: {}", e.getMessage(), e);
            return DocumentManagementResponse.builder()
                    .success(false)
                    .message("Upload thất bại: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public String getActiveRoadmapDocumentUri() {
        // Nếu đã có cache và vẫn còn hiệu lực
        if (cachedRoadmapDocumentUri != null && cachedRoadmapDocumentFileName != null) {
            try {
                // Kiểm tra trạng thái file
                String state = checkFileState(cachedRoadmapDocumentFileName);
                if ("ACTIVE".equals(state)) {
                    log.info("Using cached roadmap document URI: {}", cachedRoadmapDocumentUri);
                    return cachedRoadmapDocumentUri;
                } else {
                    log.warn("Cached roadmap document is not ACTIVE (state: {}), clearing cache", state);
                    cachedRoadmapDocumentUri = null;
                    cachedRoadmapDocumentFileName = null;
                }
            } catch (Exception e) {
                log.warn("Error checking cached roadmap document state, clearing cache: {}", e.getMessage());
                cachedRoadmapDocumentUri = null;
                cachedRoadmapDocumentFileName = null;
            }
        }

        // Tìm file tài liệu roadmap trong File Store (CHỈ tìm files có prefix, lấy file gần nhất)
        try {
            FileListResponse fileList = listFiles(100, null);

            if (fileList.getFiles() != null) {
                FileUploadResponse.GeminiFileInfo latestRoadmapDoc = null;
                
                for (FileUploadResponse.GeminiFileInfo file : fileList.getFiles()) {
                    // CHỈ lấy files có prefix ROADMAP_REFERENCE và đang ACTIVE
                    if (file.getDisplayName() != null &&
                        file.getDisplayName().startsWith(ROADMAP_REFERENCE_PREFIX) &&
                        "ACTIVE".equals(file.getState())) {
                        
                        if (latestRoadmapDoc == null || 
                            (file.getCreateTime() != null && latestRoadmapDoc.getCreateTime() != null &&
                             file.getCreateTime().compareTo(latestRoadmapDoc.getCreateTime()) > 0)) {
                            latestRoadmapDoc = file;
                        }
                    }
                }
                
                if (latestRoadmapDoc != null) {
                    cachedRoadmapDocumentUri = latestRoadmapDoc.getUri();
                    cachedRoadmapDocumentFileName = latestRoadmapDoc.getName();
                    log.info("Found active roadmap document: {} (created: {}, displayName: {})",
                            latestRoadmapDoc.getName(), latestRoadmapDoc.getCreateTime(),
                            latestRoadmapDoc.getDisplayName());
                    return cachedRoadmapDocumentUri;
                }
            }

            log.info("No active roadmap document found in File Store");
            return null;

        } catch (Exception e) {
            log.error("Error getting active roadmap document: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Resource loadRoadmapDocumentAsResource() {
        // Try to use cached file name first
        if (cachedRoadmapDocumentFileName != null) {
            log.info("Loading roadmap document from local storage using cached ID: {}",
                     cachedRoadmapDocumentFileName);
            try {
                return loadFileAsResource(cachedRoadmapDocumentFileName, ROADMAP_REFERENCE_DIR);
            } catch (Exception e) {
                log.warn("Failed to load using cached name, trying to find by displayName: {}",
                         e.getMessage());
            }
        }

        // Fallback: Try to find by searching for roadmap document in list
        try {
            FileListResponse files = listFiles(100, null);
            if (files.getFiles() != null) {
                FileUploadResponse.GeminiFileInfo latestRoadmapDoc = null;
                
                for (FileUploadResponse.GeminiFileInfo file : files.getFiles()) {
                    // Tìm file có prefix ROADMAP_REFERENCE
                    if (file.getDisplayName() != null &&
                        file.getDisplayName().startsWith(ROADMAP_REFERENCE_PREFIX)) {

                        if (latestRoadmapDoc == null || 
                            (file.getCreateTime() != null && latestRoadmapDoc.getCreateTime() != null &&
                             file.getCreateTime().compareTo(latestRoadmapDoc.getCreateTime()) > 0)) {
                            latestRoadmapDoc = file;
                        }
                    }
                }
                
                if (latestRoadmapDoc != null) {
                    log.info("Found roadmap document by prefix, loading: {}", latestRoadmapDoc.getName());
                    return loadFileAsResource(latestRoadmapDoc.getName(), ROADMAP_REFERENCE_DIR);
                }
            }
        } catch (Exception e) {
            log.error("Failed to find roadmap document: {}", e.getMessage());
        }

        throw new RuntimeException("No roadmap reference document found");
    }

    @Override
    public DocumentManagementResponse deleteRoadmapDocument(String fileName) {
        try {
            log.info("Deleting roadmap reference document: {}", fileName);
            
            DocumentManagementResponse response = deleteFile(fileName);
            
            if (response.isSuccess()) {
                log.info("Successfully deleted roadmap reference document: {}", fileName);
            }
            
            return response;

        } catch (Exception e) {
            log.error("Failed to delete roadmap reference document: {}", e.getMessage(), e);
            return DocumentManagementResponse.builder()
                    .success(false)
                    .message("Xóa tài liệu roadmap thất bại: " + e.getMessage())
                    .fileName(fileName)
                    .build();
        }
    }

    @Override
    public DocumentManagementResponse uploadWorkoutFeedbackReferenceDocument(MultipartFile file) {
        try {
            log.info("Uploading workout feedback reference document: {}", file.getOriginalFilename());

            // Thêm prefix để phân biệt loại file + timestamp để tránh trùng
            String displayName = WORKOUT_FEEDBACK_PREFIX + "Workout Feedback Reference - " + Instant.now().toString();
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Validate file type (accept PDF, markdown, txt)
            if (!contentType.contains("pdf") &&
                !contentType.contains("markdown") &&
                !contentType.contains("text")) {
                log.warn("Unsupported file type for workout feedback document: {}", contentType);
                return DocumentManagementResponse.builder()
                        .success(false)
                        .message("Chỉ hỗ trợ file PDF, Markdown hoặc Text")
                        .build();
            }

            // Upload to Gemini với display name có prefix
            String url = fileUploadUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("multipart/related"));
            headers.set("X-Goog-Upload-Protocol", "multipart");

            Map<String, Object> fileInfoMap = new HashMap<>();
            fileInfoMap.put("display_name", displayName);

            Map<String, Object> fileMetadata = new HashMap<>();
            fileMetadata.put("file", fileInfoMap);

            HttpHeaders metadataHeaders = new HttpHeaders();
            metadataHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> metadataPart = new HttpEntity<>(fileMetadata, metadataHeaders);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return displayName;
                }
            };

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.valueOf(contentType));
            HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(fileResource, fileHeaders);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", metadataPart);
            body.add("file", filePart);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<FileUploadResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    FileUploadResponse.class
            );

            if (response.getBody() == null || response.getBody().getFile() == null) {
                throw new RuntimeException("Invalid response from Gemini File API");
            }

            FileUploadResponse.GeminiFileInfo fileInfo = response.getBody().getFile();

            // Lưu vào local storage với subdirectory riêng cho workout feedback references
            saveFileToLocal(file, fileInfo.getName(), WORKOUT_FEEDBACK_DIR);

            // Cache URI và file name
            cachedWorkoutFeedbackDocumentUri = fileInfo.getUri();
            cachedWorkoutFeedbackDocumentFileName = fileInfo.getName();

            log.info("Successfully uploaded workout feedback reference with prefix: {} -> {}",
                    fileInfo.getName(), fileInfo.getUri());

            return DocumentManagementResponse.builder()
                    .success(true)
                    .message("Upload tài liệu tham khảo workout feedback thành công")
                    .fileName(fileInfo.getName())
                    .fileUri(fileInfo.getUri())
                    .state(fileInfo.getState())
                    .displayName(fileInfo.getDisplayName())
                    .sizeBytes(fileInfo.getSizeBytes() != null ? Long.parseLong(fileInfo.getSizeBytes()) : null)
                    .expirationTime(fileInfo.getExpirationTime())
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload workout feedback reference document: {}", e.getMessage(), e);
            return DocumentManagementResponse.builder()
                    .success(false)
                    .message("Upload thất bại: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public String getActiveWorkoutFeedbackDocumentUri() {
        // Nếu đã có cache và vẫn còn hiệu lực
        if (cachedWorkoutFeedbackDocumentUri != null && cachedWorkoutFeedbackDocumentFileName != null) {
            try {
                // Kiểm tra trạng thái file
                String state = checkFileState(cachedWorkoutFeedbackDocumentFileName);
                if ("ACTIVE".equals(state)) {
                    log.info("Using cached workout feedback document URI: {}", cachedWorkoutFeedbackDocumentUri);
                    return cachedWorkoutFeedbackDocumentUri;
                } else {
                    log.warn("Cached workout feedback document is not ACTIVE (state: {}), clearing cache", state);
                    cachedWorkoutFeedbackDocumentUri = null;
                    cachedWorkoutFeedbackDocumentFileName = null;
                }
            } catch (Exception e) {
                log.warn("Error checking cached workout feedback document state, clearing cache: {}", e.getMessage());
                cachedWorkoutFeedbackDocumentUri = null;
                cachedWorkoutFeedbackDocumentFileName = null;
            }
        }

        // Tìm file tài liệu workout feedback trong File Store (CHỈ tìm files có prefix, lấy file gần nhất)
        try {
            FileListResponse fileList = listFiles(100, null);

            if (fileList.getFiles() != null) {
                FileUploadResponse.GeminiFileInfo latestWorkoutDoc = null;

                for (FileUploadResponse.GeminiFileInfo file : fileList.getFiles()) {
                    // CHỈ lấy files có prefix WORKOUT_FEEDBACK_REFERENCE và đang ACTIVE
                    if (file.getDisplayName() != null &&
                        file.getDisplayName().startsWith(WORKOUT_FEEDBACK_PREFIX) &&
                        "ACTIVE".equals(file.getState())) {

                        if (latestWorkoutDoc == null ||
                            (file.getCreateTime() != null && latestWorkoutDoc.getCreateTime() != null &&
                             file.getCreateTime().compareTo(latestWorkoutDoc.getCreateTime()) > 0)) {
                            latestWorkoutDoc = file;
                        }
                    }
                }

                if (latestWorkoutDoc != null) {
                    cachedWorkoutFeedbackDocumentUri = latestWorkoutDoc.getUri();
                    cachedWorkoutFeedbackDocumentFileName = latestWorkoutDoc.getName();
                    log.info("Found active workout feedback document: {} (created: {}, displayName: {})",
                            latestWorkoutDoc.getName(), latestWorkoutDoc.getCreateTime(),
                            latestWorkoutDoc.getDisplayName());
                    return cachedWorkoutFeedbackDocumentUri;
                }
            }

            log.info("No active workout feedback document found in File Store");
            return null;

        } catch (Exception e) {
            log.error("Error getting active workout feedback document: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Resource loadWorkoutFeedbackDocumentAsResource() {
        // Try to use cached file name first
        if (cachedWorkoutFeedbackDocumentFileName != null) {
            log.info("Loading workout feedback document from local storage using cached ID: {}",
                     cachedWorkoutFeedbackDocumentFileName);
            try {
                return loadFileAsResource(cachedWorkoutFeedbackDocumentFileName, WORKOUT_FEEDBACK_DIR);
            } catch (Exception e) {
                log.warn("Failed to load using cached name, trying to find by displayName: {}",
                         e.getMessage());
            }
        }

        // Fallback: Try to find by searching for workout feedback document in list
        try {
            FileListResponse files = listFiles(100, null);
            if (files.getFiles() != null) {
                FileUploadResponse.GeminiFileInfo latestWorkoutDoc = null;

                for (FileUploadResponse.GeminiFileInfo file : files.getFiles()) {
                    // Tìm file có prefix WORKOUT_FEEDBACK_REFERENCE
                    if (file.getDisplayName() != null &&
                        file.getDisplayName().startsWith(WORKOUT_FEEDBACK_PREFIX)) {

                        if (latestWorkoutDoc == null ||
                            (file.getCreateTime() != null && latestWorkoutDoc.getCreateTime() != null &&
                             file.getCreateTime().compareTo(latestWorkoutDoc.getCreateTime()) > 0)) {
                            latestWorkoutDoc = file;
                        }
                    }
                }

                if (latestWorkoutDoc != null) {
                    log.info("Found workout feedback document by prefix, loading: {}", latestWorkoutDoc.getName());
                    return loadFileAsResource(latestWorkoutDoc.getName(), WORKOUT_FEEDBACK_DIR);
                }
            }
        } catch (Exception e) {
            log.error("Failed to find workout feedback document: {}", e.getMessage());
        }

        throw new RuntimeException("No workout feedback reference document found");
    }

    @Override
    public DocumentManagementResponse deleteWorkoutFeedbackDocument(String fileName) {
        try {
            log.info("Deleting workout feedback reference document: {}", fileName);

            DocumentManagementResponse response = deleteFile(fileName);

            if (response.isSuccess()) {
                log.info("Successfully deleted workout feedback reference document: {}", fileName);
            }

            return response;

        } catch (Exception e) {
            log.error("Failed to delete workout feedback reference document: {}", e.getMessage(), e);
            return DocumentManagementResponse.builder()
                    .success(false)
                    .message("Xóa tài liệu workout feedback thất bại: " + e.getMessage())
                    .fileName(fileName)
                    .build();
        }
    }

    @Override
    public DocumentManagementResponse uploadRoadmapReviewReferenceDocument(MultipartFile file) {
        try {
            log.info("Uploading roadmap review reference document: {}", file.getOriginalFilename());

            // Thêm prefix để phân biệt loại file + timestamp để tránh trùng
            String displayName = ROADMAP_REVIEW_PREFIX + "Roadmap Review - " + Instant.now().toString();
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Validate file type (accept PDF, markdown, txt)
            if (!contentType.contains("pdf") && 
                !contentType.contains("markdown") && 
                !contentType.contains("text")) {
                log.warn("Unsupported file type for roadmap review document: {}", contentType);
                return DocumentManagementResponse.builder()
                        .success(false)
                        .message("Chỉ hỗ trợ file PDF, Markdown hoặc Text")
                        .build();
            }

            // Upload to Gemini với display name có prefix
            String url = fileUploadUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("multipart/related"));
            headers.set("X-Goog-Upload-Protocol", "multipart");

            Map<String, Object> fileInfoMap = new HashMap<>();
            fileInfoMap.put("display_name", displayName);

            Map<String, Object> fileMetadata = new HashMap<>();
            fileMetadata.put("file", fileInfoMap);

            HttpHeaders metadataHeaders = new HttpHeaders();
            metadataHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> metadataPart = new HttpEntity<>(fileMetadata, metadataHeaders);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return displayName;
                }
            };

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.valueOf(contentType));
            HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(fileResource, fileHeaders);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", metadataPart);
            body.add("file", filePart);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<FileUploadResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    FileUploadResponse.class
            );

            if (response.getBody() == null || response.getBody().getFile() == null) {
                throw new RuntimeException("Invalid response from Gemini File API");
            }

            FileUploadResponse.GeminiFileInfo fileInfo = response.getBody().getFile();

            // Lưu vào local storage với subdirectory riêng cho roadmap review references
            saveFileToLocal(file, fileInfo.getName(), ROADMAP_REVIEW_DIR);

            // Cache URI và file name
            cachedRoadmapReviewDocumentUri = fileInfo.getUri();
            cachedRoadmapReviewDocumentFileName = fileInfo.getName();

            log.info("Successfully uploaded roadmap review reference with prefix: {} -> {}",
                    fileInfo.getName(), fileInfo.getUri());

            return DocumentManagementResponse.builder()
                    .success(true)
                    .message("Upload tài liệu tham khảo roadmap review thành công")
                    .fileName(fileInfo.getName())
                    .fileUri(fileInfo.getUri())
                    .state(fileInfo.getState())
                    .displayName(fileInfo.getDisplayName())
                    .sizeBytes(fileInfo.getSizeBytes() != null ? Long.parseLong(fileInfo.getSizeBytes()) : null)
                    .expirationTime(fileInfo.getExpirationTime())
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload roadmap review document: {}", e.getMessage(), e);
            return DocumentManagementResponse.builder()
                    .success(false)
                    .message("Upload thất bại: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public String getActiveRoadmapReviewDocumentUri() {
        // Nếu đã có cache và vẫn còn hiệu lực
        if (cachedRoadmapReviewDocumentUri != null && cachedRoadmapReviewDocumentFileName != null) {
            try {
                // Kiểm tra trạng thái file
                String state = checkFileState(cachedRoadmapReviewDocumentFileName);
                if ("ACTIVE".equals(state)) {
                    log.info("Using cached roadmap review document URI: {}", cachedRoadmapReviewDocumentUri);
                    return cachedRoadmapReviewDocumentUri;
                } else {
                    log.warn("Cached roadmap review document is not ACTIVE (state: {}), clearing cache", state);
                    cachedRoadmapReviewDocumentUri = null;
                    cachedRoadmapReviewDocumentFileName = null;
                }
            } catch (Exception e) {
                log.warn("Error checking cached roadmap review document state, clearing cache: {}", e.getMessage());
                cachedRoadmapReviewDocumentUri = null;
                cachedRoadmapReviewDocumentFileName = null;
            }
        }

        // Tìm file tài liệu roadmap review trong File Store (CHỈ tìm files có prefix, lấy file gần nhất)
        try {
            FileListResponse fileList = listFiles(100, null);

            if (fileList.getFiles() != null) {
                FileUploadResponse.GeminiFileInfo latestRoadmapReviewDoc = null;
                
                for (FileUploadResponse.GeminiFileInfo file : fileList.getFiles()) {
                    // CHỈ lấy files có prefix ROADMAP_REVIEW_REFERENCE và đang ACTIVE
                    if (file.getDisplayName() != null &&
                        file.getDisplayName().startsWith(ROADMAP_REVIEW_PREFIX) &&
                        "ACTIVE".equals(file.getState())) {
                        
                        if (latestRoadmapReviewDoc == null || 
                            (file.getCreateTime() != null && latestRoadmapReviewDoc.getCreateTime() != null &&
                             file.getCreateTime().compareTo(latestRoadmapReviewDoc.getCreateTime()) > 0)) {
                            latestRoadmapReviewDoc = file;
                        }
                    }
                }
                
                if (latestRoadmapReviewDoc != null) {
                    cachedRoadmapReviewDocumentUri = latestRoadmapReviewDoc.getUri();
                    cachedRoadmapReviewDocumentFileName = latestRoadmapReviewDoc.getName();
                    log.info("Found active roadmap review document: {} (created: {}, displayName: {})",
                            latestRoadmapReviewDoc.getName(), latestRoadmapReviewDoc.getCreateTime(),
                            latestRoadmapReviewDoc.getDisplayName());
                    return cachedRoadmapReviewDocumentUri;
                }
            }

            log.info("No active roadmap review document found in File Store");
            return null;

        } catch (Exception e) {
            log.error("Error getting active roadmap review document: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Resource loadRoadmapReviewDocumentAsResource() {
        // Try to use cached file name first
        if (cachedRoadmapReviewDocumentFileName != null) {
            log.info("Loading roadmap review document from local storage using cached ID: {}",
                     cachedRoadmapReviewDocumentFileName);
            try {
                return loadFileAsResource(cachedRoadmapReviewDocumentFileName, ROADMAP_REVIEW_DIR);
            } catch (Exception e) {
                log.warn("Failed to load using cached name, trying to find by displayName: {}",
                         e.getMessage());
            }
        }

        // Fallback: Try to find by searching for roadmap review document in list
        try {
            FileListResponse files = listFiles(100, null);
            if (files.getFiles() != null) {
                FileUploadResponse.GeminiFileInfo latestRoadmapReviewDoc = null;
                
                for (FileUploadResponse.GeminiFileInfo file : files.getFiles()) {
                    // Tìm file có prefix ROADMAP_REVIEW_REFERENCE
                    if (file.getDisplayName() != null &&
                        file.getDisplayName().startsWith(ROADMAP_REVIEW_PREFIX)) {

                        if (latestRoadmapReviewDoc == null || 
                            (file.getCreateTime() != null && latestRoadmapReviewDoc.getCreateTime() != null &&
                             file.getCreateTime().compareTo(latestRoadmapReviewDoc.getCreateTime()) > 0)) {
                            latestRoadmapReviewDoc = file;
                        }
                    }
                }
                
                if (latestRoadmapReviewDoc != null) {
                    log.info("Found roadmap review document by prefix, loading: {}", latestRoadmapReviewDoc.getName());
                    return loadFileAsResource(latestRoadmapReviewDoc.getName(), ROADMAP_REVIEW_DIR);
                }
            }
        } catch (Exception e) {
            log.error("Failed to find roadmap review document: {}", e.getMessage());
        }

        throw new RuntimeException("No roadmap review reference document found");
    }

    @Override
    public DocumentManagementResponse deleteRoadmapReviewDocument(String fileName) {
        try {
            log.info("Deleting roadmap review reference document: {}", fileName);
            
            DocumentManagementResponse response = deleteFile(fileName);
            
            if (response.isSuccess()) {
                log.info("Successfully deleted roadmap review reference document: {}", fileName);
            }
            
            return response;

        } catch (Exception e) {
            log.error("Failed to delete roadmap review reference document: {}", e.getMessage(), e);
            return DocumentManagementResponse.builder()
                    .success(false)
                    .message("Xóa tài liệu roadmap review thất bại: " + e.getMessage())
                    .fileName(fileName)
                    .build();
        }
    }
}






















