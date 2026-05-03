package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "personal_stage_supplements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalStageSupplement {

    @Id
    @GeneratedValue
    @Column(name = "personal_stage_supplement_id", nullable = false, updatable = false)
    private UUID personalStageSupplementId;

    @NotNull(message = "Personal stage must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_stage_id", nullable = false)
    private PersonalStage personalStage;

    @NotNull(message = "Supplement must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplement_id", nullable = false)
    private Supplement supplement;

    @Size(max = 255)
    @Column(name = "recommended_timing", length = 255)
    private String recommendedTiming;

    @Size(max = 100)
    @Column(name = "dosage", length = 100)
    private String dosage;

    @Size(max = 1000)
    @Column(name = "reason", length = 1000)
    private String reason;

    @Size(max = 20)
    @Column(name = "priority", length = 20)
    private String priority;

    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "is_optional", nullable = false)
    @Builder.Default
    private boolean optional = false;

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
