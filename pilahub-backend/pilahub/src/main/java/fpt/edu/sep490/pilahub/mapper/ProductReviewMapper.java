package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.ProductReviewDto;
import fpt.edu.sep490.pilahub.dto.request.product.CreateProductReviewRequest;
import fpt.edu.sep490.pilahub.dto.request.product.UpdateProductReviewRequest;
import fpt.edu.sep490.pilahub.pojo.ProductReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductReviewMapper {

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "accountId", source = "account.accountId")
    ProductReviewDto toDto(ProductReview productReview);

    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "reviewerName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "verifiedPurchase", ignore = true)
    ProductReview toEntity(CreateProductReviewRequest request);

    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "reviewerName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "verifiedPurchase", ignore = true)
    void updateEntityFromRequest(UpdateProductReviewRequest request, @MappingTarget ProductReview productReview);
}
