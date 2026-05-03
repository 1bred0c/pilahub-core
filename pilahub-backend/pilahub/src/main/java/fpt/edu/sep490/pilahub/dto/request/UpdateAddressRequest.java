package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request to update address information")
public record UpdateAddressRequest(
        @Schema(
                description = "Receiver's full name",
                example = "John Doe"
        )
        @Size(max = 255, message = "Receiver name must not exceed 255 characters")
        String receiverName,

        @Schema(
                description = "Receiver's phone number",
                example = "+84901234567"
        )
        @Pattern(
                regexp = "^\\+?[0-9]{9,15}$",
                message = "Receiver phone format is invalid"
        )
        String receiverPhone,

        @Schema(
                description = "Full address line",
                example = "123 Main Street, Apartment 4B"
        )
        @Size(max = 500, message = "Address line must not exceed 500 characters")
        String addressLine,

        @Schema(
                description = "Province",
                example = "Ho Chi Minh City"
        )
        @Size(max = 100, message = "Province must not exceed 100 characters")
        String province,

        @Schema(
                description = "City",
                example = "Ho Chi Minh City"
        )
        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @Schema(
                description = "District",
                example = "District 1"
        )
        @Size(max = 100, message = "District must not exceed 100 characters")
        String district,

        @Schema(
                description = "Ward",
                example = "Ben Nghe Ward"
        )
        @Size(max = 100, message = "Ward must not exceed 100 characters")
        String ward,

        @Schema(
                description = "Set as default address",
                example = "false"
        )
        Boolean isDefault
) {
}
