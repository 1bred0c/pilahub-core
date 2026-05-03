package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.IngredientDto;
import fpt.edu.sep490.pilahub.dto.IngredientWithRulesDto;
import fpt.edu.sep490.pilahub.dto.request.ingredient.CreateIngredientRequest;
import fpt.edu.sep490.pilahub.dto.request.ingredient.CreateIngredientRuleRequest;
import fpt.edu.sep490.pilahub.dto.request.ingredient.UpdateIngredientRequest;
import fpt.edu.sep490.pilahub.dto.request.ingredient.UpdateIngredientRuleRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.IngredientMapper;
import fpt.edu.sep490.pilahub.mapper.IngredientRuleMapper;
import fpt.edu.sep490.pilahub.pojo.Ingredient;
import fpt.edu.sep490.pilahub.pojo.IngredientRule;
import fpt.edu.sep490.pilahub.repository.IngredientRepository;
import fpt.edu.sep490.pilahub.repository.IngredientRuleRepository;
import fpt.edu.sep490.pilahub.service.IngredientService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientRuleRepository ingredientRuleRepository;
    private final IngredientMapper ingredientMapper;
    private final IngredientRuleMapper ingredientRuleMapper;

    @Override
    public IngredientWithRulesDto createIngredient(CreateIngredientRequest request) {
        log.info("Creating ingredient with name: {}", request.name());

        Ingredient ingredient = ingredientMapper.toEntity(request);
        Ingredient saved = ingredientRepository.save(ingredient);

        if (request.ingredientRules() != null && !request.ingredientRules().isEmpty()) {
            List<IngredientRule> rules = request.ingredientRules().stream()
                    .map(ingredientRuleMapper::toEntity)
                    .peek(rule -> rule.setIngredient(saved))
                    .toList();

            ingredientRuleRepository.saveAll(rules);
            log.info("Created {} ingredient rules for ingredient ID: {}", rules.size(), saved.getIngredientId());
        }

        log.info("Ingredient created successfully with ID: {}", saved.getIngredientId());
        return toIngredientWithRulesDto(saved);
    }

    @Override
    public IngredientWithRulesDto getById(UUID ingredientId) {
        log.info("Fetching ingredient with ID: {}", ingredientId);

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> {
                    log.error("Ingredient not found with ID: {}", ingredientId);
                    return new ResourceNotFoundException("Ingredient", "id", ingredientId);
                });

        return toIngredientWithRulesDto(ingredient);
    }

    @Override
    public List<IngredientDto> getAll() {
        log.info("Fetching all ingredients");

        return ingredientRepository.findAll().stream()
                .map(ingredientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<IngredientDto> getAllActive() {
        log.info("Fetching all active ingredients");

        return ingredientRepository.findByActiveTrue().stream()
                .map(ingredientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<IngredientDto> searchByName(String name) {
        log.info("Searching ingredients with name containing: {}", name);

        return ingredientRepository.findByNameContainingIgnoreCase(name).stream()
                .map(ingredientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IngredientWithRulesDto updateIngredient(UUID ingredientId, UpdateIngredientRequest request) {
        log.info("Patching ingredient with ID: {}", ingredientId);

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", "id", ingredientId));

        // update partial ingredient
        ingredientMapper.updateEntity(ingredient, request);

        // xử lý rules (PATCH)
        if (request.ingredientRules() != null) {

            List<IngredientRule> toSave = new ArrayList<>();

            for (UpdateIngredientRuleRequest item : request.ingredientRules()) {

                if (item.ruleId() != null) {
                    // update existing rule
                    IngredientRule existing = ingredientRuleRepository.findById(item.ruleId())
                            .orElseThrow(() -> new ResourceNotFoundException("IngredientRule", "id", item.ruleId()));

                    ingredientRuleMapper.updateEntity(existing, item);
                    toSave.add(existing);

                } else {
                    // create new rule
                    IngredientRule newRule = new IngredientRule();
                    newRule.setRuleType(item.ruleType());
                    newRule.setRuleDescription(item.ruleDescription());
                    newRule.setOperator(item.operator());
                    newRule.setValue(item.value());
                    newRule.setSeverity(item.severity());
                    newRule.setAction(item.action());
                    newRule.setIngredient(ingredient);
                    toSave.add(newRule);
                }
            }

            ingredientRuleRepository.saveAll(toSave);

            log.info("Patched {} rules for ingredient ID: {}", toSave.size(), ingredientId);
        }

        Ingredient updated = ingredientRepository.save(ingredient);

        return toIngredientWithRulesDto(updated);
    }

    @Override
    public IngredientWithRulesDto addIngredientRule(UUID ingredientId, CreateIngredientRuleRequest request) {
        log.info("Adding new rule for ingredient with ID: {}", ingredientId);

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> {
                    log.error("Ingredient not found with ID: {}", ingredientId);
                    return new ResourceNotFoundException("Ingredient", "id", ingredientId);
                });

        IngredientRule ingredientRule = ingredientRuleMapper.toEntity(request);
        ingredientRule.setIngredient(ingredient);
        ingredientRuleRepository.save(ingredientRule);

        log.info("Added rule {} to ingredient ID: {}", ingredientRule.getIngredientRuleId(), ingredientId);
        return toIngredientWithRulesDto(ingredient);
    }

    private IngredientWithRulesDto toIngredientWithRulesDto(Ingredient ingredient) {
        IngredientDto ingredientDto = ingredientMapper.toDto(ingredient);

        return new IngredientWithRulesDto(
                ingredientDto.ingredientId(),
                ingredientDto.name(),
                ingredientDto.active(),
                ingredientDto.createdAt(),
                ingredientDto.updatedAt(),
                ingredientRuleRepository.findByIngredient_IngredientId(ingredient.getIngredientId()).stream()
                        .map(ingredientRuleMapper::toDto)
                        .collect(Collectors.toList()));
    }

    @Override
    public void deactivateIngredient(UUID ingredientId) {
        log.info("Deactivating ingredient with ID: {}", ingredientId);

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> {
                    log.error("Ingredient not found with ID: {}", ingredientId);
                    return new ResourceNotFoundException("Ingredient", "id", ingredientId);
                });

        ingredient.setActive(false);
        ingredientRepository.save(ingredient);

        log.info("Ingredient deactivated successfully with ID: {}", ingredientId);
    }

    @Override
    public void activateIngredient(UUID ingredientId) {
        log.info("Activating ingredient with ID: {}", ingredientId);

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> {
                    log.error("Ingredient not found with ID: {}", ingredientId);
                    return new ResourceNotFoundException("Ingredient", "id", ingredientId);
                });

        ingredient.setActive(true);
        ingredientRepository.save(ingredient);

        log.info("Ingredient activated successfully with ID: {}", ingredientId);
    }
}
