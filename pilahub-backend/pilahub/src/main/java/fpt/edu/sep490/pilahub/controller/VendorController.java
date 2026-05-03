package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.VendorDto;
import fpt.edu.sep490.pilahub.dto.request.vendor.CreateVendorRequest;
import fpt.edu.sep490.pilahub.dto.request.vendor.UpdateVendorAdminRequest;
import fpt.edu.sep490.pilahub.dto.request.vendor.UpdateVendorRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.VendorService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
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
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendor", description = "Manage vendor profiles")
public class VendorController {

    private final VendorService vendorService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @Operation(summary = "Create vendor profile", description = "Create a new vendor profile for the current account")
    @ApiResponse(responseCode = "201", description = "Vendor profile created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<VendorDto>> createVendor(@Valid @RequestBody CreateVendorRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();
        VendorDto vendor = vendorService.createVendor(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Vendor profile created successfully", vendor));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vendor by ID", description = "Retrieve a vendor profile by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @ApiResponse(responseCode = "404", description = "Vendor not found")
    public ResponseEntity<APIResponse<VendorDto>> getVendorById(@PathVariable("id") UUID vendorId) {
        VendorDto vendor = vendorService.getById(vendorId);
        return ResponseEntity.ok(APIResponse.success("Vendor retrieved successfully", vendor));
    }

    @GetMapping
    @Operation(summary = "Get all vendors", description = "Retrieve all vendor profiles")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<VendorDto>>> getAllVendors() {
        List<VendorDto> vendors = vendorService.getAll();
        return ResponseEntity.ok(APIResponse.success("Vendors retrieved successfully", vendors));
    }

    @GetMapping("/verified")
    @Operation(summary = "Get all verified vendors", description = "Retrieve all verified vendor profiles")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<VendorDto>>> getAllVerifiedVendors() {
        List<VendorDto> vendors = vendorService.getAllVerified();
        return ResponseEntity.ok(APIResponse.success("Verified vendors retrieved successfully", vendors));
    }

    @GetMapping("/search")
    @Operation(summary = "Search vendors by business name", description = "Search for vendors by business name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<VendorDto>>> searchVendors(@RequestParam("q") String query) {
        List<VendorDto> vendors = vendorService.searchByBusinessName(query);
        return ResponseEntity.ok(APIResponse.success("Search completed successfully", vendors));
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Get vendors by city", description = "Retrieve vendors filtered by city")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<VendorDto>>> getVendorsByCity(@PathVariable String city) {
        List<VendorDto> vendors = vendorService.getByCity(city);
        return ResponseEntity.ok(APIResponse.success("Vendors retrieved successfully", vendors));
    }

    @GetMapping("/country/{country}")
    @Operation(summary = "Get vendors by country", description = "Retrieve vendors filtered by country")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<VendorDto>>> getVendorsByCountry(@PathVariable String country) {
        List<VendorDto> vendors = vendorService.getByCountry(country);
        return ResponseEntity.ok(APIResponse.success("Vendors retrieved successfully", vendors));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Update vendor profile", description = "Update vendor profile information")
    @ApiResponse(responseCode = "200", description = "Vendor profile updated successfully")
    @ApiResponse(responseCode = "404", description = "Vendor not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<VendorDto>> updateVendor(
            @PathVariable("id") UUID vendorId,
            @Valid @RequestBody UpdateVendorRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();

        // Verify that the vendor belongs to the current user
        if (!vendorId.equals(accountId)) {
            throw new IllegalStateException("You can only update your own profile");
        }

        VendorDto vendor = vendorService.updateVendor(vendorId, request);
        return ResponseEntity.ok(APIResponse.success("Vendor profile updated successfully", vendor));
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify vendor", description = "Verify a vendor profile")
    @ApiResponse(responseCode = "200", description = "Vendor verified successfully")
    @ApiResponse(responseCode = "404", description = "Vendor not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> verifyVendor(@PathVariable("id") UUID vendorId) {
        vendorService.verifyVendor(vendorId);
        return ResponseEntity.ok(APIResponse.success("Vendor verified successfully", null));
    }

    @PatchMapping("/{id}/unverify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unverify vendor", description = "Unverify a vendor profile")
    @ApiResponse(responseCode = "200", description = "Vendor unverified successfully")
    @ApiResponse(responseCode = "404", description = "Vendor not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> unverifyVendor(@PathVariable("id") UUID vendorId) {
        vendorService.unverifyVendor(vendorId);
        return ResponseEntity.ok(APIResponse.success("Vendor unverified successfully", null));
    }

    @PatchMapping("/{id}/platform-settings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update vendor platform settings", description = "Update platform fee percentage and holding days (Admin only)")
    @ApiResponse(responseCode = "200", description = "Platform settings updated successfully")
    @ApiResponse(responseCode = "404", description = "Vendor not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<VendorDto>> updateVendorPlatformSettings(
            @PathVariable("id") UUID vendorId,
            @Valid @RequestBody UpdateVendorAdminRequest request) {
        VendorDto vendor = vendorService.updateVendorPlatformSettings(vendorId, request);
        return ResponseEntity.ok(APIResponse.success("Vendor platform settings updated successfully", vendor));
    }
}
