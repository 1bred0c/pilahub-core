package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.PurposeDto;
import fpt.edu.sep490.pilahub.dto.request.purpose.CreatePurposeRequest;
import fpt.edu.sep490.pilahub.dto.request.purpose.UpdatePurposeRequest;
import fpt.edu.sep490.pilahub.exception.DuplicateResourceException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.PurposeMapper;
import fpt.edu.sep490.pilahub.pojo.Purpose;
import fpt.edu.sep490.pilahub.repository.PurposeRepository;
import fpt.edu.sep490.pilahub.service.PurposeService;
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
public class PurposeServiceImpl implements PurposeService {

    private final PurposeRepository purposeRepository;
    private final PurposeMapper purposeMapper;

    @Override
    public PurposeDto createPurpose(CreatePurposeRequest request) {
        log.info("Creating purpose with code: {}", request.code());

        if (purposeRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Purpose with name '" + request.name() + "' already exists");
        }

        if (purposeRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Purpose with code '" + request.code() + "' already exists");
        }

        Purpose purpose = purposeMapper.toEntity(request);
        Purpose saved = purposeRepository.save(purpose);

        log.info("Successfully created purpose with ID: {}", saved.getPurposeId());
        return purposeMapper.toDto(saved);
    }

    @Override
    public PurposeDto getById(UUID purposeId) {
        log.info("Fetching purpose by ID: {}", purposeId);

        Purpose purpose = purposeRepository.findById(purposeId)
                .orElseThrow(() -> new ResourceNotFoundException("Purpose", "id", purposeId));

        return purposeMapper.toDto(purpose);
    }

    @Override
    public PurposeDto getByCode(String code) {
        log.info("Fetching purpose by code: {}", code);

        Purpose purpose = purposeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Purpose", "code", code));

        return purposeMapper.toDto(purpose);
    }

    @Override
    public List<PurposeDto> getAll() {
        log.info("Fetching all purposes");

        return purposeRepository.findAll().stream()
                .map(purposeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurposeDto> getAllActive() {
        log.info("Fetching all active purposes");

        return purposeRepository.findByActiveTrue().stream()
                .map(purposeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurposeDto> searchByName(String name) {
        log.info("Searching purposes by name: {}", name);

        return purposeRepository.findByNameContainingIgnoreCase(name).stream()
                .map(purposeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PurposeDto updatePurpose(UUID purposeId, UpdatePurposeRequest request) {
        log.info("Updating purpose with ID: {}", purposeId);

        Purpose purpose = purposeRepository.findById(purposeId)
                .orElseThrow(() -> new ResourceNotFoundException("Purpose", "id", purposeId));

        if (request.name() != null && !request.name().equals(purpose.getName()) && purposeRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Purpose with name '" + request.name() + "' already exists");
        }

        if (request.code() != null && !request.code().equals(purpose.getCode()) && purposeRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Purpose with code '" + request.code() + "' already exists");
        }

        purposeMapper.updateEntity(purpose, request);
        Purpose updated = purposeRepository.save(purpose);

        log.info("Successfully updated purpose with ID: {}", purposeId);
        return purposeMapper.toDto(updated);
    }

    @Override
    public void deactivatePurpose(UUID purposeId) {
        log.info("Deactivating purpose with ID: {}", purposeId);

        Purpose purpose = purposeRepository.findById(purposeId)
                .orElseThrow(() -> new ResourceNotFoundException("Purpose", "id", purposeId));

        purpose.setActive(false);
        purposeRepository.save(purpose);

        log.info("Successfully deactivated purpose with ID: {}", purposeId);
    }
}
