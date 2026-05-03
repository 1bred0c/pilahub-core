package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/** Ward entry from GHN master-data. */
@Schema(description = "GHN ward entry")
public record GhnWardDto(

        @Schema(description = "Ward code", example = "030112")
        @JsonProperty("WardCode")
        String wardCode,

        @Schema(description = "District ID this ward belongs to", example = "1820")
        @JsonProperty("DistrictID")
        Integer districtId,

        @Schema(description = "Ward name", example = "Phường 12")
        @JsonProperty("WardName")
        String wardName
) {}
