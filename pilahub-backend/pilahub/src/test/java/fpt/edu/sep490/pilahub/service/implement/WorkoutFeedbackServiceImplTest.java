package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.WorkoutFeedbackDto;
import fpt.edu.sep490.pilahub.dto.request.workout.WorkoutFeedbackAIRequest;
import fpt.edu.sep490.pilahub.dto.response.WorkoutFeedbackAIResponse;
import fpt.edu.sep490.pilahub.enums.InjuryStatus;
import fpt.edu.sep490.pilahub.enums.WorkoutLevel;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.WorkoutFeedbackMapper;
import fpt.edu.sep490.pilahub.pojo.BodyPart;
import fpt.edu.sep490.pilahub.pojo.Exercise;
import fpt.edu.sep490.pilahub.pojo.HeartRateLog;
import fpt.edu.sep490.pilahub.pojo.Injury;
import fpt.edu.sep490.pilahub.pojo.MistakeLog;
import fpt.edu.sep490.pilahub.pojo.PersonalInjury;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.pojo.WorkoutFeedback;
import fpt.edu.sep490.pilahub.pojo.WorkoutSession;
import fpt.edu.sep490.pilahub.repository.HeartRateLogRepository;
import fpt.edu.sep490.pilahub.repository.MistakeLogRepository;
import fpt.edu.sep490.pilahub.repository.PersonalInjuryRepository;
import fpt.edu.sep490.pilahub.repository.WorkoutFeedbackRepository;
import fpt.edu.sep490.pilahub.repository.WorkoutSessionRepository;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutFeedbackServiceImplTest {

    @Mock
    private WorkoutFeedbackRepository workoutFeedbackRepository;

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;

    @Mock
    private MistakeLogRepository mistakeLogRepository;

    @Mock
    private HeartRateLogRepository heartRateLogRepository;

    @Mock
    private PersonalInjuryRepository personalInjuryRepository;

    @Mock
    private WorkoutFeedbackMapper workoutFeedbackMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WorkoutFeedbackServiceImpl workoutFeedbackService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workoutFeedbackService, "aiServerUrl", "http://ai.test");
        ReflectionTestUtils.setField(workoutFeedbackService, "aiFeedbackEndpoint", "/workout-feedback");
    }

    @Test
    void testUTCID01_GenerateFeedbackSuccessfully_WithValidSession() {
        // Arrange
        UUID workoutSessionId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        WorkoutSession workoutSession = buildValidWorkoutSession(workoutSessionId, traineeId);

        WorkoutFeedbackAIResponse aiResponse = new WorkoutFeedbackAIResponse(
                4,
                87.5,
                82.0,
                85.0,
                "Good breathing rhythm",
                "Hip stability drops near end",
                "Control transitions and keep neutral spine",
                "pilahub-workout-analyzer-v1.0"
        );

        WorkoutFeedback savedFeedback = buildSavedFeedback(workoutSession, aiResponse);
        WorkoutFeedbackDto expectedDto = buildExpectedDto(savedFeedback);

        when(workoutSessionRepository.findById(workoutSessionId)).thenReturn(Optional.of(workoutSession));
        when(workoutFeedbackRepository.existsByWorkoutSession_WorkoutSessionId(workoutSessionId)).thenReturn(false);
        when(personalInjuryRepository.findByTraineeTraineeIdAndStatus(traineeId, InjuryStatus.ACTIVE))
                .thenReturn(List.of(buildPersonalInjury("Lower back strain")));
        when(heartRateLogRepository.findByWorkoutSession_WorkoutSessionIdOrderByRecordedAtAsc(workoutSessionId))
                .thenReturn(List.of(
                        HeartRateLog.builder().heartRate(120).build(),
                        HeartRateLog.builder().heartRate(140).build()
                ));
        when(mistakeLogRepository.findByWorkoutSession_WorkoutSessionIdOrderByRecordedAtSecondAsc(workoutSessionId))
                .thenReturn(List.of(
                        MistakeLog.builder()
                                .bodyPart(BodyPart.builder().name("Core").build())
                                .recordedAtSecond(20.0)
                                .duration(3.0)
                                .details("Core disengagement")
                                .build(),
                        MistakeLog.builder()
                                .bodyPart(BodyPart.builder().name("Core").build())
                                .recordedAtSecond(50.0)
                                .duration(2.0)
                                .details("Neck tension")
                                .build()
                ));
        when(restTemplate.postForEntity(
                eq("http://ai.test/workout-feedback"),
                any(HttpEntity.class),
                eq(WorkoutFeedbackAIResponse.class)
        )).thenReturn(ResponseEntity.ok(aiResponse));
        when(workoutFeedbackRepository.save(any(WorkoutFeedback.class))).thenReturn(savedFeedback);
        when(workoutFeedbackMapper.toDto(savedFeedback)).thenReturn(expectedDto);

        // Act
        WorkoutFeedbackDto actual = workoutFeedbackService.generateFeedback(workoutSessionId, traineeId);

        // Assert
        assertSame(expectedDto, actual);

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(
                eq("http://ai.test/workout-feedback"),
                requestCaptor.capture(),
                eq(WorkoutFeedbackAIResponse.class)
        );

        WorkoutFeedbackAIRequest requestBody = (WorkoutFeedbackAIRequest) requestCaptor.getValue().getBody();
        assertNotNull(requestBody);
        assertEquals(workoutSessionId.toString(), requestBody.workoutSessionId());
        assertEquals(traineeId.toString(), requestBody.traineeInfo().traineeId());
        assertEquals(130.0, requestBody.sessionMetrics().averageHeartRate());
        assertEquals(140, requestBody.sessionMetrics().maxHeartRate());
        assertEquals(2, requestBody.mistakeSummary().totalMistakes());
        assertEquals("Lower back strain", requestBody.traineeInfo().injuries().get(0));

        ArgumentCaptor<WorkoutFeedback> feedbackCaptor = ArgumentCaptor.forClass(WorkoutFeedback.class);
        verify(workoutFeedbackRepository).save(feedbackCaptor.capture());
        assertEquals(aiResponse.formScore(), feedbackCaptor.getValue().getFormScore());
        assertEquals(aiResponse.aiModel(), feedbackCaptor.getValue().getAiModel());

        verify(workoutFeedbackMapper).toDto(savedFeedback);
    }

    @Test
    void testUTCID02_GenerateFeedback_WhenWorkoutSessionNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        UUID workoutSessionId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        when(workoutSessionRepository.findById(workoutSessionId)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutFeedbackService.generateFeedback(workoutSessionId, traineeId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("WorkoutSession"));
        verify(workoutSessionRepository).findById(workoutSessionId);
        verifyNoInteractions(
                workoutFeedbackRepository,
                personalInjuryRepository,
                heartRateLogRepository,
                mistakeLogRepository,
                restTemplate,
                workoutFeedbackMapper
        );
    }

    @Test
    void testUTCID03_GenerateFeedback_WhenSessionBelongsToAnotherTrainee_ThrowsAccessDeniedException() {
        // Arrange
        UUID workoutSessionId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        WorkoutSession workoutSession = buildValidWorkoutSession(workoutSessionId, ownerId);

        when(workoutSessionRepository.findById(workoutSessionId)).thenReturn(Optional.of(workoutSession));

        // Act
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> workoutFeedbackService.generateFeedback(workoutSessionId, requesterId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("do not have permission"));
        verify(workoutFeedbackRepository, never()).existsByWorkoutSession_WorkoutSessionId(any(UUID.class));
        verifyNoInteractions(personalInjuryRepository, heartRateLogRepository, mistakeLogRepository, restTemplate, workoutFeedbackMapper);
    }

    @Test
    void testUTCID04_GenerateFeedback_WhenAITrackingDisabled_ThrowsIllegalStateException() {
        // Arrange
        UUID workoutSessionId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        WorkoutSession workoutSession = buildValidWorkoutSession(workoutSessionId, traineeId);
        workoutSession.setHaveAITracking(false);

        when(workoutSessionRepository.findById(workoutSessionId)).thenReturn(Optional.of(workoutSession));

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> workoutFeedbackService.generateFeedback(workoutSessionId, traineeId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("AI tracking must be enabled"));
        verify(workoutFeedbackRepository, never()).existsByWorkoutSession_WorkoutSessionId(any(UUID.class));
        verifyNoInteractions(personalInjuryRepository, heartRateLogRepository, mistakeLogRepository, restTemplate, workoutFeedbackMapper);
    }

    @Test
    void testUTCID05_GenerateFeedback_WhenSessionNotCompleted_ThrowsIllegalStateException() {
        // Arrange
        UUID workoutSessionId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        WorkoutSession workoutSession = buildValidWorkoutSession(workoutSessionId, traineeId);
        workoutSession.setCompleted(false);

        when(workoutSessionRepository.findById(workoutSessionId)).thenReturn(Optional.of(workoutSession));

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> workoutFeedbackService.generateFeedback(workoutSessionId, traineeId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("must be completed"));
        verify(workoutFeedbackRepository, never()).existsByWorkoutSession_WorkoutSessionId(any(UUID.class));
        verifyNoInteractions(personalInjuryRepository, heartRateLogRepository, mistakeLogRepository, restTemplate, workoutFeedbackMapper);
    }

    @Test
    void testUTCID06_GenerateFeedback_WhenFeedbackAlreadyExists_ThrowsIllegalStateException() {
        // Arrange
        UUID workoutSessionId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        WorkoutSession workoutSession = buildValidWorkoutSession(workoutSessionId, traineeId);

        when(workoutSessionRepository.findById(workoutSessionId)).thenReturn(Optional.of(workoutSession));
        when(workoutFeedbackRepository.existsByWorkoutSession_WorkoutSessionId(workoutSessionId)).thenReturn(true);

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> workoutFeedbackService.generateFeedback(workoutSessionId, traineeId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("Feedback already exists"));
        verify(workoutFeedbackRepository).existsByWorkoutSession_WorkoutSessionId(workoutSessionId);
        verifyNoInteractions(personalInjuryRepository, heartRateLogRepository, mistakeLogRepository, restTemplate, workoutFeedbackMapper);
    }

    @Test
    void testUTCID07_GenerateFeedback_WhenAiServerReturnsNon2xx_ThrowsWrappedRuntimeException() {
        // Arrange
        UUID workoutSessionId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        WorkoutSession workoutSession = buildValidWorkoutSession(workoutSessionId, traineeId);
        mockCommonDataForTryBlock(workoutSessionId, traineeId, workoutSession);

        when(restTemplate.postForEntity(
                eq("http://ai.test/workout-feedback"),
                any(HttpEntity.class),
                eq(WorkoutFeedbackAIResponse.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workoutFeedbackService.generateFeedback(workoutSessionId, traineeId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("Failed to generate feedback:"));
        assertTrue(exception.getMessage().contains("Failed to get feedback from AI server"));
        verify(workoutFeedbackRepository, never()).save(any(WorkoutFeedback.class));
        verify(workoutFeedbackMapper, never()).toDto(any(WorkoutFeedback.class));
    }

    @Test
    void testUTCID08_GenerateFeedback_WhenAiServerReturnsNullBody_ThrowsWrappedRuntimeException() {
        // Arrange
        UUID workoutSessionId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        WorkoutSession workoutSession = buildValidWorkoutSession(workoutSessionId, traineeId);
        mockCommonDataForTryBlock(workoutSessionId, traineeId, workoutSession);

        when(restTemplate.postForEntity(
                eq("http://ai.test/workout-feedback"),
                any(HttpEntity.class),
                eq(WorkoutFeedbackAIResponse.class)
        )).thenReturn(ResponseEntity.ok(null));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workoutFeedbackService.generateFeedback(workoutSessionId, traineeId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("Failed to generate feedback:"));
        assertTrue(exception.getMessage().contains("Failed to get feedback from AI server"));
        verify(workoutFeedbackRepository, never()).save(any(WorkoutFeedback.class));
        verify(workoutFeedbackMapper, never()).toDto(any(WorkoutFeedback.class));
    }

    @Test
    void testUTCID09_GenerateFeedback_WhenAiTimeout_ThrowsWrappedRuntimeException() {
        // Arrange
        UUID workoutSessionId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        WorkoutSession workoutSession = buildValidWorkoutSession(workoutSessionId, traineeId);
        mockCommonDataForTryBlock(workoutSessionId, traineeId, workoutSession);

        when(restTemplate.postForEntity(
                eq("http://ai.test/workout-feedback"),
                any(HttpEntity.class),
                eq(WorkoutFeedbackAIResponse.class)
        )).thenThrow(new RuntimeException("connection timeout"));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workoutFeedbackService.generateFeedback(workoutSessionId, traineeId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("Failed to generate feedback:"));
        assertTrue(exception.getMessage().contains("AI server timeout"));
        verify(workoutFeedbackRepository, never()).save(any(WorkoutFeedback.class));
        verify(workoutFeedbackMapper, never()).toDto(any(WorkoutFeedback.class));
    }

    @Test
    void testUTCID10_GenerateFeedback_WhenSaveFails_ThrowsWrappedRuntimeException() {
        // Arrange
        UUID workoutSessionId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        WorkoutSession workoutSession = buildValidWorkoutSession(workoutSessionId, traineeId);
        mockCommonDataForTryBlock(workoutSessionId, traineeId, workoutSession);

        WorkoutFeedbackAIResponse aiResponse = new WorkoutFeedbackAIResponse(
                2,
                80.0,
                76.0,
                78.0,
                "Strength",
                "Weakness",
                "Recommendation",
                "test-model"
        );

        when(restTemplate.postForEntity(
                eq("http://ai.test/workout-feedback"),
                any(HttpEntity.class),
                eq(WorkoutFeedbackAIResponse.class)
        )).thenReturn(ResponseEntity.ok(aiResponse));
        when(workoutFeedbackRepository.save(any(WorkoutFeedback.class))).thenThrow(new RuntimeException("db write failed"));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workoutFeedbackService.generateFeedback(workoutSessionId, traineeId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("Failed to generate feedback:"));
        assertTrue(exception.getMessage().contains("db write failed"));
        verify(workoutFeedbackMapper, never()).toDto(any(WorkoutFeedback.class));
    }

    @Test
    void testUTCID11_GenerateFeedback_WhenMapperFails_ThrowsWrappedRuntimeException() {
        // Arrange
        UUID workoutSessionId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        WorkoutSession workoutSession = buildValidWorkoutSession(workoutSessionId, traineeId);
        mockCommonDataForTryBlock(workoutSessionId, traineeId, workoutSession);

        WorkoutFeedbackAIResponse aiResponse = new WorkoutFeedbackAIResponse(
                1,
                90.0,
                91.0,
                92.0,
                "Strong control",
                "Minor imbalance",
                "Continue progression",
                "test-model"
        );
        WorkoutFeedback savedFeedback = buildSavedFeedback(workoutSession, aiResponse);

        when(restTemplate.postForEntity(
                eq("http://ai.test/workout-feedback"),
                any(HttpEntity.class),
                eq(WorkoutFeedbackAIResponse.class)
        )).thenReturn(ResponseEntity.ok(aiResponse));
        when(workoutFeedbackRepository.save(any(WorkoutFeedback.class))).thenReturn(savedFeedback);
        when(workoutFeedbackMapper.toDto(savedFeedback)).thenThrow(new RuntimeException("mapper failure"));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workoutFeedbackService.generateFeedback(workoutSessionId, traineeId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("Failed to generate feedback:"));
        assertTrue(exception.getMessage().contains("mapper failure"));
    }

    @Test
    void testUTCID12_GenerateFeedbackSuccessfully_WithFallbackMetricsAndUnknownBodyPart() {
        // Arrange
        UUID workoutSessionId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        WorkoutSession workoutSession = buildValidWorkoutSession(workoutSessionId, traineeId);
        workoutSession.setDurationSeconds(null);
        workoutSession.setStartTime(null);
        workoutSession.setEndTime(null);

        WorkoutFeedbackAIResponse aiResponse = new WorkoutFeedbackAIResponse(
                3,
                83.0,
                79.0,
                81.0,
                "Good consistency",
                "Posture drifts",
                "Focus on neutral spine",
                "pilahub-workout-analyzer-v1.0"
        );
        WorkoutFeedback savedFeedback = buildSavedFeedback(workoutSession, aiResponse);
        WorkoutFeedbackDto expectedDto = buildExpectedDto(savedFeedback);

        when(workoutSessionRepository.findById(workoutSessionId)).thenReturn(Optional.of(workoutSession));
        when(workoutFeedbackRepository.existsByWorkoutSession_WorkoutSessionId(workoutSessionId)).thenReturn(false);
        when(personalInjuryRepository.findByTraineeTraineeIdAndStatus(traineeId, InjuryStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(heartRateLogRepository.findByWorkoutSession_WorkoutSessionIdOrderByRecordedAtAsc(workoutSessionId))
                .thenReturn(Collections.emptyList());
        when(mistakeLogRepository.findByWorkoutSession_WorkoutSessionIdOrderByRecordedAtSecondAsc(workoutSessionId))
                .thenReturn(List.of(
                        MistakeLog.builder()
                                .bodyPart(null)
                                .recordedAtSecond(null)
                                .duration(null)
                                .details(null)
                                .build()
                ));
        when(restTemplate.postForEntity(
                eq("http://ai.test/workout-feedback"),
                any(HttpEntity.class),
                eq(WorkoutFeedbackAIResponse.class)
        )).thenReturn(ResponseEntity.ok(aiResponse));
        when(workoutFeedbackRepository.save(any(WorkoutFeedback.class))).thenReturn(savedFeedback);
        when(workoutFeedbackMapper.toDto(savedFeedback)).thenReturn(expectedDto);

        // Act
        WorkoutFeedbackDto actual = workoutFeedbackService.generateFeedback(workoutSessionId, traineeId);

        // Assert
        assertSame(expectedDto, actual);

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(
                eq("http://ai.test/workout-feedback"),
                requestCaptor.capture(),
                eq(WorkoutFeedbackAIResponse.class)
        );

        WorkoutFeedbackAIRequest requestBody = (WorkoutFeedbackAIRequest) requestCaptor.getValue().getBody();
        assertNotNull(requestBody);
        assertEquals(0.0, requestBody.sessionMetrics().totalDuration());
        assertNotNull(requestBody.sessionMetrics().startTime());
        assertNotNull(requestBody.sessionMetrics().endTime());
        assertEquals("Unknown", requestBody.mistakeSummary().detailedMistakes().get(0).bodyPartName());
        assertEquals("Unknown", requestBody.mistakeSummary().mistakesByBodyPart().get(0).bodyPartName());
    }

    private void mockCommonDataForTryBlock(UUID workoutSessionId, UUID traineeId, WorkoutSession workoutSession) {
        when(workoutSessionRepository.findById(workoutSessionId)).thenReturn(Optional.of(workoutSession));
        when(workoutFeedbackRepository.existsByWorkoutSession_WorkoutSessionId(workoutSessionId)).thenReturn(false);
        when(personalInjuryRepository.findByTraineeTraineeIdAndStatus(traineeId, InjuryStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(heartRateLogRepository.findByWorkoutSession_WorkoutSessionIdOrderByRecordedAtAsc(workoutSessionId))
                .thenReturn(Collections.emptyList());
        when(mistakeLogRepository.findByWorkoutSession_WorkoutSessionIdOrderByRecordedAtSecondAsc(workoutSessionId))
                .thenReturn(Collections.emptyList());
    }

    private WorkoutSession buildValidWorkoutSession(UUID workoutSessionId, UUID traineeId) {
        Instant start = Instant.parse("2026-04-15T08:00:00Z");
        Instant end = Instant.parse("2026-04-15T08:05:00Z");

        Trainee trainee = Trainee.builder()
                .traineeId(traineeId)
                .fullName("UTC Trainee")
                .age(28)
                .workoutLevel(WorkoutLevel.INTERMEDIATE)
                .createdAt(Instant.now())
                .build();

        Exercise exercise = Exercise.builder()
                .exerciseId(UUID.randomUUID())
                .name("Pilates Hundred")
                .description("Core breathing exercise")
                .duration(300)
                .prerequisites("Neutral spine")
                .contraindications("Lower back pain")
                .bodyParts(Set.of(BodyPart.builder().name("Core").build()))
                .createdAt(Instant.now())
                .build();

        return WorkoutSession.builder()
                .workoutSessionId(workoutSessionId)
                .trainee(trainee)
                .exercise(exercise)
                .haveAITracking(true)
                .haveIOTDeviceTracking(false)
                .startTime(start)
                .endTime(end)
                .durationSeconds(300.0)
                .recordUrl("record.json")
                .completed(true)
                .recordAvailable(true)
                .createdAt(Instant.now())
                .build();
    }

    private PersonalInjury buildPersonalInjury(String injuryName) {
        return PersonalInjury.builder()
                .injury(Injury.builder().name(injuryName).build())
                .status(InjuryStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
    }

    private WorkoutFeedback buildSavedFeedback(WorkoutSession workoutSession, WorkoutFeedbackAIResponse aiResponse) {
        return WorkoutFeedback.builder()
                .workoutFeedbackId(UUID.randomUUID())
                .workoutSession(workoutSession)
                .totalMistakes(aiResponse.totalMistakes())
                .formScore(aiResponse.formScore())
                .enduranceScore(aiResponse.enduranceScore())
                .overallScore(aiResponse.overallScore())
                .strengths(aiResponse.strengths())
                .weaknesses(aiResponse.weaknesses())
                .recommendations(aiResponse.recommendations())
                .aiModel(aiResponse.aiModel())
                .generatedAt(Instant.now())
                .build();
    }

    private WorkoutFeedbackDto buildExpectedDto(WorkoutFeedback feedback) {
        return new WorkoutFeedbackDto(
                feedback.getWorkoutFeedbackId(),
                feedback.getWorkoutSession().getWorkoutSessionId(),
                feedback.getTotalMistakes(),
                feedback.getFormScore(),
                feedback.getEnduranceScore(),
                feedback.getOverallScore(),
                feedback.getStrengths(),
                feedback.getWeaknesses(),
                feedback.getRecommendations(),
                feedback.getAiModel(),
                feedback.getGeneratedAt()
        );
    }
}

