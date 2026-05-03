package pilahub.service;

import pilahub.dto.response.HealthProfileAssessmentResponse;

public interface ResponseFilterService {
    /**
     * Parse raw Gemini response and filter/validate the content
     */
    HealthProfileAssessmentResponse parseAndFilterResponse(String rawResponse);

    /**
     * Validate the parsed response
     */
    boolean validateResponse(HealthProfileAssessmentResponse response);

    /**
     * Clean and sanitize JSON string from Gemini response
     */
    String extractJsonFromResponse(String rawResponse);
}
