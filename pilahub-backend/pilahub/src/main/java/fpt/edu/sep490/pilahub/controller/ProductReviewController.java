package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.ProductReviewDto;
import fpt.edu.sep490.pilahub.dto.request.product.CreateProductReviewRequest;
import fpt.edu.sep490.pilahub.dto.request.product.UpdateProductReviewRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.ProductReviewService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-reviews")
@RequiredArgsConstructor
@Tag(name = "Product Review", description = "Manage product reviews and ratings")
public class ProductReviewController {

    private final ProductReviewService productReviewService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'VENDOR')")
    @Operation(summary = "Create product review", description = "Submit a review for a product")
    @ApiResponse(responseCode = "201", description = "Review created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or duplicate review")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<ProductReviewDto>> createReview(
            @Valid @RequestBody CreateProductReviewRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();
        ProductReviewDto review = productReviewService.createReview(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Review submitted successfully", review));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID", description = "Retrieve a specific product review by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @ApiResponse(responseCode = "404", description = "Review not found")
    public ResponseEntity<APIResponse<ProductReviewDto>> getReviewById(@PathVariable("id") UUID reviewId) {
        ProductReviewDto review = productReviewService.getById(reviewId);
        return ResponseEntity.ok(APIResponse.success("Review retrieved successfully", review));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews by product ID", description = "Retrieve all reviews for a specific product")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<ProductReviewDto>>> getReviewsByProductId(
            @PathVariable("productId") UUID productId) {
        List<ProductReviewDto> reviews = productReviewService.getByProductId(productId);
        return ResponseEntity.ok(APIResponse.success("Reviews retrieved successfully", reviews));
    }

    // @GetMapping("/account/me")
    // @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'VENDOR')")
    // @Operation(summary = "Get my reviews", description = "Retrieve all reviews submitted by the current user")
    // @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    // @ApiResponse(responseCode = "401", description = "Unauthorized")
    // public ResponseEntity<APIResponse<List<ProductReviewDto>>> getMyReviews() {
    //     UUID accountId = securityUtil.getCurrentUserId();
    //     List<ProductReviewDto> reviews = productReviewService.getByAccountId(accountId);
    //     return ResponseEntity.ok(APIResponse.success("Reviews retrieved successfully", reviews));
    // }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'VENDOR')")
    @Operation(summary = "Update review", description = "Update your own product review")
    @ApiResponse(responseCode = "200", description = "Review updated successfully")
    @ApiResponse(responseCode = "404", description = "Review not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Cannot update other users' reviews")
    public ResponseEntity<APIResponse<ProductReviewDto>> updateReview(
            @PathVariable("id") UUID reviewId,
            @Valid @RequestBody UpdateProductReviewRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();
        ProductReviewDto review = productReviewService.updateReview(reviewId, accountId, request);
        return ResponseEntity.ok(APIResponse.success("Review updated successfully", review));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Delete review", description = "Delete your own product review (or any review if admin)")
    @ApiResponse(responseCode = "200", description = "Review deleted successfully")
    @ApiResponse(responseCode = "404", description = "Review not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Cannot delete other users' reviews")
    public ResponseEntity<APIResponse<Void>> deleteReview(@PathVariable("id") UUID reviewId) {
        UUID accountId = securityUtil.getCurrentUserId();
        productReviewService.deleteReview(reviewId, accountId);
        return ResponseEntity.ok(APIResponse.success("Review deleted successfully", null));
    }
}
