package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.WorkoutSessionDto;
import fpt.edu.sep490.pilahub.dto.request.CompleteWorkoutSessionRequest;
import fpt.edu.sep490.pilahub.dto.request.workout.StartFreeWorkoutRequest;
import fpt.edu.sep490.pilahub.dto.request.workout.StartLessonExerciseWorkoutRequest;
import fpt.edu.sep490.pilahub.dto.request.workout.StartPersonalExerciseWorkoutRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.WorkoutSessionMapper;
import fpt.edu.sep490.pilahub.pojo.*;
import fpt.edu.sep490.pilahub.repository.*;
import fpt.edu.sep490.pilahub.service.WorkoutSessionService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkoutSessionServiceImpl implements WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final TraineeRepository traineeRepository;
    private final ExerciseRepository exerciseRepository;
    private final PersonalExerciseRepository personalExerciseRepository;
    private final CourseLessonProgressRepository courseLessonProgressRepository;
    private final LessonExerciseProgressRepository lessonExerciseProgressRepository;
    private final LessonExerciseRepository lessonExerciseRepository;
    private final WorkoutSessionMapper workoutSessionMapper;
    private final SecurityUtil securityUtil;

    @Override
    public WorkoutSessionDto startPersonalExerciseWorkout(StartPersonalExerciseWorkoutRequest request) {
        // Get current trainee
        UUID traineeId = securityUtil.getCurrentUserId();
        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", traineeId));

        // Get personal exercise (which contains the exercise reference)
        PersonalExercise personalExercise = personalExerciseRepository.findById(request.personalExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("PersonalExercise", "id", request.personalExerciseId()));

        // Get exercise from personal exercise
        Exercise exercise = personalExercise.getExercise();

        // Create workout session
        WorkoutSession workoutSession = WorkoutSession.builder()
                .trainee(trainee)
                .personalExercise(personalExercise)
                .exercise(exercise)
                .haveAITracking(request.haveAITracking())
                .haveIOTDeviceTracking(request.haveIOTDeviceTracking())
                .startTime(Instant.now())
                .completed(false)
                .build();

        return workoutSessionMapper.toDto(workoutSessionRepository.save(workoutSession));
    }

    @Override
    public WorkoutSessionDto startLessonExerciseWorkout(StartLessonExerciseWorkoutRequest request) {
        // Get current trainee
        UUID traineeId = securityUtil.getCurrentUserId();
        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", traineeId));

        // Get course lesson progress
        CourseLessonProgress courseLessonProgress = courseLessonProgressRepository.findById(request.courseLessonProgressId())
                .orElseThrow(() -> new ResourceNotFoundException("CourseLessonProgress", "id", request.courseLessonProgressId()));

        // Get lesson exercise
        LessonExercise lessonExercise = lessonExerciseRepository.findById(request.lessonExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("LessonExercise", "id", request.lessonExerciseId()));

        // Verify lesson exercise belongs to the same lesson in course lesson progress
        if (!lessonExercise.getLesson().getLessonId().equals(courseLessonProgress.getCourseLesson().getLesson().getLessonId())) {
            throw new IllegalArgumentException("Lesson exercise does not belong to the lesson in course lesson progress");
        }

        // Get or create lesson exercise progress
        LessonExerciseProgress lessonExerciseProgress = lessonExerciseProgressRepository
                .findByCourseLessonProgress_ProgressIdAndLessonExercise_LessonExerciseId(
                        request.courseLessonProgressId(), request.lessonExerciseId())
                .orElseGet(() -> {
                    LessonExerciseProgress newProgress = LessonExerciseProgress.builder()
                            .courseLessonProgress(courseLessonProgress)
                            .lessonExercise(lessonExercise)
                            .startedAt(Instant.now())
                            .completed(false)
                            .build();
                    return lessonExerciseProgressRepository.save(newProgress);
                });

        // Get exercise from lesson exercise
        Exercise exercise = lessonExercise.getExercise();

        // Create workout session
        WorkoutSession workoutSession = WorkoutSession.builder()
                .trainee(trainee)
                .lessonExerciseProgress(lessonExerciseProgress)
                .exercise(exercise)
                .haveAITracking(request.haveAITracking())
                .haveIOTDeviceTracking(request.haveIOTDeviceTracking())
                .startTime(Instant.now())
                .completed(false)
                .build();

        return workoutSessionMapper.toDto(workoutSessionRepository.save(workoutSession));
    }

    @Override
    public WorkoutSessionDto startFreeWorkout(StartFreeWorkoutRequest request) {
        // Get current trainee
        UUID traineeId = securityUtil.getCurrentUserId();
        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", traineeId));

        // Get exercise
        Exercise exercise = exerciseRepository.findById(request.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", request.exerciseId()));

        // Create workout session (no personal exercise or course lesson progress)
        WorkoutSession workoutSession = WorkoutSession.builder()
                .trainee(trainee)
                .exercise(exercise)
                .haveAITracking(request.haveAITracking())
                .haveIOTDeviceTracking(request.haveIOTDeviceTracking())
                .startTime(Instant.now())
                .completed(false)
                .build();

        return workoutSessionMapper.toDto(workoutSessionRepository.save(workoutSession));
    }

    @Override
    public WorkoutSessionDto completeWorkoutSession(UUID workoutSessionId, CompleteWorkoutSessionRequest request) {
        // Get current trainee
        UUID traineeId = securityUtil.getCurrentUserId();

        // Get workout session
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", "id", workoutSessionId));

        // Verify ownership
        if (!workoutSession.getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only complete your own workout sessions");
        }

        // Verify not already completed
        if (workoutSession.isCompleted()) {
            throw new IllegalStateException("Workout session is already completed");
        }

        // Set end time to current time (automatically)
        Instant endTime = Instant.now();
        workoutSession.setEndTime(endTime);
        workoutSession.setCompleted(true);

        // Set record URL from request and mark as available
        workoutSession.setRecordUrl(request.recordUrl());
        if (request.recordUrl() == null || request.recordUrl().equals("")) {
            workoutSession.setRecordAvailable(false);
        } else {
            workoutSession.setRecordAvailable(true);
        }

        // Calculate duration
        if (workoutSession.getStartTime() != null) {
            Duration duration = Duration.between(workoutSession.getStartTime(), endTime);
            workoutSession.setDurationSeconds(duration.toSeconds() + duration.getNano() / 1_000_000_000.0);
        }


        // Update PersonalExercise if associated
        if (workoutSession.getPersonalExercise() != null) {
            PersonalExercise personalExercise = workoutSession.getPersonalExercise();
            personalExercise.setCompleted(true);
            personalExercise.setCompletedAt(Instant.now());
            personalExerciseRepository.save(personalExercise);
        }

        // Update LessonExerciseProgress if associated
        if (workoutSession.getLessonExerciseProgress() != null) {
            LessonExerciseProgress lessonExerciseProgress = workoutSession.getLessonExerciseProgress();
            lessonExerciseProgress.setCompleted(true);
            lessonExerciseProgress.setCompletedAt(Instant.now());
            lessonExerciseProgressRepository.save(lessonExerciseProgress);

            // Check if all exercises in the lesson are completed
            CourseLessonProgress courseLessonProgress = lessonExerciseProgress.getCourseLessonProgress();
            if (isLessonFullyCompleted(courseLessonProgress.getProgressId())) {
                courseLessonProgress.setCompleted(true);
                courseLessonProgress.setCompletedAt(Instant.now());
                courseLessonProgressRepository.save(courseLessonProgress);
            }
        }


        return workoutSessionMapper.toDto(workoutSessionRepository.save(workoutSession));
    }

    /**
     * Check if all exercises in a lesson are completed
     */
    private boolean isLessonFullyCompleted(UUID courseLessonProgressId) {
        long totalExercises = lessonExerciseProgressRepository.countByCourseLessonProgress_ProgressId(courseLessonProgressId);
        long completedExercises = lessonExerciseProgressRepository.countByCourseLessonProgress_ProgressIdAndCompletedTrue(courseLessonProgressId);

        return totalExercises > 0 && totalExercises == completedExercises;
    }

    @Override
    public WorkoutSessionDto getById(UUID workoutSessionId) {
        // Get current trainee
        UUID traineeId = securityUtil.getCurrentUserId();

        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", "id", workoutSessionId));

        // Verify ownership
        if (!workoutSession.getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only view your own workout sessions");
        }

        return workoutSessionMapper.toDto(workoutSession);
    }

    @Override
    public List<WorkoutSessionDto> getMyWorkoutSessions() {
        UUID traineeId = securityUtil.getCurrentUserId();
        return workoutSessionRepository.findByTrainee_TraineeIdOrderByCreatedAtDesc(traineeId).stream()
                .map(workoutSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutSessionDto> getMyCompletedWorkoutSessions() {
        UUID traineeId = securityUtil.getCurrentUserId();
        return workoutSessionRepository.findByTrainee_TraineeIdAndCompletedTrue(traineeId).stream()
                .map(workoutSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutSessionDto> getMyIncompleteWorkoutSessions() {
        UUID traineeId = securityUtil.getCurrentUserId();
        return workoutSessionRepository.findByTrainee_TraineeIdAndCompletedFalse(traineeId).stream()
                .map(workoutSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutSessionDto> getWorkoutSessionsByDateRange(Instant from, Instant to) {
        UUID traineeId = securityUtil.getCurrentUserId();
        return workoutSessionRepository.findByTrainee_TraineeIdAndStartTimeBetween(traineeId, from, to).stream()
                .map(workoutSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutSessionDto> getWorkoutSessionsByExercise(UUID exerciseId) {
        // Verify exercise exists
        if (!exerciseRepository.existsById(exerciseId)) {
            throw new ResourceNotFoundException("Exercise", "id", exerciseId);
        }

        UUID traineeId = securityUtil.getCurrentUserId();
        return workoutSessionRepository.findByExercise_ExerciseId(exerciseId).stream()
                .filter(ws -> ws.getTrainee().getTraineeId().equals(traineeId))
                .map(workoutSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutSessionDto> getWorkoutSessionsByExerciseWithFilters(UUID exerciseId, UUID lessonExerciseProgressId, UUID personalExerciseId) {
        // Verify exercise exists
        if (!exerciseRepository.existsById(exerciseId)) {
            throw new ResourceNotFoundException("Exercise", "id", exerciseId);
        }

        UUID traineeId = securityUtil.getCurrentUserId();
        return workoutSessionRepository.findByExercise_ExerciseId(exerciseId).stream()
                .filter(ws -> ws.getTrainee().getTraineeId().equals(traineeId))
                .filter(ws -> {
                    // If both filters are null, get free workouts (no lessonExerciseProgress and no personalExercise)
                    if (lessonExerciseProgressId == null && personalExerciseId == null) {
                        return ws.getLessonExerciseProgress() == null && ws.getPersonalExercise() == null;
                    }
                    // If lessonExerciseProgressId is provided, filter by it
                    if (lessonExerciseProgressId != null) {
                        return ws.getLessonExerciseProgress() != null && 
                               ws.getLessonExerciseProgress().getLessonExerciseProgressId().equals(lessonExerciseProgressId);
                    }
                    // If personalExerciseId is provided, filter by it
                    if (personalExerciseId != null) {
                        return ws.getPersonalExercise() != null && 
                               ws.getPersonalExercise().getPersonalExerciseId().equals(personalExerciseId);
                    }
                    return true;
                })
                .map(workoutSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteWorkoutSession(UUID workoutSessionId) {
        // Get current trainee
        UUID traineeId = securityUtil.getCurrentUserId();

        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", "id", workoutSessionId));

        // Verify ownership
        if (!workoutSession.getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only delete your own workout sessions");
        }

        workoutSessionRepository.deleteById(workoutSessionId);
    }
}





