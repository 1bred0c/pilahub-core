package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.ExerciseDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateExerciseRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateExerciseRequest;
import fpt.edu.sep490.pilahub.enums.DifficultyLevel;
import fpt.edu.sep490.pilahub.enums.ExerciseType;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.ExerciseMapper;
import fpt.edu.sep490.pilahub.pojo.BodyPart;
import fpt.edu.sep490.pilahub.pojo.Exercise;
import fpt.edu.sep490.pilahub.pojo.Tutorial;
import fpt.edu.sep490.pilahub.repository.BodyPartRepository;
import fpt.edu.sep490.pilahub.repository.ExerciseRepository;
import fpt.edu.sep490.pilahub.repository.TutorialRepository;
import fpt.edu.sep490.pilahub.repository.WorkoutSessionRepository;
import fpt.edu.sep490.pilahub.service.ExerciseService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final BodyPartRepository bodyPartRepository;
    private final ExerciseMapper exerciseMapper;
    private final TutorialRepository tutorialRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final SecurityUtil securityUtil;

    @Override
    public ExerciseDto createExercise(CreateExerciseRequest request) {
        Exercise exercise = exerciseMapper.toEntity(request);

        if (request.bodyParts() != null && !request.bodyParts().isEmpty()) {
            Set<BodyPart> bodyParts = new HashSet<>();

            // Chuẩn bị danh sách tên đã chuẩn hóa
            Set<String> normalizedNames = request.bodyParts().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());

            if (!normalizedNames.isEmpty()) {
                // Tìm tất cả bodyParts đã tồn tại trong 1 query
                Map<String, BodyPart> existingBodyParts = bodyPartRepository
                        .findByNameIn(normalizedNames)
                        .stream()
                        .collect(Collectors.toMap(
                                BodyPart::getName,
                                Function.identity()));

                // Xác định các bodyPart cần tạo mới
                List<BodyPart> newBodyParts = normalizedNames.stream()
                        .filter(name -> !existingBodyParts.containsKey(name))
                        .map(name -> BodyPart.builder().name(name).build())
                        .collect(Collectors.toList());

                // Lưu tất cả bodyPart mới trong 1 query (batch insert)
                if (!newBodyParts.isEmpty()) {
                    bodyPartRepository.saveAll(newBodyParts);
                    newBodyParts.forEach(bp -> existingBodyParts.put(bp.getName(), bp));
                }

                // Thêm tất cả vào set
                normalizedNames.forEach(name -> bodyParts.add(existingBodyParts.get(name)));
            }

            exercise.setBodyParts(bodyParts);
        }

        Exercise saved = exerciseRepository.save(exercise);
        return toExerciseDto(saved);
    }

    @Override
    public ExerciseDto getById(UUID exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", exerciseId));
        return toExerciseDto(exercise);
    }

    @Override
    public ExerciseDto getActiveById(UUID exerciseId) {
        Exercise exercise = exerciseRepository.findByExerciseIdAndActiveTrue(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", exerciseId));
        return toExerciseDto(exercise);
    }

    @Override
    public List<ExerciseDto> getAll() {
        return toExerciseDtos(exerciseRepository.findAll());
    }

    @Override
    public List<ExerciseDto> getAllActive() {
        return toExerciseDtos(exerciseRepository.findByActiveTrue());
    }

    @Override
    public List<ExerciseDto> searchByName(String name) {
        return toExerciseDtos(exerciseRepository.findByNameContainingIgnoreCase(name));
    }

    @Override
    public List<ExerciseDto> getByDifficultyLevel(String difficultyLevel) {
        DifficultyLevel level = DifficultyLevel.valueOf(difficultyLevel);
        return toExerciseDtos(exerciseRepository.findByDifficultyLevel(level));
    }

    @Override
    public List<ExerciseDto> getActiveByDifficultyLevel(String difficultyLevel) {
        DifficultyLevel level = DifficultyLevel.valueOf(difficultyLevel);
        return toExerciseDtos(exerciseRepository.findByDifficultyLevelAndActiveTrue(level));
    }

    @Override
    public ExerciseDto updateExercise(UUID exerciseId, UpdateExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", exerciseId));

        exerciseMapper.updateEntityFromRequest(request, exercise);

        if (request.bodyParts() != null) {
            Set<BodyPart> bodyParts = new HashSet<>();
            for (String bodyPartName : request.bodyParts()) {
                BodyPart bodyPart = bodyPartRepository.findByName(bodyPartName)
                        .orElseGet(() -> {
                            BodyPart newBodyPart = BodyPart.builder()
                                    .name(bodyPartName)
                                    .build();
                            return bodyPartRepository.save(newBodyPart);
                        });
                bodyParts.add(bodyPart);
            }
            exercise.setBodyParts(bodyParts);
        }

        Exercise updated = exerciseRepository.save(exercise);
        return toExerciseDto(updated);
    }

    private ExerciseDto toExerciseDto(Exercise exercise) {
        UUID traineeId = getCurrentTraineeId();
        boolean havePracticed = traineeId != null
                && workoutSessionRepository.existsByTrainee_TraineeIdAndExercise_ExerciseId(traineeId,
                        exercise.getExerciseId());
        return exerciseMapper.toDto(exercise, havePracticed);
    }

    private List<ExerciseDto> toExerciseDtos(List<Exercise> exercises) {
        UUID traineeId = getCurrentTraineeId();
        if (traineeId == null) {
            return exercises.stream()
                    .map(exerciseMapper::toDto)
                    .collect(Collectors.toList());
        }

        Set<UUID> practicedExerciseIds = workoutSessionRepository.findDistinctExerciseIdsByTraineeId(traineeId);
        return exercises.stream()
                .map(exercise -> exerciseMapper.toDto(exercise,
                        practicedExerciseIds.contains(exercise.getExerciseId())))
                .collect(Collectors.toList());
    }

    private UUID getCurrentTraineeId() {
        try {
            if (securityUtil.getCurrentUserRole() == Role.TRAINEE) {
                return securityUtil.getCurrentUserId();
            }
        } catch (RuntimeException ignored) {
            return null;
        }
        return null;
    }

    @Transactional
    @Override
    public void activateExercise(UUID exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", exerciseId));

        Tutorial tutorial = tutorialRepository.findByExercise_ExerciseId(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutorial does not exith with exercise: ", "id",
                        exerciseId));
        exercise.setActive(true);
        tutorial.setPublished(true);
        exerciseRepository.save(exercise);
        tutorialRepository.save(tutorial);
    }

    @Transactional
    @Override
    public void deactivateExercise(UUID exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", exerciseId));
        Tutorial tutorial = tutorialRepository.findByExercise_ExerciseId(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutorial does not exith with exercise: ", "id",
                        exerciseId));

        exercise.setActive(false);
        tutorial.setPublished(false);
        exerciseRepository.save(exercise);
        tutorialRepository.save(tutorial);
        ;
    }

    @Override
    public void deleteExercise(UUID exerciseId) {
        if (!exerciseRepository.existsById(exerciseId)) {
            throw new ResourceNotFoundException("Exercise", "id", exerciseId);
        }
        if (tutorialRepository.existsByExercise_ExerciseId(exerciseId)) {
            tutorialRepository.deleteByExercise_ExerciseId(exerciseId);
        }
        exerciseRepository.deleteById(exerciseId);
    }
}
