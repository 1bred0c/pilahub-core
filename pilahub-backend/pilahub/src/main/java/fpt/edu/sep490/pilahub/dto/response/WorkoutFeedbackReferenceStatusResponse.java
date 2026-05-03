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
@Schema(description = "Response for workout feedback reference document status check")
public class WorkoutFeedbackReferenceStatusResponse {

    @Schema(description = "Whether an active workout feedback reference document exists", example = "true")
    private boolean hasActiveDocument;

    @Schema(description = "URI of active workout feedback reference document (if exists)",
            example = "https://generativelanguage.googleapis.com/v1beta/files/abc123")
    private String documentUri;

    @Schema(description = "Status message", example = "Tài liệu tham khảo workout feedback đang hoạt động")
    private String message;
}

