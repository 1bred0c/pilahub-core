package fpt.edu.sep490.pilahub.pojo;

import com.fasterxml.jackson.databind.JsonNode;
import fpt.edu.sep490.pilahub.enums.AIModel;
import fpt.edu.sep490.pilahub.enums.HealthProfileLevel;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "health_profile_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthProfileAssessment {

    @Id
    @GeneratedValue
    @Column(name = "health_profile_assessment_id", nullable = false, updatable = false)
    private UUID healthProfileAssessmentId;

    @NotNull(message = "Health profile must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id", nullable = false)
    private HealthProfile healthProfile;

    @NotNull(message = "Score must not be null")
    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score must not exceed 100")
    @Column(name = "score", nullable = false)
    private Integer score;

    @NotNull(message = "Health profile level must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "health_profile_level", nullable = false, length = 30)
    private HealthProfileLevel healthProfileLevel;

    @Type(JsonType.class)
    @Column(name = "highlights", columnDefinition = "jsonb")
    private JsonNode highlights;

    @Type(JsonType.class)
    @Column(name = "risks", columnDefinition = "jsonb")
    private JsonNode risks;

    @Type(JsonType.class)
    @Column(name = "explanations", columnDefinition = "jsonb")
    private JsonNode explanations;

    @Type(JsonType.class)
    @Column(name = "recommendations", columnDefinition = "jsonb")
    private JsonNode recommendations;

    @DecimalMin(value = "0.0", message = "Confidence score must not be negative")
    @DecimalMax(value = "1.0", message = "Confidence score must not exceed 1.0")
    @Column(name = "confidence_score", precision = 4, scale = 3)
    private BigDecimal confidenceScore;

    @NotNull(message = "AI model must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_model", nullable = false, length = 50)
    private AIModel aiModel;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
