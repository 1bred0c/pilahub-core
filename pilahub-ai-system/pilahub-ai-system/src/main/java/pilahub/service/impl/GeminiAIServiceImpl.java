package pilahub.service.impl;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pilahub.config.GeminiConfig;
import pilahub.dto.HealthProfileRequest;
import pilahub.dto.response.HealthProfileAssessmentResponse;
import pilahub.enums.AIModel;
import pilahub.service.GeminiAIService;
import pilahub.service.GeminiFileStoreService;
import pilahub.service.PromptBuilderService;
import pilahub.service.ResponseFilterService;
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAIServiceImpl implements GeminiAIService {

    private final Client geminiClient;
    private final GeminiConfig geminiConfig;
    private final PromptBuilderService promptBuilderService;
    private final ResponseFilterService responseFilterService;
    private final GeminiFileStoreService fileStoreService;

    @Override
    public String callGeminiAPI(String prompt) {
        try {
            log.info("Calling Gemini API with model: {}", geminiConfig.getModel());
            log.debug("Prompt length: {} characters", prompt.length());

            GenerateContentResponse response = geminiClient.models.generateContent(
                geminiConfig.getModel(),
                prompt,
                null
            );

            String responseText = response.text();
            log.info("Received response from Gemini API, length: {} characters", responseText.length());
            log.debug("Raw response: {}", responseText);

            return responseText;

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    @Override
    public HealthProfileAssessmentResponse assessHealthProfile(HealthProfileRequest request) {
        try {
            log.info("Starting health profile assessment for age: {}, gender: {}",
                    request.getAge(), request.getGender());

            // Step 1: Try to get file URI from File Store
            String fileUri = null;
            try {
                fileUri = fileStoreService.getActiveScoringGuidelineUri();
                if (fileUri != null) {
                    log.info("Using scoring guideline from File Store: {}", fileUri);
                } else {
                    log.warn("No active scoring guideline found, using legacy prompt");
                }
            } catch (Exception e) {
                log.warn("Error getting scoring guideline from File Store, using legacy prompt: {}", e.getMessage());
            }

            // Step 2: Build prompt (with or without file reference)
            String prompt;
            if (fileUri != null) {
                prompt = promptBuilderService.buildHealthAssessmentPromptWithFile(request, fileUri);
                log.debug("Built prompt with file reference, length: {} characters", prompt.length());
            } else {
                prompt = promptBuilderService.buildHealthAssessmentPrompt(request);
                log.debug("Built legacy prompt, length: {} characters", prompt.length());
            }

            // Step 3: Call Gemini API
            String rawResponse = callGeminiAPI(prompt);

            // Step 4: Parse and filter response
            HealthProfileAssessmentResponse assessment =
                responseFilterService.parseAndFilterResponse(rawResponse);

            // Step 5: Set metadata - use model from config
            AIModel aiModel = AIModel.fromModelName(geminiConfig.getModel());
            assessment.setAiModel(aiModel);

            log.info("Health profile assessment completed successfully. Score: {}, Level: {}",
                    assessment.getScore(), assessment.getHealthProfileLevel());

            return assessment;

        } catch (Exception e) {
            log.error("Error during health profile assessment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to assess health profile: " + e.getMessage(), e);
        }
    }
}
