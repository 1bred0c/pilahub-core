package fpt.edu.sep490.pilahub.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.edu.sep490.pilahub.dto.RoadmapReviewDto;
import fpt.edu.sep490.pilahub.pojo.RoadmapReview;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RoadmapReviewMapper {

    private final ObjectMapper objectMapper;

    public RoadmapReviewDto toDto(RoadmapReview review) {
        Map<String, Integer> subScores = readJson(review.getSubScoresJson(), new TypeReference<>() {}, Collections.emptyMap());
        Map<String, RoadmapReviewDto.DeltaMetric> deltaMetrics = readJson(
                review.getDeltaMetricsJson(), new TypeReference<>() {}, Collections.emptyMap());
        List<RoadmapReviewDto.PrioritizedRecommendation> recommendations = readJson(
                review.getPrioritizedRecommendationsJson(), new TypeReference<>() {}, Collections.emptyList());

        return new RoadmapReviewDto(
                review.getRoadmapReviewId(),
                review.getRoadmap().getRoadmapId(),
                review.getOverallScore(),
                subScores,
                deltaMetrics,
                review.getNarrativeSummary(),
                recommendations,
                review.getConfidenceLevel(),
                review.getCreatedAt()
        );
    }

    private <T> T readJson(String json, TypeReference<T> typeReference, T defaultValue) {
        if (json == null || json.isBlank()) {
            return defaultValue;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

