package fpt.edu.sep490.pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response for document upload operation")
public class DocumentUploadResponse {

    @Schema(description = "Success status", example = "true")
    private boolean success;

    @Schema(description = "Response message", example = "Upload quy định chấm điểm thành công")
    private String message;

    @Schema(description = "File name/ID in Gemini File Store", example = "files/abc123xyz")
    private String fileName;

    @Schema(description = "File URI for AI reference", example = "https://generativelanguage.googleapis.com/v1beta/files/abc123xyz")
    private String fileUri;

    @Schema(description = "File state: PROCESSING, ACTIVE, FAILED", example = "ACTIVE")
    private String state;

    @Schema(description = "Display name of the file", example = "Quy định chấm điểm hồ sơ sức khỏe")
    private String displayName;

    @Schema(description = "File size in bytes", example = "5775")
    private Long sizeBytes;

    @Schema(description = "File expiration time (ISO 8601)", example = "2026-03-11T10:30:00Z")
    private String expirationTime;
}

