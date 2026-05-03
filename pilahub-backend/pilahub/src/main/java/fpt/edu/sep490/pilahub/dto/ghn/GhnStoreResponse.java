package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A single store entry returned by the GHN store endpoints:
 * <ul>
 * <li>{@code POST /shiip/public-api/v2/shop/register} — {@code data._id} is the
 * new shop ID</li>
 * <li>{@code GET  /shiip/public-api/v2/shop/all} — each element in
 * {@code data.shops}</li>
 * </ul>
 * The field {@code _id} is what GHN calls the <em>Shop ID</em> and is the value
 * that must be used in the {@code ShopId} request header for order operations.
 */
@Schema(description = "GHN store / shop information")
@JsonIgnoreProperties(ignoreUnknown = true)
public record GhnStoreResponse(

                @Schema(description = "GHN Shop ID — persist this as Vendor.ghnShopId", example = "4933035") @JsonProperty("_id") Integer id,

                @Schema(description = "Store name", example = "Shop ABC") @JsonProperty("name") String name,

                @Schema(description = "Contact phone", example = "0901234567") @JsonProperty("phone") String phone,

                @Schema(description = "Street address", example = "123 Lê Lợi") @JsonProperty("address") String address,

                @Schema(description = "Ward code", example = "20308") @JsonProperty("ward_code") String wardCode,

                @Schema(description = "District ID", example = "1450") @JsonProperty("district_id") Integer districtId,

                @Schema(description = "GHN client/platform ID this store belongs to") @JsonProperty("client_id") Integer clientId,

                @Schema(description = "Store status: 1=active, 2=inactive", example = "1") @JsonProperty("status") Integer status,

                @Schema(description = "Version / last-updated marker from GHN") @JsonProperty("version_no") String versionNo) {
}
