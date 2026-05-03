package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.ProductDto;
import fpt.edu.sep490.pilahub.dto.request.product.CreateProductRequest;
import fpt.edu.sep490.pilahub.dto.request.product.UpdateProductRequest;
import fpt.edu.sep490.pilahub.dto.request.product.ProductFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {

        ProductDto createProduct(UUID vendorId, CreateProductRequest request);

        ProductDto getById(UUID productId);

        ProductDto updateProduct(UUID productId, UpdateProductRequest request);

        ProductDto updateRuleViolation(UUID productId, boolean ruleViolation);

        void deleteProduct(UUID productId);

        void activateProduct(UUID productId);

        void deactivateProduct(UUID productId);

        Page<ProductDto> getProducts(ProductFilterRequest filter, Pageable pageable);

        Page<ProductDto> getSupplementProductByRoadmapId(
                        UUID roadmapId,
                        ProductFilterRequest filter,
                        Pageable pageable);

        Page<ProductDto> getEquipmentProductByRoadmapId(
                        UUID roadmapId,
                        ProductFilterRequest filter,
                        Pageable pageable);

        Page<ProductDto> getAllProductByRoadmapId(
                        UUID roadmapId,
                        ProductFilterRequest filter,
                        Pageable pageable);
}
