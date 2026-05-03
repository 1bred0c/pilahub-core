package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.CategoryDto;
import fpt.edu.sep490.pilahub.dto.request.category.CreateCategoryRequest;
import fpt.edu.sep490.pilahub.dto.request.category.UpdateCategoryRequest;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryDto createCategory(CreateCategoryRequest request);

    CategoryDto getById(UUID categoryId);

    List<CategoryDto> getAll();

    List<CategoryDto> getAllActive();

    List<CategoryDto> searchByName(String name);

    List<CategoryDto> getSubcategories(UUID parentCategoryId);

    List<CategoryDto> getRootCategories();

    List<CategoryDto> getActiveRootCategories();

    List<CategoryDto> getGlobalCategories();

    CategoryDto updateCategory(UUID categoryId, UpdateCategoryRequest request);

    void activateCategory(UUID categoryId);

    void deactivateCategory(UUID categoryId);

    void deleteCategory(UUID categoryId);
}
