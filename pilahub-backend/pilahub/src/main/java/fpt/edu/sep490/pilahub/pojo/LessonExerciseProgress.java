package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lesson_exercise_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonExerciseProgress {

    @Id
    @GeneratedValue
    @Column(name = "lesson_exercise_progress_id", nullable = false, updatable = false)
    private UUID lessonExerciseProgressId;

    @NotNull(message = "Course lesson progress must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_lesson_progress_id", nullable = false)
    private CourseLessonProgress courseLessonProgress;

    @NotNull(message = "Lesson exercise must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_exercise_id", nullable = false)
    private LessonExercise lessonExercise;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private boolean completed = false;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

