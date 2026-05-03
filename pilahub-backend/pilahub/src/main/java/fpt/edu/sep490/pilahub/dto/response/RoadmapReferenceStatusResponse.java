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
@Schema(description = "Response for roadmap reference document status check")
public class RoadmapReferenceStatusResponse {

    @Schema(description = "Whether an active roadmap reference document exists", example = "true")
    private boolean hasActiveDocument;

    @Schema(description = "URI of active roadmap reference document (if exists)",
            example = "https://generativelanguage.googleapis.com/v1beta/files/abc123")
    private String documentUri;

    @Schema(description = "Status message", example = "Tài liệu tham khảo roadmap đang hoạt động")
    private String message;
}
