package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.LessonExerciseDto;
import fpt.edu.sep490.pilahub.dto.request.lesson.CreateLessonExerciseRequest;
import fpt.edu.sep490.pilahub.dto.request.lesson.UpdateLessonExerciseRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.LessonExerciseMapper;
import fpt.edu.sep490.pilahub.pojo.Exercise;
import fpt.edu.sep490.pilahub.pojo.Lesson;
import fpt.edu.sep490.pilahub.pojo.LessonExercise;
import fpt.edu.sep490.pilahub.repository.ExerciseRepository;
import fpt.edu.sep490.pilahub.repository.LessonExerciseRepository;
import fpt.edu.sep490.pilahub.repository.LessonRepository;
import fpt.edu.sep490.pilahub.service.LessonExerciseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonExerciseServiceImpl implements LessonExerciseService {

    private final LessonExerciseRepository lessonExerciseRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final LessonExerciseMapper lessonExerciseMapper;

    @Override
    public List<LessonExerciseDto> createLessonExercise(UUID lessonId, List<CreateLessonExerciseRequest> requests) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        Set<UUID> exerciseIds = requests.stream().map(CreateLessonExerciseRequest::exerciseId).collect(Collectors.toSet());

        Map<UUID, Exercise> exerciseMap = exerciseRepository.findAllById(exerciseIds)
                .stream()
                .collect(Collectors.toMap(Exercise::getExerciseId, exercise -> exercise));

        List<UUID> notExistedExerciseIds = exerciseIds
                .stream()
                .filter(exerciseId -> !exerciseMap.containsKey(exerciseId))
                .collect(Collectors.toList());

        if (!notExistedExerciseIds.isEmpty()) {
            throw new ResourceNotFoundException("Exercise", "id", notExistedExerciseIds);
        }

        List<LessonExercise> lessonExercises = requests.stream()
                .map(request -> {
                    LessonExercise lessonExercise = new LessonExercise();
                    lessonExercise.setLesson(lesson);
                    lessonExercise.setExercise(exerciseMap.get(request.exerciseId()));
                    lessonExercise.setDisplayOrder(request.displayOrder());
                    lessonExercise.setSets(request.sets());
                    lessonExercise.setReps(request.reps());
                    lessonExercise.setDurationSeconds(request.durationSeconds());
                    lessonExercise.setRestSeconds(request.restSeconds());
                    lessonExercise.setNotes(request.notes());
                    return lessonExercise;
                })
                .collect(Collectors.toList());

        List<LessonExercise> savedLessonExercises = lessonExerciseRepository.saveAll(lessonExercises);

        return savedLessonExercises
                .stream()
                .map(lessonExerciseMapper::toDto)
                .collect(Collectors.toList());

    }

    @Override
    public LessonExerciseDto getById(UUID lessonExerciseId) {
        LessonExercise lessonExercise = lessonExerciseRepository.findById(lessonExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("LessonExercise", "id", lessonExerciseId));
        return lessonExerciseMapper.toDto(lessonExercise);
    }

    @Override
    public List<LessonExerciseDto> getByLessonId(UUID lessonId) {
        return lessonExerciseRepository.findByLesson_LessonId(lessonId).stream()
                .map(lessonExerciseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LessonExerciseDto> getByLessonIdOrdered(UUID lessonId) {
        return lessonExerciseRepository.findByLesson_LessonIdOrderByDisplayOrderAsc(lessonId).stream()
                .map(lessonExerciseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LessonExerciseDto updateLessonExercise(UUID lessonExerciseId, UpdateLessonExerciseRequest request) {
        LessonExercise lessonExercise = lessonExerciseRepository.findById(lessonExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("LessonExercise", "id", lessonExerciseId));

        lessonExerciseMapper.updateEntityFromRequest(request, lessonExercise);

        LessonExercise updated = lessonExerciseRepository.save(lessonExercise);
        return lessonExerciseMapper.toDto(updated);
    }

    @Override
    public void deleteLessonExercise(UUID lessonExerciseId) {
        if (!lessonExerciseRepository.existsById(lessonExerciseId)) {
            throw new ResourceNotFoundException("LessonExercise", "id", lessonExerciseId);
        }
        lessonExerciseRepository.deleteById(lessonExerciseId);
    }

    @Override
    public void deleteByLessonId(UUID lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson", "id", lessonId);
        }
        lessonExerciseRepository.deleteByLesson_LessonId(lessonId);
    }
}
