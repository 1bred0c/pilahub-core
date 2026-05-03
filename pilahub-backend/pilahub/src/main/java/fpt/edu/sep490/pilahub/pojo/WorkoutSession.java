package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workout_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Check(constraints = "(CASE WHEN personal_exercise_id IS NOT NULL THEN 1 ELSE 0 END + " +
                     "CASE WHEN lesson_exercise_progress_id IS NOT NULL THEN 1 ELSE 0 END) <= 1")
public class WorkoutSession {

    @Id
    @GeneratedValue
    @Column(name = "workout_session_id", nullable = false, updatable = false)
    private UUID workoutSessionId;

    @NotNull(message = "Trainee must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_exercise_id")
    private PersonalExercise personalExercise;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_exercise_progress_id")
    private LessonExerciseProgress lessonExerciseProgress;

    @NotNull(message = "Exercise must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "have_ai_tracking", nullable = false)
    @Builder.Default
    private boolean haveAITracking = false;

    @Column(name = "have_iot_device_tracking", nullable = false)
    @Builder.Default
    private boolean haveIOTDeviceTracking = false;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "duration_seconds")
    private Double durationSeconds;

    @Size(max = 1000)
    @Column(name = "record_url", length = 1000)
    private String recordUrl;

    @Column(name = "is_record_available", nullable = false)
    @Builder.Default
    private boolean recordAvailable = false;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private boolean completed = false;

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
