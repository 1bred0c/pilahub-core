package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.SupplementIngredient;
import fpt.edu.sep490.pilahub.pojo.Supplement;
import fpt.edu.sep490.pilahub.pojo.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplementIngredientRepository extends JpaRepository<SupplementIngredient, UUID> {

    List<SupplementIngredient> findBySupplement(Supplement supplement);

    List<SupplementIngredient> findBySupplement_SupplementId(UUID supplementId);

    List<SupplementIngredient> findByIngredient_IngredientId(UUID ingredientId);

    boolean existsBySupplement_SupplementIdAndIngredient_IngredientId(UUID supplementId, UUID ingredientId);

    void deleteBySupplement_SupplementId(UUID supplementId);
}
