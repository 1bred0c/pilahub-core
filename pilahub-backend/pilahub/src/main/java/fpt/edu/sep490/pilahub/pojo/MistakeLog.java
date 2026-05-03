package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mistake_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MistakeLog {

    @Id
    @GeneratedValue
    @Column(name = "mistake_log_id", nullable = false, updatable = false)
    private UUID mistakeLogId;

    @NotNull(message = "Workout session must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id", nullable = false)
    private WorkoutSession workoutSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "body_part_id")
    private BodyPart bodyPart;

    @Size(max = 1000)
    @Column(name = "details", length = 1000)
    private String details;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "recorded_at_second")
    private Double recordedAtSecond;

    @Column(name = "duration")
    private Double duration;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
