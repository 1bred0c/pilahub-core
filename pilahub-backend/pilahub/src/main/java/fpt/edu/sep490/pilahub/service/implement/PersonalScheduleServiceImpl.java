package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.PersonalExerciseDto;
import fpt.edu.sep490.pilahub.dto.PersonalScheduleDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalScheduleRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalScheduleRequest;
import fpt.edu.sep490.pilahub.enums.RoadmapStatus;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.PersonalScheduleMapper;
import fpt.edu.sep490.pilahub.pojo.PersonalExercise;
import fpt.edu.sep490.pilahub.pojo.PersonalSchedule;
import fpt.edu.sep490.pilahub.pojo.PersonalStage;
import fpt.edu.sep490.pilahub.pojo.Roadmap;
import fpt.edu.sep490.pilahub.repository.PersonalScheduleRepository;
import fpt.edu.sep490.pilahub.repository.PersonalStageRepository;
import fpt.edu.sep490.pilahub.repository.RoadmapRepository;
import fpt.edu.sep490.pilahub.service.PersonalScheduleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonalScheduleServiceImpl implements PersonalScheduleService {

    private final PersonalScheduleRepository personalScheduleRepository;
    private final PersonalStageRepository personalStageRepository;
    private final PersonalScheduleMapper personalScheduleMapper;
    private  final RoadmapRepository roadmapRepository;

    @Override
    public PersonalScheduleDto createSchedule(CreatePersonalScheduleRequest request) {
        PersonalStage personalStage = personalStageRepository.findById(request.personalStageId())
                .orElseThrow(() -> new ResourceNotFoundException("PersonalStage", "id", request.personalStageId()));

        PersonalSchedule personalSchedule = PersonalSchedule.builder()
                .personalStage(personalStage)
                .scheduleName(request.scheduleName())
                .description(request.description())
                .scheduledDate(request.scheduledDate())
                .durationMinutes(request.durationMinutes())
                .completed(false)
                .build();

        return personalScheduleMapper.toDto(personalScheduleRepository.save(personalSchedule));
    }

    @Override
    public PersonalScheduleDto getById(UUID personalScheduleId) {
        PersonalSchedule personalSchedule = personalScheduleRepository.findById(personalScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalSchedule", "id", personalScheduleId));
        return personalScheduleMapper.toDto(personalSchedule);
    }

    @Override
    public List<PersonalScheduleDto> getByPersonalStageId(UUID personalStageId) {
        PersonalStage personalStage = personalStageRepository.findById(personalStageId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalStage", "id", personalStageId));
        return personalScheduleRepository.findByPersonalStageOrderByScheduledDateAsc(personalStage).stream()
                .map(personalScheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PersonalScheduleDto> getByDateRange(Instant startDate, Instant endDate) {
        return personalScheduleRepository.findByScheduledDateBetween(startDate, endDate).stream()
                .map(personalScheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PersonalScheduleDto updateSchedule(UUID personalScheduleId, UpdatePersonalScheduleRequest request) {
        PersonalSchedule personalSchedule = personalScheduleRepository.findById(personalScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalSchedule", "id", personalScheduleId));

        if (request.scheduleName() != null) {
            personalSchedule.setScheduleName(request.scheduleName());
        }
        if (request.description() != null) {
            personalSchedule.setDescription(request.description());
        }
        if (request.scheduledDate() != null) {
            personalSchedule.setScheduledDate(request.scheduledDate());
        }
        if (request.durationMinutes() != null) {
            personalSchedule.setDurationMinutes(request.durationMinutes());
        }

        return personalScheduleMapper.toDto(personalScheduleRepository.save(personalSchedule));
    }

    @Override
    public void deleteSchedule(UUID personalScheduleId) {
        if (!personalScheduleRepository.existsById(personalScheduleId)) {
            throw new ResourceNotFoundException("PersonalSchedule", "id", personalScheduleId);
        }
        personalScheduleRepository.deleteById(personalScheduleId);
    }

    @Override
    public List<PersonalScheduleDto> getCompleted() {
        return personalScheduleRepository.findByCompletedTrue().stream()
                .map(personalScheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PersonalScheduleDto> getIncomplete() {
        return personalScheduleRepository.findByCompletedFalse().stream()
                .map(personalScheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PersonalScheduleDto markAsCompleted(UUID personalScheduleId) {

        PersonalSchedule personalSchedule = personalScheduleRepository.findById(personalScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalSchedule", "id", personalScheduleId));

        personalSchedule.setCompleted(true);
        personalSchedule.setCompletedAt(Instant.now());

        UUID personalStageId = personalSchedule.getPersonalStage().getPersonalStageId();
        updateCompleteStage(personalStageId);

        UUID roadmapId = personalSchedule.getPersonalStage().getRoadmap().getRoadmapId();
        updateRoadmapProgress(roadmapId);

        return personalScheduleMapper.toDto(personalScheduleRepository.save(personalSchedule));
    }

    private void updateRoadmapProgress(UUID roadmapId){
        int completedSchedule = personalScheduleRepository.countCompletedSchedulesInRoadmap(roadmapId);
        int totalSchedules = personalScheduleRepository.countTotalSchedulesInRoadmap(roadmapId);
        int percent = (int) Math.round((double) completedSchedule / totalSchedules * 100);
        Roadmap roadmap = roadmapRepository.findById(roadmapId).orElseThrow(() -> new ResourceNotFoundException("Roadmap not found"));
        roadmap.setProgressPercent(percent);
        if (percent == 100){
            roadmap.setStatus(RoadmapStatus.COMPLETED);
        }
        roadmapRepository.save(roadmap);
    }

    private void updateCompleteStage(UUID personalStageId){
        PersonalStage personalStage = personalStageRepository.findById(personalStageId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalStage", "id", personalStageId));
        if (personalScheduleRepository.existsByPersonalStageAndCompletedFalse(personalStage)) {
            return;
        }
        personalStage.setCompleted(true);
        personalStageRepository.save(personalStage);
    }
}
