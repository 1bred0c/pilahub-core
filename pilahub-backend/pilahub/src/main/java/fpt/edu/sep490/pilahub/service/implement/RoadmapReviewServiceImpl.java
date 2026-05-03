package fpt.edu.sep490.pilahub.service.implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.edu.sep490.pilahub.dto.RoadmapReviewDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.RoadmapReviewAIRequest;
import fpt.edu.sep490.pilahub.dto.response.RoadmapReviewAIResponse;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.enums.WorkoutFrequency;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.RoadmapReviewMapper;
import fpt.edu.sep490.pilahub.pojo.HealthProfile;
import fpt.edu.sep490.pilahub.pojo.Roadmap;
import fpt.edu.sep490.pilahub.pojo.RoadmapGoal;
import fpt.edu.sep490.pilahub.pojo.RoadmapReview;
import fpt.edu.sep490.pilahub.repository.*;
import fpt.edu.sep490.pilahub.service.RoadmapReviewService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoadmapReviewServiceImpl implements RoadmapReviewService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapReviewRepository roadmapReviewRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final PersonalScheduleRepository personalScheduleRepository;
    private final PersonalExerciseRepository personalExerciseRepository;
    private final RoadmapReviewMapper roadmapReviewMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SecurityUtil securityUtil;

    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    @Value("${ai.server.roadmap-review-endpoint:/api/v1/roadmap-review/analyze}")
    private String aiRoadmapReviewEndpoint;

    @Override
    public RoadmapReviewDto generateReview(UUID roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));

        validateAccess(roadmap);

        if (roadmapReviewRepository.existsByRoadmap_RoadmapId(roadmapId)) {
            throw new IllegalStateException("Review already exists for this roadmap");
        }

        if (roadmap.getProgressPercent() == null || roadmap.getProgressPercent() < 100) {
            throw new IllegalStateException("Roadmap progress must be 100% before generating review");
        }

        if (roadmap.getInitialHealthProfileId() == null || roadmap.getFinalHealthProfileId() == null) {
            throw new IllegalStateException("Initial and final health profile IDs must be set before generating review");
        }

        HealthProfile initialProfile = healthProfileRepository.findById(roadmap.getInitialHealthProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", roadmap.getInitialHealthProfileId()));
        HealthProfile finalProfile = healthProfileRepository.findById(roadmap.getFinalHealthProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", roadmap.getFinalHealthProfileId()));

        RoadmapReviewAIRequest aiRequest = buildAIRequest(roadmap, initialProfile, finalProfile);
        RoadmapReviewAIResponse aiResponse = callAIServer(aiRequest);

        RoadmapReview review = RoadmapReview.builder()
                .roadmap(roadmap)
                .overallScore(aiResponse.overallScore())
                .subScoresJson(writeJson(aiResponse.subScores()))
                .deltaMetricsJson(writeJson(aiResponse.deltaMetrics()))
                .narrativeSummary(aiResponse.narrativeSummary())
                .prioritizedRecommendationsJson(writeJson(aiResponse.prioritizedRecommendations()))
                .confidenceLevel(aiResponse.confidenceLevel())
                .build();

        RoadmapReview savedReview = roadmapReviewRepository.save(review);
        log.info("Roadmap review generated successfully for roadmap {}", roadmapId);
        return roadmapReviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public RoadmapReviewDto getReviewByRoadmapId(UUID roadmapId) {
        RoadmapReview review = roadmapReviewRepository.findByRoadmap_RoadmapId(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("RoadmapReview", "roadmapId", roadmapId));

        validateAccess(review.getRoadmap());
        return roadmapReviewMapper.toDto(review);
    }

    private void validateAccess(Roadmap roadmap) {
        Role role = securityUtil.getCurrentUserRole();
        UUID currentUserId = securityUtil.getCurrentUserId();

        if (role == Role.TRAINEE && !Objects.equals(roadmap.getTrainee().getTraineeId(), currentUserId)) {
            throw new AccessDeniedException("You do not have permission to access this roadmap review");
        }

        if (role == Role.COACH && roadmap.getCoach() != null &&
                !Objects.equals(roadmap.getCoach().getCoachId(), currentUserId)) {
            throw new AccessDeniedException("You do not have permission to access this roadmap review");
        }
    }

    private RoadmapReviewAIRequest buildAIRequest(Roadmap roadmap, HealthProfile initialProfile, HealthProfile finalProfile) {
        List<RoadmapReviewAIRequest.GoalInfo> goals = roadmap.getRoadmapGoals().stream()
                .map(this::mapGoalInfo)
                .collect(Collectors.toList());

        RoadmapReviewAIRequest.RoadmapInfo roadmapInfo = new RoadmapReviewAIRequest.RoadmapInfo(
                roadmap.getRoadmapId().toString(),
                roadmap.getTitle(),
                roadmap.getDescription(),
                roadmap.getStartDate() != null ? roadmap.getStartDate().toString() : null,
                roadmap.getEndDate() != null ? roadmap.getEndDate().toString() : null,
                roadmap.getProgressPercent(),
                roadmap.getStatus() != null ? roadmap.getStatus().name() : null,
                roadmap.getSource(),
                goals,
                roadmap.getInitialHealthProfileId() != null ? roadmap.getInitialHealthProfileId().toString() : null,
                roadmap.getFinalHealthProfileId() != null ? roadmap.getFinalHealthProfileId().toString() : null
        );

        RoadmapReviewAIRequest.HealthProfileSnapshot initialSnapshot = mapHealthProfile(initialProfile);
        RoadmapReviewAIRequest.HealthProfileSnapshot finalSnapshot = mapHealthProfile(finalProfile);

        RoadmapReviewAIRequest.TraineeContext traineeContext = new RoadmapReviewAIRequest.TraineeContext(
                roadmap.getTrainee().getAge(),
                roadmap.getTrainee().getGender() != null ? roadmap.getTrainee().getGender().name() : null,
                mapWorkoutFrequency(roadmap.getTrainee().getWorkoutFrequency())
        );

        RoadmapReviewAIRequest.ExecutionSummary executionSummary = buildExecutionSummary(roadmap.getRoadmapId());

        return new RoadmapReviewAIRequest(
                roadmapInfo,
                initialSnapshot,
                finalSnapshot,
                traineeContext,
                executionSummary
        );
    }

    private RoadmapReviewAIRequest.GoalInfo mapGoalInfo(RoadmapGoal goal) {
        return new RoadmapReviewAIRequest.GoalInfo(
                goal.getFitnessGoal().getGoalId().toString(),
                goal.getFitnessGoal().getCode(),
                goal.getFitnessGoal().getVietnameseName(),
                goal.getIsPrimary(),
                goal.getGoalOrder()
        );
    }

    private RoadmapReviewAIRequest.HealthProfileSnapshot mapHealthProfile(HealthProfile profile) {
        return new RoadmapReviewAIRequest.HealthProfileSnapshot(
                profile.getHealthProfileId().toString(),
                profile.getCreatedAt() != null ? profile.getCreatedAt().toString() : null,
                profile.getHeightCm(),
                profile.getWeightKg(),
                profile.getBmi(),
                profile.getBodyFatPercentage(),
                profile.getMuscleMassKg(),
                profile.getWaistCm(),
                profile.getHipCm(),
                profile.getSource() != null ? profile.getSource().name() : null,
                profile.getMetadata()
        );
    }

    private RoadmapReviewAIRequest.ExecutionSummary buildExecutionSummary(UUID roadmapId) {
        int totalSchedules = personalScheduleRepository.countTotalSchedulesInRoadmap(roadmapId);
        int completedSchedules = personalScheduleRepository.countCompletedSchedulesInRoadmap(roadmapId);
        int totalExercises = personalExerciseRepository.countTotalExercisesInRoadmap(roadmapId);
        int completedExercises = personalExerciseRepository.countCompletedExercisesInRoadmap(roadmapId);

        Double completionRate = totalSchedules > 0 ? (double) completedSchedules / totalSchedules : 0.0;

        return new RoadmapReviewAIRequest.ExecutionSummary(
                totalSchedules,
                completedSchedules,
                totalExercises,
                completedExercises,
                completionRate
        );
    }

    private Integer mapWorkoutFrequency(WorkoutFrequency workoutFrequency) {
        if (workoutFrequency == null) {
            return null;
        }
        return switch (workoutFrequency) {
            case SEDENTARY -> 0;
            case LIGHT -> 2;
            case MODERATE -> 4;
            case ACTIVE -> 6;
            case ATHLETE -> 7;
        };
    }

    private RoadmapReviewAIResponse callAIServer(RoadmapReviewAIRequest request) {
        String url = aiServerUrl + aiRoadmapReviewEndpoint;
        log.info("Calling AI server at: {} for roadmap review", url);

        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RoadmapReviewAIRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<RoadmapReviewAIResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    RoadmapReviewAIResponse.class
            );

            long duration = System.currentTimeMillis() - startTime;

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("AI server returned non-successful status: {} after {}ms", response.getStatusCode(), duration);
                throw new RuntimeException("Failed to get review from AI server. Status: " + response.getStatusCode());
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

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("Failed to serialize JSON: {}", e.getMessage());
            return null;
        }
    }
}

