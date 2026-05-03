package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bank information from VietQR API")
public record BankInfoDto(
        @Schema(description = "Bank code (app ID)", example = "bidv")
        String bankCode,

        @Schema(description = "Bank name", example = "Ngân hàng TMCP Đầu tư và Phát triển Việt Nam")
        String bankName,

        @Schema(description = "Bank logo URL", example = "https://example.com/logo.png")
        String bankLogo
) {
}
