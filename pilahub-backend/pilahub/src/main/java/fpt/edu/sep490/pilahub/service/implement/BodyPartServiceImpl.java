package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.BodyPartDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateBodyPartRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateBodyPartRequest;
import fpt.edu.sep490.pilahub.exception.DuplicateResourceException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.BodyPartMapper;
import fpt.edu.sep490.pilahub.pojo.BodyPart;
import fpt.edu.sep490.pilahub.repository.BodyPartRepository;
import fpt.edu.sep490.pilahub.service.BodyPartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BodyPartServiceImpl implements BodyPartService {

    private final BodyPartRepository bodyPartRepository;
    private final BodyPartMapper bodyPartMapper;

    @Override
    public BodyPartDto createBodyPart(CreateBodyPartRequest request) {
        log.info("Creating body part with name: {}", request.name());

        if (bodyPartRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Body part with name '" + request.name() + "' already exists");
        }

        BodyPart bodyPart = bodyPartMapper.toEntity(request);
        BodyPart savedBodyPart = bodyPartRepository.save(bodyPart);
        return bodyPartMapper.toDto(savedBodyPart);
    }

    @Override
    public List<BodyPartDto> getAll() {
        log.info("Fetching all body parts");
        return bodyPartMapper.toDto(bodyPartRepository.findAllByOrderByNameAsc());
    }

    @Override
    public BodyPartDto getById(UUID bodyPartId) {
        log.info("Fetching body part with ID: {}", bodyPartId);

        BodyPart bodyPart = bodyPartRepository.findById(bodyPartId)
                .orElseThrow(() -> new ResourceNotFoundException("BodyPart", "id", bodyPartId));
        return bodyPartMapper.toDto(bodyPart);
    }

    @Override
    public List<BodyPartDto> searchByName(String name) {
        log.info("Searching body parts with name containing: {}", name);
        return bodyPartMapper.toDto(bodyPartRepository.findByNameContainingIgnoreCase(name));
    }

    @Override
    public BodyPartDto updateBodyPart(UUID bodyPartId, UpdateBodyPartRequest request) {
        log.info("Updating body part with ID: {}", bodyPartId);

        BodyPart bodyPart = bodyPartRepository.findById(bodyPartId)
                .orElseThrow(() -> new ResourceNotFoundException("BodyPart", "id", bodyPartId));

        if (request.name() != null && !request.name().equalsIgnoreCase(bodyPart.getName())
                && bodyPartRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Body part with name '" + request.name() + "' already exists");
        }

        bodyPartMapper.updateEntityFromRequest(request, bodyPart);
        BodyPart updatedBodyPart = bodyPartRepository.save(bodyPart);
        return bodyPartMapper.toDto(updatedBodyPart);
    }

    @Override
    public void deleteBodyPart(UUID bodyPartId) {
        log.info("Deleting body part with ID: {}", bodyPartId);

        if (!bodyPartRepository.existsById(bodyPartId)) {
            throw new ResourceNotFoundException("BodyPart", "id", bodyPartId);
        }

        bodyPartRepository.deleteById(bodyPartId);
    }
}

