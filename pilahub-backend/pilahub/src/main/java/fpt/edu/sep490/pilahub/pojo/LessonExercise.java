package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lesson_exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonExercise {

    @Id
    @GeneratedValue
    @Column(name = "lesson_exercise_id", nullable = false, updatable = false)
    private UUID lessonExerciseId;

    @NotNull(message = "Lesson must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

     @NotNull(message = "Exercise must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Min(value = 1, message = "Order must be at least 1")
    @Column(name = "display_order")
    private Integer displayOrder;

    @Min(value = 1, message = "Sets must be at least 1")
    @Column(name = "sets")
    private Integer sets;

    @Min(value = 1, message = "Reps must be at least 1")
    @Column(name = "reps")
    private Integer reps;

    @Min(value = 0, message = "Duration must be at least 0")
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Min(value = 0, message = "Rest time must be at least 0")
    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;

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
