package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.ExerciseEquipmentDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateExerciseEquipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateExerciseEquipmentRequest;
import fpt.edu.sep490.pilahub.exception.DuplicateResourceException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.ExerciseEquipmentMapper;
import fpt.edu.sep490.pilahub.pojo.Equipment;
import fpt.edu.sep490.pilahub.pojo.Exercise;
import fpt.edu.sep490.pilahub.pojo.ExerciseEquipment;
import fpt.edu.sep490.pilahub.repository.EquipmentRepository;
import fpt.edu.sep490.pilahub.repository.ExerciseEquipmentRepository;
import fpt.edu.sep490.pilahub.repository.ExerciseRepository;
import fpt.edu.sep490.pilahub.service.ExerciseEquipmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseEquipmentServiceImpl implements ExerciseEquipmentService {

    private final ExerciseEquipmentRepository exerciseEquipmentRepository;
    private final ExerciseRepository exerciseRepository;
    private final EquipmentRepository equipmentRepository;
    private final ExerciseEquipmentMapper exerciseEquipmentMapper;

    @Override
    public ExerciseEquipmentDto createExerciseEquipment(CreateExerciseEquipmentRequest request) {
        // Check if exercise exists
        Exercise exercise = exerciseRepository.findById(request.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", request.exerciseId()));

        // Check if equipment exists
        Equipment equipment = equipmentRepository.findById(request.equipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", request.equipmentId()));

        // Check for duplicate relationship
        if (exerciseEquipmentRepository.existsByExercise_ExerciseIdAndEquipment_EquipmentId(
                request.exerciseId(), request.equipmentId())) {
            throw new DuplicateResourceException("Exercise equipment relationship already exists");
        }

        ExerciseEquipment exerciseEquipment = exerciseEquipmentMapper.toEntity(request);
        exerciseEquipment.setExercise(exercise);
        exerciseEquipment.setEquipment(equipment);

        ExerciseEquipment saved = exerciseEquipmentRepository.save(exerciseEquipment);
        return exerciseEquipmentMapper.toDto(saved);
    }

    @Override
    public ExerciseEquipmentDto getById(UUID exerciseEquipmentId) {
        ExerciseEquipment exerciseEquipment = exerciseEquipmentRepository.findById(exerciseEquipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("ExerciseEquipment", "id", exerciseEquipmentId));
        return exerciseEquipmentMapper.toDto(exerciseEquipment);
    }

    @Override
    public List<ExerciseEquipmentDto> getByExerciseId(UUID exerciseId) {
        return exerciseEquipmentRepository.findByExercise_ExerciseId(exerciseId).stream()
                .map(exerciseEquipmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExerciseEquipmentDto> getByEquipmentId(UUID equipmentId) {
        return exerciseEquipmentRepository.findByEquipment_EquipmentId(equipmentId).stream()
                .map(exerciseEquipmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExerciseEquipmentDto> getRequiredByExerciseId(UUID exerciseId) {
        return exerciseEquipmentRepository.findByExercise_ExerciseIdAndRequiredTrue(exerciseId).stream()
                .map(exerciseEquipmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExerciseEquipmentDto> getAlternativesByExerciseId(UUID exerciseId) {
        return exerciseEquipmentRepository.findByExercise_ExerciseIdAndAlternativeTrue(exerciseId).stream()
                .map(exerciseEquipmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ExerciseEquipmentDto updateExerciseEquipment(UUID exerciseEquipmentId, UpdateExerciseEquipmentRequest request) {
        ExerciseEquipment exerciseEquipment = exerciseEquipmentRepository.findById(exerciseEquipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("ExerciseEquipment", "id", exerciseEquipmentId));

        exerciseEquipmentMapper.updateEntityFromRequest(request, exerciseEquipment);
        ExerciseEquipment updated = exerciseEquipmentRepository.save(exerciseEquipment);
        return exerciseEquipmentMapper.toDto(updated);
    }

    @Override
    public void deleteExerciseEquipment(UUID exerciseEquipmentId) {
        if (!exerciseEquipmentRepository.existsById(exerciseEquipmentId)) {
            throw new ResourceNotFoundException("ExerciseEquipment", "id", exerciseEquipmentId);
        }
        exerciseEquipmentRepository.deleteById(exerciseEquipmentId);
    }

    @Override
    public void deleteByExerciseId(UUID exerciseId) {
        exerciseEquipmentRepository.deleteByExercise_ExerciseId(exerciseId);
    }

    @Override
    public void deleteByEquipmentId(UUID equipmentId) {
        exerciseEquipmentRepository.deleteByEquipment_EquipmentId(equipmentId);
    }

    @Override
    public boolean existsByExerciseAndEquipment(UUID exerciseId, UUID equipmentId) {
        return exerciseEquipmentRepository.existsByExercise_ExerciseIdAndEquipment_EquipmentId(exerciseId, equipmentId);
    }
}
