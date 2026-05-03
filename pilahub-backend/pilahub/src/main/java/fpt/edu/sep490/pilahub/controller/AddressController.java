package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.AddressDto;
import fpt.edu.sep490.pilahub.dto.request.CreateAddressRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateAddressRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.AddressService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Address Management", description = "APIs for managing trainee addresses")
public class AddressController {

    private final AddressService addressService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Create new address (Trainee only)",
            description = "Create a new address for the authenticated trainee. Uses account ID from JWT token."
    )
    @ApiResponse(responseCode = "201", description = "Address created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<APIResponse<AddressDto>> createAddress(
            @Valid @RequestBody CreateAddressRequest request) {
        UUID traineeId = securityUtil.getCurrentUserId();
        AddressDto addressDto = addressService.createAddress(traineeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Address created successfully", addressDto));
    }

    @GetMapping
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get all addresses (Trainee only)",
            description = "Retrieve all addresses for the authenticated trainee. Uses account ID from JWT token."
    )
    @ApiResponse(responseCode = "200", description = "Addresses retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<APIResponse<List<AddressDto>>> getAllAddresses() {
        UUID traineeId = securityUtil.getCurrentUserId();
        List<AddressDto> addresses = addressService.getAddressesByTraineeId(traineeId);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d address(es) successfully", addresses.size()),
                addresses
        ));
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get address by ID (Trainee only)",
            description = "Retrieve a specific address by ID for the authenticated trainee. Uses account ID from JWT token."
    )
    @ApiResponse(responseCode = "200", description = "Address retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Address not found")
    public ResponseEntity<APIResponse<AddressDto>> getAddressById(
            @PathVariable UUID addressId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        AddressDto addressDto = addressService.getAddressById(traineeId, addressId);
        return ResponseEntity.ok(APIResponse.success("Address retrieved successfully", addressDto));
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Update address (Trainee only)",
            description = "Update a specific address for the authenticated trainee. Uses account ID from JWT token."
    )
    @ApiResponse(responseCode = "200", description = "Address updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Address not found")
    public ResponseEntity<APIResponse<AddressDto>> updateAddress(
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        UUID traineeId = securityUtil.getCurrentUserId();
        AddressDto addressDto = addressService.updateAddress(traineeId, addressId, request);
        return ResponseEntity.ok(APIResponse.success("Address updated successfully", addressDto));
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Delete address (Trainee only)",
            description = "Delete a specific address for the authenticated trainee. Uses account ID from JWT token."
    )
    @ApiResponse(responseCode = "200", description = "Address deleted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Address not found")
    public ResponseEntity<APIResponse<Void>> deleteAddress(
            @PathVariable UUID addressId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        addressService.deleteAddress(traineeId, addressId);
        return ResponseEntity.ok(APIResponse.success("Address deleted successfully", null));
    }
}
