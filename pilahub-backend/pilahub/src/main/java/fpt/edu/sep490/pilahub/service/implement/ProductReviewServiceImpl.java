package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.ProductReviewDto;
import fpt.edu.sep490.pilahub.dto.request.product.CreateProductReviewRequest;
import fpt.edu.sep490.pilahub.dto.request.product.UpdateProductReviewRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.ProductReviewMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.Product;
import fpt.edu.sep490.pilahub.pojo.ProductReview;
import fpt.edu.sep490.pilahub.repository.*;
import fpt.edu.sep490.pilahub.service.ProductReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final ProductReviewMapper productReviewMapper;
    private final CoachRepository coachRepository;
    private final TraineeRepository traineeRepository;

    @Override
    public ProductReviewDto createReview(UUID accountId, CreateProductReviewRequest request) {
        log.info("Creating review for product ID: {} from account ID: {}", request.productId(), accountId);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.productId()));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // Check if account has already reviewed this product
        if (productReviewRepository.existsByProduct_ProductIdAndAccount_AccountId(request.productId(), accountId)) {
            throw new IllegalStateException("You have already reviewed this product");
        }

        String reviewerName = traineeRepository.getById(accountId).getFullName() != null ? traineeRepository.getById(accountId).getFullName() : coachRepository.getById(accountId).getFullName();
        ProductReview review = productReviewMapper.toEntity(request);
        review.setProduct(product);
        review.setAccount(account);
        review.setReviewerName(reviewerName != null ? reviewerName : "Unknown");
        review.setVerifiedPurchase(false); // Default to false, can be updated later

        ProductReview saved = productReviewRepository.save(review);
        log.info("Successfully created review with ID: {}", saved.getReviewId());

        // Update product's average rating and review count
        updateProductRatingAndCount(product.getProductId());

        return productReviewMapper.toDto(saved);
    }

    @Override
    public ProductReviewDto getById(UUID reviewId) {
        log.info("Fetching review by ID: {}", reviewId);

        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Review", "id", reviewId));

        return productReviewMapper.toDto(review);
    }

    @Override
    public List<ProductReviewDto> getByProductId(UUID productId) {
        log.info("Fetching all reviews for product ID: {}", productId);

        return productReviewRepository.findByProduct_ProductId(productId).stream()
                .map(productReviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductReviewDto> getByAccountId(UUID accountId) {
        log.info("Fetching all reviews by account ID: {}", accountId);

        return productReviewRepository.findByAccount_AccountId(accountId).stream()
                .map(productReviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductReviewDto updateReview(UUID reviewId, UUID accountId, UpdateProductReviewRequest request) {
        log.info("Updating review ID: {} by account ID: {}", reviewId, accountId);

        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Review", "id", reviewId));

        // Verify that the review belongs to the account
        if (!review.getAccount().getAccountId().equals(accountId)) {
            throw new IllegalStateException("You can only update your own review");
        }

        productReviewMapper.updateEntityFromRequest(request, review);

        ProductReview updated = productReviewRepository.save(review);
        log.info("Successfully updated review with ID: {}", reviewId);

        // Update product's average rating
        updateProductRatingAndCount(review.getProduct().getProductId());

        return productReviewMapper.toDto(updated);
    }

    @Override
    public void deleteReview(UUID reviewId, UUID accountId) {
        log.info("Deleting review ID: {} by account ID: {}", reviewId, accountId);

        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Review", "id", reviewId));

        // Verify that the review belongs to the account
        if (!review.getAccount().getAccountId().equals(accountId)) {
            throw new IllegalStateException("You can only delete your own review");
        }

        UUID productId = review.getProduct().getProductId();
        productReviewRepository.delete(review);
        log.info("Successfully deleted review with ID: {}", reviewId);

        // Update product's average rating and review count
        updateProductRatingAndCount(productId);
    }

    private void updateProductRatingAndCount(UUID productId) {
        log.info("Updating rating and review count for product ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Double avgRating = productRepository.calculateAverageRatingByProductId(productId);
        Integer reviewCount = productRepository.countReviewsByProductId(productId);

        // Set avgRating to null if there are no reviews, otherwise round to 2 decimal places
        if (avgRating != null) {
            product.setAvgRating(Math.round(avgRating * 10.0) / 10.0);
        } else {
            product.setAvgRating(null);
        }

        product.setReviewCount(reviewCount != null ? reviewCount : 0);

        productRepository.save(product);
        log.info("Successfully updated product ID: {} with avgRating: {} and reviewCount: {}", 
                productId, product.getAvgRating(), product.getReviewCount());
    }
}
