package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.FitnessGoalDto;
import fpt.edu.sep490.pilahub.dto.request.CreateFitnessGoalRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateFitnessGoalRequest;
import fpt.edu.sep490.pilahub.exception.DuplicateResourceException;
import fpt.edu.sep490.pilahub.exception.FitnessGoalNotFoundException;
import fpt.edu.sep490.pilahub.mapper.FitnessGoalMapper;
import fpt.edu.sep490.pilahub.pojo.FitnessGoal;
import fpt.edu.sep490.pilahub.pojo.Purpose;
import fpt.edu.sep490.pilahub.repository.FitnessGoalRepository;
import fpt.edu.sep490.pilahub.repository.PurposeRepository;
import fpt.edu.sep490.pilahub.service.FitnessGoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FitnessGoalServiceImpl implements FitnessGoalService {

    private final FitnessGoalRepository fitnessGoalRepository;
    private final FitnessGoalMapper fitnessGoalMapper;
    private final PurposeRepository purposeRepository;

    @Override
    @Transactional
    public FitnessGoalDto create(CreateFitnessGoalRequest request) {
        log.info("Creating fitness goal with code: {}", request.code());

        if (fitnessGoalRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException(
                    "Fitness goal with code '" + request.code() + "' already exists");
        }

        FitnessGoal goal = FitnessGoal.builder()
                .code(request.code())
                .vietnameseName(request.vietnameseName())
                .description(request.description())
                .relatedPurposes(loadPurposesByIds(request.relatedPurposeIds()))
                .active(true)
                .build();

        FitnessGoal saved = fitnessGoalRepository.save(goal);
        log.info("Fitness goal created with ID: {}", saved.getGoalId());
        return fitnessGoalMapper.toDto(saved);
    }

    @Override
    public FitnessGoalDto getById(UUID goalId) {
        log.info("Fetching fitness goal with ID: {}", goalId);
        FitnessGoal goal = findByIdOrThrow(goalId);
        return fitnessGoalMapper.toDto(goal);
    }

    @Override
    public Page<FitnessGoalDto> getAll(Pageable pageable) {
        log.info("Fetching all fitness goals (page: {}, size: {})", pageable.getPageNumber(), pageable.getPageSize());
        return fitnessGoalRepository.findAll(pageable).map(fitnessGoalMapper::toDto);
    }

    @Override
    public List<FitnessGoalDto> getAllActive() {
        log.info("Fetching all active fitness goals");
        return fitnessGoalRepository.findByActiveTrue()
                .stream()
                .map(fitnessGoalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FitnessGoalDto> search(String keyword) {
        log.info("Searching fitness goals with keyword: {}", keyword);
        return fitnessGoalRepository
                .findByVietnameseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(fitnessGoalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FitnessGoalDto update(UUID goalId, UpdateFitnessGoalRequest request) {
        log.info("Updating fitness goal with ID: {}", goalId);
        FitnessGoal goal = findByIdOrThrow(goalId);

        if (request.vietnameseName() != null) {
            goal.setVietnameseName(request.vietnameseName());
        }
        if (request.description() != null) {
            goal.setDescription(request.description());
        }
        if (request.relatedPurposeIds() != null) {
            goal.setRelatedPurposes(loadPurposesByIds(request.relatedPurposeIds()));
        }
        if (request.active() != null) {
            goal.setActive(request.active());
        }

        FitnessGoal saved = fitnessGoalRepository.save(goal);
        log.info("Fitness goal updated: {}", saved.getGoalId());
        return fitnessGoalMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deactivate(UUID goalId) {
        log.info("Deactivating fitness goal with ID: {}", goalId);
        FitnessGoal goal = findByIdOrThrow(goalId);
        goal.setActive(false);
        fitnessGoalRepository.save(goal);
    }

    @Override
    @Transactional
    public void activate(UUID goalId) {
        log.info("Activating fitness goal with ID: {}", goalId);
        FitnessGoal goal = findByIdOrThrow(goalId);
        goal.setActive(true);
        fitnessGoalRepository.save(goal);
    }

    private FitnessGoal findByIdOrThrow(UUID goalId) {
        return fitnessGoalRepository.findById(goalId)
                .orElseThrow(() -> new FitnessGoalNotFoundException(
                        "Fitness goal not found with ID: " + goalId));
    }

    private Set<Purpose> loadPurposesByIds(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(purposeRepository.findAllById(ids));
    }
}
