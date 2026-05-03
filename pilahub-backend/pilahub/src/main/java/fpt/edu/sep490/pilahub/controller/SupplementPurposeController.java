package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.SupplementPurposeDto;
import fpt.edu.sep490.pilahub.dto.request.supplement.CreateSupplementPurposeRequest;
import fpt.edu.sep490.pilahub.dto.request.supplement.UpdateSupplementPurposeRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.SupplementPurposeService;
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
@RequestMapping("/api/supplement-purposes")
@RequiredArgsConstructor
@Tag(name = "Supplement Purpose", description = "Supplement-Purpose relationship management endpoints")
public class SupplementPurposeController {

    private final SupplementPurposeService supplementPurposeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Create supplement-purpose relationship", description = "Add a purpose to a supplement")
    @ApiResponse(responseCode = "201", description = "Relationship created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Supplement or purpose not found")
    public ResponseEntity<APIResponse<SupplementPurposeDto>> createSupplementPurpose(
            @Valid @RequestBody CreateSupplementPurposeRequest request) {
        SupplementPurposeDto supplementPurpose = supplementPurposeService.createSupplementPurpose(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Supplement-purpose relationship created successfully", supplementPurpose));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplement-purpose by ID", description = "Retrieve a specific supplement-purpose relationship by its ID")
    @ApiResponse(responseCode = "200", description = "Relationship found")
    @ApiResponse(responseCode = "404", description = "Relationship not found")
    public ResponseEntity<APIResponse<SupplementPurposeDto>> getSupplementPurposeById(@PathVariable UUID id) {
        SupplementPurposeDto supplementPurpose = supplementPurposeService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Supplement-purpose relationship retrieved successfully", supplementPurpose));
    }

    @GetMapping("/supplement/{supplementId}")
    @Operation(summary = "Get purposes by supplement", description = "Retrieve all purposes for a specific supplement")
    @ApiResponse(responseCode = "200", description = "Purposes retrieved successfully")
    public ResponseEntity<APIResponse<List<SupplementPurposeDto>>> getPurposesBySupplementId(
            @PathVariable UUID supplementId) {
        List<SupplementPurposeDto> purposes = supplementPurposeService.getBySupplementId(supplementId);
        return ResponseEntity.ok(APIResponse.success("Purposes retrieved successfully", purposes));
    }

    @GetMapping("/supplement/{supplementId}/primary")
    @Operation(summary = "Get primary purposes by supplement", description = "Retrieve primary purposes for a specific supplement")
    @ApiResponse(responseCode = "200", description = "Primary purposes retrieved successfully")
    public ResponseEntity<APIResponse<List<SupplementPurposeDto>>> getPrimaryPurposesBySupplementId(
            @PathVariable UUID supplementId) {
        List<SupplementPurposeDto> purposes = supplementPurposeService.getPrimaryPurposesBySupplementId(supplementId);
        return ResponseEntity.ok(APIResponse.success("Primary purposes retrieved successfully", purposes));
    }

    @GetMapping("/purpose/{purposeId}")
    @Operation(summary = "Get supplements by purpose", description = "Retrieve all supplements for a specific purpose")
    @ApiResponse(responseCode = "200", description = "Supplements retrieved successfully")
    public ResponseEntity<APIResponse<List<SupplementPurposeDto>>> getSupplementsByPurposeId(
            @PathVariable UUID purposeId) {
        List<SupplementPurposeDto> supplements = supplementPurposeService.getByPurposeId(purposeId);
        return ResponseEntity.ok(APIResponse.success("Supplements retrieved successfully", supplements));
    }

    @GetMapping("/check")
    @Operation(summary = "Check if relationship exists", description = "Check if a supplement has a specific purpose")
    @ApiResponse(responseCode = "200", description = "Check completed")
    public ResponseEntity<APIResponse<Boolean>> checkSupplementPurposeExists(
            @RequestParam UUID supplementId,
            @RequestParam UUID purposeId) {
        boolean exists = supplementPurposeService.existsBySupplementAndPurpose(supplementId, purposeId);
        return ResponseEntity.ok(APIResponse.success("Check completed", exists));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Update supplement-purpose", description = "Update an existing supplement-purpose relationship")
    @ApiResponse(responseCode = "200", description = "Relationship updated successfully")
    @ApiResponse(responseCode = "404", description = "Relationship not found")
    public ResponseEntity<APIResponse<SupplementPurposeDto>> updateSupplementPurpose(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplementPurposeRequest request) {
        SupplementPurposeDto supplementPurpose = supplementPurposeService.updateSupplementPurpose(id, request);
        return ResponseEntity.ok(APIResponse.success("Supplement-purpose relationship updated successfully", supplementPurpose));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Delete supplement-purpose", description = "Remove a purpose from a supplement")
    @ApiResponse(responseCode = "200", description = "Relationship deleted successfully")
    @ApiResponse(responseCode = "404", description = "Relationship not found")
    public ResponseEntity<APIResponse<Void>> deleteSupplementPurpose(@PathVariable UUID id) {
        supplementPurposeService.deleteSupplementPurpose(id);
        return ResponseEntity.ok(APIResponse.success("Supplement-purpose relationship deleted successfully", null));
    }

    @DeleteMapping("/supplement/{supplementId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete all purposes of a supplement", description = "Remove all purposes from a supplement")
    @ApiResponse(responseCode = "200", description = "All purposes deleted successfully")
    public ResponseEntity<APIResponse<Void>> deletePurposesBySupplementId(@PathVariable UUID supplementId) {
        supplementPurposeService.deleteBySupplementId(supplementId);
        return ResponseEntity.ok(APIResponse.success("All purposes deleted successfully", null));
    }
}
