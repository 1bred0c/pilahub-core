package pilahub.service;

import pilahub.dto.HealthProfileRequest;

public interface PromptBuilderService {
    /**
     * Builds a comprehensive prompt for health profile assessment
     * in Vietnamese language (Legacy - without file reference)
     */
    String buildHealthAssessmentPrompt(HealthProfileRequest request);

    /**
     * Builds a comprehensive prompt for health profile assessment
     * with file reference to scoring guideline in Gemini File Store
     */
    String buildHealthAssessmentPromptWithFile(HealthProfileRequest request, String fileUri);
}
