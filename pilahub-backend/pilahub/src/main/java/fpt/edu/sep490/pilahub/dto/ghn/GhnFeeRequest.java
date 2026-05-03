package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request body sent to the GHN <em>Calculate Fee</em> API endpoint:
 * {@code POST /shiip/public-api/v2/shipping-order/fee}
 * <p>
 * Provide either {@code serviceId} (fixed service) <b>or</b> {@code serviceTypeId}
 * (GHN picks the matching service automatically).
 */
@Schema(description = "Request to calculate GHN shipping fee")
public record GhnFeeRequest(

        @Schema(description = "Specific GHN service ID (leave null to use serviceTypeId)", example = "53320")
        @JsonProperty("service_id")
        Integer serviceId,

        /**
         * 2 = Standard (GHN), 5 = Express (GHN Express).
         * Required when {@code serviceId} is null.
         */
        @Schema(description = "Service type: 2=Standard, 5=Express", example = "2")
        @JsonProperty("service_type_id")
        Integer serviceTypeId,

        @Schema(description = "District ID of sender (from GHN master data)", example = "1482")
        @NotNull
        @JsonProperty("from_district_id")
        Integer fromDistrictId,

        @Schema(description = "Ward code of sender (from GHN master data)", example = "1A0606")
        @JsonProperty("from_ward_code")
        String fromWardCode,

        @Schema(description = "District ID of recipient (from GHN master data)", example = "1820")
        @NotNull
        @JsonProperty("to_district_id")
        Integer toDistrictId,

        @Schema(description = "Ward code of recipient (from GHN master data)", example = "030112")
        @NotNull
        @JsonProperty("to_ward_code")
        String toWardCode,

        @Schema(description = "Package height in cm", example = "15")
        @NotNull @Min(1)
        @JsonProperty("height")
        Integer height,

        @Schema(description = "Package length in cm", example = "15")
        @NotNull @Min(1)
        @JsonProperty("length")
        Integer length,

        @Schema(description = "Package width in cm", example = "15")
        @NotNull @Min(1)
        @JsonProperty("width")
        Integer width,

        @Schema(description = "Package weight in grams", example = "1000")
        @NotNull @Min(1)
        @JsonProperty("weight")
        Integer weight,

        @Schema(description = "Declared value for insurance (VND)", example = "500000")
        @JsonProperty("insurance_value")
        Integer insuranceValue,

        @Schema(description = "COD failure surcharge (VND)", example = "0")
        @JsonProperty("cod_fail_amount")
        Integer codFailAmount,

        @Schema(description = "Coupon code, if any")
        @JsonProperty("coupon")
        String coupon,

        @Schema(description = "Items in the package")
        @Valid
        @JsonProperty("items")
        List<GhnItem> items
) {}
