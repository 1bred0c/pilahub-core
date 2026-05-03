package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.config.properties.GhnProperties;
import fpt.edu.sep490.pilahub.dto.ShipmentDto;
import fpt.edu.sep490.pilahub.dto.ghn.*;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.GhnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller that exposes GHN (Giao Hàng Nhanh) shipping operations.
 * <p>
 * Base path: {@code /api/ghn}
 *
 * <h3>Endpoint groups</h3>
 * <ul>
 * <li><b>Shipment order</b> — create / cancel a GHN order for a Shipment
 * record</li>
 * <li><b>Tracking</b> — retrieve live tracking from GHN</li>
 * <li><b>Fee</b> — calculate shipping fee before confirming</li>
 * <li><b>Master data</b> — provinces / districts / wards / available
 * services</li>
 * <li><b>Webhook</b> — receive push notifications from GHN</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/ghn")
@RequiredArgsConstructor
@Tag(name = "GHN Shipping", description = "GHN (Giao Hàng Nhanh) carrier integration endpoints")
public class GhnShippingController {

        private final GhnService ghnService;
        private final GhnProperties ghnProperties;

        // ===================== STORE MANAGEMENT =====================

        @PostMapping("/vendors/{vendorId}/register-store")
        @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
        @Operation(summary = "Register vendor GHN store", description = "Registers a vendor's pickup store on GHN. "
                        + "On success the returned shop ID is persisted on the Vendor entity "
                        + "and all subsequent order calls for that vendor use it automatically.")
        @ApiResponse(responseCode = "200", description = "Store registered successfully")
        @ApiResponse(responseCode = "404", description = "Vendor not found")
        public ResponseEntity<APIResponse<GhnStoreResponse>> registerStore(
                        @Parameter(description = "Internal Vendor ID", required = true) @PathVariable UUID vendorId,
                        @Valid @RequestBody GhnCreateStoreRequest request) {
                GhnStoreResponse store = ghnService.registerStore(vendorId, request);
                return ResponseEntity.ok(APIResponse.success("GHN store registered successfully", store));
        }

        @GetMapping("/vendors/{vendorId}/store")
        @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
        @Operation(summary = "Get vendor GHN store info", description = "Fetches the GHN store details for a vendor using their stored ghnShopId.")
        @ApiResponse(responseCode = "200", description = "Store info retrieved")
        @ApiResponse(responseCode = "404", description = "Vendor not found or store not yet registered")
        public ResponseEntity<APIResponse<GhnStoreResponse>> getStoreInfo(
                        @Parameter(description = "Internal Vendor ID", required = true) @PathVariable UUID vendorId) {
                GhnStoreResponse store = ghnService.getStoreInfo(vendorId);
                return ResponseEntity.ok(APIResponse.success("Store info retrieved", store));
        }

