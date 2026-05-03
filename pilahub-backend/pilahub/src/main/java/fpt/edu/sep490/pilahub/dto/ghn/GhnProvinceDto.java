package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/** Province entry from GHN master-data. */
@Schema(description = "GHN province entry")
public record GhnProvinceDto(

        @Schema(description = "Province ID", example = "201")
        @JsonProperty("ProvinceID")
        Integer provinceId,

        @Schema(description = "Province name", example = "Hà Nội")
        @JsonProperty("ProvinceName")
        String provinceName,

        @Schema(description = "Province code", example = "HN")
        @JsonProperty("Code")
        String code
) {}
