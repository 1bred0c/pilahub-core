package pilahub.service;

import pilahub.dto.request.RoadmapAIRequest;
import pilahub.dto.response.RoadmapAIResponse;

public interface RoadmapAIService {
    /**
     * Generate roadmap using Gemini AI based on user profile and preferences
     */
    RoadmapAIResponse generateRoadmap(RoadmapAIRequest request);

    /**
     * Generate roadmap using Gemini AI with reference document
     * @param request User profile and preferences
     * @param documentUri URI of reference document in Gemini File Store (optional)
     */
    RoadmapAIResponse generateRoadmapWithDocument(RoadmapAIRequest request, String documentUri);

    /**
     * Build prompt for roadmap generation
     */
    String buildRoadmapPrompt(RoadmapAIRequest request);

    /**
     * Parse and validate roadmap response from AI
     */
    RoadmapAIResponse parseRoadmapResponse(String rawResponse);
}