        @GetMapping("/stores")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "List all GHN stores", description = "Admin: list all stores registered under the platform client token on GHN.")
        @ApiResponse(responseCode = "200", description = "Stores retrieved")
        public ResponseEntity<APIResponse<java.util.List<GhnStoreResponse>>> getAllStores(
                        @Parameter(description = "Pagination offset (GHN integer offset)", example = "0") @RequestParam(defaultValue = "0") int offset) {
                java.util.List<GhnStoreResponse> stores = ghnService.getAllStores(offset);
                return ResponseEntity.ok(APIResponse.success("Stores retrieved", stores));
        }

        // ===================== SHIPMENT ORDER =====================

        @PostMapping("/shipments/{shipmentId}/create-order")
        @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
        @Operation(summary = "Create GHN order", description = "Creates a real GHN shipping order for the given Shipment. "
                        + "Client only sends shipping options while backend maps sender/recipient/package data from shipmentId. "
                        + "On success the GHN order code is saved as the tracking number, "
                        + "shippingProvider is set to 'GHN', and the Shipment advances to DELIVERING.")
        @ApiResponse(responseCode = "200", description = "GHN order created successfully")
        @ApiResponse(responseCode = "404", description = "Shipment not found")
        @ApiResponse(responseCode = "400", description = "GHN rejected the request")
        public ResponseEntity<APIResponse<ShipmentDto>> createGhnOrder(
                        @Parameter(description = "Internal Shipment ID", required = true) @PathVariable UUID shipmentId,
                        @Valid @RequestBody GhnCreateShipmentOrderClientRequest request) {
                ShipmentDto shipment = ghnService.createShipmentOrder(shipmentId, request);
                return ResponseEntity.ok(APIResponse.success("GHN order created successfully", shipment));
        }

        @PostMapping("/shipments/{shipmentId}/cancel-order")
        @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
        @Operation(summary = "Cancel GHN order", description = "Cancels the GHN order linked to this Shipment and marks the Shipment CANCELLED.")
        @ApiResponse(responseCode = "200", description = "GHN order cancelled successfully")
        @ApiResponse(responseCode = "404", description = "Shipment not found")
        @ApiResponse(responseCode = "400", description = "Shipment not yet submitted to GHN or already delivered")
        public ResponseEntity<APIResponse<ShipmentDto>> cancelGhnOrder(
                        @Parameter(description = "Internal Shipment ID", required = true) @PathVariable UUID shipmentId,
                        @Parameter(description = "Reason for cancellation") @RequestParam(required = false, defaultValue = "") String reason) {
                ShipmentDto shipment = ghnService.cancelShipmentOrder(shipmentId, reason);
                return ResponseEntity.ok(APIResponse.success("GHN order cancelled successfully", shipment));
        }

        // ===================== TRACKING =====================

        @GetMapping("/shipments/{shipmentId}/tracking")
        @PreAuthorize("hasAnyRole('TRAINEE', 'VENDOR', 'ADMIN')")
        @Operation(summary = "Get live tracking", description = "Fetches the current tracking status and log from GHN for the given Shipment.")
        @ApiResponse(responseCode = "200", description = "Tracking info retrieved")
        @ApiResponse(responseCode = "404", description = "Shipment not found or no GHN order yet")
        public ResponseEntity<APIResponse<GhnOrderDetailResponse>> getTracking(
                        @Parameter(description = "Internal Shipment ID", required = true) @PathVariable UUID shipmentId) {
                GhnOrderDetailResponse detail = ghnService.getOrderTracking(shipmentId);
                return ResponseEntity.ok(APIResponse.success("Tracking info retrieved", detail));
        }

        // ===================== FEE CALCULATION =====================

        @PostMapping("/calculate-fee")
        @PreAuthorize("hasAnyRole('TRAINEE', 'VENDOR', 'ADMIN')")
        @Operation(summary = "Calculate shipping fee", description = "Calculate shipping fee from vendor, recipient address, and package details. "
                        + "Backend resolves vendor store and recipient district/ward codes automatically.")
        @ApiResponse(responseCode = "200", description = "Fee calculated successfully")
        public ResponseEntity<APIResponse<GhnFeeResponse>> calculateFee(
                        @Valid @RequestBody GhnCalculateFeeByProductRequest request) {
                GhnFeeResponse fee = ghnService.calculateFee(request);
                return ResponseEntity.ok(APIResponse.success("Shipping fee calculated", fee));
        }

        // ===================== MASTER DATA =====================

        @GetMapping("/provinces")
        @PreAuthorize("hasAnyRole('TRAINEE', 'VENDOR', 'ADMIN')")
        @Operation(summary = "List provinces", description = "Return all Vietnamese provinces from GHN master data")
        @ApiResponse(responseCode = "200", description = "Provinces retrieved")
        public ResponseEntity<APIResponse<List<GhnProvinceDto>>> getProvinces() {
                List<GhnProvinceDto> provinces = ghnService.getProvinces();
                return ResponseEntity.ok(APIResponse.success("Provinces retrieved", provinces));
        }

        @GetMapping("/districts/{provinceId}")
        @PreAuthorize("hasAnyRole('TRAINEE', 'VENDOR', 'ADMIN')")
        @Operation(summary = "List districts", description = "Return all districts in a given GHN province")
        @ApiResponse(responseCode = "200", description = "Districts retrieved")
        public ResponseEntity<APIResponse<List<GhnDistrictDto>>> getDistricts(
                        @Parameter(description = "GHN Province ID", required = true) @PathVariable int provinceId) {
                List<GhnDistrictDto> districts = ghnService.getDistricts(provinceId);
                return ResponseEntity.ok(APIResponse.success("Districts retrieved", districts));
        }

        @GetMapping("/wards/{districtId}")
        @PreAuthorize("hasAnyRole('TRAINEE', 'VENDOR', 'ADMIN')")
        @Operation(summary = "List wards", description = "Return all wards in a given GHN district")
        @ApiResponse(responseCode = "200", description = "Wards retrieved")
        public ResponseEntity<APIResponse<List<GhnWardDto>>> getWards(
                        @Parameter(description = "GHN District ID", required = true) @PathVariable int districtId) {
                List<GhnWardDto> wards = ghnService.getWards(districtId);
                return ResponseEntity.ok(APIResponse.success("Wards retrieved", wards));
        }

        @GetMapping("/available-services")
        @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
        @Operation(summary = "Get available services", description = "Return GHN services available between two districts for a specific vendor's shop (e.g. Standard, Express)")
        @ApiResponse(responseCode = "200", description = "Available services retrieved")
        public ResponseEntity<APIResponse<java.util.List<GhnAvailableServiceDto>>> getAvailableServices(
                        @Parameter(description = "Vendor UUID whose GHN shop ID to use", required = true) @RequestParam UUID vendorId,
                        @Parameter(description = "Sender district ID", required = true) @RequestParam int fromDistrictId,
                        @Parameter(description = "Recipient district ID", required = true) @RequestParam int toDistrictId) {
                java.util.List<GhnAvailableServiceDto> services = ghnService.getAvailableServices(vendorId,
                                fromDistrictId, toDistrictId);
                return ResponseEntity.ok(APIResponse.success("Available services retrieved", services));
        }

        // ===================== WEBHOOK =====================

        /**
         * GHN calls this endpoint whenever a shipment status changes.
         * <p>
         * No JWT authentication — the caller is GHN's servers.
         * Requests are verified by matching the {@code ClientId} in the payload
         * against {@code ghn.client-id} from application config.
         * <p>
         * Register this URL in the GHN portal with:
         * <ul>
         * <li><b>Client ID</b> — value of env {@code GHN_CLIENT_ID}</li>
         * <li><b>URL webhook</b> — {@code https://<your-domain>/api/ghn/webhook}</li>
         * <li><b>Environment</b> — Staging or Production</li>
         * <li><b>Name</b> — e.g. {@code pilahub-webhook}</li>
         * </ul>
         */
        @PostMapping("/webhook")
        @Operation(summary = "GHN webhook receiver", description = "Endpoint called by GHN whenever a shipment status changes. "
                        + "Verifies ClientId then updates the matching Shipment and OrderDetail records.")
        @ApiResponse(responseCode = "200", description = "Webhook processed successfully")
        @ApiResponse(responseCode = "401", description = "ClientId mismatch — rejected")
        public ResponseEntity<APIResponse<ShipmentDto>> receiveWebhook(
                        @RequestBody GhnWebhookPayload payload) {

                // Verify ClientId to ensure the call genuinely comes from our registered GHN
                // webhook
                String expectedClientId = ghnProperties.getClientId();
                if (expectedClientId != null && !expectedClientId.isBlank()
                                && !expectedClientId.equals(payload.clientId())) {
                        return ResponseEntity.status(401)
                                        .body(APIResponse.error("Invalid ClientId"));
                }

                ShipmentDto shipment = ghnService.processWebhook(payload);
                return ResponseEntity.ok(APIResponse.success("Webhook processed", shipment));
        }
}
