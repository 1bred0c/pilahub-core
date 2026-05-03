package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Address information")
public record AddressDto(
        @Schema(description = "Unique address identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID addressId,

        @Schema(description = "Receiver's full name", example = "John Doe")
        String receiverName,

        @Schema(description = "Receiver's phone number", example = "+84901234567")
        String receiverPhone,

        @Schema(description = "Full address line", example = "123 Main Street, Apartment 4B")
        String addressLine,

        @Schema(description = "Province", example = "Ho Chi Minh City")
        String province,

        @Schema(description = "City", example = "Ho Chi Minh City")
        String city,

        @Schema(description = "District", example = "District 1")
        String district,

        @Schema(description = "Ward", example = "Ben Nghe Ward")
        String ward,

        @Schema(description = "Is this the default address", example = "true")
        boolean isDefault,

        @Schema(description = "Address creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Address last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}
