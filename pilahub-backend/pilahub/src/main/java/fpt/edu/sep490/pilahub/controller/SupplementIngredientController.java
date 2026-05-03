package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.SupplementIngredientDto;
import fpt.edu.sep490.pilahub.dto.request.supplement.CreateSupplementIngredientRequest;
import fpt.edu.sep490.pilahub.dto.request.supplement.UpdateSupplementIngredientRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.SupplementIngredientService;
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
@RequestMapping("/api/supplement-ingredients")
@RequiredArgsConstructor
@Tag(name = "Supplement Ingredient", description = "Supplement-Ingredient relationship management endpoints")
public class SupplementIngredientController {

    private final SupplementIngredientService supplementIngredientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Create supplement-ingredient relationship", description = "Add an ingredient to a supplement")
    @ApiResponse(responseCode = "201", description = "Relationship created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Supplement or ingredient not found")
    public ResponseEntity<APIResponse<SupplementIngredientDto>> createSupplementIngredient(
            @Valid @RequestBody CreateSupplementIngredientRequest request) {
        SupplementIngredientDto supplementIngredient = supplementIngredientService.createSupplementIngredient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Supplement-ingredient relationship created successfully", supplementIngredient));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplement-ingredient by ID", description = "Retrieve a specific supplement-ingredient relationship by its ID")
    @ApiResponse(responseCode = "200", description = "Relationship found")
    @ApiResponse(responseCode = "404", description = "Relationship not found")
    public ResponseEntity<APIResponse<SupplementIngredientDto>> getSupplementIngredientById(@PathVariable UUID id) {
        SupplementIngredientDto supplementIngredient = supplementIngredientService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Supplement-ingredient relationship retrieved successfully", supplementIngredient));
    }

    @GetMapping("/supplement/{supplementId}")
    @Operation(summary = "Get ingredients by supplement", description = "Retrieve all ingredients for a specific supplement")
    @ApiResponse(responseCode = "200", description = "Ingredients retrieved successfully")
    public ResponseEntity<APIResponse<List<SupplementIngredientDto>>> getIngredientsBySupplementId(
            @PathVariable UUID supplementId) {
        List<SupplementIngredientDto> ingredients = supplementIngredientService.getBySupplementId(supplementId);
        return ResponseEntity.ok(APIResponse.success("Ingredients retrieved successfully", ingredients));
    }

    @GetMapping("/ingredient/{ingredientId}")
    @Operation(summary = "Get supplements by ingredient", description = "Retrieve all supplements containing a specific ingredient")
    @ApiResponse(responseCode = "200", description = "Supplements retrieved successfully")
    public ResponseEntity<APIResponse<List<SupplementIngredientDto>>> getSupplementsByIngredientId(
            @PathVariable UUID ingredientId) {
        List<SupplementIngredientDto> supplements = supplementIngredientService.getByIngredientId(ingredientId);
        return ResponseEntity.ok(APIResponse.success("Supplements retrieved successfully", supplements));
    }

    @GetMapping("/check")
    @Operation(summary = "Check if relationship exists", description = "Check if a supplement contains a specific ingredient")
    @ApiResponse(responseCode = "200", description = "Check completed")
    public ResponseEntity<APIResponse<Boolean>> checkSupplementIngredientExists(
            @RequestParam UUID supplementId,
            @RequestParam UUID ingredientId) {
        boolean exists = supplementIngredientService.existsBySupplementAndIngredient(supplementId, ingredientId);
        return ResponseEntity.ok(APIResponse.success("Check completed", exists));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Update supplement-ingredient", description = "Update an existing supplement-ingredient relationship")
    @ApiResponse(responseCode = "200", description = "Relationship updated successfully")
    @ApiResponse(responseCode = "404", description = "Relationship not found")
    public ResponseEntity<APIResponse<SupplementIngredientDto>> updateSupplementIngredient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplementIngredientRequest request) {
        SupplementIngredientDto supplementIngredient = supplementIngredientService.updateSupplementIngredient(id, request);
        return ResponseEntity.ok(APIResponse.success("Supplement-ingredient relationship updated successfully", supplementIngredient));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Delete supplement-ingredient", description = "Remove an ingredient from a supplement")
    @ApiResponse(responseCode = "200", description = "Relationship deleted successfully")
    @ApiResponse(responseCode = "404", description = "Relationship not found")
    public ResponseEntity<APIResponse<Void>> deleteSupplementIngredient(@PathVariable UUID id) {
        supplementIngredientService.deleteSupplementIngredient(id);
        return ResponseEntity.ok(APIResponse.success("Supplement-ingredient relationship deleted successfully", null));
    }

    @DeleteMapping("/supplement/{supplementId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete all ingredients of a supplement", description = "Remove all ingredients from a supplement")
    @ApiResponse(responseCode = "200", description = "All ingredients deleted successfully")
    public ResponseEntity<APIResponse<Void>> deleteIngredientsBySupplementId(@PathVariable UUID supplementId) {
        supplementIngredientService.deleteBySupplementId(supplementId);
        return ResponseEntity.ok(APIResponse.success("All ingredients deleted successfully", null));
    }
}
