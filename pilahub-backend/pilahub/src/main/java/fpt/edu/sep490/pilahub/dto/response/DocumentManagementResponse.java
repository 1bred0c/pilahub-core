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
@Schema(description = "Response for document management operations (delete, etc.)")
public class DocumentManagementResponse {

    @Schema(description = "Success status", example = "true")
    private boolean success;

    @Schema(description = "Response message", example = "Xóa file thành công")
    private String message;

    @Schema(description = "File name/ID", example = "files/abc123xyz")
    private String fileName;
}

