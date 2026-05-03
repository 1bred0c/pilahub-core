package pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) // Bỏ qua các trường lạ từ Gemini API
public class FileUploadResponse {

    @JsonProperty("file")
    private GeminiFileInfo file;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true) // Bỏ qua các trường lạ như "source"
    public static class GeminiFileInfo {
        private String name; // e.g., "files/abc123xyz"
        private String displayName;
        private String mimeType;
        private String sizeBytes;
        private String createTime;
        private String updateTime;
        private String expirationTime;
        private String sha256Hash;
        private String uri;
        private String state; // PROCESSING, ACTIVE, FAILED

        @JsonProperty("videoMetadata")
        private Object videoMetadata;

        @JsonProperty("error")
        private ErrorInfo error;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorInfo {
        private String message;
        private Integer code;
    }
}


