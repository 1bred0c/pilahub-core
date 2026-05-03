package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.IoTDeviceType;
import fpt.edu.sep490.pilahub.enums.IoTConnectionMethod;
import fpt.edu.sep490.pilahub.enums.IoTStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "iot_devices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_iot_device_identifier", columnNames = "device_identifier")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IotDevice {

    @Id
    @GeneratedValue
    @Column(name = "iot_device_id", nullable = false, updatable = false)
    private UUID iotDeviceId;

    @NotNull(message = "Trainee must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @NotBlank(message = "Device name must not be blank")
    @Size(max = 255)
    @Column(name = "device_name", nullable = false, length = 255)
    private String deviceName;

    @NotNull(message = "IoT device type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "iot_device_type", nullable = false, length = 50)
    private IoTDeviceType iotDeviceType;

    @NotBlank(message = "Device identifier must not be blank")
    @Size(max = 255)
    @Column(name = "device_identifier", nullable = false, unique = true, length = 255)
    private String deviceIdentifier;

    @Column(name = "connected_at")
    private Instant connectedAt;

    @NotNull(message = "Connection method must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "connection_method", nullable = false, length = 50)
    private IoTConnectionMethod connectionMethod;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @NotNull(message = "Status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private IoTStatus status = IoTStatus.DISCONNECTED;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
