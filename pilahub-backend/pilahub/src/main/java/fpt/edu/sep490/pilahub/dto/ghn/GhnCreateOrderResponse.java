package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * {@code data} payload from the GHN Create Order response.
 * The most important field is {@code orderCode}, which we persist as the
 * shipment's {@code trackingNumber}.
 */
@Schema(description = "GHN create-order result")
public record GhnCreateOrderResponse(

        @Schema(description = "GHN order code — use this as the tracking number", example = "XEYT4G")
        @JsonProperty("order_code")
        String orderCode,

        @Schema(description = "Sort code assigned by GHN", example = "SG-HCM-1232")
        @JsonProperty("sort_code")
        String sortCode,

        @Schema(description = "Transportation type", example = "truck")
        @JsonProperty("trans_type")
        String transType,

        @Schema(description = "Expected delivery time (ISO-8601)", example = "2025-08-12T10:47:39+07:00")
        @JsonProperty("expected_delivery_time")
        String expectedDeliveryTime,

        @Schema(description = "Total shipping fee charged by GHN (VND)", example = "33000")
        @JsonProperty("total_fee")
        String totalFee,

        @Schema(description = "Encoded ward info")
        @JsonProperty("ward_encode")
        String wardEncode,

        @Schema(description = "Encoded district info")
        @JsonProperty("district_encode")
        String districtEncode
) {}
