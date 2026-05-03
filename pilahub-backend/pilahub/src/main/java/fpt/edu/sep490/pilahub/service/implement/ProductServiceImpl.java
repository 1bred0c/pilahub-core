package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.ProductDto;
import fpt.edu.sep490.pilahub.dto.request.product.CreateProductRequest;
import fpt.edu.sep490.pilahub.dto.request.product.UpdateProductRequest;
import fpt.edu.sep490.pilahub.dto.request.product.ProductFilterRequest;
import fpt.edu.sep490.pilahub.enums.CategoryType;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.exception.InvalidRequestException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.ProductMapper;
import fpt.edu.sep490.pilahub.pojo.*;
import fpt.edu.sep490.pilahub.repository.*;
import fpt.edu.sep490.pilahub.service.ProductService;
import fpt.edu.sep490.pilahub.service.SystemConfigService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {

        private final ProductRepository productRepository;
        private final VendorRepository vendorRepository;
        private final CategoryRepository categoryRepository;
        private final ProductMapper productMapper;
        private final SecurityUtil securityUtil;
        private final SystemConfigService systemConfigService;
        private final PersonalStageSupplementRepository personalStageSupplementRepository;
        private final PersonalExerciseRepository personalExerciseRepository;

        @Override
        public ProductDto createProduct(UUID vendorId, CreateProductRequest request) {

                if (Boolean.TRUE.equals(request.installationSupported())
                                && (request.regionSupported() == null || request.regionSupported().isEmpty())) {
                        throw new InvalidRequestException(
                                        "regionSupported must not be null or empty when installationSupported is true");
                }

                validateSupplementExpiredDate(request.categoryType(), request.expiredDate());
                validateCreateExpiredDateMonths(request.expiredDate());

                Vendor vendor = vendorRepository.findById(vendorId)
                                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

                Category category = categoryRepository.findById(request.categoryId())
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "id",
                                                request.categoryId()));

                Product product = productMapper.toEntity(request);
                product.setVendor(vendor);
                product.setCategory(category);

                return productMapper.toDto(productRepository.save(product));
        }

        @Override
        public ProductDto getById(UUID productId) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

                return productMapper.toDto(product);
        }

        @Override
        public ProductDto updateProduct(UUID productId, UpdateProductRequest request) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

                validateVendorOwnershipForMutation(product);

                if (request.categoryId() != null) {

                        Category category = categoryRepository.findById(request.categoryId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Category", "id",
                                                        request.categoryId()));

                        product.setCategory(category);
                }

                CategoryType finalCategoryType = request.categoryType() != null ? request.categoryType()
                                : product.getCategoryType();
                Instant finalExpiredDate = request.expiredDate() != null ? request.expiredDate()
                                : product.getExpiredDate();
                validateSupplementExpiredDate(finalCategoryType, finalExpiredDate);

                if (request.expiredDate() != null && !Objects.equals(request.expiredDate(), product.getExpiredDate())) {
                        validateCreateExpiredDateMonths(request.expiredDate());
                }

                productMapper.updateEntityFromRequest(request, product);

                return productMapper.toDto(productRepository.save(product));
        }

        @Override
        public ProductDto updateRuleViolation(UUID productId, boolean ruleViolation) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

                product.setRuleViolation(ruleViolation);

                return productMapper.toDto(productRepository.save(product));
        }

        @Override
        public void deleteProduct(UUID productId) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

                productRepository.delete(product);
        }

        @Override
        public void activateProduct(UUID productId) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

                if (securityUtil.getCurrentUserRole() == Role.VENDOR) {
                        validateVendorOwnershipForMutation(product);
                        if (product.isRuleViolation()) {
                                throw new InvalidRequestException(
                                                "Vendor cannot activate product marked as rule violation");
                        }
                }

                validateSupplementExpiredDate(product.getCategoryType(), product.getExpiredDate());
                validateActiveProductExpiredDateMonths(product.getExpiredDate());

                product.setActive(true);
        }

        @Override
        public void deactivateProduct(UUID productId) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

                validateVendorOwnershipForMutation(product);

                product.setActive(false);
        }

        @Override
        public Page<ProductDto> getProducts(ProductFilterRequest filter, Pageable pageable) {

                return filterProducts(filter, null, pageable);
        }

        @Override
        public Page<ProductDto> getSupplementProductByRoadmapId(
                        UUID roadmapId,
                        ProductFilterRequest filter,
                        Pageable pageable) {

                List<String> names = personalStageSupplementRepository.findSupplementNamesByRoadmapId(roadmapId);
                List<UUID> refIds = findSupplementRefIdsByRoadmapId(roadmapId);
                Specification<Product> spec = buildRoadmapSpec(filter, names, CategoryType.SUPPLEMENT, refIds);

                return productRepository.findAll(spec, pageable)
                                .map(productMapper::toDto);
        }

        @Override
        public Page<ProductDto> getEquipmentProductByRoadmapId(
                        UUID roadmapId,
                        ProductFilterRequest filter,
                        Pageable pageable) {

                List<String> names = personalExerciseRepository.findEquipmentNamesByRoadmapId(roadmapId);
                List<UUID> refIds = findEquipmentRefIdsByRoadmapId(roadmapId);
                Specification<Product> spec = buildRoadmapSpec(filter, names, CategoryType.EQUIPMENT, refIds);

                return productRepository.findAll(spec, pageable)
                                .map(productMapper::toDto);
        }

        @Override
        public Page<ProductDto> getAllProductByRoadmapId(
                        UUID roadmapId,
                        ProductFilterRequest filter,
                        Pageable pageable) {

                List<String> names = getAllRoadmapProductNames(roadmapId);
                List<UUID> supplementRefIds = findSupplementRefIdsByRoadmapId(roadmapId);
                List<UUID> equipmentRefIds = findEquipmentRefIdsByRoadmapId(roadmapId);
                Specification<Product> spec = buildRoadmapSpec(filter, names, supplementRefIds, equipmentRefIds);

                return productRepository.findAll(spec, pageable)
                                .map(productMapper::toDto);
        }

        private void validateSupplementExpiredDate(CategoryType categoryType, Instant expiredDate) {

                if (categoryType == CategoryType.SUPPLEMENT && expiredDate == null) {
                        throw new InvalidRequestException(
                                        "expiredDate must not be null when categoryType is SUPPLEMENT");
                }

                if (expiredDate != null && !expiredDate.isAfter(Instant.now())) {
                        throw new InvalidRequestException(
                                        "expiredDate must be after current time");
                }
        }

        private void validateCreateExpiredDateMonths(Instant expiredDate) {

                if (expiredDate == null) {
                        return;
                }

                int requiredMonths = systemConfigService.getProductCreateRequiredExpiryMonths();
                Instant minExpiredDate = Instant.now()
                                .atOffset(ZoneOffset.UTC)
                                .plusMonths(requiredMonths)
                                .toInstant();

                if (expiredDate.isBefore(minExpiredDate)) {
                        throw new InvalidRequestException(
                                        "expiredDate must be at least " + requiredMonths
                                                        + " month(s) after created time");
                }
        }

        private void validateActiveProductExpiredDateMonths(Instant expiredDate) {

                if (expiredDate == null) {
                        return;
                }

                int requiredMonths = systemConfigService.getActiveProductMinRequiredMonths();
                Instant minExpiredDate = Instant.now()
                                .atOffset(ZoneOffset.UTC)
                                .plusMonths(requiredMonths)
                                .toInstant();

                if (expiredDate.isBefore(minExpiredDate)) {
                        throw new InvalidRequestException(
                                        "expiredDate must be at least " + requiredMonths
                                                        + " month(s) from now to activate product");
                }
        }

        private Specification<Product> buildRoadmapSpec(
                        ProductFilterRequest filter,
                        List<String> names,
                        CategoryType type,
                        List<UUID> refIds) {

                Specification<Product> base = buildBaseFilter(filter);
                List<String> normalizedNames = normalizeNames(names);

                Specification<Product> roadmap = (root, query, cb) -> {

                        Predicate byName = normalizedNames.isEmpty()
                                        ? cb.disjunction()
                                        : cb.lower(root.get("name")).in(normalizedNames);

                        Predicate byRef = (refIds == null || refIds.isEmpty())
                                        ? cb.disjunction()
                                        : cb.and(
                                                        cb.equal(root.get("categoryType"), type),
                                                        root.get("refId").in(refIds));

                        return cb.or(byName, byRef);
                };

                return base.and(roadmap);
        }

        private Specification<Product> buildRoadmapSpec(
                        ProductFilterRequest filter,
                        List<String> names,
                        List<UUID> supplementRefIds,
                        List<UUID> equipmentRefIds) {

                Specification<Product> base = buildBaseFilter(filter);
                List<String> normalizedNames = normalizeNames(names);

                Specification<Product> roadmap = (root, query, cb) -> {

                        Predicate byName = normalizedNames.isEmpty()
                                        ? cb.disjunction()
                                        : cb.lower(root.get("name")).in(normalizedNames);

                        Predicate bySupplement = (supplementRefIds == null || supplementRefIds.isEmpty())
                                        ? cb.disjunction()
                                        : cb.and(
                                                        cb.equal(root.get("categoryType"), CategoryType.SUPPLEMENT),
                                                        root.get("refId").in(supplementRefIds));

                        Predicate byEquipment = (equipmentRefIds == null || equipmentRefIds.isEmpty())
                                        ? cb.disjunction()
                                        : cb.and(
                                                        cb.equal(root.get("categoryType"), CategoryType.EQUIPMENT),
                                                        root.get("refId").in(equipmentRefIds));

                        return cb.or(byName, bySupplement, byEquipment);
                };

                return base.and(roadmap);
        }

        private Specification<Product> buildBaseFilter(ProductFilterRequest filter) {

                boolean isTrainee = securityUtil.getCurrentUser().getRole() == Role.TRAINEE;

                Specification<Product> spec = (root, query, cb) -> cb.conjunction();

                if (filter != null) {

                        if (filter.name() != null && !filter.name().isBlank()) {
                                String normalizedName = filter.name().trim().toLowerCase(Locale.ROOT);
                                spec = spec.and((root, q, cb) -> cb.like(
                                                cb.lower(root.get("name")),
                                                "%" + normalizedName + "%"));
                        }

                        if (filter.vendorId() != null) {
                                spec = spec.and((root, q, cb) -> cb.equal(root.get("vendor").get("vendorId"),
                                                filter.vendorId()));
                        }

                        if (filter.categoryId() != null) {
                                spec = spec.and((root, q, cb) -> cb.equal(root.get("category").get("categoryId"),
                                                filter.categoryId()));
                        }

                        if (filter.brand() != null && !filter.brand().isBlank()) {
                                String normalizedBrand = filter.brand().trim().toLowerCase(Locale.ROOT);
                                spec = spec.and((root, q, cb) -> cb.like(
                                                cb.lower(root.get("brand")),
                                                "%" + normalizedBrand + "%"));
                        }

                        if (filter.minPrice() != null) {
                                spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("price"),
                                                filter.minPrice()));
                        }

                        if (filter.maxPrice() != null) {
                                spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("price"),
                                                filter.maxPrice()));
                        }

                        if (filter.minRating() != null) {
                                spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("avgRating"),
                                                filter.minRating()));
                        }

                        if (filter.active() != null) {
                                spec = spec.and((root, q, cb) -> cb.equal(root.get("active"), filter.active()));
                        }
                }

                if (isTrainee) {
                        spec = spec.and((root, q, cb) -> cb.isTrue(root.get("active")));
                }

                return spec;
        }

        private List<String> getAllRoadmapProductNames(UUID roadmapId) {

                List<String> supplementNames = personalStageSupplementRepository
                                .findSupplementNamesByRoadmapId(roadmapId);

                List<String> equipmentNames = personalExerciseRepository.findEquipmentNamesByRoadmapId(roadmapId);

                return Stream.concat(
                                supplementNames.stream(),
                                equipmentNames.stream()).distinct().toList();
        }

        private Page<ProductDto> filterProducts(
                        ProductFilterRequest filter,
                        List<String> names,
                        Pageable pageable) {

                List<String> normalizedNames = normalizeNames(names);

                Specification<Product> spec = Specification.allOf(
                                buildBaseFilter(filter),
                                (root, query, cb) -> {
                                        if (normalizedNames.isEmpty()) {
                                                return cb.conjunction();
                                        }
                                        return cb.lower(root.get("name")).in(normalizedNames);
                                });

                return productRepository.findAll(spec, pageable)
                                .map(productMapper::toDto);
        }

        private List<UUID> findSupplementRefIdsByRoadmapId(UUID roadmapId) {
                return personalStageSupplementRepository.findSupplementIdsByRoadmapId(roadmapId);
        }

        private List<UUID> findEquipmentRefIdsByRoadmapId(UUID roadmapId) {
                return personalExerciseRepository.findEquipmentIdsByRoadmapId(roadmapId);
        }

        private List<String> normalizeNames(List<String> names) {
                if (names == null) {
                        return List.of();
                }

                return names.stream()
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(name -> !name.isEmpty())
                                .map(name -> name.toLowerCase(Locale.ROOT))
                                .distinct()
                                .toList();
        }

        private void validateVendorOwnershipForMutation(Product product) {
                if (securityUtil.getCurrentUserRole() != Role.VENDOR) {
                        return;
                }

                if (product.getVendor() == null || product.getVendor().getVendorId() == null) {
                        throw new InvalidRequestException("Product has no associated vendor");
                }

                UUID currentUserId = securityUtil.getCurrentUserId();
                if (!Objects.equals(product.getVendor().getVendorId(), currentUserId)) {
                        throw new InvalidRequestException("You can only modify your own products");
                }
        }

}
