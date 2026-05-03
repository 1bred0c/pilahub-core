package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.ProductReviewDto;
import fpt.edu.sep490.pilahub.dto.request.product.CreateProductReviewRequest;
import fpt.edu.sep490.pilahub.dto.request.product.UpdateProductReviewRequest;

import java.util.List;
import java.util.UUID;

public interface ProductReviewService {

    ProductReviewDto createReview(UUID accountId, CreateProductReviewRequest request);

    ProductReviewDto getById(UUID reviewId);

    List<ProductReviewDto> getByProductId(UUID productId);

    List<ProductReviewDto> getByAccountId(UUID accountId);

    ProductReviewDto updateReview(UUID reviewId, UUID accountId, UpdateProductReviewRequest request);

    void deleteReview(UUID reviewId, UUID accountId);
}
