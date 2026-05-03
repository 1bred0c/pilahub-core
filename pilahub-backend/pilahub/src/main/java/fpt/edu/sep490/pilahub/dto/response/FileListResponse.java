package fpt.edu.sep490.pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response for file list operation")
public class FileListResponse {

    @Schema(description = "List of files in Gemini File Store")
    private List<GeminiFileInfo> files;

    @Schema(description = "Token for next page (if available)", example = "eyJwYWdlIjoyfQ==")
    private String nextPageToken;
}

