package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "personal_stages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalStage {

    @Id
    @GeneratedValue
    @Column(name = "personal_stage_id", nullable = false, updatable = false)
    private UUID personalStageId;

    @NotNull(message = "Roadmap must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    private Stage stage;

    @NotBlank(message = "Stage name must not be blank")
    @Size(max = 255)
    @Column(name = "stage_name", nullable = false, length = 255)
    private String stageName;

    @Size(max = 2000)
    @Column(name = "stage_description", length = 2000)
    private String stageDescription;

    @NotNull(message = "Stage order must not be null")
    @Min(value = 1, message = "Stage order must be at least 1")
    @Column(name = "stage_order", nullable = false)
    private Integer stageOrder;

    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

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
