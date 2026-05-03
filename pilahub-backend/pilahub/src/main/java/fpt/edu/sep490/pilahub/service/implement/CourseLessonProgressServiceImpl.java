package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.CourseLessonProgressDto;
import fpt.edu.sep490.pilahub.enums.NotificationType;
import fpt.edu.sep490.pilahub.event.NotificationEvent;
import fpt.edu.sep490.pilahub.dto.request.course.CreateCourseLessonProgressRequest;
import fpt.edu.sep490.pilahub.dto.request.course.UpdateCourseLessonProgressRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.CourseLessonProgressMapper;
import fpt.edu.sep490.pilahub.pojo.CourseLesson;
import fpt.edu.sep490.pilahub.pojo.CourseLessonProgress;
import fpt.edu.sep490.pilahub.pojo.TraineeCourse;
import fpt.edu.sep490.pilahub.repository.CourseLessonProgressRepository;
import fpt.edu.sep490.pilahub.repository.CourseLessonRepository;
import fpt.edu.sep490.pilahub.repository.TraineeCourseRepository;
import fpt.edu.sep490.pilahub.service.CourseLessonProgressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseLessonProgressServiceImpl implements CourseLessonProgressService {

    private final CourseLessonProgressRepository courseLessonProgressRepository;
    private final TraineeCourseRepository traineeCourseRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final CourseLessonProgressMapper courseLessonProgressMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public List<CourseLessonProgressDto> createProgress(CreateCourseLessonProgressRequest scheduleRequest) {
        // Validate trainee course exists
        TraineeCourse traineeCourse = traineeCourseRepository.findById(scheduleRequest.traineeCourseId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("TraineeCourse", "id", scheduleRequest.traineeCourseId()));

        // Get all lessons for the course, ordered by lesson order
        List<CourseLesson> courseLessons = courseLessonRepository
                .findByCourse_CourseIdOrderByDisplayOrderAsc(traineeCourse.getCourse().getCourseId());

        if (courseLessons.isEmpty()) {
            throw new IllegalStateException("Course has no lessons");
        }

        boolean hasSchedule = scheduleRequest.startDate() != null
                && scheduleRequest.trainingDays() != null
                && !scheduleRequest.trainingDays().isEmpty();

        List<LocalDate> scheduledDates = new ArrayList<>();

        if (hasSchedule) {
            List<DayOfWeek> days = scheduleRequest.trainingDays()
                    .stream()
                    .map(DayOfWeek::of)
                    .toList();

            scheduledDates = calculateScheduleDates(
                    scheduleRequest.startDate(),
                    days,
                    courseLessons.size());

            if (scheduledDates.size() < courseLessons.size()) {
                throw new IllegalArgumentException(
                        String.format("Cannot schedule %d lessons with only %d training days per week.",
                                courseLessons.size(), scheduleRequest.trainingDays().size()));
            }
        }

        List<CourseLessonProgress> progressList = new ArrayList<>();

        for (int i = 0; i < courseLessons.size(); i++) {
            CourseLesson courseLesson = courseLessons.get(i);

            // Check duplicate
            if (courseLessonProgressRepository
                    .existsByTraineeCourse_TraineeCourseIdAndCourseLesson_CourseLessonId(
                            scheduleRequest.traineeCourseId(), courseLesson.getCourseLessonId())) {
                throw new IllegalStateException(
                        String.format("Progress already exists for lesson: %s",
                                courseLesson.getLesson().getName()));
            }

            Instant lessonStartTime = null;

            if (hasSchedule) {
                LocalDate scheduledDate = scheduledDates.get(i);
                lessonStartTime = scheduledDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            }

            CourseLessonProgress progress = CourseLessonProgress.builder()
                    .traineeCourse(traineeCourse)
                    .courseLesson(courseLesson)
                    .startedAt(lessonStartTime) // null nếu không có lịch
                    .completed(false)
                    .build();

            progressList.add(progress);
        }

        return courseLessonProgressRepository.saveAll(progressList)
                .stream()
                .map(courseLessonProgressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<CourseLessonProgressDto> resetProgressAndReschedule(CreateCourseLessonProgressRequest scheduleRequest) {
        TraineeCourse traineeCourse = traineeCourseRepository.findById(scheduleRequest.traineeCourseId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("TraineeCourse", "id", scheduleRequest.traineeCourseId()));

        List<CourseLesson> courseLessons = courseLessonRepository
                .findByCourse_CourseIdOrderByDisplayOrderAsc(traineeCourse.getCourse().getCourseId());

        if (courseLessons.isEmpty()) {
            throw new IllegalStateException("Course has no lessons");
        }

        if (scheduleRequest.trainingDays() == null || scheduleRequest.trainingDays().isEmpty()) {
            throw new IllegalArgumentException("Training days list must not be empty");
        }

        List<DayOfWeek> days = scheduleRequest.trainingDays()
                .stream()
                .map(DayOfWeek::of)
                .toList();

        List<LocalDate> scheduledDates = calculateScheduleDates(
                scheduleRequest.startDate(),
                days,
                courseLessons.size());

        if (scheduledDates.size() < courseLessons.size()) {
            throw new IllegalArgumentException(
                    String.format("Cannot schedule %d lessons with only %d training days per week. " +
                            "Need more weeks or training days.",
                            courseLessons.size(), scheduleRequest.trainingDays().size()));
        }

        List<CourseLessonProgress> existingProgress = courseLessonProgressRepository
                .findByTraineeCourse_TraineeCourseId(scheduleRequest.traineeCourseId());

        Map<UUID, CourseLessonProgress> progressByLessonId = new HashMap<>();
        for (CourseLessonProgress progress : existingProgress) {
            progressByLessonId.put(progress.getCourseLesson().getCourseLessonId(), progress);
        }

        Set<UUID> currentLessonIds = courseLessons.stream()
                .map(CourseLesson::getCourseLessonId)
                .collect(Collectors.toCollection(HashSet::new));

        List<CourseLessonProgress> staleProgress = existingProgress.stream()
                .filter(progress -> !currentLessonIds.contains(progress.getCourseLesson().getCourseLessonId()))
                .collect(Collectors.toList());
        if (!staleProgress.isEmpty()) {
            courseLessonProgressRepository.deleteAll(staleProgress);
        }

        List<CourseLessonProgress> toSave = new ArrayList<>();
        for (int i = 0; i < courseLessons.size(); i++) {
            CourseLesson lesson = courseLessons.get(i);
            LocalDate scheduledDate = scheduledDates.get(i);
            Instant lessonStartTime = scheduledDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

            CourseLessonProgress progress = progressByLessonId.get(lesson.getCourseLessonId());
            if (progress == null) {
                progress = CourseLessonProgress.builder()
                        .traineeCourse(traineeCourse)
                        .courseLesson(lesson)
                        .build();
            }

            progress.setStartedAt(lessonStartTime);
            progress.setCompleted(false);
            progress.setCompletedAt(null);
            toSave.add(progress);
        }

        List<CourseLessonProgress> saved = courseLessonProgressRepository.saveAll(toSave);
        recalculateAndUpdateTraineeCourseProgress(scheduleRequest.traineeCourseId());

        return saved.stream()
                .map(courseLessonProgressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<CourseLessonProgressDto> rescheduleIncompleteProgress(
            CreateCourseLessonProgressRequest scheduleRequest) {
        if (!traineeCourseRepository.existsById(scheduleRequest.traineeCourseId())) {
            throw new ResourceNotFoundException("TraineeCourse", "id", scheduleRequest.traineeCourseId());
        }

        if (scheduleRequest.trainingDays() == null || scheduleRequest.trainingDays().isEmpty()) {
            throw new IllegalArgumentException("Training days list must not be empty");
        }

        List<CourseLessonProgress> incompleteProgress = courseLessonProgressRepository
                .findByTraineeCourse_TraineeCourseIdAndCompletedFalse(scheduleRequest.traineeCourseId())
                .stream()
                .sorted((a, b) -> Integer.compare(
                        a.getCourseLesson().getDisplayOrder(),
                        b.getCourseLesson().getDisplayOrder()))
                .collect(Collectors.toList());

        if (incompleteProgress.isEmpty()) {
            throw new IllegalStateException("No incomplete lesson progress found to reschedule");
        }

        List<DayOfWeek> days = scheduleRequest.trainingDays()
                .stream()
                .map(DayOfWeek::of)
                .toList();

        List<LocalDate> scheduledDates = calculateScheduleDates(
                scheduleRequest.startDate(),
                days,
                incompleteProgress.size());

        if (scheduledDates.size() < incompleteProgress.size()) {
            throw new IllegalArgumentException(
                    String.format("Cannot schedule %d lessons with only %d training days per week. " +
                            "Need more weeks or training days.",
                            incompleteProgress.size(), scheduleRequest.trainingDays().size()));
        }

        for (int i = 0; i < incompleteProgress.size(); i++) {
            LocalDate scheduledDate = scheduledDates.get(i);
            Instant lessonStartTime = scheduledDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

            CourseLessonProgress progress = incompleteProgress.get(i);
            progress.setStartedAt(lessonStartTime);
            progress.setCompleted(false);
            progress.setCompletedAt(null);
        }

        List<CourseLessonProgress> saved = courseLessonProgressRepository.saveAll(incompleteProgress);
        recalculateAndUpdateTraineeCourseProgress(scheduleRequest.traineeCourseId());

        return saved.stream()
                .map(courseLessonProgressMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Calculate lesson dates from the provided start date (inclusive) using the
     * selected training days until enough dates are produced.
     */
    private List<LocalDate> calculateScheduleDates(LocalDate startDate,
            List<DayOfWeek> trainingDays,
            int totalLessons) {

        List<LocalDate> scheduledDates = new ArrayList<>();

        List<DayOfWeek> sortedDays = trainingDays.stream()
                .distinct()
                .sorted()
                .toList();

        Set<DayOfWeek> trainingDaySet = new HashSet<>(sortedDays);

        LocalDate currentDate = startDate;
        LocalDate today = LocalDate.now();

        if (currentDate.equals(today)) {
            currentDate = currentDate.plusDays(1);
        }

        while (!trainingDaySet.contains(currentDate.getDayOfWeek())) {
            currentDate = currentDate.plusDays(1);
        }

        while (scheduledDates.size() < totalLessons) {
            if (trainingDaySet.contains(currentDate.getDayOfWeek())) {
                scheduledDates.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        return scheduledDates;
    }

    @Override
    public CourseLessonProgressDto getById(UUID progressId) {
        CourseLessonProgress progress = courseLessonProgressRepository.findById(progressId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseLessonProgress", "id", progressId));
        return courseLessonProgressMapper.toDto(progress);
    }

    @Override
    public List<CourseLessonProgressDto> getByTraineeCourseId(UUID traineeCourseId) {
        // Validate trainee course exists
        if (!traineeCourseRepository.existsById(traineeCourseId)) {
            throw new ResourceNotFoundException("TraineeCourse", "id", traineeCourseId);
        }

        return courseLessonProgressRepository.findByTraineeCourse_TraineeCourseId(traineeCourseId).stream()
                .map(courseLessonProgressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseLessonProgressDto> getByCourseLessonId(UUID courseLessonId) {
        // Validate course lesson exists
        if (!courseLessonRepository.existsById(courseLessonId)) {
            throw new ResourceNotFoundException("CourseLesson", "id", courseLessonId);
        }

        return courseLessonProgressRepository.findByCourseLesson_CourseLessonId(courseLessonId).stream()
                .map(courseLessonProgressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CourseLessonProgressDto getByTraineeCourseIdAndCourseLessonId(UUID traineeCourseId, UUID courseLessonId) {
        CourseLessonProgress progress = courseLessonProgressRepository
                .findByTraineeCourse_TraineeCourseIdAndCourseLesson_CourseLessonId(traineeCourseId, courseLessonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CourseLessonProgress not found for traineeCourseId: " + traineeCourseId +
                                " and courseLessonId: " + courseLessonId));
        return courseLessonProgressMapper.toDto(progress);
    }

    @Override
    public List<CourseLessonProgressDto> getCompletedByTraineeCourseId(UUID traineeCourseId) {
        // Validate trainee course exists
        if (!traineeCourseRepository.existsById(traineeCourseId)) {
            throw new ResourceNotFoundException("TraineeCourse", "id", traineeCourseId);
        }

        return courseLessonProgressRepository.findByTraineeCourse_TraineeCourseIdAndCompletedTrue(traineeCourseId)
                .stream()
                .map(courseLessonProgressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseLessonProgressDto> getIncompleteByTraineeCourseId(UUID traineeCourseId) {
        // Validate trainee course exists
        if (!traineeCourseRepository.existsById(traineeCourseId)) {
            throw new ResourceNotFoundException("TraineeCourse", "id", traineeCourseId);
        }

        return courseLessonProgressRepository.findByTraineeCourse_TraineeCourseIdAndCompletedFalse(traineeCourseId)
                .stream()
                .map(courseLessonProgressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CourseLessonProgressDto updateProgress(UUID progressId, UpdateCourseLessonProgressRequest request) {
        CourseLessonProgress progress = courseLessonProgressRepository.findById(progressId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseLessonProgress", "id", progressId));

        boolean wasCompleted = progress.isCompleted();
        courseLessonProgressMapper.updateEntityFromRequest(request, progress);

        if (!wasCompleted && progress.isCompleted()) {
            progress.setCompletedAt(Instant.now());
        } else if (wasCompleted && !progress.isCompleted()) {
            progress.setCompletedAt(null);
        }

        CourseLessonProgress updated = courseLessonProgressRepository.save(progress);
        recalculateAndUpdateTraineeCourseProgress(updated.getTraineeCourse().getTraineeCourseId());
        return courseLessonProgressMapper.toDto(updated);
    }

    @Override
    public CourseLessonProgressDto startLesson(UUID progressId) {
        CourseLessonProgress progress = courseLessonProgressRepository.findById(progressId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseLessonProgress", "id", progressId));

        if (progress.getStartedAt() == null) {
            progress.setStartedAt(Instant.now());
        }

        CourseLessonProgress updated = courseLessonProgressRepository.save(progress);
        return courseLessonProgressMapper.toDto(updated);
    }

    @Override
    public CourseLessonProgressDto markAsCompleted(UUID progressId) {
        CourseLessonProgress progress = courseLessonProgressRepository.findById(progressId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseLessonProgress", "id", progressId));

        progress.setCompleted(true);
        progress.setCompletedAt(Instant.now());

        CourseLessonProgress updated = courseLessonProgressRepository.save(progress);

        recalculateAndUpdateTraineeCourseProgress(updated.getTraineeCourse().getTraineeCourseId());

        UUID traineeId = updated.getTraineeCourse().getTrainee().getTraineeId();
        UUID traineeCourseId = updated.getTraineeCourse().getTraineeCourseId();
        String lessonName = updated.getCourseLesson().getLesson().getName();
        String courseName = updated.getTraineeCourse().getCourse().getName();

        // Notify: lesson completed
        eventPublisher.publishEvent(new NotificationEvent(
                this,
                traineeId,
                NotificationType.LESSON_COMPLETED,
                "Hoàn Thành Bài Học",
                "Tuyệt vời! Bạn đã hoàn thành \"" + lessonName + "\". Cố gắng tiếp tục nhé!",
                progressId, "COURSE_LESSON_PROGRESS"));

        // Notify: course completed (if no incomplete lessons remain)
        boolean allDone = courseLessonProgressRepository
                .findByTraineeCourse_TraineeCourseIdAndCompletedFalse(traineeCourseId)
                .isEmpty();
        if (allDone) {
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    traineeId,
                    NotificationType.COURSE_COMPLETED,
                    "Hoàn Thành Khóa Học!",
                    "Chúc mừng! Bạn đã hoàn thành khóa học \"" + courseName + "\". Làm tốt lắm!",
                    traineeCourseId, "TRAINEE_COURSE"));
        }

        return courseLessonProgressMapper.toDto(updated);
    }

    private void recalculateAndUpdateTraineeCourseProgress(UUID traineeCourseId) {
        List<CourseLessonProgress> progressList = courseLessonProgressRepository
                .findByTraineeCourse_TraineeCourseId(traineeCourseId);

        int totalLessons = progressList.size();
        long completedLessons = progressList.stream()
                .filter(CourseLessonProgress::isCompleted)
                .count();

        int progressPercentage = totalLessons == 0
                ? 0
                : (int) Math.round((completedLessons * 100.0) / totalLessons);

        TraineeCourse traineeCourse = traineeCourseRepository.findById(traineeCourseId)
                .orElseThrow(() -> new ResourceNotFoundException("TraineeCourse", "id", traineeCourseId));

        traineeCourse.setProgressPercentage(progressPercentage);
        traineeCourseRepository.save(traineeCourse);
    }

    @Override
    public void deleteProgress(UUID progressId) {
        if (!courseLessonProgressRepository.existsById(progressId)) {
            throw new ResourceNotFoundException("CourseLessonProgress", "id", progressId);
        }
        courseLessonProgressRepository.deleteById(progressId);
    }

    @Override
    public void deleteByTraineeCourseId(UUID traineeCourseId) {
        if (!traineeCourseRepository.existsById(traineeCourseId)) {
            throw new ResourceNotFoundException("TraineeCourse", "id", traineeCourseId);
        }
        courseLessonProgressRepository.deleteByTraineeCourse_TraineeCourseId(traineeCourseId);
    }

    @Override
    public void deleteByCourseLessonId(UUID courseLessonId) {
        if (!courseLessonRepository.existsById(courseLessonId)) {
            throw new ResourceNotFoundException("CourseLesson", "id", courseLessonId);
        }
        courseLessonProgressRepository.deleteByCourseLesson_CourseLessonId(courseLessonId);
    }

    @Override
    public boolean existsByTraineeCourseIdAndCourseLessonId(UUID traineeCourseId, UUID courseLessonId) {
        return courseLessonProgressRepository
                .existsByTraineeCourse_TraineeCourseIdAndCourseLesson_CourseLessonId(traineeCourseId, courseLessonId);
    }
}
