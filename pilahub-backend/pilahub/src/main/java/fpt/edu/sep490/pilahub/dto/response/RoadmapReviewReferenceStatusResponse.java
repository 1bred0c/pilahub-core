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
@Schema(description = "Response for roadmap review reference document status check")
public class RoadmapReviewReferenceStatusResponse {

    @Schema(description = "Whether an active roadmap review reference document exists", example = "true")
    private boolean hasActiveDocument;

    @Schema(description = "URI of active roadmap review reference document (if exists)",
            example = "https://generativelanguage.googleapis.com/v1beta/files/abc123")
    private String documentUri;

    @Schema(description = "Status message", example = "Tài liệu tham khảo review roadmap đang hoạt động")
    private String message;
}

