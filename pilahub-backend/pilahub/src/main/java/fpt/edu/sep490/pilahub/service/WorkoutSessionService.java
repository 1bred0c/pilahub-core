package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.WorkoutSessionDto;
import fpt.edu.sep490.pilahub.dto.request.CompleteWorkoutSessionRequest;
import fpt.edu.sep490.pilahub.dto.request.workout.StartFreeWorkoutRequest;
import fpt.edu.sep490.pilahub.dto.request.workout.StartLessonExerciseWorkoutRequest;
import fpt.edu.sep490.pilahub.dto.request.workout.StartPersonalExerciseWorkoutRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WorkoutSessionService {

    WorkoutSessionDto startPersonalExerciseWorkout(StartPersonalExerciseWorkoutRequest request);

    WorkoutSessionDto startLessonExerciseWorkout(StartLessonExerciseWorkoutRequest request);

    WorkoutSessionDto startFreeWorkout(StartFreeWorkoutRequest request);

    WorkoutSessionDto completeWorkoutSession(UUID workoutSessionId, CompleteWorkoutSessionRequest request);

    WorkoutSessionDto getById(UUID workoutSessionId);

    List<WorkoutSessionDto> getMyWorkoutSessions();

    List<WorkoutSessionDto> getMyCompletedWorkoutSessions();

    List<WorkoutSessionDto> getMyIncompleteWorkoutSessions();

    List<WorkoutSessionDto> getWorkoutSessionsByDateRange(Instant from, Instant to);

    List<WorkoutSessionDto> getWorkoutSessionsByExercise(UUID exerciseId);

    List<WorkoutSessionDto> getWorkoutSessionsByExerciseWithFilters(UUID exerciseId, UUID lessonExerciseProgressId, UUID personalExerciseId);

    void deleteWorkoutSession(UUID workoutSessionId);
}


