package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.RoadmapStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roadmaps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roadmap {

    @Id
    @GeneratedValue
    @Column(name = "roadmap_id", nullable = false, updatable = false)
    private UUID roadmapId;

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Min(value = 0, message = "Progress percent must be at least 0")
    @Max(value = 100, message = "Progress percent must not exceed 100")
    @Column(name = "progress_percent")
    @Builder.Default
    private Integer progressPercent = 0;

    @Size(max = 255)
    @Column(name = "source", length = 255)
    private String source;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoadmapStatus status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id")
    private Coach coach;

    @Column(name = "initial_health_profile_id")
    private UUID initialHealthProfileId;

    @Column(name = "final_health_profile_id")
    private UUID finalHealthProfileId;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RoadmapGoal> roadmapGoals = new ArrayList<>();

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
