package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.IngredientRuleDto;
import fpt.edu.sep490.pilahub.enums.RuleType;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.IngredientRuleMapper;
import fpt.edu.sep490.pilahub.pojo.IngredientRule;
import fpt.edu.sep490.pilahub.repository.IngredientRuleRepository;
import fpt.edu.sep490.pilahub.service.IngredientRuleService;
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
public class IngredientRuleServiceImpl implements IngredientRuleService {

    private final IngredientRuleRepository ingredientRuleRepository;
    private final IngredientRuleMapper ingredientRuleMapper;

    @Override
    public List<IngredientRuleDto> getByIngredientId(UUID ingredientId) {
        log.info("Fetching rules for ingredient ID: {}", ingredientId);

        return ingredientRuleRepository.findByIngredient_IngredientId(ingredientId).stream()
                .map(ingredientRuleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<IngredientRuleDto> getByRuleType(String ruleType) {
        log.info("Fetching rules by type: {}", ruleType);

        RuleType type = RuleType.valueOf(ruleType);
        return ingredientRuleRepository.findByRuleType(type).stream()
                .map(ingredientRuleMapper::toDto)
                .collect(Collectors.toList());
    }
}
