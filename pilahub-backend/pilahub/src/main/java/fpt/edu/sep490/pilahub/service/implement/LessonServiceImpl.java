package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.LessonDto;
import fpt.edu.sep490.pilahub.dto.request.lesson.CreateLessonRequest;
import fpt.edu.sep490.pilahub.dto.request.lesson.UpdateLessonRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.LessonMapper;
import fpt.edu.sep490.pilahub.pojo.Lesson;
import fpt.edu.sep490.pilahub.repository.LessonRepository;
import fpt.edu.sep490.pilahub.service.LessonService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;

    @Override
    public LessonDto createLesson(CreateLessonRequest request) {
        Lesson lesson = lessonMapper.toEntity(request);
        Lesson saved = lessonRepository.save(lesson);
        return lessonMapper.toDto(saved);
    }

    @Override
    public LessonDto getById(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        return lessonMapper.toDto(lesson);
    }

    @Override
    public List<LessonDto> getAll() {
        return lessonRepository.findByActiveTrue().stream()
                .map(lessonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LessonDto> searchByName(String name) {
        return lessonRepository.findByNameContainingIgnoreCase(name).stream()
                .map(lessonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LessonDto updateLesson(UUID lessonId, UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        lessonMapper.updateEntityFromRequest(request, lesson);

        Lesson updated = lessonRepository.save(lesson);
        return lessonMapper.toDto(updated);
    }

    @Override
    public void deactivateLesson(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        lesson.setActive(false);
        lessonRepository.save(lesson);
    }

    @Override
    public void deleteLesson(UUID lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson", "id", lessonId);
        }
        lessonRepository.deleteById(lessonId);
    }
}
