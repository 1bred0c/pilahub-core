package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.SupplementIngredientDto;
import fpt.edu.sep490.pilahub.dto.request.supplement.CreateSupplementIngredientRequest;
import fpt.edu.sep490.pilahub.dto.request.supplement.UpdateSupplementIngredientRequest;
import fpt.edu.sep490.pilahub.pojo.SupplementIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {IngredientMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplementIngredientMapper {

    @Mapping(target = "supplementId", source = "supplement.supplementId")
    SupplementIngredientDto toDto(SupplementIngredient supplementIngredient);

    @Mapping(target = "supplementIngredientId", ignore = true)
    @Mapping(target = "supplement", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SupplementIngredient toEntity(CreateSupplementIngredientRequest request);

    @Mapping(target = "supplementIngredientId", ignore = true)
    @Mapping(target = "supplement", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget SupplementIngredient supplementIngredient, UpdateSupplementIngredientRequest request);
}
