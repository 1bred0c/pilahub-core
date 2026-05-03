package pilahub.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pilahub.dto.response.HealthProfileAssessmentResponse;
import pilahub.dto.response.HighlightDTO;
import pilahub.dto.response.RecommendationsDTO;
import pilahub.dto.response.RiskDTO;
import pilahub.enums.HealthProfileLevel;
import pilahub.enums.RiskSeverity;
import pilahub.service.ResponseFilterService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResponseFilterServiceImpl implements ResponseFilterService {

    private final ObjectMapper objectMapper;

    @Override
    public HealthProfileAssessmentResponse parseAndFilterResponse(String rawResponse) {
        try {
            log.debug("Parsing raw response from Gemini");

            // Extract JSON from response
            String jsonString = extractJsonFromResponse(rawResponse);
            log.debug("Extracted JSON: {}", jsonString);

            // Parse JSON to JsonNode first for flexible handling
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            // Build response object
            HealthProfileAssessmentResponse response = HealthProfileAssessmentResponse.builder()
                .score(jsonNode.get("score").asInt())
                .healthProfileLevel(HealthProfileLevel.valueOf(jsonNode.get("healthProfileLevel").asText()))
                .highlights(parseHighlights(jsonNode.get("highlights")))
                .risks(parseRisks(jsonNode.get("risks")))
                .explanations(jsonNode.get("explanations"))
                .recommendations(parseRecommendations(jsonNode.get("recommendations")))
                .confidenceScore(BigDecimal.valueOf(jsonNode.get("confidenceScore").asDouble()))
                .assessedAt(Instant.now())
                .build();

            // Validate response
            if (!validateResponse(response)) {
                throw new IllegalArgumentException("Invalid response from AI");
            }

            log.info("Successfully parsed and validated AI response");
            return response;

        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractJsonFromResponse(String rawResponse) {
        // Remove markdown code blocks if present
        String cleaned = rawResponse.trim();

        // Remove ```json and ``` markers
        cleaned = cleaned.replaceAll("^```json\\s*", "");
        cleaned = cleaned.replaceAll("^```\\s*", "");
        cleaned = cleaned.replaceAll("\\s*```$", "");

        // Find JSON object pattern
        Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(cleaned);

        if (matcher.find()) {
            return matcher.group();
        }

        // If no pattern match, return cleaned string
        return cleaned.trim();
    }

    @Override
    public boolean validateResponse(HealthProfileAssessmentResponse response) {
        if (response == null) {
            log.warn("Response is null");
            return false;
        }

        if (response.getScore() == null || response.getScore() < 0 || response.getScore() > 100) {
            log.warn("Invalid score: {}", response.getScore());
            return false;
        }

        if (response.getHealthProfileLevel() == null) {
            log.warn("Health profile level is null");
            return false;
        }

        if (response.getHighlights() == null || response.getHighlights().isEmpty()) {
            log.warn("Highlights are empty");
            return false;
        }

        if (response.getRecommendations() == null) {
            log.warn("Recommendations are null");
            return false;
        }

        if (response.getConfidenceScore() == null ||
            response.getConfidenceScore().compareTo(BigDecimal.ZERO) < 0 ||
            response.getConfidenceScore().compareTo(BigDecimal.ONE) > 0) {
            log.warn("Invalid confidence score: {}", response.getConfidenceScore());
            return false;
        }

        log.debug("Response validation passed");
        return true;
    }

    private List<HighlightDTO> parseHighlights(JsonNode highlightsNode) {
        try {
            return objectMapper.convertValue(
                highlightsNode,
                new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error parsing highlights: {}", e.getMessage());
            throw new RuntimeException("Failed to parse highlights", e);
        }
    }

    private List<RiskDTO> parseRisks(JsonNode risksNode) {
        try {
            List<RiskDTO> risks = objectMapper.convertValue(
                risksNode,
                new TypeReference<>() {}
            );

            // Validate and set default severity if needed
            risks.forEach(risk -> {
                if (risk.getSeverity() == null) {
                    risk.setSeverity(RiskSeverity.MODERATE);
                }
            });

            return risks;
        } catch (Exception e) {
            log.error("Error parsing risks: {}", e.getMessage());
            throw new RuntimeException("Failed to parse risks", e);
        }
    }

    private RecommendationsDTO parseRecommendations(JsonNode recommendationsNode) {
        try {
            return objectMapper.convertValue(
                recommendationsNode,
                RecommendationsDTO.class
            );
        } catch (Exception e) {
            log.error("Error parsing recommendations: {}", e.getMessage());
            throw new RuntimeException("Failed to parse recommendations", e);
        }
    }
}
