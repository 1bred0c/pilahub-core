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
@Schema(description = "Gemini file information")
public class GeminiFileInfo {

    @Schema(description = "File name/ID", example = "files/abc123")
    private String name;

    @Schema(description = "Display name", example = "Quy định chấm điểm hồ sơ sức khỏe")
    private String displayName;

    @Schema(description = "MIME type", example = "text/markdown")
    private String mimeType;

    @Schema(description = "File size in bytes", example = "5775")
    private String sizeBytes;

    @Schema(description = "File state: PROCESSING, ACTIVE, FAILED", example = "ACTIVE")
    private String state;

    @Schema(description = "Creation time (ISO 8601)", example = "2026-03-09T10:00:00Z")
    private String createTime;

    @Schema(description = "Last update time (ISO 8601)", example = "2026-03-09T10:00:05Z")
    private String updateTime;

    @Schema(description = "Expiration time (ISO 8601)", example = "2026-03-11T10:00:00Z")
    private String expirationTime;

    @Schema(description = "File URI", example = "https://generativelanguage.googleapis.com/v1beta/files/abc123")
    private String uri;

    @Schema(description = "SHA-256 hash of file content")
    private String sha256Hash;
}

