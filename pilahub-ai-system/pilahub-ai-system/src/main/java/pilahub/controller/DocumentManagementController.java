package pilahub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pilahub.dto.response.DocumentManagementResponse;
import pilahub.dto.response.FileListResponse;
import pilahub.dto.response.FileUploadResponse;
import pilahub.service.GeminiFileStoreService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/documents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Document Management", description = "API quản lý tài liệu cho AI System")
public class DocumentManagementController {

    private final GeminiFileStoreService fileStoreService;

    @PostMapping(value = "/upload-scoring-guideline", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload quy định chấm điểm",
               description = "Upload file quy định chấm điểm hồ sơ sức khỏe lên Gemini File Store")
    public ResponseEntity<DocumentManagementResponse> uploadScoringGuideline(
            @RequestParam("file") MultipartFile file) {

        log.info("Received request to upload scoring guideline: {}", file.getOriginalFilename());

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("File không được để trống")
                            .build()
            );
        }

        // Check file type (accept markdown, txt, pdf)
        String contentType = file.getContentType();
        if (contentType == null ||
            (!contentType.contains("markdown") &&
             !contentType.contains("text") &&
             !contentType.contains("pdf"))) {
            log.warn("Invalid file type: {}", contentType);
        }

        try {
            DocumentManagementResponse response = fileStoreService.uploadHealthScoringGuideline(file);

            if (response.isSuccess()) {
                log.info("Successfully uploaded scoring guideline: {}", response.getFileName());
                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to upload scoring guideline: {}", response.getMessage());
                return ResponseEntity.internalServerError().body(response);
            }

        } catch (Exception e) {
            log.error("Error uploading scoring guideline: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("Lỗi server: " + e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file bất kỳ", description = "Upload file lên Gemini File Store")
    public ResponseEntity<DocumentManagementResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "displayName", required = false) String displayName) {

        log.info("Received request to upload file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("File không được để trống")
                            .build()
            );
        }

        try {
            String finalDisplayName = displayName != null ? displayName : file.getOriginalFilename();
            FileUploadResponse.GeminiFileInfo fileInfo = fileStoreService.uploadFile(file, finalDisplayName);

            return ResponseEntity.ok(DocumentManagementResponse.builder()
                    .success(true)
                    .message("Upload file thành công")
                    .fileName(fileInfo.getName())
                    .fileUri(fileInfo.getUri())
                    .state(fileInfo.getState())
                    .displayName(fileInfo.getDisplayName())
                    .sizeBytes(fileInfo.getSizeBytes() != null ? Long.parseLong(fileInfo.getSizeBytes()) : null)
                    .expirationTime(fileInfo.getExpirationTime())
                    .build());

        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("Lỗi server: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/list")
    @Operation(summary = "Liệt kê tất cả files", description = "Lấy danh sách tất cả files trong Gemini File Store")
    public ResponseEntity<FileListResponse> listFiles(
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pageToken", required = false) String pageToken) {

        log.info("Listing files from Gemini File Store");

        try {
            FileListResponse response = fileStoreService.listFiles(pageSize, pageToken);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error listing files: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/file/{fileName}")
    @Operation(summary = "Lấy thông tin file", description = "Lấy thông tin chi tiết của một file")
    public ResponseEntity<FileUploadResponse.GeminiFileInfo> getFile(@PathVariable String fileName) {

        log.info("Getting file info: {}", fileName);

        try {
            FileUploadResponse.GeminiFileInfo fileInfo = fileStoreService.getFile(fileName);
            return ResponseEntity.ok(fileInfo);

        } catch (Exception e) {
            log.error("Error getting file info: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/file/{fileName}")
    @Operation(summary = "Xóa file", description = "Xóa file khỏi Gemini File Store")
    public ResponseEntity<DocumentManagementResponse> deleteFile(@PathVariable String fileName) {

        log.info("Deleting file: {}", fileName);

        try {
            DocumentManagementResponse response = fileStoreService.deleteFile(fileName);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.internalServerError().body(response);
            }

        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("Lỗi server: " + e.getMessage())
                            .fileName(fileName)
                            .build()
            );
        }
    }

    @GetMapping("/scoring-guideline/status")
    @Operation(summary = "Kiểm tra trạng thái file quy định chấm điểm",
               description = "Kiểm tra xem file quy định chấm điểm có đang hoạt động không")
    public ResponseEntity<Map<String, Object>> getScoringGuidelineStatus() {

        log.info("Checking scoring guideline status");

        try {
            String uri = fileStoreService.getActiveScoringGuidelineUri();

            Map<String, Object> status = new HashMap<>();
            status.put("hasActiveGuideline", uri != null);
            status.put("guidelineUri", uri);

            if (uri != null) {
                status.put("message", "File quy định chấm điểm đang hoạt động");
            } else {
                status.put("message", "Chưa có file quy định chấm điểm hoặc file chưa sẵn sàng");
            }

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error checking scoring guideline status: {}", e.getMessage(), e);

            Map<String, Object> status = new HashMap<>();
            status.put("hasActiveGuideline", false);
            status.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(status);
        }
    }

    @GetMapping("/download/**")
    @Operation(summary = "Download file từ local storage",
               description = "Tải file đã upload về từ local storage")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {

        // Extract fileName from path after "/download/"
        String requestPath = request.getRequestURI();
        String fileName = requestPath.substring(requestPath.indexOf("/download/") + 10);

        log.info("Downloading file: {}", fileName);

        try {
            Resource resource = fileStoreService.loadFileAsResource(fileName);

            // Determine content type from actual resource filename
            String actualFilename = resource.getFilename();
            String contentType = "application/octet-stream";

            if (actualFilename != null) {
                if (actualFilename.endsWith(".md")) {
                    contentType = "text/markdown";
                } else if (actualFilename.endsWith(".txt")) {
                    contentType = "text/plain";
                } else if (actualFilename.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (actualFilename.endsWith(".json")) {
                    contentType = "application/json";
                }
            }

            // Extract just the filename for Content-Disposition (without path)
            String displayFileName = actualFilename != null ? actualFilename : fileName;
            if (displayFileName.contains("/")) {
                displayFileName = displayFileName.substring(displayFileName.lastIndexOf("/") + 1);
            }

            log.info("Sending file: {} (Content-Type: {})", displayFileName, contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + displayFileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download-guideline")
    @Operation(summary = "Download quy định chấm điểm",
               description = "Tải file quy định chấm điểm hồ sơ sức khỏe từ local storage")
    public ResponseEntity<Resource> downloadScoringGuideline() {

        log.info("Downloading scoring guideline");

        try {
            Resource resource = fileStoreService.loadScoringGuidelineAsResource();

            // Tự động detect content type từ filename
            String filename = resource.getFilename();
            String contentType = "application/octet-stream";
            String displayFilename = "quy-dinh-cham-diem-suc-khoe";

            if (filename != null) {
                if (filename.endsWith(".pdf")) {
                    contentType = "application/pdf";
                    displayFilename += ".pdf";
                } else if (filename.endsWith(".md")) {
                    contentType = "text/markdown";
                    displayFilename += ".md";
                } else if (filename.endsWith(".txt")) {
                    contentType = "text/plain";
                    displayFilename += ".txt";
                } else {
                    // Giữ nguyên tên file gốc nếu không match
                    displayFilename = filename;
                }
            } else {
                displayFilename += ".pdf"; // Default
            }

            log.info("Sending scoring guideline: {} (Content-Type: {})", displayFilename, contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + displayFilename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading scoring guideline: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/upload-roadmap-reference", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload tài liệu tham khảo cho roadmap",
               description = "Upload file tài liệu tham khảo (PDF, markdown, text) để AI sử dụng khi tạo roadmap")
    public ResponseEntity<DocumentManagementResponse> uploadRoadmapReference(
            @RequestParam("file") MultipartFile file) {

        log.info("Received request to upload roadmap reference document: {}", file.getOriginalFilename());

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("File không được để trống")
                            .build()
            );
        }

        // Check file type (accept PDF, markdown, txt)
        String contentType = file.getContentType();
        if (contentType == null ||
            (!contentType.contains("pdf") &&
             !contentType.contains("markdown") &&
             !contentType.contains("text"))) {
            log.warn("Invalid file type for roadmap reference: {}", contentType);
            return ResponseEntity.badRequest().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("Chỉ chấp nhận file PDF, Markdown (.md) hoặc Text (.txt)")
                            .build()
            );
        }

        try {
            DocumentManagementResponse response = fileStoreService.uploadRoadmapReferenceDocument(file);

            if (response.isSuccess()) {
                log.info("Successfully uploaded roadmap reference: {}", response.getFileName());
                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to upload roadmap reference: {}", response.getMessage());
                return ResponseEntity.internalServerError().body(response);
            }

        } catch (Exception e) {
            log.error("Error uploading roadmap reference: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("Lỗi server: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/roadmap-reference/status")
    @Operation(summary = "Kiểm tra trạng thái tài liệu roadmap",
               description = "Kiểm tra xem tài liệu tham khảo roadmap có đang hoạt động không")
    public ResponseEntity<Map<String, Object>> getRoadmapReferenceStatus() {

        log.info("Checking roadmap reference document status");

        try {
            String uri = fileStoreService.getActiveRoadmapDocumentUri();

            Map<String, Object> status = new HashMap<>();
            status.put("hasActiveDocument", uri != null);
            status.put("documentUri", uri);

            if (uri != null) {
                status.put("message", "Tài liệu tham khảo roadmap đang hoạt động");
            } else {
                status.put("message", "Chưa có tài liệu tham khảo hoặc tài liệu chưa sẵn sàng");
            }

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error checking roadmap reference status: {}", e.getMessage(), e);

            Map<String, Object> status = new HashMap<>();
            status.put("hasActiveDocument", false);
            status.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(status);
        }
    }

    @GetMapping("/download-roadmap-reference")
    @Operation(summary = "Download tài liệu roadmap",
               description = "Tải file tài liệu tham khảo roadmap từ local storage")
    public ResponseEntity<Resource> downloadRoadmapReference() {

        log.info("Downloading roadmap reference document");

        try {
            Resource resource = fileStoreService.loadRoadmapDocumentAsResource();

            // Determine content type from filename
            String filename = resource.getFilename();
            String contentType = "application/octet-stream";
            
            if (filename != null) {
                if (filename.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (filename.endsWith(".md")) {
                    contentType = "text/markdown";
                } else if (filename.endsWith(".txt")) {
                    contentType = "text/plain";
                }
            }

            String displayName = filename != null ? filename : "roadmap-reference-document.pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + displayName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading roadmap reference: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/roadmap-reference/{fileName}")
    @Operation(summary = "Xóa tài liệu roadmap",
               description = "Xóa file tài liệu tham khảo roadmap khỏi Gemini File Store")
    public ResponseEntity<DocumentManagementResponse> deleteRoadmapReference(@PathVariable String fileName) {

        log.info("Deleting roadmap reference document: {}", fileName);

        try {
            DocumentManagementResponse response = fileStoreService.deleteRoadmapDocument(fileName);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.internalServerError().body(response);
            }

        } catch (Exception e) {
            log.error("Error deleting roadmap reference: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("Lỗi server: " + e.getMessage())
                            .fileName(fileName)
                            .build()
            );
        }
    }

    // ========== ROADMAP REVIEW REFERENCE DOCUMENT APIs ==========

    @PostMapping(value = "/upload-roadmap-review-reference", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload tài liệu tham khảo cho roadmap review",
               description = "Upload file tài liệu tham khảo (PDF, markdown, text) để AI sử dụng khi đánh giá roadmap")
    public ResponseEntity<DocumentManagementResponse> uploadRoadmapReviewReference(
            @RequestParam("file") MultipartFile file) {

        log.info("Received request to upload roadmap review reference: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("File không được để trống")
                            .build()
            );
        }

        try {
            DocumentManagementResponse response = fileStoreService.uploadRoadmapReviewReferenceDocument(file);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("Error uploading roadmap review reference: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("Lỗi server: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/roadmap-review-reference/status")
    @Operation(summary = "Kiểm tra trạng thái tài liệu roadmap review",
               description = "Kiểm tra xem tài liệu tham khảo roadmap review có đang hoạt động không")
    public ResponseEntity<Map<String, Object>> getRoadmapReviewReferenceStatus() {

        log.info("Checking roadmap review reference document status");

        try {
            String uri = fileStoreService.getActiveRoadmapReviewDocumentUri();

            Map<String, Object> status = new HashMap<>();
            status.put("hasActiveDocument", uri != null);
            status.put("documentUri", uri);

            if (uri != null) {
                status.put("message", "Tài liệu tham khảo roadmap review đang hoạt động");
            } else {
                status.put("message", "Chưa có tài liệu tham khảo hoặc tài liệu chưa sẵn sàng");
            }

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error checking roadmap review reference status: {}", e.getMessage(), e);

            Map<String, Object> status = new HashMap<>();
            status.put("hasActiveDocument", false);
            status.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(status);
        }
    }

    @GetMapping("/download-roadmap-review-reference")
    @Operation(summary = "Download tài liệu roadmap review",
               description = "Tải file tài liệu tham khảo roadmap review từ local storage")
    public ResponseEntity<Resource> downloadRoadmapReviewReference() {

        log.info("Downloading roadmap review reference document");

        try {
            Resource resource = fileStoreService.loadRoadmapReviewDocumentAsResource();

            String filename = resource.getFilename();
            String contentType = "application/octet-stream";

            if (filename != null) {
                if (filename.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (filename.endsWith(".md")) {
                    contentType = "text/markdown";
                } else if (filename.endsWith(".txt")) {
                    contentType = "text/plain";
                }
            }

            String displayName = filename != null ? filename : "roadmap-review-reference-document.pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + displayName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading roadmap review reference: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/roadmap-review-reference/{fileName}")
    @Operation(summary = "Xóa tài liệu roadmap review",
               description = "Xóa file tài liệu tham khảo roadmap review khỏi Gemini File Store")
    public ResponseEntity<DocumentManagementResponse> deleteRoadmapReviewReference(@PathVariable String fileName) {

        log.info("Deleting roadmap review reference document: {}", fileName);

        try {
            DocumentManagementResponse response = fileStoreService.deleteRoadmapReviewDocument(fileName);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.internalServerError().body(response);
            }

        } catch (Exception e) {
            log.error("Error deleting roadmap review reference: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("Lỗi server: " + e.getMessage())
                            .fileName(fileName)
                            .build()
            );
        }
    }

    // ========== WORKOUT FEEDBACK REFERENCE DOCUMENT APIs ==========

    @PostMapping(value = "/upload-workout-feedback-reference", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload tài liệu tham khảo cho workout feedback",
               description = "Upload file tài liệu tham khảo (PDF, markdown, text) để AI sử dụng khi phân tích workout feedback")
    public ResponseEntity<DocumentManagementResponse> uploadWorkoutFeedbackReference(
            @RequestParam("file") MultipartFile file) {

        log.info("Received request to upload workout feedback reference: {}", file.getOriginalFilename());

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("File không được để trống")
                            .build()
            );
        }

        try {
            DocumentManagementResponse response = fileStoreService.uploadWorkoutFeedbackReferenceDocument(file);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("Error uploading workout feedback reference: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("Lỗi server: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/workout-feedback-reference/status")
    @Operation(summary = "Kiểm tra trạng thái tài liệu workout feedback",
               description = "Kiểm tra xem tài liệu tham khảo workout feedback có đang hoạt động không")
    public ResponseEntity<Map<String, Object>> getWorkoutFeedbackReferenceStatus() {

        log.info("Checking workout feedback reference document status");

        try {
            String uri = fileStoreService.getActiveWorkoutFeedbackDocumentUri();

            Map<String, Object> status = new HashMap<>();
            status.put("hasActiveDocument", uri != null);
            status.put("documentUri", uri);

            if (uri != null) {
                status.put("message", "Tài liệu tham khảo workout feedback đang hoạt động");
            } else {
                status.put("message", "Chưa có tài liệu tham khảo workout feedback hoặc tài liệu chưa sẵn sàng");
            }

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error checking workout feedback reference status: {}", e.getMessage(), e);
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("hasActiveDocument", false);
            errorStatus.put("message", "Lỗi kiểm tra trạng thái: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorStatus);
        }
    }

    @GetMapping("/download-workout-feedback-reference")
    @Operation(summary = "Download tài liệu workout feedback",
               description = "Tải file tài liệu tham khảo workout feedback từ local storage")
    public ResponseEntity<Resource> downloadWorkoutFeedbackReference() {

        log.info("Downloading workout feedback reference document");

        try {
            Resource resource = fileStoreService.loadWorkoutFeedbackDocumentAsResource();

            // Determine content type from filename
            String filename = resource.getFilename();
            String contentType = "application/octet-stream";

            if (filename != null) {
                if (filename.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (filename.endsWith(".md")) {
                    contentType = "text/markdown";
                } else if (filename.endsWith(".txt")) {
                    contentType = "text/plain";
                }
            }

            String displayName = filename != null ? filename : "workout-feedback-reference-document.pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + displayName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading workout feedback reference: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/workout-feedback-reference/{fileName}")
    @Operation(summary = "Xóa tài liệu workout feedback",
               description = "Xóa file tài liệu tham khảo workout feedback khỏi Gemini File Store")
    public ResponseEntity<DocumentManagementResponse> deleteWorkoutFeedbackReference(@PathVariable String fileName) {

        log.info("Deleting workout feedback reference document: {}", fileName);

        try {
            DocumentManagementResponse response = fileStoreService.deleteWorkoutFeedbackDocument(fileName);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.internalServerError().body(response);
            }

        } catch (Exception e) {
            log.error("Error deleting workout feedback reference: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    DocumentManagementResponse.builder()
                            .success(false)
                            .message("Lỗi server: " + e.getMessage())
                            .fileName(fileName)
                            .build()
            );
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Kiểm tra service có hoạt động không")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Document Management Service is running");
    }
}





