package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.ShipmentDto;
import fpt.edu.sep490.pilahub.dto.request.shipment.CreateShipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.shipment.UpdateShipmentFeeRequest;
import fpt.edu.sep490.pilahub.dto.request.shipment.UpdateShipmentTrackingRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.enums.ShipmentStatus;
import fpt.edu.sep490.pilahub.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Tag(name = "Shipment", description = "Shipment management endpoints (multi-vendor order fulfillment)")
public class ShipmentController {

    private final ShipmentService shipmentService;

    // ===================== CREATION =====================

    @PostMapping("/order/{orderId}/create")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Create shipment record for an order", description = "Creates or reuses exactly one shipment for the order and assigns order items to it")
    @ApiResponse(responseCode = "201", description = "Shipment created successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<APIResponse<ShipmentDto>> createShipmentForOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId) {
        ShipmentDto shipment = shipmentService.createShipmentForOrder(orderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Shipment created successfully", shipment));
    }

    @PostMapping("/{shipmentId}/create")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Create shipment execution", description = "Create shipment execution for provider GHN or SELF on an existing shipment")
    @ApiResponse(responseCode = "200", description = "Shipment execution created successfully")
    @ApiResponse(responseCode = "404", description = "Shipment not found")
    public ResponseEntity<APIResponse<ShipmentDto>> createShipment(
            @Parameter(description = "Shipment ID", required = true) @PathVariable UUID shipmentId,
            @Valid @RequestBody CreateShipmentRequest request) {
        ShipmentDto shipment = shipmentService.createShippingProvider(shipmentId, request);
        return ResponseEntity.ok(APIResponse.success("Shipment execution created successfully", shipment));
    }

    // ===================== READ =====================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Get shipment by ID", description = "Retrieve a specific shipment by its ID")
    @ApiResponse(responseCode = "200", description = "Shipment found")
    @ApiResponse(responseCode = "404", description = "Shipment not found")
    public ResponseEntity<APIResponse<ShipmentDto>> getShipmentById(
            @Parameter(description = "Shipment ID", required = true) @PathVariable UUID id) {
        ShipmentDto shipment = shipmentService.getShipmentById(id);
        return ResponseEntity.ok(APIResponse.success("Shipment retrieved successfully", shipment));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Get shipments by order", description = "Retrieve all shipments for a given order")
    @ApiResponse(responseCode = "200", description = "Shipments retrieved successfully")
    public ResponseEntity<APIResponse<List<ShipmentDto>>> getShipmentsByOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId) {
        List<ShipmentDto> shipments = shipmentService.getShipmentsByOrderId(orderId);
        return ResponseEntity.ok(APIResponse.success("Shipments retrieved successfully", shipments));
    }

    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Get shipments by vendor", description = "Retrieve all shipments fulfilled by a given vendor")
    @ApiResponse(responseCode = "200", description = "Shipments retrieved successfully")
    public ResponseEntity<APIResponse<List<ShipmentDto>>> getShipmentsByVendor(
            @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId) {
        List<ShipmentDto> shipments = shipmentService.getShipmentsByVendorId(vendorId);
        return ResponseEntity.ok(APIResponse.success("Shipments retrieved successfully", shipments));
    }

    @GetMapping("/order/{orderId}/vendor/{vendorId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Get shipment by order and vendor", description = "Retrieve shipment(s) for a specific order / vendor pair")
    @ApiResponse(responseCode = "200", description = "Shipments retrieved successfully")
    public ResponseEntity<APIResponse<List<ShipmentDto>>> getShipmentsByOrderAndVendor(
            @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId,
            @Parameter(description = "Vendor ID", required = true) @PathVariable UUID vendorId) {
        List<ShipmentDto> shipments = shipmentService.getShipmentsByOrderAndVendor(orderId, vendorId);
        return ResponseEntity.ok(APIResponse.success("Shipments retrieved successfully", shipments));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get shipments by status", description = "Retrieve all shipments in a given status (admin only)")
    @ApiResponse(responseCode = "200", description = "Shipments retrieved successfully")
    public ResponseEntity<APIResponse<List<ShipmentDto>>> getShipmentsByStatus(
            @Parameter(description = "Shipment status", required = true) @PathVariable ShipmentStatus status) {
        List<ShipmentDto> shipments = shipmentService.getShipmentsByStatus(status);
        return ResponseEntity.ok(APIResponse.success("Shipments retrieved successfully", shipments));
    }

    // ===================== STATUS UPDATES =====================

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Update shipment status", description = "Advance the shipment status. Relevant timestamps (shippedAt, deliveredAt, etc.) are "
            + "set automatically. Also propagates the new status to all items in the shipment.")
    @ApiResponse(responseCode = "200", description = "Shipment status updated successfully")
    @ApiResponse(responseCode = "404", description = "Shipment not found")
    public ResponseEntity<APIResponse<ShipmentDto>> updateShipmentStatus(
            @Parameter(description = "Shipment ID", required = true) @PathVariable UUID id,
            @Parameter(description = "New shipment status", required = true) @RequestParam ShipmentStatus status) {
        ShipmentDto shipment = shipmentService.updateShipmentStatus(id, status);
        return ResponseEntity.ok(APIResponse.success("Shipment status updated successfully", shipment));
    }

    // @PutMapping("/{id}/cancel")
    // @PreAuthorize("hasAnyRole('TRAINEE', 'VENDOR', 'ADMIN')")
    // @Operation(
    // summary = "Cancel a shipment",
    // description = "Cancel a shipment with a mandatory reason. Propagates
    // CANCELLED status to all items."
    // )
    // @ApiResponse(responseCode = "200", description = "Shipment cancelled
    // successfully")
    // @ApiResponse(responseCode = "400", description = "Invalid input or shipment
    // already delivered/cancelled")
    // @ApiResponse(responseCode = "404", description = "Shipment not found")
    // public ResponseEntity<APIResponse<ShipmentDto>> cancelShipment(
    // @Parameter(description = "Shipment ID", required = true) @PathVariable UUID
    // id,
    // @Valid @RequestBody CancelShipmentRequest request) {
    // ShipmentDto shipment = shipmentService.cancelShipment(id, request);
    // return ResponseEntity.ok(APIResponse.success("Shipment cancelled
    // successfully", shipment));
    // }

    // ===================== LOGISTICS UPDATES =====================

    @PutMapping("/{id}/tracking")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Update tracking info", description = "Update the shipping carrier and tracking number")
    @ApiResponse(responseCode = "200", description = "Tracking info updated successfully")
    @ApiResponse(responseCode = "404", description = "Shipment not found")
    public ResponseEntity<APIResponse<ShipmentDto>> updateTracking(
            @Parameter(description = "Shipment ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateShipmentTrackingRequest request) {
        ShipmentDto shipment = shipmentService.updateTracking(id, request);
        return ResponseEntity.ok(APIResponse.success("Tracking info updated successfully", shipment));
    }

    @PutMapping("/{id}/shipping-fee")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update shipping fee", description = "Update the shipping fee for a shipment and recalculate the parent order total (admin only)")
    @ApiResponse(responseCode = "200", description = "Shipping fee updated successfully")
    @ApiResponse(responseCode = "404", description = "Shipment not found")
    public ResponseEntity<APIResponse<ShipmentDto>> updateShippingFee(
            @Parameter(description = "Shipment ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateShipmentFeeRequest request) {
        ShipmentDto shipment = shipmentService.updateShippingFee(id, request);
        return ResponseEntity.ok(APIResponse.success("Shipping fee updated successfully", shipment));
    }
}
