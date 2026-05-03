package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.PersonalInjuryDto;
import fpt.edu.sep490.pilahub.dto.request.injury.CreatePersonalInjuryRequest;
import fpt.edu.sep490.pilahub.dto.request.injury.UpdatePersonalInjuryRequest;
import fpt.edu.sep490.pilahub.enums.InjuryStatus;
import fpt.edu.sep490.pilahub.exception.InjuryNotFoundException;
import fpt.edu.sep490.pilahub.exception.PersonalInjuryNotFoundException;
import fpt.edu.sep490.pilahub.exception.TraineeNotFoundException;
import fpt.edu.sep490.pilahub.mapper.PersonalInjuryMapper;
import fpt.edu.sep490.pilahub.pojo.Injury;
import fpt.edu.sep490.pilahub.pojo.PersonalInjury;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.repository.InjuryRepository;
import fpt.edu.sep490.pilahub.repository.PersonalInjuryRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.service.PersonalInjuryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PersonalInjuryServiceImpl implements PersonalInjuryService {

    private final PersonalInjuryRepository personalInjuryRepository;
    private final InjuryRepository injuryRepository;
    private final TraineeRepository traineeRepository;
    private final PersonalInjuryMapper personalInjuryMapper;

    @Override
    public PersonalInjuryDto createPersonalInjury(UUID traineeId, CreatePersonalInjuryRequest request) {
        log.info("Creating personal injury for trainee ID: {}", traineeId);

        // Verify trainee exists
        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> {
                    log.error("Trainee not found with ID: {}", traineeId);
                    return new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
                });

        // Verify injury exists
        Injury injury = injuryRepository.findById(request.injuryId())
                .orElseThrow(() -> {
                    log.error("Injury not found with ID: {}", request.injuryId());
                    return new InjuryNotFoundException("Injury not found with ID: " + request.injuryId());
                });

        PersonalInjury personalInjury = PersonalInjury.builder()
                .trainee(trainee)
                .injury(injury)
                .status(InjuryStatus.ACTIVE)
                .notes(request.notes())
                .build();

        PersonalInjury saved = personalInjuryRepository.save(personalInjury);
        log.info("Personal injury created successfully with ID: {}", saved.getPersonalInjuryId());

        return personalInjuryMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalInjuryDto getById(UUID personalInjuryId) {
        log.info("Fetching personal injury with ID: {}", personalInjuryId);

        PersonalInjury personalInjury = personalInjuryRepository.findById(personalInjuryId)
                .orElseThrow(() -> {
                    log.error("Personal injury not found with ID: {}", personalInjuryId);
                    return new PersonalInjuryNotFoundException("Personal injury not found with ID: " + personalInjuryId);
                });

        return personalInjuryMapper.toDto(personalInjury);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalInjuryDto> getMyInjuries(UUID traineeId) {
        log.info("Fetching all injuries for trainee ID: {}", traineeId);

        // Verify trainee exists
        if (!traineeRepository.existsById(traineeId)) {
            log.error("Trainee not found with ID: {}", traineeId);
            throw new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
        }

        return personalInjuryRepository.findByTraineeTraineeId(traineeId).stream()
                .map(personalInjuryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalInjuryDto> getMyInjuriesByStatus(UUID traineeId, InjuryStatus status) {
        log.info("Fetching {} injuries for trainee ID: {}", status, traineeId);

        // Verify trainee exists
        if (!traineeRepository.existsById(traineeId)) {
            log.error("Trainee not found with ID: {}", traineeId);
            throw new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
        }

        return personalInjuryRepository.findByTraineeTraineeIdAndStatus(traineeId, status).stream()
                .map(personalInjuryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PersonalInjuryDto updatePersonalInjury(UUID personalInjuryId, UUID traineeId, UpdatePersonalInjuryRequest request) {
        log.info("Updating personal injury ID: {} for trainee ID: {}", personalInjuryId, traineeId);

        PersonalInjury personalInjury = personalInjuryRepository.findById(personalInjuryId)
                .orElseThrow(() -> {
                    log.error("Personal injury not found with ID: {}", personalInjuryId);
                    return new PersonalInjuryNotFoundException("Personal injury not found with ID: " + personalInjuryId);
                });

        // Verify ownership
        if (!personalInjury.getTrainee().getTraineeId().equals(traineeId)) {
            log.error("Trainee ID: {} is not the owner of personal injury ID: {}", traineeId, personalInjuryId);
            throw new IllegalArgumentException("You are not the owner of this injury record");
        }

        // Update fields if provided
        if (request.status() != null) {
            personalInjury.setStatus(request.status());
        }
        if (request.notes() != null) {
            personalInjury.setNotes(request.notes());
        }

        PersonalInjury updated = personalInjuryRepository.save(personalInjury);
        log.info("Personal injury updated successfully");

        return personalInjuryMapper.toDto(updated);
    }

    @Override
    public PersonalInjuryDto markAsRecovered(UUID personalInjuryId, UUID traineeId) {
        log.info("Marking personal injury ID: {} as recovered for trainee ID: {}", personalInjuryId, traineeId);

        PersonalInjury personalInjury = personalInjuryRepository.findById(personalInjuryId)
                .orElseThrow(() -> {
                    log.error("Personal injury not found with ID: {}", personalInjuryId);
                    return new PersonalInjuryNotFoundException("Personal injury not found with ID: " + personalInjuryId);
                });

        // Verify ownership
        if (!personalInjury.getTrainee().getTraineeId().equals(traineeId)) {
            log.error("Trainee ID: {} is not the owner of personal injury ID: {}", traineeId, personalInjuryId);
            throw new IllegalArgumentException("You are not the owner of this injury record");
        }

        personalInjury.setStatus(InjuryStatus.RECOVERED);
        PersonalInjury updated = personalInjuryRepository.save(personalInjury);
        log.info("Personal injury marked as recovered successfully");

        return personalInjuryMapper.toDto(updated);
    }

    @Override
    public void deletePersonalInjury(UUID personalInjuryId, UUID traineeId) {
        log.info("Deleting personal injury ID: {} for trainee ID: {}", personalInjuryId, traineeId);

        PersonalInjury personalInjury = personalInjuryRepository.findById(personalInjuryId)
                .orElseThrow(() -> {
                    log.error("Personal injury not found with ID: {}", personalInjuryId);
                    return new PersonalInjuryNotFoundException("Personal injury not found with ID: " + personalInjuryId);
                });

        // Verify ownership
        if (!personalInjury.getTrainee().getTraineeId().equals(traineeId)) {
            log.error("Trainee ID: {} is not the owner of personal injury ID: {}", traineeId, personalInjuryId);
            throw new IllegalArgumentException("You are not the owner of this injury record");
        }

        personalInjuryRepository.delete(personalInjury);
        log.info("Personal injury deleted successfully");
    }
}
