package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.PersonalExerciseDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalExerciseRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalExerciseRequest;
import fpt.edu.sep490.pilahub.enums.RoadmapStatus;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.PersonalExerciseMapper;
import fpt.edu.sep490.pilahub.pojo.*;
import fpt.edu.sep490.pilahub.repository.ExerciseRepository;
import fpt.edu.sep490.pilahub.repository.PersonalExerciseRepository;
import fpt.edu.sep490.pilahub.repository.PersonalScheduleRepository;
import fpt.edu.sep490.pilahub.repository.RoadmapRepository;
import fpt.edu.sep490.pilahub.service.PersonalExerciseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonalExerciseServiceImpl implements PersonalExerciseService {

    private final PersonalExerciseRepository personalExerciseRepository;
    private final PersonalScheduleRepository personalScheduleRepository;
    private final ExerciseRepository exerciseRepository;
    private final PersonalExerciseMapper personalExerciseMapper;

    @Override
    public PersonalExerciseDto createPersonalExercise(CreatePersonalExerciseRequest request) {
        PersonalSchedule personalSchedule = personalScheduleRepository.findById(request.personalScheduleId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("PersonalSchedule", "id", request.personalScheduleId()));

        Exercise exercise = exerciseRepository.findById(request.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", request.exerciseId()));

        PersonalExercise personalExercise = PersonalExercise.builder()
                .personalSchedule(personalSchedule)
                .exercise(exercise)
                .exerciseOrder(request.exerciseOrder())
                .sets(request.sets())
                .reps(request.reps())
                .durationSeconds(request.durationSeconds())
                .restSeconds(request.restSeconds())
                .notes(request.notes())
                .completed(false)
                .build();

        return personalExerciseMapper.toDto(personalExerciseRepository.save(personalExercise));
    }

    @Override
    public PersonalExerciseDto getById(UUID personalExerciseId) {
        PersonalExercise personalExercise = personalExerciseRepository.findById(personalExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalExercise", "id", personalExerciseId));
        return personalExerciseMapper.toDto(personalExercise);
    }

    @Override
    public List<PersonalExerciseDto> getByPersonalScheduleId(UUID personalScheduleId) {
        PersonalSchedule personalSchedule = personalScheduleRepository.findById(personalScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalSchedule", "id", personalScheduleId));

        return personalExerciseRepository.findByPersonalScheduleOrderByExerciseOrderAsc(personalSchedule).stream()
                .map(personalExerciseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PersonalExerciseDto updatePersonalExercise(UUID personalExerciseId, UpdatePersonalExerciseRequest request) {
        PersonalExercise personalExercise = personalExerciseRepository.findById(personalExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalExercise", "id", personalExerciseId));

        if (request.exerciseOrder() != null) {
            personalExercise.setExerciseOrder(request.exerciseOrder());
        }
        if (request.sets() != null) {
            personalExercise.setSets(request.sets());
        }
        if (request.reps() != null) {
            personalExercise.setReps(request.reps());
        }
        if (request.durationSeconds() != null) {
            personalExercise.setDurationSeconds(request.durationSeconds());
        }
        if (request.restSeconds() != null) {
            personalExercise.setRestSeconds(request.restSeconds());
        }
        if (request.notes() != null) {
            personalExercise.setNotes(request.notes());
        }

        return personalExerciseMapper.toDto(personalExerciseRepository.save(personalExercise));
    }

    @Override
    public PersonalExerciseDto markAsCompleted(UUID personalExerciseId) {
        PersonalExercise personalExercise = personalExerciseRepository.findById(personalExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalExercise", "id", personalExerciseId));

        personalExercise.setCompleted(true);
        personalExercise.setCompletedAt(Instant.now());
        PersonalExercise saved = personalExerciseRepository.save(personalExercise);

        return personalExerciseMapper.toDto(saved);
    }

    @Override
    public void deletePersonalExercise(UUID personalExerciseId) {
        if (!personalExerciseRepository.existsById(personalExerciseId)) {
            throw new ResourceNotFoundException("PersonalExercise", "id", personalExerciseId);
        }
        personalExerciseRepository.deleteById(personalExerciseId);
    }

    @Override
    public List<PersonalExerciseDto> getCompleted() {
        return personalExerciseRepository.findByCompletedTrue().stream()
                .map(personalExerciseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PersonalExerciseDto> getIncomplete() {
        return personalExerciseRepository.findByCompletedFalse().stream()
                .map(personalExerciseMapper::toDto)
                .collect(Collectors.toList());
    }

}
