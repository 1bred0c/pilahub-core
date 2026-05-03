package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Vendor information")
public record VendorDto(
        @Schema(description = "Unique vendor identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID vendorId,

        @Schema(description = "Business name", example = "Pilates Equipment Pro")
        String businessName,

        @Schema(description = "Business logo URL", example = "https://example.com/logo.jpg")
        String logoUrl,

        @Schema(description = "Phone number", example = "+1234567890")
        String phoneNumber,

        @Schema(description = "Business address", example = "123 Main Street")
        String address,

        @Schema(description = "City", example = "New York")
        String city,

        @Schema(description = "Country", example = "USA")
        String country,

        @Schema(description = "Business license URL", example = "https://example.com/license.pdf")
        String businessLicenseUrl,

        @Schema(description = "Whether the vendor is verified", example = "true")
        boolean verified,

        @Schema(description = "Platform fee percentage charged by the platform", example = "10.0")
        Double platformFeePercentage,

        @Schema(description = "Number of days funds are held before release to vendor", example = "3")
        Integer holdingDays,

        @Schema(description = "Vendor creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}