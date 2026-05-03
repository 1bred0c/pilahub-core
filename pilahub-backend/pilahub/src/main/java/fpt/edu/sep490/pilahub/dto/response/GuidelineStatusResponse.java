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
@Schema(description = "Response for scoring guideline status check")
public class GuidelineStatusResponse {

    @Schema(description = "Whether an active guideline exists", example = "true")
    private boolean hasActiveGuideline;

    @Schema(description = "URI of active guideline file (if exists)", example = "https://generativelanguage.googleapis.com/v1beta/files/abc123")
    private String guidelineUri;

    @Schema(description = "Status message", example = "File quy định chấm điểm đang hoạt động")
    private String message;
}

