package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.CategoryDto;
import fpt.edu.sep490.pilahub.dto.request.category.CreateCategoryRequest;
import fpt.edu.sep490.pilahub.dto.request.category.UpdateCategoryRequest;
import fpt.edu.sep490.pilahub.pojo.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "parentCategoryId", source = "parentCategory.categoryId")
    CategoryDto toDto(Category category);

    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(UpdateCategoryRequest request, @MappingTarget Category category);
}
