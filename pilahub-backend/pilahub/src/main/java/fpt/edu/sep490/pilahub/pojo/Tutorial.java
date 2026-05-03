package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tutorials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tutorial {

    @Id
    @GeneratedValue
    @Column(name = "tutorial_id", nullable = false, updatable = false)
    private UUID tutorialId;

    @NotNull(message = "Exercise must not be null")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false, unique = true)
    private Exercise exercise;

    @Size(max = 500)
    @Column(name = "practice_video_url", length = 500)
    private String practiceVideoUrl;

    @Size(max = 500)
    @Column(name = "theory_video_url", length = 500)
    private String theoryVideoUrl;

    @Size(max = 2000)
    @Column(name = "common_mistakes", length = 2000)
    private String commonMistakes;

    @Size(max = 2000)
    @Column(name = "guidelines", length = 2000)
    private String guidelines;

    @Size(max = 1000)
    @Column(name = "breathing_technique", length = 1000)
    private String breathingTechnique;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean published = false;

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
