package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.StageDto;
import fpt.edu.sep490.pilahub.dto.request.CreateStageRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateStageRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.StageMapper;
import fpt.edu.sep490.pilahub.pojo.Stage;
import fpt.edu.sep490.pilahub.repository.StageRepository;
import fpt.edu.sep490.pilahub.service.StageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StageServiceImpl implements StageService {

    private final StageRepository stageRepository;
    private final StageMapper stageMapper;

    @Override
    public StageDto createStage(CreateStageRequest request) {
        Stage stage = stageMapper.toEntity(request);
        Stage saved = stageRepository.save(stage);
        return stageMapper.toDto(saved);
    }

    @Override
    public StageDto getById(UUID stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage", "id", stageId));
        return stageMapper.toDto(stage);
    }

    @Override
    public List<StageDto> getAll() {
        return stageRepository.findAll().stream()
                .map(stageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StageDto> getAllActive() {
        return stageRepository.findAllByActiveTrue().stream()
                .map(stageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StageDto> searchByName(String name) {
        return stageRepository.findAll().stream()
                .filter(stage -> stage.getName().toLowerCase().contains(name.toLowerCase()))
                .map(stageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public StageDto updateStage(UUID stageId, UpdateStageRequest request) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage", "id", stageId));

        stageMapper.updateEntityFromRequest(request, stage);

        Stage updated = stageRepository.save(stage);
        return stageMapper.toDto(updated);
    }

    @Override
    public void deactivateStage(UUID stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage", "id", stageId));

        stage.setActive(false);
        stageRepository.save(stage);
    }

    @Override
    public void activateStage(UUID stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage", "id", stageId));

        stage.setActive(true);
        stageRepository.save(stage);
    }

    @Override
    public void deleteStage(UUID stageId) {
        if (!stageRepository.existsById(stageId)) {
            throw new ResourceNotFoundException("Stage", "id", stageId);
        }
        stageRepository.deleteById(stageId);
    }
}
