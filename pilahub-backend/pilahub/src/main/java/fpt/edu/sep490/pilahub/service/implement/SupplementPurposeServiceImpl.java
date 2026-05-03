package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.SupplementPurposeDto;
import fpt.edu.sep490.pilahub.dto.request.supplement.CreateSupplementPurposeRequest;
import fpt.edu.sep490.pilahub.dto.request.supplement.UpdateSupplementPurposeRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.SupplementPurposeMapper;
import fpt.edu.sep490.pilahub.pojo.Purpose;
import fpt.edu.sep490.pilahub.pojo.Supplement;
import fpt.edu.sep490.pilahub.pojo.SupplementPurpose;
import fpt.edu.sep490.pilahub.repository.PurposeRepository;
import fpt.edu.sep490.pilahub.repository.SupplementPurposeRepository;
import fpt.edu.sep490.pilahub.repository.SupplementRepository;
import fpt.edu.sep490.pilahub.service.SupplementPurposeService;
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
public class SupplementPurposeServiceImpl implements SupplementPurposeService {

    private final SupplementPurposeRepository supplementPurposeRepository;
    private final SupplementRepository supplementRepository;
    private final PurposeRepository purposeRepository;
    private final SupplementPurposeMapper supplementPurposeMapper;

    @Override
    public SupplementPurposeDto createSupplementPurpose(CreateSupplementPurposeRequest request) {
        log.info("Creating supplement purpose relationship for supplement ID: {} and purpose ID: {}", 
                request.supplementId(), request.purposeId());

        // Validate supplement exists
        Supplement supplement = supplementRepository.findById(request.supplementId())
                .orElseThrow(() -> {
                    log.error("Supplement not found with ID: {}", request.supplementId());
                    return new ResourceNotFoundException("Supplement", "id", request.supplementId());
                });

        // Validate purpose exists
        Purpose purpose = purposeRepository.findById(request.purposeId())
                .orElseThrow(() -> {
                    log.error("Purpose not found with ID: {}", request.purposeId());
                    return new ResourceNotFoundException("Purpose", "id", request.purposeId());
                });

        SupplementPurpose supplementPurpose = supplementPurposeMapper.toEntity(request);
        supplementPurpose.setSupplement(supplement);
        supplementPurpose.setPurpose(purpose);

        SupplementPurpose saved = supplementPurposeRepository.save(supplementPurpose);

        log.info("Supplement purpose relationship created successfully with ID: {}", saved.getSupplementPurposeId());
        return supplementPurposeMapper.toDto(saved);
    }

    @Override
    public SupplementPurposeDto getById(UUID supplementPurposeId) {
        log.info("Fetching supplement purpose with ID: {}", supplementPurposeId);

        SupplementPurpose supplementPurpose = supplementPurposeRepository.findById(supplementPurposeId)
                .orElseThrow(() -> {
                    log.error("Supplement purpose not found with ID: {}", supplementPurposeId);
                    return new ResourceNotFoundException("SupplementPurpose", "id", supplementPurposeId);
                });

        return supplementPurposeMapper.toDto(supplementPurpose);
    }

    @Override
    public List<SupplementPurposeDto> getBySupplementId(UUID supplementId) {
        log.info("Fetching purposes for supplement ID: {}", supplementId);

        return supplementPurposeRepository.findBySupplement_SupplementId(supplementId).stream()
                .map(supplementPurposeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplementPurposeDto> getPrimaryPurposesBySupplementId(UUID supplementId) {
        log.info("Fetching primary purposes for supplement ID: {}", supplementId);

        return supplementPurposeRepository.findBySupplement_SupplementIdAndPrimaryTrue(supplementId).stream()
                .map(supplementPurposeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplementPurposeDto> getByPurposeId(UUID purposeId) {
        log.info("Fetching supplements for purpose ID: {}", purposeId);

        return supplementPurposeRepository.findByPurpose_PurposeId(purposeId).stream()
                .map(supplementPurposeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SupplementPurposeDto updateSupplementPurpose(UUID supplementPurposeId, 
                                                        UpdateSupplementPurposeRequest request) {
        log.info("Updating supplement purpose with ID: {}", supplementPurposeId);

        SupplementPurpose supplementPurpose = supplementPurposeRepository.findById(supplementPurposeId)
                .orElseThrow(() -> {
                    log.error("Supplement purpose not found with ID: {}", supplementPurposeId);
                    return new ResourceNotFoundException("SupplementPurpose", "id", supplementPurposeId);
                });

        supplementPurposeMapper.updateEntity(supplementPurpose, request);
        SupplementPurpose updated = supplementPurposeRepository.save(supplementPurpose);

        log.info("Supplement purpose updated successfully with ID: {}", supplementPurposeId);
        return supplementPurposeMapper.toDto(updated);
    }

    @Override
    public void deleteSupplementPurpose(UUID supplementPurposeId) {
        log.info("Deleting supplement purpose with ID: {}", supplementPurposeId);

        if (!supplementPurposeRepository.existsById(supplementPurposeId)) {
            log.error("Supplement purpose not found with ID: {}", supplementPurposeId);
            throw new ResourceNotFoundException("SupplementPurpose", "id", supplementPurposeId);
        }

        supplementPurposeRepository.deleteById(supplementPurposeId);
        log.info("Supplement purpose deleted successfully with ID: {}", supplementPurposeId);
    }

    @Override
    public void deleteBySupplementId(UUID supplementId) {
        log.info("Deleting all purposes for supplement ID: {}", supplementId);

        supplementPurposeRepository.deleteBySupplement_SupplementId(supplementId);
        log.info("All purposes deleted successfully for supplement ID: {}", supplementId);
    }

    @Override
    public boolean existsBySupplementAndPurpose(UUID supplementId, UUID purposeId) {
        log.info("Checking if supplement {} has purpose {}", supplementId, purposeId);

        return supplementPurposeRepository.existsBySupplement_SupplementIdAndPurpose_PurposeId(
                supplementId, purposeId);
    }
}
