package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.ProductDto;
import fpt.edu.sep490.pilahub.dto.request.product.CreateProductRequest;
import fpt.edu.sep490.pilahub.dto.request.product.UpdateProductRequest;
import fpt.edu.sep490.pilahub.pojo.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "vendorId", source = "vendor.vendorId")
    @Mapping(target = "vendorBusinessName", source = "vendor.businessName")
    @Mapping(target = "categoryId", source = "category.categoryId")
    @Mapping(target = "categoryName", source = "category.name")
    ProductDto toDto(Product product);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "ruleViolation", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "ruleViolation", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    void updateEntityFromRequest(UpdateProductRequest request, @MappingTarget Product product);

    default List<String> map(String[] value) {
        if (value == null) {
            return null;
        }
        return Arrays.asList(value);
    }

    default String[] map(List<String> value) {
        if (value == null) {
            return null;
        }
        return value.toArray(new String[0]);
    }
}
