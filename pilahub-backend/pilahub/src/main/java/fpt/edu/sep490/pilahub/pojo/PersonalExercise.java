package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "personal_exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalExercise {

    @Id
    @GeneratedValue
    @Column(name = "personal_exercise_id", nullable = false, updatable = false)
    private UUID personalExerciseId;

    @NotNull(message = "Personal schedule must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_schedule_id", nullable = false)
    private PersonalSchedule personalSchedule;

    @NotNull(message = "Exercise must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @NotNull(message = "Exercise order must not be null")
    @Min(value = 1, message = "Exercise order must be at least 1")
    @Column(name = "exercise_order", nullable = false)
    private Integer exerciseOrder;

    @Min(value = 1, message = "Sets must be at least 1")
    @Column(name = "sets")
    private Integer sets;

    @Min(value = 1, message = "Reps must be at least 1")
    @Column(name = "reps")
    private Integer reps;

    @Min(value = 1, message = "Duration must be at least 1")
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Min(value = 1, message = "Rest time must be at least 1")
    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(name = "completed_at")
    private Instant completedAt;

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
