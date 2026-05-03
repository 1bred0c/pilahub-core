package fpt.edu.sep490.pilahub.dto.request.vendor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request to create a new vendor profile")
public record CreateVendorRequest(
        @Schema(description = "Business name", example = "Pilates Equipment Pro", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Business name must not be blank")
        @Size(max = 255, message = "Business name must not exceed 255 characters")
        String businessName,

        @Schema(description = "Business logo URL", example = "https://example.com/logo.jpg")
        @Size(max = 500, message = "Logo URL must not exceed 500 characters")
        String logoUrl,

        @Schema(description = "Phone number", example = "+1234567890")
        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phoneNumber,

        @Schema(description = "Business address", example = "123 Main Street")
        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        @Schema(description = "City", example = "New York")
        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @Schema(description = "Country", example = "USA")
        @Size(max = 100, message = "Country must not exceed 100 characters")
        String country,

        @Schema(description = "Business license URL", example = "https://example.com/license.pdf")
        @Size(max = 500, message = "Business license URL must not exceed 500 characters")
        String businessLicenseUrl

) {
}