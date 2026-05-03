package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.ExerciseDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateExerciseRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateExerciseRequest;

import java.util.List;
import java.util.UUID;

public interface ExerciseService {

    ExerciseDto createExercise(CreateExerciseRequest request);

    ExerciseDto getById(UUID exerciseId);

    ExerciseDto getActiveById(UUID exerciseId);

    List<ExerciseDto> getAll();

    List<ExerciseDto> getAllActive();

    List<ExerciseDto> searchByName(String name);

    List<ExerciseDto> getByDifficultyLevel(String difficultyLevel);

    List<ExerciseDto> getActiveByDifficultyLevel(String difficultyLevel);

    ExerciseDto updateExercise(UUID exerciseId, UpdateExerciseRequest request);

    void activateExercise(UUID exerciseId);

    void deactivateExercise(UUID exerciseId);

    void deleteExercise(UUID exerciseId);
}
