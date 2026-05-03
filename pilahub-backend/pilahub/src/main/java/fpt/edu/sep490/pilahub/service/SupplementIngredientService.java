package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.SupplementIngredientDto;
import fpt.edu.sep490.pilahub.dto.request.supplement.CreateSupplementIngredientRequest;
import fpt.edu.sep490.pilahub.dto.request.supplement.UpdateSupplementIngredientRequest;

import java.util.List;
import java.util.UUID;

public interface SupplementIngredientService {

    SupplementIngredientDto createSupplementIngredient(CreateSupplementIngredientRequest request);

    SupplementIngredientDto getById(UUID supplementIngredientId);

    List<SupplementIngredientDto> getBySupplementId(UUID supplementId);

    List<SupplementIngredientDto> getByIngredientId(UUID ingredientId);

    SupplementIngredientDto updateSupplementIngredient(UUID supplementIngredientId, UpdateSupplementIngredientRequest request);

    void deleteSupplementIngredient(UUID supplementIngredientId);

    void deleteBySupplementId(UUID supplementId);

    boolean existsBySupplementAndIngredient(UUID supplementId, UUID ingredientId);
}
