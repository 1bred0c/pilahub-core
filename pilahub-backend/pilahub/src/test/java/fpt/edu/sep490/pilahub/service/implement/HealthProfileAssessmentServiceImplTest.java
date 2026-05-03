package fpt.edu.sep490.pilahub.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import fpt.edu.sep490.pilahub.dto.HealthProfileAssessmentDto;
import fpt.edu.sep490.pilahub.dto.request.HealthProfileAIRequest;
import fpt.edu.sep490.pilahub.dto.response.HealthProfileAssessmentAIResponse;
import fpt.edu.sep490.pilahub.enums.AIModel;
import fpt.edu.sep490.pilahub.enums.Gender;
import fpt.edu.sep490.pilahub.enums.HealthProfileLevel;
import fpt.edu.sep490.pilahub.enums.InjuryStatus;
import fpt.edu.sep490.pilahub.enums.ProfileSource;
import fpt.edu.sep490.pilahub.enums.WorkoutFrequency;
import fpt.edu.sep490.pilahub.enums.WorkoutLevel;
import fpt.edu.sep490.pilahub.mapper.HealthProfileAssessmentMapper;
import fpt.edu.sep490.pilahub.pojo.BodyPart;
import fpt.edu.sep490.pilahub.pojo.HealthProfile;
import fpt.edu.sep490.pilahub.pojo.HealthProfileAssessment;
import fpt.edu.sep490.pilahub.pojo.Injury;
import fpt.edu.sep490.pilahub.pojo.PersonalInjury;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.repository.HealthProfileAssessmentRepository;
import fpt.edu.sep490.pilahub.repository.HealthProfileRepository;
import fpt.edu.sep490.pilahub.repository.PersonalInjuryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthProfileAssessmentServiceImplTest {

    @Mock
    private HealthProfileAssessmentRepository assessmentRepository;

    @Mock
    private HealthProfileRepository healthProfileRepository;

    @Mock
    private PersonalInjuryRepository personalInjuryRepository;

    @Mock
    private HealthProfileAssessmentMapper assessmentMapper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private HealthProfileAssessmentServiceImpl healthProfileAssessmentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(healthProfileAssessmentService, "aiServerUrl", "http://ai.test");
        ReflectionTestUtils.setField(healthProfileAssessmentService, "aiAssessmentEndpoint", "/assessment");
    }

    @Test
    void testUTCID01_CreateAssessmentSuccessfully_WithInjuriesAndValidMetadata() throws Exception {
        // Arrange
        HealthProfile healthProfile = buildHealthProfile("{\"device\":\"InBody\"}");
        PersonalInjury injury = buildPersonalInjury(InjuryStatus.ACTIVE);
        Map<String, Object> parsedMetadata = new HashMap<>();
        parsedMetadata.put("device", "InBody");

        HealthProfileAssessmentAIResponse aiResponse = buildAiResponse();
        HealthProfileAssessment savedAssessment = buildSavedAssessment(healthProfile, aiResponse);
        HealthProfileAssessmentDto expectedDto = buildExpectedDto(savedAssessment);

        when(personalInjuryRepository.findByTraineeTraineeIdAndStatus(
                healthProfile.getTrainee().getTraineeId(), InjuryStatus.ACTIVE))
                .thenReturn(List.of(injury));
        when(objectMapper.readValue(healthProfile.getMetadata(), Map.class)).thenReturn(parsedMetadata);
        when(restTemplate.postForEntity(
                eq("http://ai.test/assessment"),
                any(HttpEntity.class),
                eq(HealthProfileAssessmentAIResponse.class)))
                .thenReturn(ResponseEntity.ok(aiResponse));
        when(assessmentRepository.save(any(HealthProfileAssessment.class))).thenReturn(savedAssessment);
        when(assessmentMapper.toDto(savedAssessment)).thenReturn(expectedDto);

        // Act
        HealthProfileAssessmentDto actual = healthProfileAssessmentService.createAssessment(healthProfile);

        // Assert
        assertSame(expectedDto, actual);

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(
                eq("http://ai.test/assessment"),
                requestCaptor.capture(),
                eq(HealthProfileAssessmentAIResponse.class));

        HealthProfileAIRequest capturedRequest = (HealthProfileAIRequest) requestCaptor.getValue().getBody();
        assertNotNull(capturedRequest);
        assertEquals(healthProfile.getTrainee().getAge(), capturedRequest.age());
        assertEquals(1, capturedRequest.injuries().size());
        assertEquals("Lower Back", capturedRequest.injuries().get(0).affectedBodyParts().get(0).name());
        assertEquals("InBody", capturedRequest.metaData().get("device"));

        ArgumentCaptor<HealthProfileAssessment> assessmentCaptor = ArgumentCaptor.forClass(HealthProfileAssessment.class);
        verify(assessmentRepository).save(assessmentCaptor.capture());
        assertEquals(aiResponse.score(), assessmentCaptor.getValue().getScore());
        assertEquals(aiResponse.aiModel(), assessmentCaptor.getValue().getAiModel());

        verify(assessmentMapper).toDto(savedAssessment);
    }

    @Test
    void testUTCID02_CreateAssessmentSuccessfully_WithEmptyInjuriesAndNullMetadata() {
        // Arrange
        HealthProfile healthProfile = buildHealthProfile(null);
        HealthProfileAssessmentAIResponse aiResponse = buildAiResponse();
        HealthProfileAssessment savedAssessment = buildSavedAssessment(healthProfile, aiResponse);
        HealthProfileAssessmentDto expectedDto = buildExpectedDto(savedAssessment);

        when(personalInjuryRepository.findByTraineeTraineeIdAndStatus(
                healthProfile.getTrainee().getTraineeId(), InjuryStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(restTemplate.postForEntity(
                eq("http://ai.test/assessment"),
                any(HttpEntity.class),
                eq(HealthProfileAssessmentAIResponse.class)))
                .thenReturn(ResponseEntity.ok(aiResponse));
        when(assessmentRepository.save(any(HealthProfileAssessment.class))).thenReturn(savedAssessment);
        when(assessmentMapper.toDto(savedAssessment)).thenReturn(expectedDto);

        // Act
        HealthProfileAssessmentDto actual = healthProfileAssessmentService.createAssessment(healthProfile);

        // Assert
        assertSame(expectedDto, actual);

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(
                eq("http://ai.test/assessment"),
                requestCaptor.capture(),
                eq(HealthProfileAssessmentAIResponse.class));

        HealthProfileAIRequest capturedRequest = (HealthProfileAIRequest) requestCaptor.getValue().getBody();
        assertNotNull(capturedRequest);
        assertTrue(capturedRequest.injuries().isEmpty());
        assertTrue(capturedRequest.metaData().isEmpty());
    }

    @Test
    void testUTCID03_CreateAssessmentSuccessfully_WithInvalidMetadata_FallbackToEmptyMap() throws Exception {
        // Arrange
        HealthProfile healthProfile = buildHealthProfile("not-json");
        HealthProfileAssessmentAIResponse aiResponse = buildAiResponse();
        HealthProfileAssessment savedAssessment = buildSavedAssessment(healthProfile, aiResponse);
        HealthProfileAssessmentDto expectedDto = buildExpectedDto(savedAssessment);

        when(personalInjuryRepository.findByTraineeTraineeIdAndStatus(
                healthProfile.getTrainee().getTraineeId(), InjuryStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(objectMapper.readValue(healthProfile.getMetadata(), Map.class))
                .thenThrow(new RuntimeException("invalid json"));
        when(restTemplate.postForEntity(
                eq("http://ai.test/assessment"),
                any(HttpEntity.class),
                eq(HealthProfileAssessmentAIResponse.class)))
                .thenReturn(ResponseEntity.ok(aiResponse));
        when(assessmentRepository.save(any(HealthProfileAssessment.class))).thenReturn(savedAssessment);
        when(assessmentMapper.toDto(savedAssessment)).thenReturn(expectedDto);

        // Act
        HealthProfileAssessmentDto actual = healthProfileAssessmentService.createAssessment(healthProfile);

        // Assert
        assertSame(expectedDto, actual);

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(
                eq("http://ai.test/assessment"),
                requestCaptor.capture(),
                eq(HealthProfileAssessmentAIResponse.class));

        HealthProfileAIRequest capturedRequest = (HealthProfileAIRequest) requestCaptor.getValue().getBody();
        assertNotNull(capturedRequest);
        assertTrue(capturedRequest.metaData().isEmpty());
    }

    @Test
    void testUTCID04_CreateAssessment_WhenAiServerReturnsNon2xx_ThrowsWrappedRuntimeException() {
        // Arrange
        HealthProfile healthProfile = buildHealthProfile(null);
        when(personalInjuryRepository.findByTraineeTraineeIdAndStatus(
                healthProfile.getTrainee().getTraineeId(), InjuryStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(restTemplate.postForEntity(
                eq("http://ai.test/assessment"),
                any(HttpEntity.class),
                eq(HealthProfileAssessmentAIResponse.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> healthProfileAssessmentService.createAssessment(healthProfile));

        // Assert
        assertTrue(exception.getMessage().contains("Failed to create assessment:"));
        assertTrue(exception.getMessage().contains("Failed to get assessment from AI server"));
        verify(assessmentRepository, never()).save(any(HealthProfileAssessment.class));
        verify(assessmentMapper, never()).toDto(any(HealthProfileAssessment.class));
    }

    @Test
    void testUTCID05_CreateAssessment_WhenAiServerReturnsNullBody_ThrowsWrappedRuntimeException() {
        // Arrange
        HealthProfile healthProfile = buildHealthProfile(null);
        when(personalInjuryRepository.findByTraineeTraineeIdAndStatus(
                healthProfile.getTrainee().getTraineeId(), InjuryStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(restTemplate.postForEntity(
                eq("http://ai.test/assessment"),
                any(HttpEntity.class),
                eq(HealthProfileAssessmentAIResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> healthProfileAssessmentService.createAssessment(healthProfile));

        // Assert
        assertTrue(exception.getMessage().contains("Failed to create assessment:"));
        assertTrue(exception.getMessage().contains("Failed to get assessment from AI server"));
        verify(assessmentRepository, never()).save(any(HealthProfileAssessment.class));
        verify(assessmentMapper, never()).toDto(any(HealthProfileAssessment.class));
    }

    @Test
    void testUTCID06_CreateAssessment_WhenAiTimeout_ThrowsWrappedTimeoutException() {
        // Arrange
        HealthProfile healthProfile = buildHealthProfile(null);
        when(personalInjuryRepository.findByTraineeTraineeIdAndStatus(
                healthProfile.getTrainee().getTraineeId(), InjuryStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(restTemplate.postForEntity(
                eq("http://ai.test/assessment"),
                any(HttpEntity.class),
                eq(HealthProfileAssessmentAIResponse.class)))
                .thenThrow(new RuntimeException("connection timeout"));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> healthProfileAssessmentService.createAssessment(healthProfile));

        // Assert
        assertTrue(exception.getMessage().contains("Failed to create assessment:"));
        assertTrue(exception.getMessage().contains("AI server timeout"));
        verify(assessmentRepository, never()).save(any(HealthProfileAssessment.class));
        verify(assessmentMapper, never()).toDto(any(HealthProfileAssessment.class));
    }

    @Test
    void testUTCID07_CreateAssessment_WhenSaveAssessmentFails_ThrowsWrappedRuntimeException() {
        // Arrange
        HealthProfile healthProfile = buildHealthProfile(null);
        HealthProfileAssessmentAIResponse aiResponse = buildAiResponse();

        when(personalInjuryRepository.findByTraineeTraineeIdAndStatus(
                healthProfile.getTrainee().getTraineeId(), InjuryStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(restTemplate.postForEntity(
                eq("http://ai.test/assessment"),
                any(HttpEntity.class),
                eq(HealthProfileAssessmentAIResponse.class)))
                .thenReturn(ResponseEntity.ok(aiResponse));
        doThrow(new RuntimeException("db error"))
                .when(assessmentRepository).save(any(HealthProfileAssessment.class));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> healthProfileAssessmentService.createAssessment(healthProfile));

        // Assert
        assertTrue(exception.getMessage().contains("Failed to create assessment:"));
        assertTrue(exception.getMessage().contains("db error"));
        verify(assessmentMapper, never()).toDto(any(HealthProfileAssessment.class));
    }

    @Test
    void testUTCID08_CreateAssessment_WhenHealthProfileIsNull_ThrowsNullPointerException() {
        // Arrange - no setup needed

        // Act
        assertThrows(NullPointerException.class,
                () -> healthProfileAssessmentService.createAssessment(null));

        // Assert
        verify(personalInjuryRepository, never()).findByTraineeTraineeIdAndStatus(any(UUID.class), any(InjuryStatus.class));
        verify(restTemplate, never()).postForEntity(any(String.class), any(HttpEntity.class), eq(HealthProfileAssessmentAIResponse.class));
        verify(assessmentRepository, never()).save(any(HealthProfileAssessment.class));
        verify(assessmentMapper, never()).toDto(any(HealthProfileAssessment.class));
    }

    private HealthProfile buildHealthProfile(String metadata) {
        UUID traineeId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        Trainee trainee = Trainee.builder()
                .traineeId(traineeId)
                .age(27)
                .gender(Gender.MALE)
                .workoutLevel(WorkoutLevel.INTERMEDIATE)
                .workoutFrequency(WorkoutFrequency.MODERATE)
                .fullName("UTC Trainee")
                .createdAt(Instant.now())
                .build();

        return HealthProfile.builder()
                .healthProfileId(profileId)
                .trainee(trainee)
                .heightCm(BigDecimal.valueOf(175))
                .weightKg(BigDecimal.valueOf(72))
                .bmi(BigDecimal.valueOf(23.5))
                .bodyFatPercentage(BigDecimal.valueOf(17.2))
                .muscleMassKg(BigDecimal.valueOf(31.2))
                .waistCm(BigDecimal.valueOf(82))
                .hipCm(BigDecimal.valueOf(95))
                .source(ProfileSource.Manual)
                .metadata(metadata)
                .isLatest(true)
                .createdAt(Instant.now())
                .build();
    }

    private PersonalInjury buildPersonalInjury(InjuryStatus status) {
        BodyPart lowerBack = BodyPart.builder()
                .name("Lower Back")
                .description("Lumbar region")
                .build();

        Injury injury = Injury.builder()
                .name("Back Strain")
                .description("Lower back strain")
                .symptoms("Pain and stiffness")
                .causes("Overload")
                .treatmentSuggestions("Rest")
                .preventionTips("Warm-up")
                .affectedBodyParts(Set.of(lowerBack))
                .createdAt(Instant.now())
                .build();

        return PersonalInjury.builder()
                .injury(injury)
                .status(status)
                .createdAt(Instant.now())
                .build();
    }

    private HealthProfileAssessmentAIResponse buildAiResponse() {
        JsonNode highlights = JsonNodeFactory.instance.objectNode().put("summary", "Good progress");
        JsonNode risks = JsonNodeFactory.instance.objectNode().put("risk", "Low");
        JsonNode explanations = JsonNodeFactory.instance.objectNode().put("note", "Maintain consistency");
        JsonNode recommendations = JsonNodeFactory.instance.objectNode().put("action", "Increase protein");

        return new HealthProfileAssessmentAIResponse(
                82,
                HealthProfileLevel.GOOD,
                highlights,
                risks,
                explanations,
                recommendations,
                BigDecimal.valueOf(0.91),
                AIModel.GEMINI_2_5_FLASH_PREVIEW,
                Instant.now()
        );
    }

    private HealthProfileAssessment buildSavedAssessment(
            HealthProfile healthProfile,
            HealthProfileAssessmentAIResponse aiResponse
    ) {
        return HealthProfileAssessment.builder()
                .healthProfileAssessmentId(UUID.randomUUID())
                .healthProfile(healthProfile)
                .score(aiResponse.score())
                .healthProfileLevel(aiResponse.healthProfileLevel())
                .highlights(aiResponse.highlights())
                .risks(aiResponse.risks())
                .explanations(aiResponse.explanations())
                .recommendations(aiResponse.recommendations())
                .confidenceScore(aiResponse.confidenceScore())
                .aiModel(aiResponse.aiModel())
                .createdAt(Instant.now())
                .build();
    }

    private HealthProfileAssessmentDto buildExpectedDto(HealthProfileAssessment assessment) {
        return new HealthProfileAssessmentDto(
                assessment.getHealthProfileAssessmentId(),
                assessment.getHealthProfile().getHealthProfileId(),
                assessment.getScore(),
                assessment.getHealthProfileLevel(),
                assessment.getHighlights(),
                assessment.getRisks(),
                assessment.getExplanations(),
                assessment.getRecommendations(),
                assessment.getConfidenceScore(),
                assessment.getAiModel(),
                assessment.getCreatedAt()
        );
    }
}

