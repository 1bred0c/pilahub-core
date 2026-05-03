package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.PackageType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Package {

    @Id
    @GeneratedValue
    @Column(name = "package_id", nullable = false, updatable = false)
    private UUID packageId;

    @NotBlank(message = "Package name must not be blank")
    @Size(max = 255)
    @Column(name = "package_name", nullable = false, length = 255)
    private String packageName;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0.00", message = "Price must be greater than or equal to 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Duration must not be null")
    @Min(value = 1, message = "Duration must be at least 1 day")
    @Column(name = "duration_in_days", nullable = false)
    private Integer durationInDays;

    @NotNull(message = "Package type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false, length = 20)
    private PackageType packageType;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

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
