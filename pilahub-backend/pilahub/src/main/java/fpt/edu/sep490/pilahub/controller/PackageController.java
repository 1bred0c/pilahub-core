package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.PackageDto;
import fpt.edu.sep490.pilahub.dto.request.CreatePackageRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdatePackageRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@Tag(name = "Package Management", description = "APIs for managing subscription packages")
public class PackageController {

    private final PackageService packageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create new package (Admin only)",
            description = "Create a new subscription package. Admin access required."
    )
    @ApiResponse(responseCode = "201", description = "Package created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or duplicate package name")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<PackageDto>> createPackage(
            @Valid @RequestBody CreatePackageRequest request) {
        PackageDto packageDto = packageService.createPackage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Package created successfully", packageDto));
    }

    @GetMapping
    @Operation(
            summary = "Get all packages with pagination (Admin only)",
            description = "Retrieve a paginated list of all packages. Admin access required."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "200", description = "Packages retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<Page<PackageDto>>> getAllPackages(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PackageDto> packages = packageService.getAllPackages(pageable);

        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d package(s) from page %d of %d",
                        packages.getNumberOfElements(),
                        packages.getNumber() + 1,
                        packages.getTotalPages()),
                packages
        ));
    }

    @GetMapping("/active")
    @Operation(
            summary = "Get all active packages",
            description = "Retrieve a list of all active packages. Available for all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Active packages retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    public ResponseEntity<APIResponse<List<PackageDto>>> getActivePackages() {
        List<PackageDto> activePackages = packageService.getActivePackages();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d active package(s)", activePackages.size()),
                activePackages
        ));
    }

    @GetMapping("/{packageId}")
    @Operation(
            summary = "Get package by ID",
            description = "Retrieve package details by package ID. Available for all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Package found")
    @ApiResponse(responseCode = "404", description = "Package not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    public ResponseEntity<APIResponse<PackageDto>> getPackageById(
            @PathVariable UUID packageId) {
        PackageDto packageDto = packageService.getPackageById(packageId);
        return ResponseEntity.ok(APIResponse.success("Package retrieved successfully", packageDto));
    }

    @PutMapping("/{packageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update package by ID (Admin only)",
            description = "Update package details by package ID. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Package updated successfully")
    @ApiResponse(responseCode = "404", description = "Package not found")
    @ApiResponse(responseCode = "400", description = "Invalid input or duplicate package name")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<PackageDto>> updatePackage(
            @PathVariable UUID packageId,
            @Valid @RequestBody UpdatePackageRequest request) {
        PackageDto packageDto = packageService.updatePackage(packageId, request);
        return ResponseEntity.ok(APIResponse.success("Package updated successfully", packageDto));
    }

    @DeleteMapping("/{packageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Deactivate package by ID (Admin only)",
            description = "Deactivate package by setting active status to false. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Package deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Package not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<Void>> deletePackage(
            @PathVariable UUID packageId) {
        packageService.deletePackage(packageId);
        return ResponseEntity.ok(APIResponse.success("Package deactivated successfully", null));
    }

    @PatchMapping("/{packageId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Activate package by ID (Admin only)",
            description = "Activate package by setting active status to true. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Package activated successfully")
    @ApiResponse(responseCode = "404", description = "Package not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<PackageDto>> activatePackage(
            @PathVariable UUID packageId) {
        PackageDto packageDto = packageService.activatePackage(packageId);
        return ResponseEntity.ok(APIResponse.success("Package activated successfully", packageDto));
    }
}
