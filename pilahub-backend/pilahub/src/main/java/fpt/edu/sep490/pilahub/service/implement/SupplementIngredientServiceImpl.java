package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.SupplementIngredientDto;
import fpt.edu.sep490.pilahub.dto.request.supplement.CreateSupplementIngredientRequest;
import fpt.edu.sep490.pilahub.dto.request.supplement.UpdateSupplementIngredientRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.SupplementIngredientMapper;
import fpt.edu.sep490.pilahub.pojo.Ingredient;
import fpt.edu.sep490.pilahub.pojo.Supplement;
import fpt.edu.sep490.pilahub.pojo.SupplementIngredient;
import fpt.edu.sep490.pilahub.repository.IngredientRepository;
import fpt.edu.sep490.pilahub.repository.SupplementIngredientRepository;
import fpt.edu.sep490.pilahub.repository.SupplementRepository;
import fpt.edu.sep490.pilahub.service.SupplementIngredientService;
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
public class SupplementIngredientServiceImpl implements SupplementIngredientService {

    private final SupplementIngredientRepository supplementIngredientRepository;
    private final SupplementRepository supplementRepository;
    private final IngredientRepository ingredientRepository;
    private final SupplementIngredientMapper supplementIngredientMapper;

    @Override
    public SupplementIngredientDto createSupplementIngredient(CreateSupplementIngredientRequest request) {
        log.info("Creating supplement ingredient relationship for supplement ID: {} and ingredient ID: {}", 
                request.supplementId(), request.ingredientId());

        // Validate supplement exists
        Supplement supplement = supplementRepository.findById(request.supplementId())
                .orElseThrow(() -> {
                    log.error("Supplement not found with ID: {}", request.supplementId());
                    return new ResourceNotFoundException("Supplement", "id", request.supplementId());
                });

        // Validate ingredient exists
        Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
                .orElseThrow(() -> {
                    log.error("Ingredient not found with ID: {}", request.ingredientId());
                    return new ResourceNotFoundException("Ingredient", "id", request.ingredientId());
                });

        SupplementIngredient supplementIngredient = supplementIngredientMapper.toEntity(request);
        supplementIngredient.setSupplement(supplement);
        supplementIngredient.setIngredient(ingredient);

        SupplementIngredient saved = supplementIngredientRepository.save(supplementIngredient);

        log.info("Supplement ingredient relationship created successfully with ID: {}", saved.getSupplementIngredientId());
        return supplementIngredientMapper.toDto(saved);
    }

    @Override
    public SupplementIngredientDto getById(UUID supplementIngredientId) {
        log.info("Fetching supplement ingredient with ID: {}", supplementIngredientId);

        SupplementIngredient supplementIngredient = supplementIngredientRepository.findById(supplementIngredientId)
                .orElseThrow(() -> {
                    log.error("Supplement ingredient not found with ID: {}", supplementIngredientId);
                    return new ResourceNotFoundException("SupplementIngredient", "id", supplementIngredientId);
                });

        return supplementIngredientMapper.toDto(supplementIngredient);
    }

    @Override
    public List<SupplementIngredientDto> getBySupplementId(UUID supplementId) {
        log.info("Fetching ingredients for supplement ID: {}", supplementId);

        return supplementIngredientRepository.findBySupplement_SupplementId(supplementId).stream()
                .map(supplementIngredientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplementIngredientDto> getByIngredientId(UUID ingredientId) {
        log.info("Fetching supplements containing ingredient ID: {}", ingredientId);

        return supplementIngredientRepository.findByIngredient_IngredientId(ingredientId).stream()
                .map(supplementIngredientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SupplementIngredientDto updateSupplementIngredient(UUID supplementIngredientId, 
                                                              UpdateSupplementIngredientRequest request) {
        log.info("Updating supplement ingredient with ID: {}", supplementIngredientId);

        SupplementIngredient supplementIngredient = supplementIngredientRepository.findById(supplementIngredientId)
                .orElseThrow(() -> {
                    log.error("Supplement ingredient not found with ID: {}", supplementIngredientId);
                    return new ResourceNotFoundException("SupplementIngredient", "id", supplementIngredientId);
                });

        supplementIngredientMapper.updateEntity(supplementIngredient, request);
        SupplementIngredient updated = supplementIngredientRepository.save(supplementIngredient);

        log.info("Supplement ingredient updated successfully with ID: {}", supplementIngredientId);
        return supplementIngredientMapper.toDto(updated);
    }

    @Override
    public void deleteSupplementIngredient(UUID supplementIngredientId) {
        log.info("Deleting supplement ingredient with ID: {}", supplementIngredientId);

        if (!supplementIngredientRepository.existsById(supplementIngredientId)) {
            log.error("Supplement ingredient not found with ID: {}", supplementIngredientId);
            throw new ResourceNotFoundException("SupplementIngredient", "id", supplementIngredientId);
        }

        supplementIngredientRepository.deleteById(supplementIngredientId);
        log.info("Supplement ingredient deleted successfully with ID: {}", supplementIngredientId);
    }

    @Override
    public void deleteBySupplementId(UUID supplementId) {
        log.info("Deleting all ingredients for supplement ID: {}", supplementId);

        supplementIngredientRepository.deleteBySupplement_SupplementId(supplementId);
        log.info("All ingredients deleted successfully for supplement ID: {}", supplementId);
    }

    @Override
    public boolean existsBySupplementAndIngredient(UUID supplementId, UUID ingredientId) {
        log.info("Checking if supplement {} contains ingredient {}", supplementId, ingredientId);

        return supplementIngredientRepository.existsBySupplement_SupplementIdAndIngredient_IngredientId(
                supplementId, ingredientId);
    }
}
