package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.SupplementDto;
import fpt.edu.sep490.pilahub.dto.request.supplement.CreateSupplementRequest;
import fpt.edu.sep490.pilahub.dto.request.supplement.UpdateSupplementRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.SupplementMapper;
import fpt.edu.sep490.pilahub.pojo.Supplement;
import fpt.edu.sep490.pilahub.repository.SupplementRepository;
import fpt.edu.sep490.pilahub.service.SupplementService;
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
public class SupplementServiceImpl implements SupplementService {

    private final SupplementRepository supplementRepository;
    private final SupplementMapper supplementMapper;

    @Override
    public SupplementDto createSupplement(CreateSupplementRequest request) {
        log.info("Creating supplement with name: {}", request.name());

        Supplement supplement = supplementMapper.toEntity(request);
        Supplement saved = supplementRepository.save(supplement);

        log.info("Supplement created successfully with ID: {}", saved.getSupplementId());
        return supplementMapper.toDto(saved);
    }

    @Override
    public SupplementDto getById(UUID supplementId) {
        log.info("Fetching supplement with ID: {}", supplementId);

        Supplement supplement = supplementRepository.findById(supplementId)
                .orElseThrow(() -> {
                    log.error("Supplement not found with ID: {}", supplementId);
                    return new ResourceNotFoundException("Supplement", "id", supplementId);
                });

        return supplementMapper.toDto(supplement);
    }

    @Override
    public List<SupplementDto> getAll() {
        log.info("Fetching all supplements");

        return supplementRepository.findAll().stream()
                .map(supplementMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplementDto> getAllActive() {
        log.info("Fetching all active supplements");

        return supplementRepository.findByActiveTrue().stream()
                .map(supplementMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplementDto> searchByName(String name) {
        log.info("Searching supplements with name containing: {}", name);

        return supplementRepository.findByNameContainingIgnoreCase(name).stream()
                .map(supplementMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplementDto> getByBrand(String brand) {
        log.info("Fetching supplements by brand: {}", brand);

        return supplementRepository.findByBrand(brand).stream()
                .map(supplementMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplementDto> getActiveByBrand(String brand) {
        log.info("Fetching active supplements by brand: {}", brand);

        return supplementRepository.findByBrandAndActiveTrue(brand).stream()
                .map(supplementMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SupplementDto updateSupplement(UUID supplementId, UpdateSupplementRequest request) {
        log.info("Updating supplement with ID: {}", supplementId);

        Supplement supplement = supplementRepository.findById(supplementId)
                .orElseThrow(() -> {
                    log.error("Supplement not found with ID: {}", supplementId);
                    return new ResourceNotFoundException("Supplement", "id", supplementId);
                });

        supplementMapper.updateEntity(supplement, request);
        Supplement updated = supplementRepository.save(supplement);

        log.info("Supplement updated successfully with ID: {}", supplementId);
        return supplementMapper.toDto(updated);
    }

    @Override
    public void deactivateSupplement(UUID supplementId) {
        log.info("Deactivating supplement with ID: {}", supplementId);

        Supplement supplement = supplementRepository.findById(supplementId)
                .orElseThrow(() -> {
                    log.error("Supplement not found with ID: {}", supplementId);
                    return new ResourceNotFoundException("Supplement", "id", supplementId);
                });

        supplement.setActive(false);
        supplementRepository.save(supplement);

        log.info("Supplement deactivated successfully with ID: {}", supplementId);
    }

    @Override
    public void deleteSupplement(UUID supplementId) {
        log.info("Deleting supplement with ID: {}", supplementId);

        if (!supplementRepository.existsById(supplementId)) {
            log.error("Supplement not found with ID: {}", supplementId);
            throw new ResourceNotFoundException("Supplement", "id", supplementId);
        }

        supplementRepository.deleteById(supplementId);
        log.info("Supplement deleted successfully with ID: {}", supplementId);
    }
}
