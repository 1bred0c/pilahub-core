package fpt.edu.sep490.pilahub.dto.request;

import fpt.edu.sep490.pilahub.enums.IoTConnectionMethod;
import fpt.edu.sep490.pilahub.enums.IoTDeviceType;
import fpt.edu.sep490.pilahub.enums.IoTStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update IoT device information")
public record UpdateIotDeviceRequest(
        @Schema(
                description = "Device name",
                example = "Apple Watch Series 9"
        )
        @Size(max = 255, message = "Device name must not exceed 255 characters")
        String deviceName,

        @Schema(
                description = "IoT device type",
                example = "SMARTWATCH"
        )
        IoTDeviceType iotDeviceType,

        @Schema(
                description = "Connection method",
                example = "WIFI"
        )
        IoTConnectionMethod connectionMethod,

        @Schema(
                description = "Device status",
                example = "CONNECTED"
        )
        IoTStatus status
) {
}
