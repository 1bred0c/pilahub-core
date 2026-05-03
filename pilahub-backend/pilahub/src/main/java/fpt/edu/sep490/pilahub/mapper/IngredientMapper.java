package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.IngredientDto;
import fpt.edu.sep490.pilahub.dto.request.ingredient.CreateIngredientRequest;
import fpt.edu.sep490.pilahub.dto.request.ingredient.UpdateIngredientRequest;
import fpt.edu.sep490.pilahub.pojo.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IngredientMapper {

    IngredientDto toDto(Ingredient ingredient);

    @Mapping(target = "ingredientId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    Ingredient toEntity(CreateIngredientRequest request);

    @Mapping(target = "ingredientId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(@MappingTarget Ingredient ingredient, UpdateIngredientRequest request);
}
