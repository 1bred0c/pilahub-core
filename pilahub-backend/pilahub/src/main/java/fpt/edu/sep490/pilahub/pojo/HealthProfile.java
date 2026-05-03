package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.ProfileSource;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "health_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthProfile {

    @Id
    @GeneratedValue
    @Column(name = "health_profile_id", nullable = false, updatable = false)
    private UUID healthProfileId;

    @NotNull(message = "Trainee must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than 0")
    @DecimalMax(value = "300.0", message = "Height must not exceed 300 cm")
    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    @DecimalMax(value = "500.0", message = "Weight must not exceed 500 kg")
    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @DecimalMin(value = "0.0", message = "BMI must not be negative")
    @DecimalMax(value = "100.0", message = "BMI must not exceed 100")
    @Column(name = "bmi", precision = 4, scale = 2)
    private BigDecimal bmi;

    @DecimalMin(value = "0.0", message = "Body fat percentage must not be negative")
    @DecimalMax(value = "100.0", message = "Body fat percentage must not exceed 100")
    @Column(name = "body_fat_percentage", precision = 4, scale = 2)
    private BigDecimal bodyFatPercentage;

    @DecimalMin(value = "0.0", message = "Muscle mass must not be negative")
    @Column(name = "muscle_mass_kg", precision = 5, scale = 2)
    private BigDecimal muscleMassKg;

    @DecimalMin(value = "0.0", message = "Waist measurement must not be negative")
    @Column(name = "waist_cm", precision = 5, scale = 2)
    private BigDecimal waistCm;

    @DecimalMin(value = "0.0", message = "Hip measurement must not be negative")
    @Column(name = "hip_cm", precision = 5, scale = 2)
    private BigDecimal hipCm;

    @NotNull(message = "Profile source must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private ProfileSource source;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "is_latest", nullable = false)
    @Builder.Default
    private boolean isLatest = false;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
