package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.Gender;
import fpt.edu.sep490.pilahub.enums.WorkoutFrequency;
import fpt.edu.sep490.pilahub.enums.WorkoutLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trainees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trainee {

    @Id
    @Column(name = "trainee_id", nullable = false, updatable = false)
    private UUID traineeId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "trainee_id")
    private Account account;

    @NotBlank(message = "Full name must not be blank")
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @NotNull(message = "Age must not be null")
    @Min(value = 1, message = "Age must be at least 1")
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

    @NotNull(message = "Workout level must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "workout_level", nullable = false, length = 30)
    private WorkoutLevel workoutLevel;

    @NotNull(message = "Workout frequency must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "workout_frequency", nullable = false, length = 30)
    private WorkoutFrequency workoutFrequency;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
