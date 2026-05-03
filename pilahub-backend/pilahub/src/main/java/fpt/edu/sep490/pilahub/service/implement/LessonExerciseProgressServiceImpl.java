package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.LessonExerciseProgressDto;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.LessonExerciseProgressMapper;
import fpt.edu.sep490.pilahub.pojo.LessonExerciseProgress;
import fpt.edu.sep490.pilahub.repository.CourseLessonProgressRepository;
import fpt.edu.sep490.pilahub.repository.LessonExerciseProgressRepository;
import fpt.edu.sep490.pilahub.service.LessonExerciseProgressService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonExerciseProgressServiceImpl implements LessonExerciseProgressService {

    private final LessonExerciseProgressRepository lessonExerciseProgressRepository;
    private final CourseLessonProgressRepository courseLessonProgressRepository;
    private final LessonExerciseProgressMapper lessonExerciseProgressMapper;
    private final SecurityUtil securityUtil;

    @Override
    public List<LessonExerciseProgressDto> getByCourseLessonProgressId(UUID courseLessonProgressId) {
        // Verify course lesson progress exists
        if (!courseLessonProgressRepository.existsById(courseLessonProgressId)) {
            throw new ResourceNotFoundException("CourseLessonProgress", "id", courseLessonProgressId);
        }

        return lessonExerciseProgressRepository.findByCourseLessonProgress_ProgressId(courseLessonProgressId).stream()
                .map(lessonExerciseProgressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LessonExerciseProgressDto> getCompletedByCourseLessonProgressId(UUID courseLessonProgressId) {
        // Verify course lesson progress exists
        if (!courseLessonProgressRepository.existsById(courseLessonProgressId)) {
            throw new ResourceNotFoundException("CourseLessonProgress", "id", courseLessonProgressId);
        }

        return lessonExerciseProgressRepository.findByCourseLessonProgress_ProgressIdAndCompletedTrue(courseLessonProgressId).stream()
                .map(lessonExerciseProgressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LessonExerciseProgressDto> getIncompleteByCourseLessonProgressId(UUID courseLessonProgressId) {
        // Verify course lesson progress exists
        if (!courseLessonProgressRepository.existsById(courseLessonProgressId)) {
            throw new ResourceNotFoundException("CourseLessonProgress", "id", courseLessonProgressId);
        }

        return lessonExerciseProgressRepository.findByCourseLessonProgress_ProgressIdAndCompletedFalse(courseLessonProgressId).stream()
                .map(lessonExerciseProgressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LessonExerciseProgressDto getById(UUID lessonExerciseProgressId) {
        LessonExerciseProgress progress = lessonExerciseProgressRepository.findById(lessonExerciseProgressId)
                .orElseThrow(() -> new ResourceNotFoundException("LessonExerciseProgress", "id", lessonExerciseProgressId));
        return lessonExerciseProgressMapper.toDto(progress);
    }
}

