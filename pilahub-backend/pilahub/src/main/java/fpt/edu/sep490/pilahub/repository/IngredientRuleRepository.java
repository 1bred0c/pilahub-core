package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.RuleAction;
import fpt.edu.sep490.pilahub.enums.RuleSeverity;
import fpt.edu.sep490.pilahub.enums.RuleType;
import fpt.edu.sep490.pilahub.pojo.IngredientRule;
import fpt.edu.sep490.pilahub.pojo.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IngredientRuleRepository extends JpaRepository<IngredientRule, UUID> {

    List<IngredientRule> findByIngredient(Ingredient ingredient);

    List<IngredientRule> findByIngredient_IngredientId(UUID ingredientId);

    List<IngredientRule> findByRuleType(RuleType ruleType);

    List<IngredientRule> findBySeverity(RuleSeverity severity);

    List<IngredientRule> findByAction(RuleAction action);

    void deleteByIngredient_IngredientId(UUID ingredientId);
}
