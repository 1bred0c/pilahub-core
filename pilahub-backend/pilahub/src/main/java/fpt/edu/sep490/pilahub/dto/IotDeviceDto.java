package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.IoTConnectionMethod;
import fpt.edu.sep490.pilahub.enums.IoTDeviceType;
import fpt.edu.sep490.pilahub.enums.IoTStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "IoT Device information")
public record IotDeviceDto(
        @Schema(description = "Unique IoT device identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID iotDeviceId,

        @Schema(description = "Device name", example = "Apple Watch Series 8")
        String deviceName,

        @Schema(description = "IoT device type", example = "SMARTWATCH")
        IoTDeviceType iotDeviceType,

        @Schema(description = "Device identifier (MAC address, serial number, etc.)", example = "AB:CD:EF:12:34:56")
        String deviceIdentifier,

        @Schema(description = "When device was connected", example = "2026-01-23T10:30:00Z")
        Instant connectedAt,

        @Schema(description = "Connection method", example = "BLUETOOTH")
        IoTConnectionMethod connectionMethod,

        @Schema(description = "Last sync timestamp", example = "2026-01-29T10:30:00Z")
        Instant lastSyncAt,

        @Schema(description = "Device status", example = "CONNECTED")
        IoTStatus status,

        @Schema(description = "Device creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-29T10:30:00Z")
        Instant updatedAt
) {
}
