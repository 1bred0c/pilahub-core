package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.PurposeDto;
import fpt.edu.sep490.pilahub.dto.request.purpose.CreatePurposeRequest;
import fpt.edu.sep490.pilahub.dto.request.purpose.UpdatePurposeRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.PurposeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/purposes")
@RequiredArgsConstructor
@Tag(name = "Purpose", description = "Purpose management endpoints")
public class PurposeController {
    private final PurposeService purposeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create purpose", description = "Create a new purpose")
    @ApiResponse(responseCode = "201", description = "Purpose created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<PurposeDto>> createPurpose(@Valid @RequestBody CreatePurposeRequest request) {
        PurposeDto purpose = purposeService.createPurpose(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Purpose created successfully", purpose));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purpose by ID", description = "Retrieve a specific purpose by its ID")
    @ApiResponse(responseCode = "200", description = "Purpose retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Purpose not found")
    public ResponseEntity<APIResponse<PurposeDto>> getPurposeById(@PathVariable("id") UUID purposeId) {
        PurposeDto purpose = purposeService.getById(purposeId);
        return ResponseEntity.ok(APIResponse.success("Purpose retrieved successfully", purpose));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get purpose by code", description = "Retrieve a specific purpose by its code")
    @ApiResponse(responseCode = "200", description = "Purpose retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Purpose not found")
    public ResponseEntity<APIResponse<PurposeDto>> getPurposeByCode(@PathVariable String code) {
        PurposeDto purpose = purposeService.getByCode(code);
        return ResponseEntity.ok(APIResponse.success("Purpose retrieved successfully", purpose));
    }

    @GetMapping
    @Operation(summary = "Get all purposes", description = "Retrieve all purposes")
    @ApiResponse(responseCode = "200", description = "Purposes retrieved successfully")
    public ResponseEntity<APIResponse<List<PurposeDto>>> getAllPurposes() {
        List<PurposeDto> purposes = purposeService.getAll();
        return ResponseEntity.ok(APIResponse.success("Purposes retrieved successfully", purposes));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active purposes", description = "Retrieve all active purposes")
    @ApiResponse(responseCode = "200", description = "Active purposes retrieved successfully")
    public ResponseEntity<APIResponse<List<PurposeDto>>> getAllActivePurposes() {
        List<PurposeDto> purposes = purposeService.getAllActive();
        return ResponseEntity.ok(APIResponse.success("Active purposes retrieved successfully", purposes));
    }

    @GetMapping("/search")
    @Operation(summary = "Search purposes by name", description = "Search for purposes by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<APIResponse<List<PurposeDto>>> searchPurposes(@RequestParam("name") String name) {
        List<PurposeDto> purposes = purposeService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed successfully", purposes));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update purpose", description = "Update an existing purpose")
    @ApiResponse(responseCode = "200", description = "Purpose updated successfully")
    @ApiResponse(responseCode = "404", description = "Purpose not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<PurposeDto>> updatePurpose(
            @PathVariable("id") UUID purposeId,
            @Valid @RequestBody UpdatePurposeRequest request) {
        PurposeDto purpose = purposeService.updatePurpose(purposeId, request);
        return ResponseEntity.ok(APIResponse.success("Purpose updated successfully", purpose));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate purpose", description = "Deactivate a purpose")
    @ApiResponse(responseCode = "200", description = "Purpose deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Purpose not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> deactivatePurpose(@PathVariable("id") UUID purposeId) {
        purposeService.deactivatePurpose(purposeId);
        return ResponseEntity.ok(APIResponse.success("Purpose deactivated successfully", null));
    }
}