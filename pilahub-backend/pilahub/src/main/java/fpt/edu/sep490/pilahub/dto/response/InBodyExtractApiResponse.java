package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "InBody extraction response returned by AI server")
public record InBodyExtractApiResponse(
        @Schema(description = "Response status", example = "success")
        String status,

        @Schema(description = "Extracted data payload")
        InBodyExtractData data,

        @Schema(description = "Response message", example = "Extracted successfully from InBody 270")
        String message
) {
}

