package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface AIDocumentManagementService {

    /**
     * Upload file quy định chấm điểm sức khỏe (main guideline cho AI assessment)
     *
     * @param file File quy định (markdown/txt/pdf, max 10MB)
     * @return Upload response với file info
     */
    DocumentUploadResponse uploadScoringGuideline(MultipartFile file);

    /**
     * Upload file bất kỳ cho các mục đích khác
     *
     * @param file File cần upload (max 10MB)
     * @param displayName Tên hiển thị (optional, mặc định dùng tên file)
     * @return Upload response với file info
     */
    DocumentUploadResponse uploadFile(MultipartFile file, String displayName);

    /**
     * Lấy danh sách tất cả files đã upload
     *
     * @param pageSize Số lượng files trên 1 page (optional)
     * @param pageToken Token để lấy page tiếp theo (optional)
     * @return Danh sách files
     */
    FileListResponse listFiles(Integer pageSize, String pageToken);

    /**
     * Lấy thông tin chi tiết của một file
     *
     * @param fileName File name (e.g., "files/abc123xyz")
     * @return File info
     */
    GeminiFileInfo getFileInfo(String fileName);

    /**
     * Xóa file khỏi File Store
     *
     * @param fileName File name (e.g., "files/abc123xyz")
     * @return Delete response
     */
    DocumentManagementResponse deleteFile(String fileName);

    /**
     * Kiểm tra trạng thái file quy định chấm điểm hiện tại
     *
     * @return Guideline status
     */
    GuidelineStatusResponse checkGuidelineStatus();

    /**
     * Tải về file quy định chấm điểm từ AI System local storage
     *
     * AI System tự động lấy file guideline đang active từ local storage
     *
     * @return File content as byte array
     */
    byte[] downloadScoringGuideline();

    /**
     * Tải về file bất kỳ từ AI System
     *
     * @param fileName File name (e.g., "files/abc123xyz")
     * @return ResponseEntity với headers và file content để extract metadata
     */
    ResponseEntity<byte[]> downloadFileWithHeaders(String fileName);

    // ======================== ROADMAP REFERENCE DOCUMENT ========================

    /**
     * Upload file tài liệu tham khảo cho AI sinh roadmap tập luyện
     *
     * @param file File tài liệu (markdown/txt/pdf, max 10MB)
     * @return Upload response với file info
     */
    DocumentUploadResponse uploadRoadmapReferenceDocument(MultipartFile file);

    /**
     * Kiểm tra trạng thái tài liệu tham khảo roadmap hiện tại
     *
     * @return Roadmap reference status
     */
    RoadmapReferenceStatusResponse checkRoadmapReferenceStatus();

    /**
     * Tải về file tài liệu tham khảo roadmap từ AI System local storage
     *
     * @return File content as byte array
     */
    byte[] downloadRoadmapReference();

    /**
     * Xóa tài liệu tham khảo roadmap khỏi File Store
     *
     * @param fileName File name (e.g., "files/abc123xyz")
     * @return Delete response
     */
    DocumentManagementResponse deleteRoadmapDocument(String fileName);

    // ======================== WORKOUT FEEDBACK REFERENCE DOCUMENT ========================

    /**
     * Upload file tài liệu tham khảo cho AI phân tích workout feedback
     *
     * @param file File tài liệu (markdown/txt/pdf, max 10MB)
     * @return Upload response với file info
     */
    DocumentUploadResponse uploadWorkoutFeedbackReferenceDocument(MultipartFile file);

    /**
     * Kiểm tra trạng thái tài liệu tham khảo workout feedback hiện tại
     *
     * @return Workout feedback reference status
     */
    WorkoutFeedbackReferenceStatusResponse checkWorkoutFeedbackReferenceStatus();

    /**
     * Tải về file tài liệu tham khảo workout feedback từ AI System local storage
     *
     * @return File content as byte array
     */
    byte[] downloadWorkoutFeedbackReference();

    /**
     * Xóa tài liệu tham khảo workout feedback khỏi File Store
     *
     * @param fileName File name (e.g., "files/abc123xyz")
     * @return Delete response
     */
    DocumentManagementResponse deleteWorkoutFeedbackDocument(String fileName);

    // ======================== ROADMAP REVIEW REFERENCE DOCUMENT ========================

    /**
     * Upload file tài liệu tham khảo cho AI review roadmap
     *
     * @param file File tài liệu (markdown/txt/pdf, max 10MB)
     * @return Upload response với file info
     */
    DocumentUploadResponse uploadRoadmapReviewReferenceDocument(MultipartFile file);

    /**
     * Kiểm tra trạng thái tài liệu tham khảo review roadmap hiện tại
     *
     * @return Roadmap review reference status
     */
    RoadmapReviewReferenceStatusResponse checkRoadmapReviewReferenceStatus();

    /**
     * Tải về file tài liệu tham khảo review roadmap từ AI System local storage
     *
     * @return File content as byte array
     */
    byte[] downloadRoadmapReviewReference();

    /**
     * Xóa tài liệu tham khảo review roadmap khỏi File Store
     *
     * @param fileName File name (e.g., "files/abc123xyz")
     * @return Delete response
     */
    DocumentManagementResponse deleteRoadmapReviewDocument(String fileName);
}
