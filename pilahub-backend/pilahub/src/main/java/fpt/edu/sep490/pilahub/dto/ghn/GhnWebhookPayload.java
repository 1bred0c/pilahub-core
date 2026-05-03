package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Payload pushed by GHN to our webhook endpoint whenever a shipment's
 * status changes.
 * <p>
 * Webhook endpoint: {@code POST /api/ghn/webhook}
 */
@Schema(description = "GHN webhook status-update payload")
public record GhnWebhookPayload(

        /**
         * Client ID assigned by GHN at webhook registration.
         * Must match {@code ghn.client-id} in application config.
         */
        @Schema(description = "Client ID from GHN webhook registration", example = "ghnclient-abc123")
        @JsonProperty("ClientId")
        String clientId,

        @Schema(description = "GHN order code", example = "XEYT4G")
        @JsonProperty("OrderCode")
        String orderCode,

        @Schema(description = "New GHN status", example = "delivered")
        @JsonProperty("Status")
        String status,

        @Schema(description = "Timestamp of the event", example = "2025-08-12T10:47:39+07:00")
        @JsonProperty("Time")
        String time,

        @Schema(description = "Reason code for failed/cancelled deliveries")
        @JsonProperty("ReasonCode")
        String reasonCode,

        @Schema(description = "Human-readable reason")
        @JsonProperty("Reason")
        String reason,

        @Schema(description = "GHN Shop ID", example = "123456")
        @JsonProperty("ShopID")
        Integer shopId,

        @Schema(description = "Total fee charged (VND)", example = "23000")
        @JsonProperty("TotalFee")
        Integer totalFee,

        @Schema(description = "COD amount (VND)", example = "0")
        @JsonProperty("CODAmount")
        Integer codAmount
) {}
