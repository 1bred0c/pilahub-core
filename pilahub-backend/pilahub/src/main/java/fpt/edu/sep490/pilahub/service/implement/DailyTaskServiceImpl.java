package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.CoachBookingDto;
import fpt.edu.sep490.pilahub.dto.CourseLessonProgressDto;
import fpt.edu.sep490.pilahub.dto.PersonalScheduleDto;
import fpt.edu.sep490.pilahub.dto.response.DailyTaskResponse;
import fpt.edu.sep490.pilahub.mapper.CoachBookingMapper;
import fpt.edu.sep490.pilahub.mapper.CourseLessonProgressMapper;
import fpt.edu.sep490.pilahub.mapper.PersonalScheduleMapper;
import fpt.edu.sep490.pilahub.repository.CoachBookingRepository;
import fpt.edu.sep490.pilahub.repository.CourseLessonProgressRepository;
import fpt.edu.sep490.pilahub.repository.PersonalScheduleRepository;
import fpt.edu.sep490.pilahub.service.DailyTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyTaskServiceImpl implements DailyTaskService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final CoachBookingRepository coachBookingRepository;
    private final PersonalScheduleRepository personalScheduleRepository;
    private final CourseLessonProgressRepository courseLessonProgressRepository;
    private final CoachBookingMapper coachBookingMapper;
    private final PersonalScheduleMapper personalScheduleMapper;
    private final CourseLessonProgressMapper courseLessonProgressMapper;

    @Override
    public DailyTaskResponse getDailyTasks(UUID traineeId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now(DEFAULT_ZONE);
        Instant startOfDay = targetDate.atStartOfDay(DEFAULT_ZONE).toInstant();
        Instant endOfDay = targetDate.plusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant();

        List<CoachBookingDto> bookings = coachBookingRepository
                .findByTrainee_TraineeIdAndStartTimeLessThanAndEndTimeGreaterThanOrderByStartTimeAsc(
                        traineeId, endOfDay, startOfDay)
                .stream()
                .map(coachBookingMapper::toDto)
                .collect(Collectors.toList());

        List<PersonalScheduleDto> roadmapSchedules = personalScheduleRepository
                .findByPersonalStage_Roadmap_Trainee_TraineeIdAndScheduledDateGreaterThanEqualAndScheduledDateLessThanOrderByScheduledDateAsc(
                        traineeId, startOfDay, endOfDay)
                .stream()
                .map(personalScheduleMapper::toDto)
                .collect(Collectors.toList());

        List<CourseLessonProgressDto> courseSchedules = courseLessonProgressRepository
                .findByTraineeCourse_Trainee_TraineeIdAndStartedAtGreaterThanEqualAndStartedAtLessThanOrderByStartedAtAsc(
                        traineeId, startOfDay, endOfDay)
                .stream()
                .map(courseLessonProgressMapper::toDto)
                .collect(Collectors.toList());

        return new DailyTaskResponse(targetDate, startOfDay, endOfDay, bookings, roadmapSchedules, courseSchedules);
    }
}