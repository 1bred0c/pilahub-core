package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/** District entry from GHN master-data. */
@Schema(description = "GHN district entry")
public record GhnDistrictDto(

        @Schema(description = "District ID", example = "1482")
        @JsonProperty("DistrictID")
        Integer districtId,

        @Schema(description = "Province ID this district belongs to", example = "201")
        @JsonProperty("ProvinceID")
        Integer provinceId,

        @Schema(description = "District name", example = "Quận Hoàn Kiếm")
        @JsonProperty("DistrictName")
        String districtName,

        @Schema(description = "District code", example = "1B")
        @JsonProperty("Code")
        String code,

        @Schema(description = "District type", example = "2")
        @JsonProperty("Type")
        Integer type
) {}
