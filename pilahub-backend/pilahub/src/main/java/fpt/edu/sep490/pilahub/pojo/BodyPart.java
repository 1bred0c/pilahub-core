package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "body_parts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BodyPart {

    @Id
    @GeneratedValue
    @Column(name = "body_part_id", nullable = false, updatable = false)
    private UUID bodyPartId;

    @NotBlank(message = "Body part name must not be blank")
    @Size(max = 100)
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @ManyToMany(mappedBy = "bodyParts")
    @Builder.Default
    private Set<Exercise> exercises = new HashSet<>();

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
