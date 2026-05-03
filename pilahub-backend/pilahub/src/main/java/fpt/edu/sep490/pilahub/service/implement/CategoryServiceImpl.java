package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.CategoryDto;
import fpt.edu.sep490.pilahub.dto.request.category.CreateCategoryRequest;
import fpt.edu.sep490.pilahub.dto.request.category.UpdateCategoryRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.CategoryMapper;
import fpt.edu.sep490.pilahub.pojo.Category;
import fpt.edu.sep490.pilahub.repository.CategoryRepository;
import fpt.edu.sep490.pilahub.service.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto createCategory(CreateCategoryRequest request) {
        log.info("Creating system category with name: {}", request.name());

        Category category = categoryMapper.toEntity(request);

        if (request.parentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(request.parentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.parentCategoryId()));
            category.setParentCategory(parentCategory);
        }

        Category saved = categoryRepository.save(category);
        log.info("Successfully created system category with ID: {}", saved.getCategoryId());

        return categoryMapper.toDto(saved);
    }

    @Override
    public CategoryDto getById(UUID categoryId) {
        log.info("Fetching category by ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        return categoryMapper.toDto(category);
    }

    @Override
    public List<CategoryDto> getAll() {
        log.info("Fetching all categories");

        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getAllActive() {
        log.info("Fetching all active categories");

        return categoryRepository.findByActiveTrue().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> searchByName(String name) {
        log.info("Searching categories by name: {}", name);

        return categoryRepository.findByNameContainingIgnoreCase(name).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getSubcategories(UUID parentCategoryId) {
        log.info("Fetching subcategories for parent category ID: {}", parentCategoryId);

        if (!categoryRepository.existsById(parentCategoryId)) {
            throw new ResourceNotFoundException("Category", "id", parentCategoryId);
        }

        return categoryRepository.findByParentCategory_CategoryId(parentCategoryId).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getRootCategories() {
        log.info("Fetching all root categories");

        return categoryRepository.findByParentCategoryIsNull().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getActiveRootCategories() {
        log.info("Fetching all active root categories");

        return categoryRepository.findByActiveTrueAndParentCategoryIsNull().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getGlobalCategories() {
        log.info("Fetching all global categories");

        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto updateCategory(UUID categoryId, UpdateCategoryRequest request) {
        log.info("Updating category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        if (request.parentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(request.parentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.parentCategoryId()));
            validateNoCycle(parentCategory, category);
            category.setParentCategory(parentCategory);
        }

        categoryMapper.updateEntityFromRequest(request, category);

        Category updated = categoryRepository.save(category);
        log.info("Successfully updated category with ID: {}", categoryId);

        return categoryMapper.toDto(updated);
    }

    @Override
    public void activateCategory(UUID categoryId) {
        log.info("Activating category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        category.setActive(true);
        categoryRepository.save(category);

        log.info("Successfully activated category with ID: {}", categoryId);
    }

    @Override
    public void deactivateCategory(UUID categoryId) {
        log.info("Deactivating category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        category.setActive(false);
        categoryRepository.save(category);

        log.info("Successfully deactivated category with ID: {}", categoryId);
    }

    @Override
    public void deleteCategory(UUID categoryId) {
        log.info("Deleting category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        categoryRepository.delete(category);

        log.info("Successfully deleted category with ID: {}", categoryId);
    }

    private void validateNoCycle(Category parent, Category child) {
        while (parent != null) {
            if (parent.getCategoryId().equals(child.getCategoryId())) {
                throw new RuntimeException("Cycle detected");
            }
            parent = parent.getParentCategory();
        }
    }
}
