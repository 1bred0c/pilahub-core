package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.IngredientRuleDto;
import fpt.edu.sep490.pilahub.dto.request.ingredient.CreateIngredientRuleRequest;
import fpt.edu.sep490.pilahub.dto.request.ingredient.UpdateIngredientRuleRequest;
import fpt.edu.sep490.pilahub.enums.RuleAction;
import fpt.edu.sep490.pilahub.enums.RuleOperator;
import fpt.edu.sep490.pilahub.enums.RuleSeverity;
import fpt.edu.sep490.pilahub.enums.RuleType;
import fpt.edu.sep490.pilahub.pojo.IngredientRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IngredientRuleMapper {

    @Mapping(target = "ingredientId", source = "ingredient.ingredientId")
    @Mapping(target = "ruleType", qualifiedByName = "ruleTypeToString")
    @Mapping(target = "operator", qualifiedByName = "operatorToString")
    @Mapping(target = "severity", qualifiedByName = "severityToString")
    @Mapping(target = "action", qualifiedByName = "actionToString")
    IngredientRuleDto toDto(IngredientRule ingredientRule);

    @Mapping(target = "ingredientRuleId", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "ruleType", qualifiedByName = "stringToRuleType")
    @Mapping(target = "operator", qualifiedByName = "stringToOperator")
    @Mapping(target = "severity", qualifiedByName = "stringToSeverity")
    @Mapping(target = "action", qualifiedByName = "stringToAction")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    IngredientRule toEntity(CreateIngredientRuleRequest request);

    @Mapping(target = "ingredientRuleId", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "ruleType", qualifiedByName = "stringToRuleType")
    @Mapping(target = "operator", qualifiedByName = "stringToOperator")
    @Mapping(target = "severity", qualifiedByName = "stringToSeverity")
    @Mapping(target = "action", qualifiedByName = "stringToAction")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget IngredientRule ingredientRule, UpdateIngredientRuleRequest request);

    @Named("ruleTypeToString")
    default String ruleTypeToString(RuleType ruleType) {
        return ruleType != null ? ruleType.name() : null;
    }

    @Named("operatorToString")
    default String operatorToString(RuleOperator operator) {
        return operator != null ? operator.name() : null;
    }

    @Named("severityToString")
    default String severityToString(RuleSeverity severity) {
        return severity != null ? severity.name() : null;
    }

    @Named("actionToString")
    default String actionToString(RuleAction action) {
        return action != null ? action.name() : null;
    }

    @Named("stringToRuleType")
    default RuleType stringToRuleType(String ruleType) {
        return ruleType != null ? RuleType.valueOf(ruleType) : null;
    }

    @Named("stringToOperator")
    default RuleOperator stringToOperator(String operator) {
        return operator != null ? RuleOperator.valueOf(operator) : null;
    }

    @Named("stringToSeverity")
    default RuleSeverity stringToSeverity(String severity) {
        return severity != null ? RuleSeverity.valueOf(severity) : null;
    }

    @Named("stringToAction")
    default RuleAction stringToAction(String action) {
        return action != null ? RuleAction.valueOf(action) : null;
    }
}
