package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.CourseDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateCourseRequest;
import fpt.edu.sep490.pilahub.dto.request.course.UpdateCourseRequest;
import fpt.edu.sep490.pilahub.dto.response.CourseEditDetailsResponse;
import fpt.edu.sep490.pilahub.dto.response.CourseWithDetailsResponse;
import fpt.edu.sep490.pilahub.enums.DifficultyLevel;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.CourseMapper;
import fpt.edu.sep490.pilahub.mapper.ExerciseMapper;
import fpt.edu.sep490.pilahub.mapper.LessonMapper;
import fpt.edu.sep490.pilahub.pojo.Course;
import fpt.edu.sep490.pilahub.pojo.CourseLesson;
import fpt.edu.sep490.pilahub.pojo.LessonExercise;
import fpt.edu.sep490.pilahub.repository.CourseLessonRepository;
import fpt.edu.sep490.pilahub.repository.CourseRepository;
import fpt.edu.sep490.pilahub.repository.LessonExerciseRepository;
import fpt.edu.sep490.pilahub.service.CourseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

        private final CourseRepository courseRepository;
        private final CourseLessonRepository courseLessonRepository;
        private final LessonExerciseRepository lessonExerciseRepository;
        private final CourseMapper courseMapper;
        private final LessonMapper lessonMapper;
        private final ExerciseMapper exerciseMapper;

        @Override
        public CourseDto createCourse(CreateCourseRequest request) {
                Course course = courseMapper.toEntity(request);

                Course saved = courseRepository.save(course);
                return courseMapper.toDto(saved);
        }

        @Override
        public CourseDto getById(UUID courseId) {
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
                return courseMapper.toDto(course);
        }

        @Override
        public CourseWithDetailsResponse getCourseWithDetails(UUID courseId) {
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

                List<CourseLesson> courseLessons = courseLessonRepository
                                .findByCourse_CourseIdOrderByDisplayOrderAsc(courseId);

                List<CourseWithDetailsResponse.LessonWithExercises> lessonDetails = courseLessons.stream()
                                .map(cl -> {
                                        List<LessonExercise> lessonExercises = lessonExerciseRepository
                                                        .findByLesson_LessonIdOrderByDisplayOrderAsc(
                                                                        cl.getLesson().getLessonId());

                                        List<CourseWithDetailsResponse.ExerciseInLesson> exercises = lessonExercises
                                                        .stream()
                                                        .map(le -> new CourseWithDetailsResponse.ExerciseInLesson(
                                                                        le.getLessonExerciseId(),
                                                                        le.getDisplayOrder(),
                                                                        le.getSets(),
                                                                        le.getReps(),
                                                                        le.getDurationSeconds(),
                                                                        le.getRestSeconds(),
                                                                        le.getNotes(),
                                                                        exerciseMapper.toDto(le.getExercise())))
                                                        .collect(Collectors.toList());

                                        return new CourseWithDetailsResponse.LessonWithExercises(
                                                        cl.getCourseLessonId(),
                                                        cl.getDisplayOrder(),
                                                        cl.getNotes(),
                                                        lessonMapper.toDto(cl.getLesson()),
                                                        exercises);
                                })
                                .collect(Collectors.toList());

                return new CourseWithDetailsResponse(courseMapper.toDto(course), lessonDetails);
        }

        @Override
        public CourseEditDetailsResponse getCourseEditDetails(UUID courseId) {
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

                List<CourseLesson> courseLessons = courseLessonRepository
                                .findByCourse_CourseIdOrderByDisplayOrderAsc(courseId);

                List<CourseEditDetailsResponse.LessonForEdit> lessons = courseLessons.stream()
                                .map(cl -> {
                                        List<LessonExercise> lessonExercises = lessonExerciseRepository
                                                        .findByLesson_LessonIdOrderByDisplayOrderAsc(
                                                                        cl.getLesson().getLessonId());

                                        List<CourseEditDetailsResponse.ExerciseForEdit> exercises = lessonExercises
                                                        .stream()
                                                        .map(le -> new CourseEditDetailsResponse.ExerciseForEdit(
                                                                        le.getLessonExerciseId(),
                                                                        le.getExercise().getExerciseId(),
                                                                        le.getDisplayOrder(),
                                                                        le.getSets(),
                                                                        le.getReps(),
                                                                        le.getDurationSeconds(),
                                                                        le.getRestSeconds(),
                                                                        le.getNotes(),
                                                                        le.getExercise().getName(),
                                                                        le.getExercise().getDescription(),
                                                                        le.getExercise().getDuration(),
                                                                        le.getExercise().getExerciseType() != null
                                                                                        ? le.getExercise()
                                                                                                        .getExerciseType()
                                                                                                        .name()
                                                                                        : null,
                                                                        le.getExercise().getDifficultyLevel() != null
                                                                                        ? le.getExercise()
                                                                                                        .getDifficultyLevel()
                                                                                                        .name()
                                                                                        : null,
                                                                        le.getExercise().getBodyParts().stream()
                                                                                        .map(bp -> bp.getName())
                                                                                        .collect(Collectors.toList()),
                                                                        le.getExercise().isEquipmentRequired(),
                                                                        le.getExercise().getImageUrl(),
                                                                        le.getExercise().getBenefits(),
                                                                        le.getExercise().getPrerequisites(),
                                                                        le.getExercise().getContraindications(),
                                                                        le.getExercise().isHaveAIsupported(),
                                                                        le.getExercise().getNameInModelAI(),
                                                                        le.getExercise().getBreathingRule() != null
                                                                                        ? le.getExercise()
                                                                                                        .getBreathingRule()
                                                                                                        .name()
                                                                                        : null,
                                                                        null,
                                                                        le.getExercise().isActive()))
                                                        .collect(Collectors.toList());

                                        return new CourseEditDetailsResponse.LessonForEdit(
                                                        cl.getCourseLessonId(),
                                                        cl.getLesson().getLessonId(),
                                                        cl.getDisplayOrder(),
                                                        cl.getNotes(),
                                                        cl.getLesson().getName(),
                                                        cl.getLesson().getDescription(),
                                                        cl.getLesson().isActive(),
                                                        exercises);
                                })
                                .collect(Collectors.toList());

                return new CourseEditDetailsResponse(
                                course.getCourseId(),
                                course.getName(),
                                course.getDescription(),
                                course.getImageUrl(),
                                course.getLevel() != null ? course.getLevel().name() : null,
                                course.getPrice(),
                                course.isActive(),
                                lessons);
        }

        @Override
        public List<CourseDto> getAll() {
                return courseRepository.findAll().stream()
                                .map(courseMapper::toDto)
                                .collect(Collectors.toList());
        }

        @Override
        public List<CourseDto> getAllActive() {
                return courseRepository.findByActiveTrue().stream()
                                .map(courseMapper::toDto)
                                .collect(Collectors.toList());
        }

        @Override
        public List<CourseDto> searchByName(String name) {
                return courseRepository.findByNameContainingIgnoreCase(name).stream()
                                .map(courseMapper::toDto)
                                .collect(Collectors.toList());
        }

        @Override
        public List<CourseDto> getByLevel(String level) {
                DifficultyLevel difficultyLevel = DifficultyLevel.valueOf(level);
                return courseRepository.findByLevel(difficultyLevel).stream()
                                .map(courseMapper::toDto)
                                .collect(Collectors.toList());
        }

        @Override
        public List<CourseDto> getActiveByLevel(String level) {
                DifficultyLevel difficultyLevel = DifficultyLevel.valueOf(level);
                return courseRepository.findByLevelAndActiveTrue(difficultyLevel).stream()
                                .map(courseMapper::toDto)
                                .collect(Collectors.toList());
        }

        @Override
        public CourseDto updateCourse(UUID courseId, UpdateCourseRequest request) {
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

                courseMapper.updateEntityFromRequest(request, course);

                Course updated = courseRepository.save(course);
                return courseMapper.toDto(updated);
        }

        @Override
        public void activateCourse(UUID courseId) {
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

                course.setActive(true);
                courseRepository.save(course);
        }

        @Override
        public void deactivateCourse(UUID courseId) {
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

                course.setActive(false);
                courseRepository.save(course);
        }

        @Override
        public void deleteCourse(UUID courseId) {
                if (!courseRepository.existsById(courseId)) {
                        throw new ResourceNotFoundException("Course", "id", courseId);
                }
                courseRepository.deleteById(courseId);
        }
}
