package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.CategoryDto;
import fpt.edu.sep490.pilahub.dto.request.category.CreateCategoryRequest;
import fpt.edu.sep490.pilahub.dto.request.category.UpdateCategoryRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create system category", description = "Create a new system-owned category")
    @ApiResponse(responseCode = "201", description = "Category created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<CategoryDto>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryDto category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Category created successfully", category));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
    @ApiResponse(responseCode = "200", description = "Category retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<APIResponse<CategoryDto>> getCategoryById(@PathVariable("id") UUID categoryId) {
        CategoryDto category = categoryService.getById(categoryId);
        return ResponseEntity.ok(APIResponse.success("Category retrieved successfully", category));
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve all categories")
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    public ResponseEntity<APIResponse<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAll();
        return ResponseEntity.ok(APIResponse.success("Categories retrieved successfully", categories));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active categories", description = "Retrieve all active categories")
    @ApiResponse(responseCode = "200", description = "Active categories retrieved successfully")
    public ResponseEntity<APIResponse<List<CategoryDto>>> getAllActiveCategories() {
        List<CategoryDto> categories = categoryService.getAllActive();
        return ResponseEntity.ok(APIResponse.success("Active categories retrieved successfully", categories));
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories by name", description = "Search for categories by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<APIResponse<List<CategoryDto>>> searchCategories(@RequestParam("name") String name) {
        List<CategoryDto> categories = categoryService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed successfully", categories));
    }

    @GetMapping("/{id}/subcategories")
    @Operation(summary = "Get subcategories", description = "Retrieve all subcategories of a parent category")
    @ApiResponse(responseCode = "200", description = "Subcategories retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Parent category not found")
    public ResponseEntity<APIResponse<List<CategoryDto>>> getSubcategories(@PathVariable("id") UUID parentCategoryId) {
        List<CategoryDto> categories = categoryService.getSubcategories(parentCategoryId);
        return ResponseEntity.ok(APIResponse.success("Subcategories retrieved successfully", categories));
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories", description = "Retrieve all root categories (categories without a parent)")
    @ApiResponse(responseCode = "200", description = "Root categories retrieved successfully")
    public ResponseEntity<APIResponse<List<CategoryDto>>> getRootCategories() {
        List<CategoryDto> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(APIResponse.success("Root categories retrieved successfully", categories));
    }

    @GetMapping("/root/active")
    @Operation(summary = "Get active root categories", description = "Retrieve all active root categories")
    @ApiResponse(responseCode = "200", description = "Active root categories retrieved successfully")
    public ResponseEntity<APIResponse<List<CategoryDto>>> getActiveRootCategories() {
        List<CategoryDto> categories = categoryService.getActiveRootCategories();
        return ResponseEntity.ok(APIResponse.success("Active root categories retrieved successfully", categories));
    }

    @GetMapping("/global")
    @Operation(summary = "Get global categories", description = "Retrieve all global (system-owned) categories")
    @ApiResponse(responseCode = "200", description = "Global categories retrieved successfully")
    public ResponseEntity<APIResponse<List<CategoryDto>>> getGlobalCategories() {
        List<CategoryDto> categories = categoryService.getGlobalCategories();
        return ResponseEntity.ok(APIResponse.success("Global categories retrieved successfully", categories));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category", description = "Update an existing category")
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<CategoryDto>> updateCategory(
            @PathVariable("id") UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryDto category = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(APIResponse.success("Category updated successfully", category));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate category", description = "Activate a category")
    @ApiResponse(responseCode = "200", description = "Category activated successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> activateCategory(@PathVariable("id") UUID categoryId) {
        categoryService.activateCategory(categoryId);
        return ResponseEntity.ok(APIResponse.success("Category activated successfully", null));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate category", description = "Deactivate a category")
    @ApiResponse(responseCode = "200", description = "Category deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> deactivateCategory(@PathVariable("id") UUID categoryId) {
        categoryService.deactivateCategory(categoryId);
        return ResponseEntity.ok(APIResponse.success("Category deactivated successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category", description = "Permanently delete a category")
    @ApiResponse(responseCode = "200", description = "Category deleted successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> deleteCategory(@PathVariable("id") UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(APIResponse.success("Category deleted successfully", null));
    }
}
