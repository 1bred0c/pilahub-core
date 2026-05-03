package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.SupplementDto;
import fpt.edu.sep490.pilahub.dto.request.supplement.CreateSupplementRequest;
import fpt.edu.sep490.pilahub.dto.request.supplement.UpdateSupplementRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.SupplementService;
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
@RequestMapping("/api/supplements")
@RequiredArgsConstructor
@Tag(name = "Supplement", description = "Supplement management endpoints")
public class SupplementController {

    private final SupplementService supplementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Create supplement", description = "Create a new supplement")
    @ApiResponse(responseCode = "201", description = "Supplement created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<SupplementDto>> createSupplement(@Valid @RequestBody CreateSupplementRequest request) {
        SupplementDto supplement = supplementService.createSupplement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Supplement created successfully", supplement));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplement by ID", description = "Retrieve a specific supplement by its ID")
    @ApiResponse(responseCode = "200", description = "Supplement found")
    @ApiResponse(responseCode = "404", description = "Supplement not found")
    public ResponseEntity<APIResponse<SupplementDto>> getSupplementById(@PathVariable UUID id) {
        SupplementDto supplement = supplementService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Supplement retrieved successfully", supplement));
    }

    @GetMapping
    @Operation(summary = "Get all supplements", description = "Retrieve all supplements (active and inactive)")
    @ApiResponse(responseCode = "200", description = "Supplements retrieved successfully")
    public ResponseEntity<APIResponse<List<SupplementDto>>> getAllSupplements() {
        List<SupplementDto> supplements = supplementService.getAll();
        return ResponseEntity.ok(APIResponse.success("Supplements retrieved successfully", supplements));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active supplements", description = "Retrieve all active supplements")
    @ApiResponse(responseCode = "200", description = "Active supplements retrieved successfully")
    public ResponseEntity<APIResponse<List<SupplementDto>>> getAllActiveSupplements() {
        List<SupplementDto> supplements = supplementService.getAllActive();
        return ResponseEntity.ok(APIResponse.success("Active supplements retrieved successfully", supplements));
    }

    @GetMapping("/search")
    @Operation(summary = "Search supplements by name", description = "Search for supplements by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed")
    public ResponseEntity<APIResponse<List<SupplementDto>>> searchSupplements(@RequestParam String name) {
        List<SupplementDto> supplements = supplementService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed", supplements));
    }

    @GetMapping("/brand/{brand}")
    @Operation(summary = "Get supplements by brand", description = "Retrieve supplements filtered by brand")
    @ApiResponse(responseCode = "200", description = "Supplements retrieved successfully")
    public ResponseEntity<APIResponse<List<SupplementDto>>> getSupplementsByBrand(@PathVariable String brand) {
        List<SupplementDto> supplements = supplementService.getByBrand(brand);
        return ResponseEntity.ok(APIResponse.success("Supplements retrieved successfully", supplements));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Update supplement", description = "Update an existing supplement")
    @ApiResponse(responseCode = "200", description = "Supplement updated successfully")
    @ApiResponse(responseCode = "404", description = "Supplement not found")
    public ResponseEntity<APIResponse<SupplementDto>> updateSupplement(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplementRequest request) {
        SupplementDto supplement = supplementService.updateSupplement(id, request);
        return ResponseEntity.ok(APIResponse.success("Supplement updated successfully", supplement));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Deactivate supplement", description = "Deactivate a supplement (soft delete)")
    @ApiResponse(responseCode = "200", description = "Supplement deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Supplement not found")
    public ResponseEntity<APIResponse<Void>> deactivateSupplement(@PathVariable UUID id) {
        supplementService.deactivateSupplement(id);
        return ResponseEntity.ok(APIResponse.success("Supplement deactivated successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete supplement", description = "Permanently delete a supplement")
    @ApiResponse(responseCode = "200", description = "Supplement deleted successfully")
    @ApiResponse(responseCode = "404", description = "Supplement not found")
    public ResponseEntity<APIResponse<Void>> deleteSupplement(@PathVariable UUID id) {
        supplementService.deleteSupplement(id);
        return ResponseEntity.ok(APIResponse.success("Supplement deleted successfully", null));
    }
}
