package pilahub.service;

import pilahub.dto.request.RoadmapReviewAIRequest;
import pilahub.dto.response.RoadmapReviewAIResponse;

public interface RoadmapReviewService {
    /**
     * Analyze roadmap completion results and return structured review.
     */
    RoadmapReviewAIResponse reviewRoadmap(RoadmapReviewAIRequest request);

    /**
     * Build prompt for roadmap review analysis.
     */
    String buildRoadmapReviewPrompt(RoadmapReviewAIRequest request, String documentUri);

    /**
     * Parse AI response into structured review.
     */
    RoadmapReviewAIResponse parseRoadmapReviewResponse(String rawResponse);
}

