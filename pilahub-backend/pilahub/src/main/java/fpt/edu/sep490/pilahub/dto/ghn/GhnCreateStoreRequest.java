package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body sent to the GHN <em>Register Store</em> endpoint:
 * {@code POST /shiip/public-api/v2/shop/register}
 * <p>
 * On success GHN returns a shop ID which is persisted as
 * {@code Vendor.ghnShopId} so every subsequent order call uses it in the
 * {@code ShopId} HTTP header.
 */
@Schema(description = "Request to register a vendor store on GHN")
public record GhnCreateStoreRequest(

        @Schema(description = "Store / shop name displayed on GHN", example = "Shop ABC", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("name")
        @NotBlank
        String name,

        @Schema(description = "Contact phone number", example = "0901234567", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("phone")
        @NotBlank
        String phone,

        @Schema(description = "Street address", example = "123 Lê Lợi", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("address")
        @NotBlank
        String address,

        @Schema(description = "Ward code from GHN master data", example = "20308", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("ward_code")
        @NotBlank
        String wardCode,

        @Schema(description = "District ID from GHN master data", example = "1450", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("district_id")
        @NotNull
        Integer districtId
) {}
