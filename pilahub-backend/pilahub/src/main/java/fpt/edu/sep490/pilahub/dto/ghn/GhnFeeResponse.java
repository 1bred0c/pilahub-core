package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * {@code data} payload from the GHN Calculate Fee response.
 */
@Schema(description = "GHN shipping fee calculation result")
public record GhnFeeResponse(

        @Schema(description = "Total shipping fee (VND)", example = "42200")
        @JsonProperty("total")
        Integer total,

        @Schema(description = "Base service fee (VND)", example = "42200")
        @JsonProperty("service_fee")
        Integer serviceFee,

        @Schema(description = "Insurance fee (VND)", example = "0")
        @JsonProperty("insurance_fee")
        Integer insuranceFee,

        @Schema(description = "Pickup fee (VND)", example = "0")
        @JsonProperty("pick_station_fee")
        Integer pickStationFee,

        @Schema(description = "Coupon discount (VND)", example = "0")
        @JsonProperty("coupon_value")
        Integer couponValue,

        @Schema(description = "R2S fee (VND)", example = "0")
        @JsonProperty("r2s_fee")
        Integer r2sFee
) {}
