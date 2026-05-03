package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workout_feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutFeedback {

    @Id
    @GeneratedValue
    @Column(name = "workout_feedback_id", nullable = false, updatable = false)
    private UUID workoutFeedbackId;

    @NotNull(message = "Workout session must not be null")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id", nullable = false, unique = true)
    private WorkoutSession workoutSession;

    @Min(value = 0, message = "Total mistakes must not be negative")
    @Column(name = "total_mistakes")
    private Integer totalMistakes;

    @DecimalMin(value = "0.0", message = "Form score must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Form score must be between 0 and 100")
    @Column(name = "form_score")
    private Double formScore;

    @DecimalMin(value = "0.0", message = "Endurance score must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Endurance score must be between 0 and 100")
    @Column(name = "endurance_score")
    private Double enduranceScore;

    @DecimalMin(value = "0.0", message = "Overall score must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Overall score must be between 0 and 100")
    @Column(name = "overall_score")
    private Double overallScore;

    @Size(max = 5000)
    @Column(name = "strengths", length = 5000)
    private String strengths;

    @Size(max = 5000)
    @Column(name = "weaknesses", length = 5000)
    private String weaknesses;

    @Size(max = 5000)
    @Column(name = "recommendations", length = 5000)
    private String recommendations;

    @Size(max = 100)
    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @NotNull
    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt;

    @PrePersist
    protected void onCreate() {
        this.generatedAt = Instant.now();
    }
}
