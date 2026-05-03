package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.ShipmentDto;
import fpt.edu.sep490.pilahub.dto.request.shipment.CancelShipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.shipment.CreateShipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.shipment.UpdateShipmentFeeRequest;
import fpt.edu.sep490.pilahub.dto.request.shipment.UpdateShipmentTrackingRequest;
import fpt.edu.sep490.pilahub.enums.ShipmentStatus;

import java.util.List;
import java.util.UUID;

public interface ShipmentService {

    // ========== READ ==========

    /** Get a shipment by its ID. */
    ShipmentDto getShipmentById(UUID shipmentId);

    /** Get all shipments that belong to a given order. */
    List<ShipmentDto> getShipmentsByOrderId(UUID orderId);

    /** Get all shipments fulfilled by a given vendor. */
    List<ShipmentDto> getShipmentsByVendorId(UUID vendorId);

    /**
     * Get all shipments for a specific order / vendor pair (at most one in the
     * normal flow).
     */
    List<ShipmentDto> getShipmentsByOrderAndVendor(UUID orderId, UUID vendorId);

    /** Get all shipments in a given status (admin use). */
    List<ShipmentDto> getShipmentsByStatus(ShipmentStatus status);

    // ========== CREATION ==========

    ShipmentDto createShipmentForOrder(UUID orderId);

    ShipmentDto createShippingProvider(UUID shipmentId, CreateShipmentRequest request);

    // ========== STATUS ==========

    /**
     * Advance the shipment's status and record relevant timestamps:
     * <ul>
     * <li>DELIVERING → sets {@code shippedAt}</li>
     * <li>DELIVERED → sets {@code deliveredAt}, {@code returnDeadline} (+7 d),
     * {@code payoutReleaseDate} (vendor holdingDays)</li>
     * <li>CANCELLED → sets {@code cancelledAt}; delegates cancellation reason
     * via {@link CancelShipmentRequest}</li>
     * </ul>
     * Also propagates the new status to every OrderDetail in the shipment.
     */
    ShipmentDto updateShipmentStatus(UUID shipmentId, ShipmentStatus newStatus);

    /** Cancel a shipment with a reason and propagate CANCELLED to its items. */
    ShipmentDto cancelShipment(UUID shipmentId, CancelShipmentRequest request);

    // ========== UPDATE ==========

    /** Update the carrier / tracking number for a shipment. */
    ShipmentDto updateTracking(UUID shipmentId, UpdateShipmentTrackingRequest request);

    /**
     * Update the shipping fee for a shipment and recalculate the parent order
     * total.
     */
    ShipmentDto updateShippingFee(UUID shipmentId, UpdateShipmentFeeRequest request);
}
