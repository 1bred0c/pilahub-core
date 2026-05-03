package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.response.*;
import fpt.edu.sep490.pilahub.service.AIDocumentManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/ai-documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Document Management (Admin)", description = "APIs for managing AI system documents (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AIDocumentController {

    private final AIDocumentManagementService aiDocService;

    @PostMapping(value = "/scoring-guideline", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Upload scoring guideline (Admin only)",
            description = """
                    Upload file quy định chấm điểm sức khỏe cho AI System.
                    File này sẽ được sử dụng để AI đánh giá hồ sơ sức khỏe của trainee.
                    
                    **Supported formats**: Markdown (.md), Text (.txt), PDF (.pdf)
                    
                    **Max size**: 10MB
                    
                    **Note**: Files trong Gemini File Store hết hạn sau 48 giờ.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Upload successful",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid file or file too large")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<DocumentUploadResponse>> uploadScoringGuideline(
            @Parameter(description = "File quy định chấm điểm (max 10MB)", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Admin uploading scoring guideline: {}", file.getOriginalFilename());

        DocumentUploadResponse response = aiDocService.uploadScoringGuideline(file);

        return ResponseEntity.ok(APIResponse.success(
                "Upload scoring guideline thành công",
                response
        ));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Upload generic file (Admin only)",
            description = """
                    Upload file bất kỳ cho AI System.
                    
                    **Supported formats**: Markdown (.md), Text (.txt), PDF (.pdf)
                    
                    **Max size**: 10MB
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Upload successful",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid file or file too large")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<DocumentUploadResponse>> uploadFile(
            @Parameter(description = "File cần upload (max 10MB)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Tên hiển thị (optional, mặc định dùng tên file)")
            @RequestParam(value = "displayName", required = false) String displayName) {

        log.info("Admin uploading file: {} with displayName: {}", file.getOriginalFilename(), displayName);

        DocumentUploadResponse response = aiDocService.uploadFile(file, displayName);

        return ResponseEntity.ok(APIResponse.success(
                "Upload file thành công",
                response
        ));
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all files (Admin only)",
            description = """
                    Lấy danh sách tất cả files đã upload lên AI System.
                    
                    **Pagination**: Sử dụng pageSize và pageToken để phân trang.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Files retrieved successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<FileListResponse>> listFiles(
            @Parameter(description = "Số lượng files trên 1 page (default: 100)")
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @Parameter(description = "Token để lấy page tiếp theo")
            @RequestParam(value = "pageToken", required = false) String pageToken) {

        log.info("Admin listing files with pageSize: {}, pageToken: {}", pageSize, pageToken);

        FileListResponse response = aiDocService.listFiles(pageSize, pageToken);

        int fileCount = response.getFiles() != null ? response.getFiles().size() : 0;
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d file(s) successfully", fileCount),
                response
        ));
    }

    @GetMapping("/file/{fileName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get file info (Admin only)",
            description = """
                    Lấy thông tin chi tiết của một file.
                    
                    **fileName format**: files/abc123xyz
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "File info retrieved successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "File not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<GeminiFileInfo>> getFileInfo(
            @Parameter(description = "File name (e.g., files/abc123xyz)", required = true)
            @PathVariable String fileName) {

        log.info("Admin getting file info for: {}", fileName);

        GeminiFileInfo response = aiDocService.getFileInfo(fileName);

        return ResponseEntity.ok(APIResponse.success(
                "File info retrieved successfully",
                response
        ));
    }

    @DeleteMapping("/file/{fileName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete file (Admin only)",
            description = """
                    Xóa file khỏi AI System File Store.
                    
                    **fileName format**: files/abc123xyz
                    
                    **Warning**: This action cannot be undone.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "File deleted successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "File not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<DocumentManagementResponse>> deleteFile(
            @Parameter(description = "File name (e.g., files/abc123xyz)", required = true)
            @PathVariable String fileName) {

        log.info("Admin deleting file: {}", fileName);

        DocumentManagementResponse response = aiDocService.deleteFile(fileName);

        return ResponseEntity.ok(APIResponse.success(
                "File deleted successfully",
                response
        ));
    }

    @GetMapping("/guideline-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Check scoring guideline status (Admin only)",
            description = """
                    Kiểm tra trạng thái file quy định chấm điểm hiện tại.
                    
                    Trả về thông tin về file quy định đang hoạt động (nếu có).
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Guideline status retrieved successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<GuidelineStatusResponse>> checkGuidelineStatus() {

        log.info("Admin checking guideline status");

        GuidelineStatusResponse response = aiDocService.checkGuidelineStatus();

        return ResponseEntity.ok(APIResponse.success(
                response.getMessage(),
                response
        ));
    }

    @GetMapping("/download-guideline")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Download scoring guideline (Admin only)",
            description = """
                    Tải về file quy định chấm điểm từ AI System local storage.
                    
                    AI System sẽ tự động lấy file guideline đang active từ local storage.
                    
                    Server sẽ đóng vai trò trung gian lấy file từ AI System và stream về cho người dùng.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "File downloaded successfully",
            content = @Content(mediaType = "application/pdf")
    )
    @ApiResponse(responseCode = "404", description = "File not found or no active guideline")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<byte[]> downloadScoringGuideline() {

        log.info("Admin downloading scoring guideline from AI System");

        byte[] fileContent = aiDocService.downloadScoringGuideline();

        if (fileContent == null || fileContent.length == 0) {
            log.error("Downloaded file is empty or not found");
            return ResponseEntity.notFound().build();
        }

        // Set headers for PDF download
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "quy-dinh-cham-diem-suc-khoe.pdf");
        headers.setContentLength(fileContent.length);

        log.info("Download successful: {} bytes", fileContent.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    @GetMapping("/download/{fileName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Download file (Admin only)",
            description = """
                    Tải về file bất kỳ từ AI System.
                    
                    Server sẽ đóng vai trò trung gian lấy file từ AI System và stream về cho người dùng.
                    
                    **fileName format**: files/abc123xyz (hoặc chỉ cần phần ID: abc123xyz)
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "File downloaded successfully",
            content = @Content(mediaType = "application/pdf")
    )
    @ApiResponse(responseCode = "404", description = "File not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<byte[]> downloadFile(
            @Parameter(description = "File name (e.g., files/abc123xyz or abc123xyz)", required = true)
            @PathVariable String fileName) {

        log.info("Admin downloading file: {}", fileName);

        // Đảm bảo fileName có format đúng
        String normalizedFileName = fileName.startsWith("files/") ? fileName : "files/" + fileName;

        // Download file content từ AI System - GET FULL RESPONSE WITH HEADERS
        ResponseEntity<byte[]> aiResponse = aiDocService.downloadFileWithHeaders(normalizedFileName);

        byte[] fileContent = aiResponse.getBody();
        if (fileContent == null || fileContent.length == 0) {
            log.error("Downloaded file is empty: {}", normalizedFileName);
            return ResponseEntity.notFound().build();
        }

        // Extract headers from AI System response
        org.springframework.http.HttpHeaders aiHeaders = aiResponse.getHeaders();

        // Get Content-Disposition from AI System (contains actual filename)
        org.springframework.http.ContentDisposition aiContentDisposition = aiHeaders.getContentDisposition();
        String actualFileName = aiContentDisposition.getFilename();

        // Use filename from AI System, but change extension to .pdf
        String finalFileName;
        if (actualFileName != null && !actualFileName.isEmpty()) {
            // Remove extension and add .pdf
            int lastDot = actualFileName.lastIndexOf('.');
            String baseName = lastDot > 0 ? actualFileName.substring(0, lastDot) : actualFileName;
            finalFileName = baseName + ".pdf";
        } else {
            finalFileName = fileName.replaceAll("files/", "") + ".pdf";
        }

        log.info("Download successful: {} bytes, final filename: {}", fileContent.length, finalFileName);

        // Set response headers - always return as PDF
        org.springframework.http.HttpHeaders responseHeaders = new org.springframework.http.HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_PDF);
        responseHeaders.setContentDispositionFormData("attachment", finalFileName);
        responseHeaders.setContentLength(fileContent.length);

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(fileContent);
    }

    // ======================== ROADMAP REFERENCE DOCUMENT ========================

    @PostMapping(value = "/roadmap-reference", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Upload roadmap reference document (Admin only)",
            description = """
                    Upload file tài liệu tham khảo cho AI sinh roadmap tập luyện.
                    File này sẽ được AI sử dụng làm cơ sở để tạo roadmap luyện tập cá nhân hóa.
                    
                    **Supported formats**: Markdown (.md), Text (.txt), PDF (.pdf)
                    
                    **Max size**: 10MB
                    
                    **Note**: Files trong Gemini File Store hết hạn sau 48 giờ.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Upload successful",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid file or file too large")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<DocumentUploadResponse>> uploadRoadmapReferenceDocument(
            @Parameter(description = "File tài liệu tham khảo roadmap (max 10MB)", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Admin uploading roadmap reference document: {}", file.getOriginalFilename());

        DocumentUploadResponse response = aiDocService.uploadRoadmapReferenceDocument(file);

        return ResponseEntity.ok(APIResponse.success(
                "Upload tài liệu tham khảo roadmap thành công",
                response
        ));
    }

    @GetMapping("/roadmap-reference/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Check roadmap reference document status (Admin only)",
            description = """
                    Kiểm tra trạng thái tài liệu tham khảo roadmap hiện tại.
                    
                    Trả về thông tin về tài liệu tham khảo đang hoạt động (nếu có).
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Roadmap reference status retrieved successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<RoadmapReferenceStatusResponse>> checkRoadmapReferenceStatus() {

        log.info("Admin checking roadmap reference document status");

        RoadmapReferenceStatusResponse response = aiDocService.checkRoadmapReferenceStatus();

        return ResponseEntity.ok(APIResponse.success(
                response.getMessage(),
                response
        ));
    }

    @GetMapping("/download-roadmap-reference")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Download roadmap reference document (Admin only)",
            description = """
                    Tải về file tài liệu tham khảo roadmap từ AI System local storage.
                    
                    AI System sẽ tự động lấy file tài liệu tham khảo roadmap đang active từ local storage.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "File downloaded successfully",
            content = @Content(mediaType = "application/pdf")
    )
    @ApiResponse(responseCode = "404", description = "File not found or no active roadmap reference document")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<byte[]> downloadRoadmapReference() {

        log.info("Admin downloading roadmap reference document from AI System");

        byte[] fileContent = aiDocService.downloadRoadmapReference();

        if (fileContent == null || fileContent.length == 0) {
            log.error("Downloaded roadmap reference file is empty or not found");
            return ResponseEntity.notFound().build();
        }

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "roadmap-reference.pdf");
        headers.setContentLength(fileContent.length);

        log.info("Download roadmap reference successful: {} bytes", fileContent.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    @DeleteMapping("/roadmap-reference/{fileName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete roadmap reference document (Admin only)",
            description = """
                    Xóa tài liệu tham khảo roadmap khỏi AI System File Store.
                    
                    **fileName format**: files/abc123xyz
                    
                    **Warning**: This action cannot be undone.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Roadmap reference document deleted successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "File not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<DocumentManagementResponse>> deleteRoadmapDocument(
            @Parameter(description = "File name (e.g., files/abc123xyz)", required = true)
            @PathVariable String fileName) {

        log.info("Admin deleting roadmap reference document: {}", fileName);

        DocumentManagementResponse response = aiDocService.deleteRoadmapDocument(fileName);

        return ResponseEntity.ok(APIResponse.success(
                "Xóa tài liệu tham khảo roadmap thành công",
                response
        ));
    }

    // ======================== WORKOUT FEEDBACK REFERENCE DOCUMENT ========================

    @PostMapping(value = "/workout-feedback-reference", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Upload workout feedback reference document (Admin only)",
            description = """
                    Upload file tài liệu tham khảo cho AI phân tích workout feedback.
                    File này sẽ được AI sử dụng làm cơ sở để tạo phản hồi và đánh giá workout sessions.
                    
                    **Supported formats**: Markdown (.md), Text (.txt), PDF (.pdf)
                    
                    **Max size**: 10MB
                    
                    **Note**: Files trong Gemini File Store hết hạn sau 48 giờ.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Upload successful",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid file or file too large")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<DocumentUploadResponse>> uploadWorkoutFeedbackReferenceDocument(
            @Parameter(description = "File tài liệu tham khảo workout feedback (max 10MB)", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Admin uploading workout feedback reference document: {}", file.getOriginalFilename());

        DocumentUploadResponse response = aiDocService.uploadWorkoutFeedbackReferenceDocument(file);

        return ResponseEntity.ok(APIResponse.success(
                "Upload tài liệu tham khảo workout feedback thành công",
                response
        ));
    }

    @GetMapping("/workout-feedback-reference/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Check workout feedback reference document status (Admin only)",
            description = """
                    Kiểm tra trạng thái tài liệu tham khảo workout feedback hiện tại.
                    
                    Trả về thông tin về tài liệu tham khảo đang hoạt động (nếu có).
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Workout feedback reference status retrieved successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<WorkoutFeedbackReferenceStatusResponse>> checkWorkoutFeedbackReferenceStatus() {

        log.info("Admin checking workout feedback reference document status");

        WorkoutFeedbackReferenceStatusResponse response = aiDocService.checkWorkoutFeedbackReferenceStatus();

        return ResponseEntity.ok(APIResponse.success(
                response.getMessage(),
                response
        ));
    }

    @GetMapping("/download-workout-feedback-reference")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Download workout feedback reference document (Admin only)",
            description = """
                    Tải về file tài liệu tham khảo workout feedback từ AI System local storage.
                    
                    AI System sẽ tự động lấy file tài liệu tham khảo workout feedback đang active từ local storage.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "File downloaded successfully",
            content = @Content(mediaType = "application/pdf")
    )
    @ApiResponse(responseCode = "404", description = "File not found or no active workout feedback reference document")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<byte[]> downloadWorkoutFeedbackReference() {

        log.info("Admin downloading workout feedback reference document from AI System");

        byte[] fileContent = aiDocService.downloadWorkoutFeedbackReference();

        if (fileContent == null || fileContent.length == 0) {
            log.error("Downloaded workout feedback reference file is empty or not found");
            return ResponseEntity.notFound().build();
        }

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "workout-feedback-reference.pdf");
        headers.setContentLength(fileContent.length);

        log.info("Download workout feedback reference successful: {} bytes", fileContent.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    @DeleteMapping("/workout-feedback-reference/{fileName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete workout feedback reference document (Admin only)",
            description = """
                    Xóa tài liệu tham khảo workout feedback khỏi AI System File Store.
                    
                    **fileName format**: files/abc123xyz
                    
                    **Warning**: This action cannot be undone.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Workout feedback reference document deleted successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "File not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<DocumentManagementResponse>> deleteWorkoutFeedbackDocument(
            @Parameter(description = "File name (e.g., files/abc123xyz)", required = true)
            @PathVariable String fileName) {

        log.info("Admin deleting workout feedback reference document: {}", fileName);

        DocumentManagementResponse response = aiDocService.deleteWorkoutFeedbackDocument(fileName);

        return ResponseEntity.ok(APIResponse.success(
                "Xóa tài liệu tham khảo workout feedback thành công",
                response
        ));
    }

    // ======================== ROADMAP REVIEW REFERENCE DOCUMENT ========================

    @PostMapping(value = "/roadmap-review-reference", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Upload roadmap review reference document (Admin only)",
            description = """
                    Upload file tài liệu tham khảo cho AI review roadmap.
                    File này sẽ được AI sử dụng làm cơ sở để đánh giá hiệu quả roadmap.

                    **Supported formats**: Markdown (.md), Text (.txt), PDF (.pdf)

                    **Max size**: 10MB

                    **Note**: Files trong Gemini File Store hết hạn sau 48 giờ.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Upload successful",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid file or file too large")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<DocumentUploadResponse>> uploadRoadmapReviewReferenceDocument(
            @Parameter(description = "File tài liệu tham khảo review roadmap (max 10MB)", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Admin uploading roadmap review reference document: {}", file.getOriginalFilename());

        DocumentUploadResponse response = aiDocService.uploadRoadmapReviewReferenceDocument(file);

        return ResponseEntity.ok(APIResponse.success(
                "Upload tài liệu tham khảo review roadmap thành công",
                response
        ));
    }

    @GetMapping("/roadmap-review-reference/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Check roadmap review reference document status (Admin only)",
            description = """
                    Kiểm tra trạng thái tài liệu tham khảo review roadmap hiện tại.

                    Trả về thông tin về tài liệu tham khảo đang hoạt động (nếu có).
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Roadmap review reference status retrieved successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<RoadmapReviewReferenceStatusResponse>> checkRoadmapReviewReferenceStatus() {

        log.info("Admin checking roadmap review reference document status");

        RoadmapReviewReferenceStatusResponse response = aiDocService.checkRoadmapReviewReferenceStatus();

        return ResponseEntity.ok(APIResponse.success(
                response.getMessage(),
                response
        ));
    }

    @GetMapping("/download-roadmap-review-reference")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Download roadmap review reference document (Admin only)",
            description = """
                    Tải về file tài liệu tham khảo review roadmap từ AI System local storage.

                    AI System sẽ tự động lấy file tài liệu tham khảo review roadmap đang active từ local storage.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "File downloaded successfully",
            content = @Content(mediaType = "application/pdf")
    )
    @ApiResponse(responseCode = "404", description = "File not found or no active roadmap review reference document")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<byte[]> downloadRoadmapReviewReference() {

        log.info("Admin downloading roadmap review reference document from AI System");

        byte[] fileContent = aiDocService.downloadRoadmapReviewReference();

        if (fileContent == null || fileContent.length == 0) {
            log.error("Downloaded roadmap review reference file is empty or not found");
            return ResponseEntity.notFound().build();
        }

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "roadmap-review-reference.pdf");
        headers.setContentLength(fileContent.length);

        log.info("Download roadmap review reference successful: {} bytes", fileContent.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    @DeleteMapping("/roadmap-review-reference/{fileName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete roadmap review reference document (Admin only)",
            description = """
                    Xóa tài liệu tham khảo review roadmap khỏi AI System File Store.

                    **fileName format**: files/abc123xyz

                    **Warning**: This action cannot be undone.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Roadmap review reference document deleted successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "File not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "500", description = "AI System error or connection failed")
    public ResponseEntity<APIResponse<DocumentManagementResponse>> deleteRoadmapReviewDocument(
            @Parameter(description = "File name (e.g., files/abc123xyz)", required = true)
            @PathVariable String fileName) {

        log.info("Admin deleting roadmap review reference document: {}", fileName);

        DocumentManagementResponse response = aiDocService.deleteRoadmapReviewDocument(fileName);

        return ResponseEntity.ok(APIResponse.success(
                "Xóa tài liệu tham khảo review roadmap thành công",
                response
        ));
    }
}
