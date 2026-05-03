package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.PersonalExerciseDto;
import fpt.edu.sep490.pilahub.dto.PersonalScheduleDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalScheduleRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalScheduleRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PersonalScheduleService {

    PersonalScheduleDto createSchedule(CreatePersonalScheduleRequest request);

    PersonalScheduleDto getById(UUID personalScheduleId);

    List<PersonalScheduleDto> getByPersonalStageId(UUID personalStageId);

    List<PersonalScheduleDto> getByDateRange(Instant startDate, Instant endDate);

    PersonalScheduleDto updateSchedule(UUID personalScheduleId, UpdatePersonalScheduleRequest request);

    void deleteSchedule(UUID personalScheduleId);

    List<PersonalScheduleDto> getCompleted();

    List<PersonalScheduleDto> getIncomplete();

    PersonalScheduleDto markAsCompleted(UUID personalScheduleId);
}
