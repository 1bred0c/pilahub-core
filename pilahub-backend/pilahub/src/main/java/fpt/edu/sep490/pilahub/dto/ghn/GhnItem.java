package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A single product line-item used in both fee-calculation and create-order
 * requests sent to the GHN API.
 */
@Schema(description = "Product item for GHN shipping operations")
public record GhnItem(

        @Schema(description = "Product / item name", example = "Áo Polo")
        @JsonProperty("name")
        String name,

        @Schema(description = "Internal SKU / product code", example = "SKU-001")
        @JsonProperty("code")
        String code,

        @Schema(description = "Quantity", example = "2")
        @JsonProperty("quantity")
        Integer quantity,

        @Schema(description = "Unit price (VND)", example = "200000")
        @JsonProperty("price")
        Integer price,

        @Schema(description = "Length in cm", example = "12")
        @JsonProperty("length")
        Integer length,

        @Schema(description = "Width in cm", example = "12")
        @JsonProperty("width")
        Integer width,

        @Schema(description = "Height in cm", example = "12")
        @JsonProperty("height")
        Integer height,

        @Schema(description = "Weight in grams", example = "1200")
        @JsonProperty("weight")
        Integer weight
) {}
