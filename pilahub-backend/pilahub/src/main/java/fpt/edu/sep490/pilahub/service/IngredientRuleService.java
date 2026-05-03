package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.IngredientRuleDto;

import java.util.List;
import java.util.UUID;

public interface IngredientRuleService {

    List<IngredientRuleDto> getByIngredientId(UUID ingredientId);

    List<IngredientRuleDto> getByRuleType(String ruleType);
}
