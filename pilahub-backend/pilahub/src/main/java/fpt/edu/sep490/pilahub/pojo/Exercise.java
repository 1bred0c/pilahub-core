package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.BreathingRule;
import fpt.edu.sep490.pilahub.enums.DifficultyLevel;
import fpt.edu.sep490.pilahub.enums.ExerciseType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue
    @Column(name = "exercise_id", nullable = false, updatable = false)
    private UUID exerciseId;

    @NotBlank(message = "Exercise name must not be blank")
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "exercise_type", length = 100)
    @Enumerated(EnumType.STRING)
    private ExerciseType exerciseType;

    @Column(name = "difficulty_level", length = 50)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @ManyToMany
    @JoinTable(name = "exercise_body_parts", joinColumns = @JoinColumn(name = "exercise_id"), inverseJoinColumns = @JoinColumn(name = "body_part_id"))
    @Builder.Default
    private Set<BodyPart> bodyParts = new HashSet<>();

    @Column(name = "equipment_required", nullable = false)
    @Builder.Default
    private boolean equipmentRequired = true;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Size(max = 1000)
    @Column(name = "benefits", length = 1000)
    private String benefits;

    @Size(max = 500)
    @Column(name = "prerequisites", length = 500)
    private String prerequisites;

    @Size(max = 500)
    @Column(name = "contraindications", length = 500)
    private String contraindications;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = false;

    @Column(name = "have_ai_supported", nullable = false)
    @Builder.Default
    private boolean haveAIsupported = false;

    @Size(max = 255)
    @Column(name = "name_in_model_ai", length = 255)
    private String nameInModelAI;

    @Column(name = "breathing_rule", length = 50)
    @Enumerated(EnumType.STRING)
    private BreathingRule breathingRule;

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
