package fpt.edu.sep490.pilahub.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/** Available GHN service between two districts. */
@Schema(description = "GHN available shipping service")
public record GhnAvailableServiceDto(

        @Schema(description = "Service ID", example = "53320")
        @JsonProperty("service_id")
        Integer serviceId,

        @Schema(description = "Short name of the service", example = "GHN Express")
        @JsonProperty("short_name")
        String shortName,

        @Schema(description = "Service type ID: 2=Standard, 5=Express", example = "2")
        @JsonProperty("service_type_id")
        Integer serviceTypeId
) {}
