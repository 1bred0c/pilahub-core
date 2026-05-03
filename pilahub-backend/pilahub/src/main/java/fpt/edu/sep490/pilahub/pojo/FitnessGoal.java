package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "fitness_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FitnessGoal {

    @Id
    @GeneratedValue
    @Column(name = "goal_id", nullable = false, updatable = false)
    private UUID goalId;

    @NotBlank(message = "Fitness goal code must not be blank")
    @Size(max = 100)
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @NotBlank(message = "Vietnamese name must not be blank")
    @Size(max = 255)
    @Column(name = "vietnamese_name", nullable = false, length = 255)
    private String vietnameseName;

    @NotBlank(message = "Description must not be blank")
    @Size(max = 500)
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "fitness_goal_purposes",
            joinColumns = @JoinColumn(name = "goal_id"),
            inverseJoinColumns = @JoinColumn(name = "purpose_id")
    )
    @Builder.Default
    private Set<Purpose> relatedPurposes = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

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
