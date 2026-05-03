package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "roadmap_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapReview {

    @Id
    @GeneratedValue
    @Column(name = "roadmap_review_id", nullable = false, updatable = false)
    private UUID roadmapReviewId;

    @NotNull(message = "Roadmap must not be null")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false, unique = true)
    private Roadmap roadmap;

    @Min(value = 0, message = "Overall score must be between 0 and 100")
    @Max(value = 100, message = "Overall score must be between 0 and 100")
    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(name = "sub_scores_json", columnDefinition = "TEXT")
    private String subScoresJson;

    @Column(name = "delta_metrics_json", columnDefinition = "TEXT")
    private String deltaMetricsJson;

    @Column(name = "narrative_summary", columnDefinition = "TEXT")
    private String narrativeSummary;

    @Column(name = "prioritized_recommendations_json", columnDefinition = "TEXT")
    private String prioritizedRecommendationsJson;

    @Min(value = 0, message = "Confidence level must be between 0 and 100")
    @Max(value = 100, message = "Confidence level must be between 0 and 100")
    @Column(name = "confidence_level")
    private Integer confidenceLevel;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

