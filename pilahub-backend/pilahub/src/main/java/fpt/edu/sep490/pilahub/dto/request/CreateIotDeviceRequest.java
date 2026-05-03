package fpt.edu.sep490.pilahub.dto.request;

import fpt.edu.sep490.pilahub.enums.IoTConnectionMethod;
import fpt.edu.sep490.pilahub.enums.IoTDeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new IoT device")
public record CreateIotDeviceRequest(
        @Schema(
                description = "Device name",
                example = "Apple Watch Series 8",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Device name must not be blank")
        @Size(max = 255, message = "Device name must not exceed 255 characters")
        String deviceName,

        @Schema(
                description = "IoT device type",
                example = "SMARTWATCH",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "IoT device type must not be null")
        IoTDeviceType iotDeviceType,

        @Schema(
                description = "Device identifier (MAC address, serial number, etc.)",
                example = "AB:CD:EF:12:34:56",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Device identifier must not be blank")
        @Size(max = 255, message = "Device identifier must not exceed 255 characters")
        String deviceIdentifier,

        @Schema(
                description = "Connection method",
                example = "BLUETOOTH",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Connection method must not be null")
        IoTConnectionMethod connectionMethod
) {
}
