package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.CourseLessonDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateCourseLessonRequest;
import fpt.edu.sep490.pilahub.dto.request.course.UpdateCourseLessonRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.CourseLessonMapper;
import fpt.edu.sep490.pilahub.pojo.Course;
import fpt.edu.sep490.pilahub.pojo.CourseLesson;
import fpt.edu.sep490.pilahub.pojo.Lesson;
import fpt.edu.sep490.pilahub.repository.CourseLessonRepository;
import fpt.edu.sep490.pilahub.repository.CourseRepository;
import fpt.edu.sep490.pilahub.repository.LessonRepository;
import fpt.edu.sep490.pilahub.service.CourseLessonService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseLessonServiceImpl implements CourseLessonService {

    private final CourseLessonRepository courseLessonRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CourseLessonMapper courseLessonMapper;

    @Override
    @Transactional
    public List<CourseLessonDto> createCourseLesson(UUID courseId, List<CreateCourseLessonRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Tìm course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // 2. Tập hợp lessonIds để query
        Set<UUID> lessonIds = requests.stream()
                .map(CreateCourseLessonRequest::lessonId)
                .collect(Collectors.toSet());

        // 3. Query tất cả lessons
        Map<UUID, Lesson> lessonMap = lessonRepository.findAllById(lessonIds)
                .stream()
                .collect(Collectors.toMap(Lesson::getLessonId, lesson -> lesson));

        // 4. Validate tất cả lessons tồn tại
        List<UUID> notExistedLessonIds = lessonIds.stream()
                .filter(id -> !lessonMap.containsKey(id))
                .collect(Collectors.toList());

        if (!notExistedLessonIds.isEmpty()) {
            throw new ResourceNotFoundException("Lessons", "ids", notExistedLessonIds);
        }

        // 5. Tạo course lessons
        List<CourseLesson> courseLessons = requests.stream()
                .map(request -> {
                    CourseLesson courseLesson = courseLessonMapper.toEntity(request);
                    courseLesson.setCourse(course); // ← THÊM COURSE
                    courseLesson.setLesson(lessonMap.get(request.lessonId()));
                    return courseLesson;
                })
                .collect(Collectors.toList());

        // 6. Kiểm tra trùng lặp nếu cần (cùng lesson trong cùng course)
        // Tùy business logic: có cho phép trùng lesson trong cùng course không?
//        validateDuplicates(courseId, courseLessons);

        List<CourseLesson> savedCourseLessons = courseLessonRepository.saveAll(courseLessons);

        return savedCourseLessons.stream()
                .map(courseLessonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CourseLessonDto getById(UUID courseLessonId) {
        CourseLesson courseLesson = courseLessonRepository.findById(courseLessonId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseLesson", "id", courseLessonId));
        return courseLessonMapper.toDto(courseLesson);
    }

    @Override
    public List<CourseLessonDto> getByCourseId(UUID courseId) {
        return courseLessonRepository.findByCourse_CourseId(courseId).stream()
                .map(courseLessonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseLessonDto> getByCourseIdOrdered(UUID courseId) {
        return courseLessonRepository.findByCourse_CourseIdOrderByDisplayOrderAsc(courseId).stream()
                .map(courseLessonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CourseLessonDto updateCourseLesson(UUID courseLessonId, UpdateCourseLessonRequest request) {
        CourseLesson courseLesson = courseLessonRepository.findById(courseLessonId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseLesson", "id", courseLessonId));

        courseLessonMapper.updateEntityFromRequest(request, courseLesson);

        CourseLesson updated = courseLessonRepository.save(courseLesson);
        return courseLessonMapper.toDto(updated);
    }

    @Override
    public void deleteCourseLesson(UUID courseLessonId) {
        if (!courseLessonRepository.existsById(courseLessonId)) {
            throw new ResourceNotFoundException("CourseLesson", "id", courseLessonId);
        }
        courseLessonRepository.deleteById(courseLessonId);
    }

    @Override
    public void deleteByCourseId(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        courseLessonRepository.deleteByCourse_CourseId(courseId);
    }
}
