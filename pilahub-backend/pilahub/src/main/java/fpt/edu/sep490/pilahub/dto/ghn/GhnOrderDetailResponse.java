package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Key fields from the GHN Get Order Detail response.
 * Full endpoint: {@code GET /shiip/public-api/v2/shipping-order/detail?order_code=XXX}
 */
@Schema(description = "GHN order tracking detail")
public record GhnOrderDetailResponse(

        @Schema(description = "GHN order code", example = "XEYT4G")
        @JsonProperty("order_code")
        String orderCode,

        @Schema(description = "Current GHN status code", example = "picking")
        @JsonProperty("status")
        String status,

        @Schema(description = "Current GHN status name (human-readable)", example = "Đang lấy hàng")
        @JsonProperty("status_name")
        String statusName,

        @Schema(description = "Total shipping fee (VND)", example = "33000")
        @JsonProperty("total_fee")
        Integer totalFee,

        @Schema(description = "COD amount (VND)", example = "0")
        @JsonProperty("cod_amount")
        Integer codAmount,

        @Schema(description = "Weight in grams", example = "500")
        @JsonProperty("weight")
        Integer weight,

        @Schema(description = "Carrier-level note")
        @JsonProperty("note")
        String note,

        @Schema(description = "Estimated delivery time")
        @JsonProperty("leadtime")
        String leadtime,

        @Schema(description = "Recipient name", example = "Nguyễn Văn A")
        @JsonProperty("to_name")
        String toName,

        @Schema(description = "Recipient phone", example = "0987654321")
        @JsonProperty("to_phone")
        String toPhone,

        @Schema(description = "Recipient address", example = "456 Nguyễn Trãi")
        @JsonProperty("to_address")
        String toAddress,

        @Schema(description = "Tracking log entries")
        @JsonProperty("log")
        List<GhnLogEntry> log
) {
    /**
     * A single entry in the GHN tracking log.
     */
    @Schema(description = "Tracking log entry")
    public record GhnLogEntry(
            @JsonProperty("status") String status,
            @JsonProperty("updated_date") String updatedDate
    ) {}
}
