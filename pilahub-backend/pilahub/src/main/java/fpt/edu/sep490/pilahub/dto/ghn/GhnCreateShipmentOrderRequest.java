package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request body sent to the GHN <em>Create Order</em> endpoint:
 * {@code POST /shiip/public-api/v2/shipping-order/create}
 * <p>
 * All field names are serialised as snake_case via {@code @JsonProperty} to
 * match the GHN JSON contract exactly.
 */
@Schema(description = "Request to create a GHN shipping order for a shipment")
public record GhnCreateShipmentOrderRequest(

        // ── Sender (vendor) info ──────────────────────────────────────────────

        @Schema(description = "Sender full name", example = "Shop ABC")
        @JsonProperty("from_name")
        @NotBlank
        String fromName,

        @Schema(description = "Sender phone number", example = "0901234567")
        @JsonProperty("from_phone")
        @NotBlank
        String fromPhone,

        @Schema(description = "Sender street address", example = "123 Lê Lợi")
        @JsonProperty("from_address")
        @NotBlank
        String fromAddress,

        @Schema(description = "Sender ward name", example = "Phường Bến Nghé")
        @JsonProperty("from_ward_name")
        @NotBlank
        String fromWardName,

        @Schema(description = "Sender district name", example = "Quận 1")
        @JsonProperty("from_district_name")
        @NotBlank
        String fromDistrictName,

        @Schema(description = "Sender province / city name", example = "TP. Hồ Chí Minh")
        @JsonProperty("from_province_name")
        @NotBlank
        String fromProvinceName,

        // ── Recipient (customer) info ─────────────────────────────────────────

        @Schema(description = "Recipient full name", example = "Nguyễn Văn A")
        @JsonProperty("to_name")
        @NotBlank
        String toName,

        @Schema(description = "Recipient phone number", example = "0987654321")
        @JsonProperty("to_phone")
        @NotBlank
        String toPhone,

        @Schema(description = "Recipient street address", example = "456 Nguyễn Trãi")
        @JsonProperty("to_address")
        @NotBlank
        String toAddress,

        @Schema(description = "Recipient ward code (from GHN master data)", example = "030112")
        @JsonProperty("to_ward_code")
        @NotBlank
        String toWardCode,

        @Schema(description = "Recipient district ID (from GHN master data)", example = "1820")
        @JsonProperty("to_district_id")
        @NotNull
        Integer toDistrictId,

        // ── Shipment parameters ───────────────────────────────────────────────

        /**
         * 1 = seller pays shipping fee, 2 = buyer pays on delivery (COD-style).
         */
        @Schema(description = "Payment type: 1=seller pays, 2=buyer pays (COD)", example = "1")
        @JsonProperty("payment_type_id")
        @NotNull
        Integer paymentTypeId,

        /**
         * CHOTHUHANG | CHOXEMHANGKHONGTHU | KHONGCHOXEMHANG
         */
        @Schema(description = "Required note: CHOTHUHANG | CHOXEMHANGKHONGTHU | KHONGCHOXEMHANG",
                example = "CHOTHUHANG")
        @JsonProperty("required_note")
        @NotBlank
        String requiredNote,

        /**
         * Use either {@code serviceTypeId} (2=Standard, 5=Express) or
         * {@code serviceId} (specific ID from /available-services). If both are
         * provided, {@code serviceId} takes precedence.
         */
        @Schema(description = "Service type ID: 2=Standard, 5=Express", example = "2")
        @JsonProperty("service_type_id")
        Integer serviceTypeId,

        @Schema(description = "Specific service ID from GHN available-services endpoint", example = "53321")
        @JsonProperty("service_id")
        Integer serviceId,

        @Schema(description = "COD amount (VND). 0 if already pre-paid.", example = "0")
        @JsonProperty("cod_amount")
        Integer codAmount,

        @Schema(description = "Declared value for insurance (VND)", example = "500000")
        @JsonProperty("insurance_value")
        Integer insuranceValue,

        @Schema(description = "Package weight in grams", example = "500")
        @JsonProperty("weight")
        @NotNull @Min(1)
        Integer weight,

        @Schema(description = "Package length in cm", example = "20")
        @JsonProperty("length")
        @NotNull @Min(1)
        Integer length,

        @Schema(description = "Package width in cm", example = "15")
        @JsonProperty("width")
        @NotNull @Min(1)
        Integer width,

        @Schema(description = "Package height in cm", example = "10")
        @JsonProperty("height")
        @NotNull @Min(1)
        Integer height,

        @Schema(description = "Your internal order/shipment reference code stored on GHN", example = "ORDER-0001")
        @JsonProperty("client_order_code")
        String clientOrderCode,

        @Schema(description = "Description of package contents", example = "Áo thun, quần jeans")
        @JsonProperty("content")
        String content,

        @Schema(description = "Optional note for the carrier / warehouse", example = "Hàng dễ vỡ")
        @JsonProperty("note")
        String note,

        @Schema(description = "Items included in the package")
        @JsonProperty("items")
        @Valid
        @NotNull
        List<GhnItem> items
) {}
