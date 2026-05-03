package fpt.edu.sep490.pilahub.service.implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.edu.sep490.pilahub.dto.HealthProfileAssessmentDto;
import fpt.edu.sep490.pilahub.dto.request.AffectedBodyPartAIRequest;
import fpt.edu.sep490.pilahub.dto.request.HealthProfileAIRequest;
import fpt.edu.sep490.pilahub.dto.request.InjuryAIRequest;
import fpt.edu.sep490.pilahub.dto.response.HealthProfileAssessmentAIResponse;
import fpt.edu.sep490.pilahub.enums.InjuryStatus;
import fpt.edu.sep490.pilahub.exception.HealthProfileNotFoundException;
import fpt.edu.sep490.pilahub.mapper.HealthProfileAssessmentMapper;
import fpt.edu.sep490.pilahub.pojo.HealthProfile;
import fpt.edu.sep490.pilahub.pojo.HealthProfileAssessment;
import fpt.edu.sep490.pilahub.pojo.PersonalInjury;
import fpt.edu.sep490.pilahub.repository.HealthProfileAssessmentRepository;
import fpt.edu.sep490.pilahub.repository.HealthProfileRepository;
import fpt.edu.sep490.pilahub.repository.PersonalInjuryRepository;
import fpt.edu.sep490.pilahub.service.HealthProfileAssessmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthProfileAssessmentServiceImpl implements HealthProfileAssessmentService {

    private final HealthProfileAssessmentRepository assessmentRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final PersonalInjuryRepository personalInjuryRepository;
    private final HealthProfileAssessmentMapper assessmentMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    @Value("${ai.server.endpoint:/api/v1/health-assessment/assess}")
    private String aiAssessmentEndpoint;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public HealthProfileAssessmentDto createAssessment(HealthProfile healthProfile) {
        log.info("Creating assessment for health profile ID: {}", healthProfile.getHealthProfileId());

        try {
            // Get trainee's injuries
            List<PersonalInjury> injuries = personalInjuryRepository
                    .findByTraineeTraineeIdAndStatus(
                            healthProfile.getTrainee().getTraineeId(),
                            InjuryStatus.ACTIVE
                    );

            // Build AI request
            HealthProfileAIRequest aiRequest = buildAIRequest(healthProfile, injuries);

            // Call AI server
            HealthProfileAssessmentAIResponse aiResponse = callAIServer(aiRequest);

            // Save assessment
            HealthProfileAssessment assessment = HealthProfileAssessment.builder()
                    .healthProfile(healthProfile)
                    .score(aiResponse.score())
                    .healthProfileLevel(aiResponse.healthProfileLevel())
                    .highlights(aiResponse.highlights())
                    .risks(aiResponse.risks())
                    .explanations(aiResponse.explanations())
                    .recommendations(aiResponse.recommendations())
                    .confidenceScore(aiResponse.confidenceScore())
                    .aiModel(aiResponse.aiModel())
                    .build();

            HealthProfileAssessment savedAssessment = assessmentRepository.save(assessment);
            log.info("Assessment created successfully with ID: {}", savedAssessment.getHealthProfileAssessmentId());

            return assessmentMapper.toDto(savedAssessment);

        } catch (Exception e) {
            log.error("Failed to create assessment for health profile ID: {}", healthProfile.getHealthProfileId(), e);
            throw new RuntimeException("Failed to create assessment: " + e.getMessage(), e);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public HealthProfileAssessmentDto getAssessmentByHealthProfileId(UUID healthProfileId, UUID traineeId) {
        log.info("Fetching assessment for health profile ID: {} and trainee ID: {}", healthProfileId, traineeId);

        HealthProfile healthProfile = healthProfileRepository.findById(healthProfileId)
                .orElseThrow(() -> new HealthProfileNotFoundException("Health profile not found with ID: " + healthProfileId));

        // Check authorization
        if (!healthProfile.getTrainee().getTraineeId().equals(traineeId)) {
            log.error("Access denied: Health profile {} does not belong to trainee {}", healthProfileId, traineeId);
            throw new AccessDeniedException("You do not have permission to access this assessment");
        }

        HealthProfileAssessment assessment = assessmentRepository.findByHealthProfileId(healthProfileId)
                .orElseThrow(() -> new HealthProfileNotFoundException("Assessment not found for health profile ID: " + healthProfileId));

        return assessmentMapper.toDto(assessment);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteAssessmentByHealthProfileId(UUID healthProfileId) {
        log.info("Deleting assessment for health profile ID: {}", healthProfileId);
        assessmentRepository.deleteByHealthProfile_HealthProfileId(healthProfileId);
        log.info("Assessment deleted successfully for health profile ID: {}", healthProfileId);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private HealthProfileAIRequest buildAIRequest(HealthProfile healthProfile, List<PersonalInjury> injuries) {
        List<InjuryAIRequest> injuryRequests = injuries.stream()
                .map(this::mapToInjuryAIRequest)
                .collect(Collectors.toList());

        Map<String, Object> metaData = new HashMap<>();
        if (healthProfile.getMetadata() != null && !healthProfile.getMetadata().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsedMetadata = objectMapper.readValue(healthProfile.getMetadata(), Map.class);
                metaData = parsedMetadata;
            } catch (Exception e) {
                log.warn("Failed to parse metadata: {}", e.getMessage());
            }
        }

        return new HealthProfileAIRequest(
                healthProfile.getTrainee().getAge(),
                healthProfile.getTrainee().getGender(),
                healthProfile.getTrainee().getWorkoutLevel(),
                healthProfile.getTrainee().getWorkoutFrequency(),
                injuryRequests,
                healthProfile.getHeightCm(),
                healthProfile.getWeightKg(),
                healthProfile.getBmi(),
                healthProfile.getBodyFatPercentage(),
                healthProfile.getMuscleMassKg(),
                healthProfile.getWaistCm(),
                healthProfile.getHipCm(),
                healthProfile.getSource(),
                metaData
        );
    }

    private InjuryAIRequest mapToInjuryAIRequest(PersonalInjury personalInjury) {
        var injury = personalInjury.getInjury();

        List<AffectedBodyPartAIRequest> bodyParts = injury.getAffectedBodyParts().stream()
                .map(bp -> new AffectedBodyPartAIRequest(bp.getName(), bp.getDescription()))
                .collect(Collectors.toList());

        return new InjuryAIRequest(
                injury.getName(),
                injury.getDescription(),
                injury.getSymptoms(),
                injury.getCauses(),
                injury.getTreatmentSuggestions(),
                injury.getPreventionTips(),
                bodyParts,
                personalInjury.getStatus().name()
        );
    }

    private HealthProfileAssessmentAIResponse callAIServer(HealthProfileAIRequest request) {
        String url = aiServerUrl + aiAssessmentEndpoint;
        log.info("Calling AI server at: {} (this may take up to 90 seconds)", url);

        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<HealthProfileAIRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<HealthProfileAssessmentAIResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    HealthProfileAssessmentAIResponse.class
            );

            long duration = System.currentTimeMillis() - startTime;

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("AI server returned non-successful status: {} after {}ms", response.getStatusCode(), duration);
                throw new RuntimeException("Failed to get assessment from AI server. Status: " + response.getStatusCode());
            }

            log.info("AI server responded successfully in {}ms", duration);
            return response.getBody();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Failed to call AI server after {}ms. Error: {}", duration, e.getMessage());

            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                throw new RuntimeException("AI server timeout after " + duration + "ms. Please try again or check if AI server is running.", e);
            }

            throw new RuntimeException("Failed to call AI server: " + e.getMessage(), e);
        }
    }
}
