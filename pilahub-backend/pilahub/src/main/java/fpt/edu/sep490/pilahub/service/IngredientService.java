package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.IngredientDto;
import fpt.edu.sep490.pilahub.dto.IngredientWithRulesDto;
import fpt.edu.sep490.pilahub.dto.request.ingredient.CreateIngredientRequest;
import fpt.edu.sep490.pilahub.dto.request.ingredient.CreateIngredientRuleRequest;
import fpt.edu.sep490.pilahub.dto.request.ingredient.UpdateIngredientRequest;

import java.util.List;
import java.util.UUID;

public interface IngredientService {

    IngredientWithRulesDto createIngredient(CreateIngredientRequest request);

    IngredientWithRulesDto getById(UUID ingredientId);

    List<IngredientDto> getAll();

    List<IngredientDto> getAllActive();

    List<IngredientDto> searchByName(String name);

    IngredientWithRulesDto updateIngredient(UUID ingredientId, UpdateIngredientRequest request);

    IngredientWithRulesDto addIngredientRule(UUID ingredientId, CreateIngredientRuleRequest request);

    void deactivateIngredient(UUID ingredientId);

    void activateIngredient(UUID ingredientId);

}
