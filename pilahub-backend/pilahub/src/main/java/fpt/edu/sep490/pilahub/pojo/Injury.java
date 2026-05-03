package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "injuries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Injury {

    @Id
    @GeneratedValue
    @Column(name = "injury_id", nullable = false, updatable = false)
    private UUID injuryId;

    @NotBlank(message = "Injury name must not be blank")
    @Size(max = 255)
    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Size(max = 500)
    @Column(name = "symptoms", length = 500)
    private String symptoms;

    @Size(max = 500)
    @Column(name = "causes", length = 500)
    private String causes;

    @Size(max = 1000)
    @Column(name = "treatment_suggestions", length = 1000)
    private String treatmentSuggestions;

    @Size(max = 1000)
    @Column(name = "prevention_tips", length = 1000)
    private String preventionTips;

    @ManyToMany
    @JoinTable(
        name = "injury_body_parts",
        joinColumns = @JoinColumn(name = "injury_id"),
        inverseJoinColumns = @JoinColumn(name = "body_part_id")
    )
    @Builder.Default
    private Set<BodyPart> affectedBodyParts = new HashSet<>();

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
