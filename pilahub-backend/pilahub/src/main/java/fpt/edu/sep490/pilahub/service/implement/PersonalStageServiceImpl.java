package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.PersonalStageDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalStageRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalStageRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.PersonalStageMapper;
import fpt.edu.sep490.pilahub.pojo.PersonalStage;
import fpt.edu.sep490.pilahub.pojo.Roadmap;
import fpt.edu.sep490.pilahub.pojo.Stage;
import fpt.edu.sep490.pilahub.repository.PersonalStageRepository;
import fpt.edu.sep490.pilahub.repository.RoadmapRepository;
import fpt.edu.sep490.pilahub.repository.StageRepository;
import fpt.edu.sep490.pilahub.service.PersonalStageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonalStageServiceImpl implements PersonalStageService {

    private final PersonalStageRepository personalStageRepository;
    private final RoadmapRepository roadmapRepository;
    private final StageRepository stageRepository;
    private final PersonalStageMapper personalStageMapper;

    @Override
    public PersonalStageDto createStage(CreatePersonalStageRequest request) {
        Roadmap roadmap = roadmapRepository.findById(request.roadmapId())
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", request.roadmapId()));

        Stage stage = null;
        if (request.stageId() != null) {
            stage = stageRepository.findById(request.stageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stage", "id", request.stageId()));
        }

        PersonalStage personalStage = PersonalStage.builder()
                .roadmap(roadmap)
                .stage(stage)
                .stageOrder(request.stageOrder())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .completed(false)
                .build();

        return personalStageMapper.toDto(personalStageRepository.save(personalStage));
    }

    @Override
    public PersonalStageDto getById(UUID personalStageId) {
        PersonalStage personalStage = personalStageRepository.findById(personalStageId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalStage", "id", personalStageId));
        return personalStageMapper.toDto(personalStage);
    }

    @Override
    public List<PersonalStageDto> getByRoadmapId(UUID roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));

        return personalStageRepository.findByRoadmapOrderByStageOrderAsc(roadmap).stream()
                .map(personalStageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PersonalStageDto updateStage(UUID personalStageId, UpdatePersonalStageRequest request) {
        PersonalStage personalStage = personalStageRepository.findById(personalStageId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalStage", "id", personalStageId));

        if (request.stageId() != null) {
            Stage stage = stageRepository.findById(request.stageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stage", "id", request.stageId()));
            personalStage.setStage(stage);
        }
        if (request.stageOrder() != null) {
            personalStage.setStageOrder(request.stageOrder());
        }
        if (request.startDate() != null) {
            personalStage.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            personalStage.setEndDate(request.endDate());
        }

        return personalStageMapper.toDto(personalStageRepository.save(personalStage));
    }

    @Override
    public void deleteStage(UUID personalStageId) {
        if (!personalStageRepository.existsById(personalStageId)) {
            throw new ResourceNotFoundException("PersonalStage", "id", personalStageId);
        }
        personalStageRepository.deleteById(personalStageId);
    }
}
