package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.ExerciseEquipmentDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateExerciseEquipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateExerciseEquipmentRequest;

import java.util.List;
import java.util.UUID;

public interface ExerciseEquipmentService {

    ExerciseEquipmentDto createExerciseEquipment(CreateExerciseEquipmentRequest request);

    ExerciseEquipmentDto getById(UUID exerciseEquipmentId);

    List<ExerciseEquipmentDto> getByExerciseId(UUID exerciseId);

    List<ExerciseEquipmentDto> getByEquipmentId(UUID equipmentId);

    List<ExerciseEquipmentDto> getRequiredByExerciseId(UUID exerciseId);

    List<ExerciseEquipmentDto> getAlternativesByExerciseId(UUID exerciseId);

    ExerciseEquipmentDto updateExerciseEquipment(UUID exerciseEquipmentId, UpdateExerciseEquipmentRequest request);

    void deleteExerciseEquipment(UUID exerciseEquipmentId);

    void deleteByExerciseId(UUID exerciseId);

    void deleteByEquipmentId(UUID equipmentId);

    boolean existsByExerciseAndEquipment(UUID exerciseId, UUID equipmentId);
}
