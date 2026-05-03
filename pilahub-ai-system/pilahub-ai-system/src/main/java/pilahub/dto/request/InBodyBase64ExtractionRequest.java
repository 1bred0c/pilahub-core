package pilahub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for extracting InBody metrics from base64 image")
public class InBodyBase64ExtractionRequest {

    @NotBlank(message = "base64Image is required")
    @JsonProperty("base64Image")
    @Schema(description = "Base64-encoded image of the InBody sheet", requiredMode = Schema.RequiredMode.REQUIRED)
    private String base64Image;

    @JsonProperty("mimeType")
    @Schema(description = "Image MIME type", example = "image/jpeg")
    private String mimeType;

    @JsonProperty("rawScanId")
    @Schema(description = "Optional scan id from main server", example = "ai_sys_001")
    private String rawScanId;
}

