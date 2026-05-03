package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "course_lesson_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLessonProgress {

    @Id
    @GeneratedValue
    @Column(name = "progress_id", nullable = false, updatable = false)
    private UUID progressId;

    @NotNull(message = "Trainee course must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_course_id", nullable = false)
    private TraineeCourse traineeCourse;

    @NotNull(message = "Course lesson must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_lesson_id", nullable = false)
    private CourseLesson courseLesson;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private boolean completed = false;

}
