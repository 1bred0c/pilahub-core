package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.TraineeDto;
import fpt.edu.sep490.pilahub.dto.request.CreateTraineeRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateTraineeRequest;
import fpt.edu.sep490.pilahub.exception.TraineeNotFoundException;
import fpt.edu.sep490.pilahub.mapper.TraineeMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.service.TraineeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final AccountRepository accountRepository;
    private final TraineeMapper traineeMapper;

    @Override
    public TraineeDto createTrainee(UUID accountId, CreateTraineeRequest request) {
        log.info("Creating new trainee for account ID: {}", accountId);

        // Check if account exists
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));

        // Check if trainee profile already exists for this account
        if (traineeRepository.existsById(accountId)) {
            throw new IllegalArgumentException("Trainee profile already exists for this account");
        }

        Trainee trainee = Trainee.builder()
                .account(account)
                .fullName(request.fullName())
                .age(request.age())
                .gender(request.gender())
                .avatarUrl(request.avatarUrl())
                .workoutLevel(request.workoutLevel())
                .workoutFrequency(request.workoutFrequency())
                .build();

        Trainee savedTrainee = traineeRepository.save(trainee);
        log.info("Trainee created successfully with ID: {}", savedTrainee.getTraineeId());

        return traineeMapper.toDto(savedTrainee);
    }

    @Override
    public TraineeDto getTraineeByAccountId(UUID accountId) {
        log.info("Fetching trainee with account ID: {}", accountId);

        Trainee trainee = traineeRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Trainee not found for account ID: {}", accountId);
                    return new TraineeNotFoundException("Trainee profile not found for this account");
                });

        return traineeMapper.toDto(trainee);
    }

    @Override
    public TraineeDto getTraineeById(UUID traineeId) {
        log.info("Fetching trainee with ID: {}", traineeId);

        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> {
                    log.error("Trainee not found with ID: {}", traineeId);
                    return new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
                });

        return traineeMapper.toDto(trainee);
    }

    @Override
    public List<TraineeDto> getAllTrainees() {
        log.info("Fetching all trainees");

        List<Trainee> trainees = traineeRepository.findAll();
        log.info("Found {} trainees", trainees.size());

        return trainees.stream()
                .map(traineeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TraineeDto updateTrainee(UUID accountId, UpdateTraineeRequest request) {
        log.info("Updating trainee for account ID: {}", accountId);

        Trainee trainee = traineeRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Trainee not found for account ID: {}", accountId);
                    return new TraineeNotFoundException("Trainee profile not found for this account");
                });

        // Update only non-null fields
        if (request.fullName() != null) {
            trainee.setFullName(request.fullName());
        }
        if (request.age() != null) {
            trainee.setAge(request.age());
        }
        if (request.gender() != null) {
            trainee.setGender(request.gender());
        }
        if (request.avatarUrl() != null) {
            trainee.setAvatarUrl(request.avatarUrl());
        }
        if (request.workoutLevel() != null) {
            trainee.setWorkoutLevel(request.workoutLevel());
        }
        if (request.workoutFrequency() != null) {
            trainee.setWorkoutFrequency(request.workoutFrequency());
        }

        Trainee updatedTrainee = traineeRepository.save(trainee);
        log.info("Trainee updated successfully for account ID: {}", accountId);

        return traineeMapper.toDto(updatedTrainee);
    }

    @Override
    public TraineeDto updateTraineeByAdmin(UUID traineeId, UpdateTraineeRequest request) {
        log.info("Admin updating trainee with ID: {}", traineeId);

        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> {
                    log.error("Trainee not found with ID: {}", traineeId);
                    return new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
                });

        // Update only non-null fields
        if (request.fullName() != null) {
            trainee.setFullName(request.fullName());
        }
        if (request.age() != null) {
            trainee.setAge(request.age());
        }
        if (request.gender() != null) {
            trainee.setGender(request.gender());
        }
        if (request.avatarUrl() != null) {
            trainee.setAvatarUrl(request.avatarUrl());
        }
        if (request.workoutLevel() != null) {
            trainee.setWorkoutLevel(request.workoutLevel());
        }
        if (request.workoutFrequency() != null) {
            trainee.setWorkoutFrequency(request.workoutFrequency());
        }

        Trainee updatedTrainee = traineeRepository.save(trainee);
        log.info("Trainee updated successfully by admin with ID: {}", traineeId);

        return traineeMapper.toDto(updatedTrainee);
    }

    @Override
    public void deleteTrainee(UUID traineeId) {
        log.info("Deleting trainee with ID: {}", traineeId);

        if (!traineeRepository.existsById(traineeId)) {
            log.error("Trainee not found with ID: {}", traineeId);
            throw new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
        }

        traineeRepository.deleteById(traineeId);
        log.info("Trainee deleted successfully with ID: {}", traineeId);
    }
}
