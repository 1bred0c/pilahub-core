package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.WorkoutFeedbackDto;
import fpt.edu.sep490.pilahub.dto.request.workout.WorkoutFeedbackAIRequest;
import fpt.edu.sep490.pilahub.dto.response.WorkoutFeedbackAIResponse;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.WorkoutFeedbackMapper;
import fpt.edu.sep490.pilahub.pojo.*;
import fpt.edu.sep490.pilahub.repository.*;
import fpt.edu.sep490.pilahub.service.WorkoutFeedbackService;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkoutFeedbackServiceImpl implements WorkoutFeedbackService {

    private final WorkoutFeedbackRepository workoutFeedbackRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final MistakeLogRepository mistakeLogRepository;
    private final HeartRateLogRepository heartRateLogRepository;
    private final PersonalInjuryRepository personalInjuryRepository;
    private final WorkoutFeedbackMapper workoutFeedbackMapper;
    private final RestTemplate restTemplate;

    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    @Value("${ai.server.workout-feedback-endpoint:/api/v1/workout-feedback/analyze}")
    private String aiFeedbackEndpoint;

    @Override
    public WorkoutFeedbackDto generateFeedback(UUID workoutSessionId, UUID traineeId) {
        log.info("Generating feedback for workout session ID: {} and trainee ID: {}", workoutSessionId, traineeId);

        // Fetch workout session
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", "id", workoutSessionId));

        // Verify ownership
        if (!workoutSession.getTrainee().getTraineeId().equals(traineeId)) {
            log.error("Access denied: Workout session {} does not belong to trainee {}", workoutSessionId, traineeId);
            throw new AccessDeniedException("You do not have permission to generate feedback for this workout session");
        }

        // Verify AI tracking is enabled
        if (!workoutSession.isHaveAITracking()) {
            throw new IllegalStateException("AI tracking must be enabled for this workout session to generate feedback");
        }

        // Verify session is completed
        if (!workoutSession.isCompleted()) {
            throw new IllegalStateException("Workout session must be completed before generating feedback");
        }

        // Check if feedback already exists
        if (workoutFeedbackRepository.existsByWorkoutSession_WorkoutSessionId(workoutSessionId)) {
            throw new IllegalStateException("Feedback already exists for this workout session");
        }

        try {
            // Build AI request
            WorkoutFeedbackAIRequest aiRequest = buildAIRequest(workoutSession);

            // Call AI server
            WorkoutFeedbackAIResponse aiResponse = callAIServer(aiRequest);

            // Save feedback
            WorkoutFeedback feedback = WorkoutFeedback.builder()
                    .workoutSession(workoutSession)
                    .totalMistakes(aiResponse.totalMistakes())
                    .formScore(aiResponse.formScore())
                    .enduranceScore(aiResponse.enduranceScore())
                    .overallScore(aiResponse.overallScore())
                    .strengths(aiResponse.strengths())
                    .weaknesses(aiResponse.weaknesses())
                    .recommendations(aiResponse.recommendations())
                    .aiModel(aiResponse.aiModel())
                    .build();

            WorkoutFeedback savedFeedback = workoutFeedbackRepository.save(feedback);
            log.info("Feedback generated successfully with ID: {}", savedFeedback.getWorkoutFeedbackId());

            return workoutFeedbackMapper.toDto(savedFeedback);

        } catch (Exception e) {
            log.error("Failed to generate feedback for workout session ID: {}", workoutSessionId, e);
            throw new RuntimeException("Failed to generate feedback: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutFeedbackDto getFeedbackByWorkoutSessionId(UUID workoutSessionId, UUID traineeId) {
        log.info("Fetching feedback for workout session ID: {} and trainee ID: {}", workoutSessionId, traineeId);

        WorkoutFeedback feedback = workoutFeedbackRepository.findByWorkoutSession_WorkoutSessionId(workoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutFeedback", "workoutSessionId", workoutSessionId));

        // Verify ownership
        if (!feedback.getWorkoutSession().getTrainee().getTraineeId().equals(traineeId)) {
            log.error("Access denied: Feedback does not belong to trainee {}", traineeId);
            throw new AccessDeniedException("You do not have permission to access this feedback");
        }

        return workoutFeedbackMapper.toDto(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutFeedbackDto> getAllFeedbackByTraineeId(UUID traineeId) {
        log.info("Fetching all feedback for trainee ID: {}", traineeId);

        return workoutFeedbackRepository.findByWorkoutSession_Trainee_TraineeIdOrderByGeneratedAtDesc(traineeId)
                .stream()
                .map(workoutFeedbackMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFeedback(UUID workoutSessionId, UUID traineeId) {
        log.info("Deleting feedback for workout session ID: {} and trainee ID: {}", workoutSessionId, traineeId);

        WorkoutFeedback feedback = workoutFeedbackRepository.findByWorkoutSession_WorkoutSessionId(workoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutFeedback", "workoutSessionId", workoutSessionId));

        // Verify ownership
        if (!feedback.getWorkoutSession().getTrainee().getTraineeId().equals(traineeId)) {
            log.error("Access denied: Feedback does not belong to trainee {}", traineeId);
            throw new AccessDeniedException("You do not have permission to delete this feedback");
        }

        workoutFeedbackRepository.delete(feedback);
        log.info("Feedback deleted successfully for workout session ID: {}", workoutSessionId);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private WorkoutFeedbackAIRequest buildAIRequest(WorkoutSession workoutSession) {
        Exercise exercise = workoutSession.getExercise();
        Trainee trainee = workoutSession.getTrainee();

        // Build traineeInfo
        WorkoutFeedbackAIRequest.TraineeInfo traineeInfo = buildTraineeInfo(trainee);

        // Build exerciseInfo
        WorkoutFeedbackAIRequest.ExerciseInfo exerciseInfo = buildExerciseInfo(exercise);

        // Build sessionMetrics
        WorkoutFeedbackAIRequest.SessionMetrics sessionMetrics = buildSessionMetrics(workoutSession);

        // Build mistakeSummary
        WorkoutFeedbackAIRequest.MistakeSummary mistakeSummary = buildMistakeSummary(workoutSession);

        return new WorkoutFeedbackAIRequest(
                workoutSession.getWorkoutSessionId().toString(),
                traineeInfo,
                exerciseInfo,
                sessionMetrics,
                mistakeSummary,
                null, // videoUrl - can be added later
                workoutSession.getRecordUrl()
        );
    }

    private WorkoutFeedbackAIRequest.TraineeInfo buildTraineeInfo(Trainee trainee) {
        // Get active injuries
        List<String> activeInjuries = personalInjuryRepository
                .findByTraineeTraineeIdAndStatus(trainee.getTraineeId(), fpt.edu.sep490.pilahub.enums.InjuryStatus.ACTIVE)
                .stream()
                .map(pi -> pi.getInjury().getName())
                .collect(Collectors.toList());

        // Map workout level to experience months (rough estimate)
        Integer experienceMonths = switch (trainee.getWorkoutLevel()) {
            case BEGINNER -> 3;
            case INTERMEDIATE -> 12;
            case ADVANCED -> 36;
        };

        // Build goals from workout frequency
        List<String> goals = List.of("Maintain fitness", "Improve overall health");

        return new WorkoutFeedbackAIRequest.TraineeInfo(
                trainee.getTraineeId().toString(),
                trainee.getFullName(),
                trainee.getAge(),
                trainee.getWorkoutLevel().toString(),
                experienceMonths,
                goals,
                activeInjuries
        );
    }

    private WorkoutFeedbackAIRequest.ExerciseInfo buildExerciseInfo(Exercise exercise) {
        List<String> targetBodyParts = exercise.getBodyParts().stream()
                .map(BodyPart::getName)
                .collect(Collectors.toList());

        return new WorkoutFeedbackAIRequest.ExerciseInfo(
                exercise.getExerciseId().toString(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getExerciseType() != null ? exercise.getExerciseType().toString() : null,
                exercise.getDifficultyLevel() != null ? exercise.getDifficultyLevel().toString() : null,
                targetBodyParts,
                exercise.getPrerequisites(), // Using prerequisites as instructions
                exercise.getContraindications(), // Using contraindications as common mistakes
                exercise.getDuration()
        );
    }

    private WorkoutFeedbackAIRequest.SessionMetrics buildSessionMetrics(WorkoutSession workoutSession) {
        // Calculate heart rate metrics
        List<HeartRateLog> heartRateLogs = heartRateLogRepository.findByWorkoutSession_WorkoutSessionIdOrderByRecordedAtAsc(
                workoutSession.getWorkoutSessionId()
        );

        Double averageHeartRate = null;
        Integer maxHeartRate = null;

        if (!heartRateLogs.isEmpty()) {
            IntSummaryStatistics stats = heartRateLogs.stream()
                    .mapToInt(HeartRateLog::getHeartRate)
                    .summaryStatistics();
            averageHeartRate = stats.getAverage();
            maxHeartRate = stats.getMax();
        }

        // Ensure totalDuration is not null (required field)
        Double totalDuration = workoutSession.getDurationSeconds();
        if (totalDuration == null && workoutSession.getStartTime() != null && workoutSession.getEndTime() != null) {
            // Calculate duration from start and end time
            totalDuration = (double) (workoutSession.getEndTime().toEpochMilli() - workoutSession.getStartTime().toEpochMilli()) / 1000.0;
        }
        if (totalDuration == null) {
            totalDuration = 0.0; // Default fallback
        }

        // Ensure startTime is not null (required field)
        String startTime = workoutSession.getStartTime() != null 
                ? workoutSession.getStartTime().toString() 
                : java.time.Instant.now().toString(); // Default to now if missing

        // Ensure endTime is not null (required field)
        String endTime;
        if (workoutSession.getEndTime() != null) {
            endTime = workoutSession.getEndTime().toString();
        } else if (workoutSession.getStartTime() != null) {
            // Calculate endTime from startTime + duration
            endTime = workoutSession.getStartTime().plusSeconds(totalDuration.longValue()).toString();
        } else {
            // Default fallback
            endTime = java.time.Instant.now().toString();
        }

        return new WorkoutFeedbackAIRequest.SessionMetrics(
                totalDuration,
                null, // completedReps - not tracked yet
                null, // targetReps - not tracked yet
                averageHeartRate,
                maxHeartRate,
                null, // caloriesBurned - not calculated yet
                startTime,
                endTime,
                workoutSession.isHaveAITracking(),
                workoutSession.isHaveIOTDeviceTracking()
        );
    }

    private WorkoutFeedbackAIRequest.MistakeSummary buildMistakeSummary(WorkoutSession workoutSession) {
        List<MistakeLog> mistakes = mistakeLogRepository.findByWorkoutSession_WorkoutSessionIdOrderByRecordedAtSecondAsc(
                workoutSession.getWorkoutSessionId()
        );

        Integer totalMistakes = mistakes.size();

        // Build detailed mistakes list (từng lỗi riêng lẻ)
        List<WorkoutFeedbackAIRequest.MistakeDetail> detailedMistakes = mistakes.stream()
                .map(m -> new WorkoutFeedbackAIRequest.MistakeDetail(
                        m.getBodyPart() != null ? m.getBodyPart().getName() : "Unknown",
                        m.getRecordedAtSecond() != null ? m.getRecordedAtSecond() : 0.0,
                        m.getDuration() != null ? m.getDuration() : 0.0,
                        m.getDetails() != null ? m.getDetails() : "Form issue detected",
                        m.getImageUrl()
                ))
                .toList();

        // Group mistakes by body part (tổng hợp theo body part)
        Map<String, List<MistakeLog>> mistakesByBodyPart = mistakes.stream()
                .filter(m -> m.getBodyPart() != null)
                .collect(Collectors.groupingBy(m -> m.getBodyPart().getName()));

        List<WorkoutFeedbackAIRequest.BodyPartMistake> bodyPartMistakes = mistakesByBodyPart.entrySet().stream()
                .map(entry -> {
                    String bodyPartName = entry.getKey();
                    List<MistakeLog> logs = entry.getValue();
                    Integer count = logs.size();

                    // Tính tổng duration cho body part này
                    Double totalDuration = logs.stream()
                            .map(MistakeLog::getDuration)
                            .filter(Objects::nonNull)
                            .reduce(0.0, Double::sum);

                    List<String> details = logs.stream()
                            .map(m -> {
                                String detail = m.getDetails() != null ? m.getDetails() : "Form issue";
                                Double recordedAt = m.getRecordedAtSecond();
                                Double duration = m.getDuration();

                                if (recordedAt != null && duration != null) {
                                    return String.format("%s at %.1fs (%.1fs)", detail, recordedAt, duration);
                                } else if (recordedAt != null) {
                                    return String.format("%s at %.1fs", detail, recordedAt);
                                }
                                return detail;
                            })
                            .toList();

                    return new WorkoutFeedbackAIRequest.BodyPartMistake(
                            bodyPartName,
                            count,
                            null, // averageSeverity - not implemented yet
                            totalDuration,
                            details
                    );
                })
                .toList();

        // If no body part grouped mistakes, create an empty list to satisfy requirement
        if (bodyPartMistakes.isEmpty() && !mistakes.isEmpty()) {
            bodyPartMistakes = List.of(new WorkoutFeedbackAIRequest.BodyPartMistake(
                    "Unknown",
                    mistakes.size(),
                    null,
                    mistakes.stream().map(MistakeLog::getDuration).filter(Objects::nonNull).reduce(0.0, Double::sum),
                    List.of("General form issues detected")
            ));
        }

        // Calculate average time between mistakes
        Double averageTimeBetweenMistakes = null;
        if (mistakes.size() > 1) {
            List<Double> times = mistakes.stream()
                    .map(MistakeLog::getRecordedAtSecond)
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();

            if (times.size() > 1) {
                double totalGaps = 0.0;
                for (int i = 1; i < times.size(); i++) {
                    totalGaps += times.get(i) - times.get(i - 1);
                }
                averageTimeBetweenMistakes = totalGaps / (times.size() - 1);
            }
        }

        // Calculate total mistake duration (tổng thời gian lỗi)
        Double totalMistakeDuration = mistakes.stream()
                .map(MistakeLog::getDuration)
                .filter(Objects::nonNull)
                .reduce(0.0, Double::sum);

        // Calculate mistake time percentage
        Double mistakeTimePercentage = null;
        if (workoutSession.getDurationSeconds() != null && workoutSession.getDurationSeconds() > 0) {
            mistakeTimePercentage = (totalMistakeDuration / workoutSession.getDurationSeconds()) * 100.0;
        }

        return new WorkoutFeedbackAIRequest.MistakeSummary(
                totalMistakes,
                detailedMistakes,
                bodyPartMistakes,
                averageTimeBetweenMistakes,
                totalMistakeDuration,
                mistakeTimePercentage
        );
    }


    private WorkoutFeedbackAIResponse callAIServer(WorkoutFeedbackAIRequest request) {
        String url = aiServerUrl + aiFeedbackEndpoint;
        log.info("Calling AI server at: {} for workout feedback analysis", url);

        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<WorkoutFeedbackAIRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<WorkoutFeedbackAIResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    WorkoutFeedbackAIResponse.class
            );

            long duration = System.currentTimeMillis() - startTime;

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("AI server returned non-successful status: {} after {}ms", response.getStatusCode(), duration);
                throw new RuntimeException("Failed to get feedback from AI server. Status: " + response.getStatusCode());
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


