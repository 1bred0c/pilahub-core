package fpt.edu.sep490.pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgoraRecordingResponse {

    @JsonProperty("resourceId")
    private String resourceId;

    @JsonProperty("sid")
    private String sid;

    @JsonProperty("serverResponse")
    private ServerResponse serverResponse;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerResponse {
        @JsonProperty("fileListMode")
        private String fileListMode;

        @JsonProperty("fileList")
        private Object fileList;

        @JsonProperty("uploadingStatus")
        private String uploadingStatus;
    }
}

