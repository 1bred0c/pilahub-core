package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.AssessmentCriterionDto;
import fpt.edu.sep490.pilahub.dto.request.assessment.CreateAssessmentCriterionRequest;
import fpt.edu.sep490.pilahub.dto.request.assessment.UpdateAssessmentCriterionRequest;
import fpt.edu.sep490.pilahub.exception.DuplicateResourceException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.AssessmentCriterionMapper;
import fpt.edu.sep490.pilahub.pojo.AssessmentCriterion;
import fpt.edu.sep490.pilahub.repository.AssessmentCriterionRepository;
import fpt.edu.sep490.pilahub.service.AssessmentCriterionService;
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
public class AssessmentCriterionServiceImpl implements AssessmentCriterionService {

    private final AssessmentCriterionRepository criterionRepository;
    private final AssessmentCriterionMapper criterionMapper;

    @Override
    public AssessmentCriterionDto create(CreateAssessmentCriterionRequest request) {
        if (criterionRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Assessment criterion with name '" + request.name() + "' already exists");
        }

        AssessmentCriterion criterion = criterionMapper.toEntity(request);
        AssessmentCriterion saved = criterionRepository.save(criterion);
        return criterionMapper.toDto(saved);
    }

    @Override
    public AssessmentCriterionDto getById(UUID criterionId) {
        AssessmentCriterion criterion = criterionRepository.findById(criterionId)
                .orElseThrow(() -> new ResourceNotFoundException("AssessmentCriterion", "id", criterionId));
        return criterionMapper.toDto(criterion);
    }

    @Override
    public List<AssessmentCriterionDto> getAll() {
        return criterionRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(criterionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssessmentCriterionDto> getAllActive() {
        return criterionRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(criterionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssessmentCriterionDto> searchByName(String name) {
        return criterionRepository.findByNameContainingIgnoreCase(name).stream()
                .map(criterionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AssessmentCriterionDto update(UUID criterionId, UpdateAssessmentCriterionRequest request) {
        AssessmentCriterion criterion = criterionRepository.findById(criterionId)
                .orElseThrow(() -> new ResourceNotFoundException("AssessmentCriterion", "id", criterionId));

        if (request.name() != null && !request.name().equals(criterion.getName())
                && criterionRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Assessment criterion with name '" + request.name() + "' already exists");
        }

        criterionMapper.updateEntity(criterion, request);
        AssessmentCriterion updated = criterionRepository.save(criterion);
        return criterionMapper.toDto(updated);
    }

    @Override
    public void deactivate(UUID criterionId) {
        AssessmentCriterion criterion = criterionRepository.findById(criterionId)
                .orElseThrow(() -> new ResourceNotFoundException("AssessmentCriterion", "id", criterionId));

        criterion.setActive(false);
        criterionRepository.save(criterion);
        log.info("Assessment criterion {} deactivated", criterionId);
    }
}

