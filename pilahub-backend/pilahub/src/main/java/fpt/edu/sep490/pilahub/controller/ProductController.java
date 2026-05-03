package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.ProductDto;
import fpt.edu.sep490.pilahub.dto.request.product.CreateProductRequest;
import fpt.edu.sep490.pilahub.dto.request.product.UpdateProductRuleViolationRequest;
import fpt.edu.sep490.pilahub.dto.request.product.UpdateProductRequest;
import fpt.edu.sep490.pilahub.dto.request.product.ProductFilterRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.dto.response.PageResponse;
import fpt.edu.sep490.pilahub.service.ProductService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product management APIs")
public class ProductController {

        private final ProductService productService;
        private final SecurityUtil securityUtil;

        @PostMapping
        @PreAuthorize("hasRole('VENDOR')")
        @Operation(summary = "Create product", description = "Create a new product for the current vendor")
        @ApiResponse(responseCode = "201", description = "Product created successfully")
        public ResponseEntity<APIResponse<ProductDto>> createProduct(
                        @Valid @RequestBody CreateProductRequest request) {

                UUID vendorId = securityUtil.getCurrentUserId();

                ProductDto product = productService.createProduct(vendorId, request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(APIResponse.success("Product created successfully", product));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
        @ApiResponse(responseCode = "200", description = "Product retrieved successfully")
        @ApiResponse(responseCode = "404", description = "Product not found")
        public ResponseEntity<APIResponse<ProductDto>> getProductById(
                        @PathVariable("id") UUID productId) {

                ProductDto product = productService.getById(productId);

                return ResponseEntity.ok(
                                APIResponse.success("Product retrieved successfully", product));
        }

        @GetMapping
        @Operation(summary = "Get products", description = "Retrieve products with filter and pagination")
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
        public ResponseEntity<APIResponse<PageResponse<ProductDto>>> getProducts(
                        @ParameterObject ProductFilterRequest filter,
                        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                Page<ProductDto> products = productService.getProducts(filter, pageable);

                PageResponse<ProductDto> pageProducts = new PageResponse<>(
                                products.getContent(),
                                products.getNumber(),
                                products.getSize(),
                                products.getTotalElements(),
                                products.getTotalPages());

                return ResponseEntity.ok(
                                APIResponse.success("Products retrieved successfully", pageProducts));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN','VENDOR')")
        @Operation(summary = "Update product", description = "Update an existing product")
        @ApiResponse(responseCode = "200", description = "Product updated successfully")
        @ApiResponse(responseCode = "404", description = "Product not found")
        public ResponseEntity<APIResponse<ProductDto>> updateProduct(
                        @PathVariable("id") UUID productId,
                        @Valid @RequestBody UpdateProductRequest request) {

                ProductDto product = productService.updateProduct(productId, request);

                return ResponseEntity.ok(
                                APIResponse.success("Product updated successfully", product));
        }

        @PatchMapping("/{id}/activate")
        @PreAuthorize("hasAnyRole('ADMIN')")
        @Operation(summary = "Activate product", description = "Activate a product")
        public ResponseEntity<APIResponse<Void>> activateProduct(
                        @PathVariable("id") UUID productId) {

                productService.activateProduct(productId);

                return ResponseEntity.ok(
                                APIResponse.success("Product activated successfully", null));
        }

        @PatchMapping("/{id}/deactivate")
        @PreAuthorize("hasAnyRole('ADMIN','VENDOR')")
        @Operation(summary = "Deactivate product", description = "Deactivate a product")
        public ResponseEntity<APIResponse<Void>> deactivateProduct(
                        @PathVariable("id") UUID productId) {

                productService.deactivateProduct(productId);

                return ResponseEntity.ok(
                                APIResponse.success("Product deactivated successfully", null));
        }

        @PatchMapping("/{id}/rule-violation")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Update product rule violation", description = "Set or clear product rule violation flag")
        public ResponseEntity<APIResponse<ProductDto>> updateRuleViolation(
                        @PathVariable("id") UUID productId,
                        @Valid @RequestBody UpdateProductRuleViolationRequest request) {

                ProductDto product = productService.updateRuleViolation(productId, request.ruleViolation());

                return ResponseEntity.ok(
                                APIResponse.success("Product rule violation updated successfully", product));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Delete product", description = "Permanently delete a product")
        public ResponseEntity<APIResponse<Void>> deleteProduct(
                        @PathVariable("id") UUID productId) {

                productService.deleteProduct(productId);

                return ResponseEntity.ok(
                                APIResponse.success("Product deleted successfully", null));
        }

        @GetMapping("/roadmaps/{roadmapId}/supplements")
        @Operation(summary = "Get supplement products by roadmap", description = "Retrieve supplement products associated with a specific roadmap")
        @ApiResponse(responseCode = "200", description = "Supplement products retrieved successfully")
        public ResponseEntity<APIResponse<PageResponse<ProductDto>>> getSupplementProductsByRoadmap(
                        @PathVariable UUID roadmapId,
                        @ParameterObject ProductFilterRequest filter,
                        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                Page<ProductDto> products = productService.getSupplementProductByRoadmapId(roadmapId, filter, pageable);

                PageResponse<ProductDto> pageProducts = new PageResponse<>(
                                products.getContent(),
                                products.getNumber(),
                                products.getSize(),
                                products.getTotalElements(),
                                products.getTotalPages());

                return ResponseEntity.ok(
                                APIResponse.success("Supplement products retrieved successfully", pageProducts));
        }

        @GetMapping("/roadmaps/{roadmapId}/equipments")
        @Operation(summary = "Get equipment products by roadmap", description = "Retrieve equipment products associated with a specific roadmap")
        @ApiResponse(responseCode = "200", description = "Equipment products retrieved successfully")
        public ResponseEntity<APIResponse<PageResponse<ProductDto>>> getEquipmentProductsByRoadmap(
                        @PathVariable UUID roadmapId,
                        @ParameterObject ProductFilterRequest filter,
                        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                Page<ProductDto> products = productService.getEquipmentProductByRoadmapId(roadmapId, filter, pageable);

                PageResponse<ProductDto> pageProducts = new PageResponse<>(
                                products.getContent(),
                                products.getNumber(),
                                products.getSize(),
                                products.getTotalElements(),
                                products.getTotalPages());

                return ResponseEntity.ok(
                                APIResponse.success("Equipment products retrieved successfully", pageProducts));
        }

        @GetMapping("/roadmaps/{roadmapId}")
        @Operation(summary = "Get all roadmap products", description = "Retrieve all products (supplements and equipments) associated with a roadmap")
        @ApiResponse(responseCode = "200", description = "Roadmap products retrieved successfully")
        public ResponseEntity<APIResponse<PageResponse<ProductDto>>> getAllProductsByRoadmap(
                        @PathVariable UUID roadmapId,
                        @ParameterObject ProductFilterRequest filter,
                        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                Page<ProductDto> products = productService.getAllProductByRoadmapId(roadmapId, filter, pageable);

                PageResponse<ProductDto> pageProducts = new PageResponse<>(
                                products.getContent(),
                                products.getNumber(),
                                products.getSize(),
                                products.getTotalElements(),
                                products.getTotalPages());

                return ResponseEntity.ok(
                                APIResponse.success("Roadmap products retrieved successfully", pageProducts));
        }
}
