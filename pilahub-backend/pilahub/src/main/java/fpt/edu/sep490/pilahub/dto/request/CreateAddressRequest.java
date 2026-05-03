package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request to create a new address")
public record CreateAddressRequest(
        @Schema(
                description = "Receiver's full name",
                example = "John Doe",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Receiver name must not be blank")
        @Size(max = 255, message = "Receiver name must not exceed 255 characters")
        String receiverName,

        @Schema(
                description = "Receiver's phone number",
                example = "+84901234567",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Receiver phone must not be blank")
        @Pattern(
                regexp = "^\\+?[0-9]{9,15}$",
                message = "Receiver phone format is invalid"
        )
        String receiverPhone,

        @Schema(
                description = "Full address line",
                example = "123 Main Street, Apartment 4B",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Address line must not be blank")
        @Size(max = 500, message = "Address line must not exceed 500 characters")
        String addressLine,

        @Schema(
                description = "Province",
                example = "Ho Chi Minh City",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Province must not be blank")
        @Size(max = 100, message = "Province must not exceed 100 characters")
        String province,

        @Schema(
                description = "City",
                example = "Ho Chi Minh City",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "City must not be blank")
        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @Schema(
                description = "District",
                example = "District 1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "District must not be blank")
        @Size(max = 100, message = "District must not exceed 100 characters")
        String district,

        @Schema(
                description = "Ward",
                example = "Ben Nghe Ward",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Ward must not be blank")
        @Size(max = 100, message = "Ward must not exceed 100 characters")
        String ward,

        @Schema(
                description = "Set as default address",
                example = "false"
        )
        Boolean isDefault
) {
}
