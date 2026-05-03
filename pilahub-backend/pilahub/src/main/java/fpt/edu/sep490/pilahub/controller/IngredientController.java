package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.IngredientDto;
import fpt.edu.sep490.pilahub.dto.IngredientWithRulesDto;
import fpt.edu.sep490.pilahub.dto.request.ingredient.CreateIngredientRequest;
import fpt.edu.sep490.pilahub.dto.request.ingredient.CreateIngredientRuleRequest;
import fpt.edu.sep490.pilahub.dto.request.ingredient.UpdateIngredientRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.IngredientService;
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
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@Tag(name = "Ingredient", description = "Ingredient management endpoints")
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Create ingredient", description = "Create a new ingredient")
    @ApiResponse(responseCode = "201", description = "Ingredient created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<IngredientWithRulesDto>> createIngredient(
            @Valid @RequestBody CreateIngredientRequest request) {
        IngredientWithRulesDto ingredient = ingredientService.createIngredient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Ingredient created successfully", ingredient));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ingredient by ID", description = "Retrieve a specific ingredient by its ID")
    @ApiResponse(responseCode = "200", description = "Ingredient found")
    @ApiResponse(responseCode = "404", description = "Ingredient not found")
    public ResponseEntity<APIResponse<IngredientWithRulesDto>> getIngredientById(@PathVariable UUID id) {
        IngredientWithRulesDto ingredient = ingredientService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Ingredient retrieved successfully", ingredient));
    }

    @GetMapping
    @Operation(summary = "Get all ingredients", description = "Retrieve all ingredients (active and inactive)")
    @ApiResponse(responseCode = "200", description = "Ingredients retrieved successfully")
    public ResponseEntity<APIResponse<List<IngredientDto>>> getAllIngredients() {
        List<IngredientDto> ingredients = ingredientService.getAll();
        return ResponseEntity.ok(APIResponse.success("Ingredients retrieved successfully", ingredients));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active ingredients", description = "Retrieve all active ingredients")
    @ApiResponse(responseCode = "200", description = "Active ingredients retrieved successfully")
    public ResponseEntity<APIResponse<List<IngredientDto>>> getAllActiveIngredients() {
        List<IngredientDto> ingredients = ingredientService.getAllActive();
        return ResponseEntity.ok(APIResponse.success("Active ingredients retrieved successfully", ingredients));
    }

    @GetMapping("/search")
    @Operation(summary = "Search ingredients by name", description = "Search for ingredients by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed")
    public ResponseEntity<APIResponse<List<IngredientDto>>> searchIngredients(@RequestParam String name) {
        List<IngredientDto> ingredients = ingredientService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed", ingredients));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Patch ingredient", description = "Partially update an existing ingredient and its rules")
    @ApiResponse(responseCode = "200", description = "Ingredient updated successfully")
    @ApiResponse(responseCode = "404", description = "Ingredient not found")
    public ResponseEntity<APIResponse<IngredientWithRulesDto>> updateIngredient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateIngredientRequest request) {

        IngredientWithRulesDto ingredient = ingredientService.updateIngredient(id, request);
        return ResponseEntity.ok(APIResponse.success("Ingredient updated successfully", ingredient));
    }

    @PostMapping("/{id}/rule/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Add ingredient rule", description = "Add a new rule to an existing ingredient")
    @ApiResponse(responseCode = "200", description = "Ingredient rule added successfully")
    @ApiResponse(responseCode = "404", description = "Ingredient not found")
    public ResponseEntity<APIResponse<IngredientWithRulesDto>> addIngredientRule(
            @PathVariable UUID id,
            @Valid @RequestBody CreateIngredientRuleRequest request) {

        IngredientWithRulesDto ingredient = ingredientService.addIngredientRule(id, request);
        return ResponseEntity.ok(APIResponse.success("Ingredient rule added successfully", ingredient));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Deactivate ingredient", description = "Deactivate an ingredient (soft delete)")
    @ApiResponse(responseCode = "200", description = "Ingredient deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Ingredient not found")
    public ResponseEntity<APIResponse<Void>> deactivateIngredient(@PathVariable UUID id) {
        ingredientService.deactivateIngredient(id);
        return ResponseEntity.ok(APIResponse.success("Ingredient deactivated successfully", null));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Activate ingredient", description = "Activate an ingredient")
    @ApiResponse(responseCode = "200", description = "Ingredient activated successfully")
    @ApiResponse(responseCode = "404", description = "Ingredient not found")
    public ResponseEntity<APIResponse<Void>> activateIngredient(@PathVariable UUID id) {
        ingredientService.activateIngredient(id);
        return ResponseEntity.ok(APIResponse.success("Ingredient activated successfully", null));
    }
}
