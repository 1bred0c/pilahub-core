package pilahub.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import pilahub.dto.response.DocumentManagementResponse;
import pilahub.dto.response.FileListResponse;
import pilahub.dto.response.FileUploadResponse;

public interface GeminiFileStoreService {

    /**
     * Upload file lên Gemini File Store
     */
    FileUploadResponse.GeminiFileInfo uploadFile(MultipartFile file, String displayName);

    /**
     * Upload file quy định chấm điểm sức khỏe lên File Store
     */
    DocumentManagementResponse uploadHealthScoringGuideline(MultipartFile file);

    /**
     * Lấy danh sách tất cả files trong File Store
     */
    FileListResponse listFiles(Integer pageSize, String pageToken);

    /**
     * Lấy thông tin chi tiết của một file
     */
    FileUploadResponse.GeminiFileInfo getFile(String fileName);

    /**
     * Xóa file khỏi File Store
     */
    DocumentManagementResponse deleteFile(String fileName);

    /**
     * Lấy URI của file quy định chấm điểm hiện tại (để dùng trong prompt)
     */
    String getActiveScoringGuidelineUri();

    /**
     * Kiểm tra trạng thái file (ACTIVE, PROCESSING, FAILED)
     */
    String checkFileState(String fileName);

    /**
     * Lấy file từ local storage để download
     */
    Resource loadFileAsResource(String fileName);

    /**
     * Lấy file quy định chấm điểm từ local storage
     */
    Resource loadScoringGuidelineAsResource();

    /**
     * Upload file tài liệu tham khảo cho roadmap generation lên File Store
     */
    DocumentManagementResponse uploadRoadmapReferenceDocument(MultipartFile file);

    /**
     * Lấy URI của file tài liệu roadmap hiện tại (để dùng trong prompt)
     */
    String getActiveRoadmapDocumentUri();

    /**
     * Lấy file tài liệu roadmap từ local storage
     */
    Resource loadRoadmapDocumentAsResource();

    /**
     * Xóa file tài liệu roadmap và clear cache
     */
    DocumentManagementResponse deleteRoadmapDocument(String fileName);

    /**
     * Upload file tài liệu tham khảo cho workout feedback analysis lên File Store
     */
    DocumentManagementResponse uploadWorkoutFeedbackReferenceDocument(MultipartFile file);

    /**
     * Lấy URI của file tài liệu workout feedback hiện tại (để dùng trong prompt)
     */
    String getActiveWorkoutFeedbackDocumentUri();

    /**
     * Lấy file tài liệu workout feedback từ local storage
     */
    Resource loadWorkoutFeedbackDocumentAsResource();

    /**
     * Xóa file tài liệu workout feedback và clear cache
     */
    DocumentManagementResponse deleteWorkoutFeedbackDocument(String fileName);

    /**
     * Upload file tài liệu tham khảo cho roadmap review lên File Store
     */
    DocumentManagementResponse uploadRoadmapReviewReferenceDocument(MultipartFile file);

    /**
     * Lấy URI của file tài liệu roadmap review hiện tại (để dùng trong prompt)
     */
    String getActiveRoadmapReviewDocumentUri();

    /**
     * Lấy file tài liệu roadmap review từ local storage
     */
    Resource loadRoadmapReviewDocumentAsResource();

    /**
     * Xóa file tài liệu roadmap review và clear cache
     */
    DocumentManagementResponse deleteRoadmapReviewDocument(String fileName);
}
