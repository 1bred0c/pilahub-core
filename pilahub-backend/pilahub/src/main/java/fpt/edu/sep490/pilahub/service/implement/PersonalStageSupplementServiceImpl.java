package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.PersonalStageSupplementDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalStageSupplementRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalStageSupplementRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.PersonalStageSupplementMapper;
import fpt.edu.sep490.pilahub.pojo.PersonalStage;
import fpt.edu.sep490.pilahub.pojo.PersonalStageSupplement;
import fpt.edu.sep490.pilahub.pojo.Supplement;
import fpt.edu.sep490.pilahub.repository.PersonalStageRepository;
import fpt.edu.sep490.pilahub.repository.PersonalStageSupplementRepository;
import fpt.edu.sep490.pilahub.repository.SupplementRepository;
import fpt.edu.sep490.pilahub.service.PersonalStageSupplementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonalStageSupplementServiceImpl implements PersonalStageSupplementService {

    private final PersonalStageSupplementRepository personalStageSupplementRepository;
    private final PersonalStageRepository personalStageRepository;
    private final SupplementRepository supplementRepository;
    private final PersonalStageSupplementMapper personalStageSupplementMapper;

    @Override
    public PersonalStageSupplementDto createPersonalStageSupplement(CreatePersonalStageSupplementRequest request) {
        // Verify personal stage exists
        PersonalStage personalStage = personalStageRepository.findById(request.personalStageId())
                .orElseThrow(() -> new ResourceNotFoundException("PersonalStage", "id", request.personalStageId()));

        // Verify supplement exists
        Supplement supplement = supplementRepository.findById(request.supplementId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplement", "id", request.supplementId()));

        // Check if the supplement is already assigned to this personal stage
        if (personalStageSupplementRepository.existsByPersonalStage_PersonalStageIdAndSupplement_SupplementId(
                request.personalStageId(), request.supplementId())) {
            throw new IllegalArgumentException("Supplement is already assigned to this personal stage");
        }

        PersonalStageSupplement personalStageSupplement = personalStageSupplementMapper.toEntity(request);
        personalStageSupplement.setPersonalStage(personalStage);
        personalStageSupplement.setSupplement(supplement);

        if (request.optional() != null) {
            personalStageSupplement.setOptional(request.optional());
        }

        PersonalStageSupplement saved = personalStageSupplementRepository.save(personalStageSupplement);
        return personalStageSupplementMapper.toDto(saved);
    }

    @Override
    public PersonalStageSupplementDto getById(UUID personalStageSupplementId) {
        PersonalStageSupplement personalStageSupplement = personalStageSupplementRepository.findById(personalStageSupplementId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalStageSupplement", "id", personalStageSupplementId));
        return personalStageSupplementMapper.toDto(personalStageSupplement);
    }

    @Override
    public List<PersonalStageSupplementDto> getByPersonalStageId(UUID personalStageId) {
        return personalStageSupplementRepository.findByPersonalStage_PersonalStageId(personalStageId).stream()
                .map(personalStageSupplementMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PersonalStageSupplementDto> getByRoadmapId(UUID roadmapId) {
        return personalStageSupplementRepository.findByPersonalStage_Roadmap_RoadmapId(roadmapId).stream()
                .map(personalStageSupplementMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PersonalStageSupplementDto updatePersonalStageSupplement(UUID personalStageSupplementId, UpdatePersonalStageSupplementRequest request) {
        PersonalStageSupplement personalStageSupplement = personalStageSupplementRepository.findById(personalStageSupplementId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalStageSupplement", "id", personalStageSupplementId));

        personalStageSupplementMapper.updateEntityFromRequest(request, personalStageSupplement);

        if (request.optional() != null) {
            personalStageSupplement.setOptional(request.optional());
        }

        PersonalStageSupplement updated = personalStageSupplementRepository.save(personalStageSupplement);
        return personalStageSupplementMapper.toDto(updated);
    }

    @Override
    public void deletePersonalStageSupplement(UUID personalStageSupplementId) {
        if (!personalStageSupplementRepository.existsById(personalStageSupplementId)) {
            throw new ResourceNotFoundException("PersonalStageSupplement", "id", personalStageSupplementId);
        }
        personalStageSupplementRepository.deleteById(personalStageSupplementId);
    }

    @Override
    public void deleteByPersonalStageId(UUID personalStageId) {
        personalStageSupplementRepository.deleteByPersonalStage_PersonalStageId(personalStageId);
    }
}
