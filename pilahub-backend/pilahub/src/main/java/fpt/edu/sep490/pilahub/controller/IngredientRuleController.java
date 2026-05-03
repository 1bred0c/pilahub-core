package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.IngredientRuleDto;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.IngredientRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ingredient-rules")
@RequiredArgsConstructor
@Tag(name = "Ingredient Rule", description = "Ingredient rule management endpoints")
public class IngredientRuleController {

    private final IngredientRuleService ingredientRuleService;

    @GetMapping("/ingredient/{ingredientId}")
    @Operation(summary = "Get rules by ingredient", description = "Retrieve all rules for a specific ingredient")
    @ApiResponse(responseCode = "200", description = "Rules retrieved successfully")
    public ResponseEntity<APIResponse<List<IngredientRuleDto>>> getRulesByIngredientId(
            @PathVariable UUID ingredientId) {
        List<IngredientRuleDto> rules = ingredientRuleService.getByIngredientId(ingredientId);
        return ResponseEntity.ok(APIResponse.success("Rules retrieved successfully", rules));
    }

    @GetMapping("/type/{ruleType}")
    @Operation(summary = "Get rules by type", description = "Retrieve all rules of a specific type (CONDITION, AGE, GENDER, etc.)")
    @ApiResponse(responseCode = "200", description = "Rules retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid rule type")
    public ResponseEntity<APIResponse<List<IngredientRuleDto>>> getRulesByType(
            @PathVariable String ruleType) {
        List<IngredientRuleDto> rules = ingredientRuleService.getByRuleType(ruleType);
        return ResponseEntity.ok(APIResponse.success("Rules retrieved successfully", rules));
    }
}
