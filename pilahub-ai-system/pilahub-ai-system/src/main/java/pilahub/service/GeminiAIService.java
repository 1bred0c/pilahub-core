package pilahub.service;

import pilahub.dto.HealthProfileRequest;
import pilahub.dto.response.HealthProfileAssessmentResponse;

public interface GeminiAIService {
    /**
     * Calls Gemini API to assess health profile
     */
    String callGeminiAPI(String prompt);

    /**
     * Process health profile assessment with Gemini AI
     */
    HealthProfileAssessmentResponse assessHealthProfile(HealthProfileRequest request);
}
