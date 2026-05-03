package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "coaches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coach {

    @Id
    @Column(name = "coach_id", nullable = false, updatable = false)
    private UUID coachId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "coach_id")
    private Account account;

    @NotBlank(message = "Full name must not be blank")
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @NotNull(message = "Age must not be null")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 150, message = "Age must not exceed 150")
    @Column(name = "age", nullable = false)
    private Integer age;

    @NotNull(message = "Gender must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    @Size(max = 500)
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Size(max = 2000)
    @Column(name = "bio", length = 2000)
    private String bio;

    @Min(value = 0, message = "Years of experience must not be negative")
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Size(max = 500)
    @Column(name = "specialization", length = 500)
    private String specialization;

    @Size(max = 500)
    @Column(name = "certifications_url", length = 500)
    private String certificationsUrl;

    @Min(value = 0, message = "Average rating must not be negative")
    @Max(value = 5, message = "Average rating must not exceed 5")
    @Column(name = "avg_rating")
    private Double avgRating;

    @NotNull(message = "Price per hour must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price per hour must be greater than 0")
    @Column(name = "price_per_hour", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePerHour;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
