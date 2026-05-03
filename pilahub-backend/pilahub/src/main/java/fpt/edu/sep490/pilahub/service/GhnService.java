package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.ShipmentDto;
import fpt.edu.sep490.pilahub.dto.ghn.*;
import fpt.edu.sep490.pilahub.pojo.Shipment;

import java.util.List;
import java.util.UUID;

/**
 * Service for interacting with the GHN (Giao Hàng Nhanh) shipping API.
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Manage per-vendor GHN stores (register, list, look up)</li>
 * <li>Calculate shipping fee before confirming a shipment</li>
 * <li>Create a GHN order when a shipment moves to PROCESSING/DELIVERING</li>
 * <li>Cancel a GHN order (propagates to the Shipment entity)</li>
 * <li>Retrieve live tracking info from GHN</li>
 * <li>Expose GHN master-data (provinces / districts / wards) for address
 * forms</li>
 * </ul>
 */
public interface GhnService {

    // ── Store management ──────────────────────────────────────────────────────

    /**
     * Register a vendor's pickup store on GHN.
     * On success GHN returns a shop ID which is persisted as
     * {@code Vendor.ghnShopId} so all subsequent order calls for that vendor
     * send the correct {@code ShopId} header automatically.
     *
     * @param vendorId internal vendor UUID
     * @param request  store name, address, district/ward details
     * @return the newly created GHN store record
     */
    GhnStoreResponse registerStore(UUID vendorId, GhnCreateStoreRequest request);

    /**
     * Fetch a vendor's GHN store info by their internally stored {@code ghnShopId}.
     *
     * @param vendorId internal vendor UUID
     * @return store detail from GHN
     */
    GhnStoreResponse getStoreInfo(UUID vendorId);

    /**
     * List all stores registered under the platform client token.
     * Useful for admins to verify/reconcile vendor shop registrations.
     *
     * @param offset pagination offset (GHN uses integer offset, not page)
     * @return list of all registered stores
     */
    List<GhnStoreResponse> getAllStores(int offset);

    // ── Fee calculation ───────────────────────────────────────────────────────

    /**
     * Call GHN to calculate the shipping fee for given package parameters.
     * This is a pure utility call — no Shipment entity is modified.
     *
     * @param vendorId vendor whose GHN shop ID to use in the {@code ShopId} header
     * @param request  fee-calculation parameters
     * @return GHN fee breakdown
     */
    GhnFeeResponse calculateFee(UUID vendorId, GhnFeeRequest request);

    /**
     * Build GHN fee request from application entities (address + vendor store)
     * and package details provided by client.
     */
    GhnFeeResponse calculateFee(GhnCalculateFeeByProductRequest request);

    // ── Order lifecycle ───────────────────────────────────────────────────────

    /**
     * Create a GHN shipping order for the given Shipment.
     * Uses the owning vendor's {@code ghnShopId} as the GHN {@code ShopId} header.
     * On success:
     * <ul>
     * <li>Persists the GHN {@code order_code} as
     * {@code Shipment.trackingNumber}</li>
     * <li>Sets {@code Shipment.shippingProvider} to
     * {@code ShippingProvider.GHN}</li>
     * <li>Sets the estimated delivery date from GHN's response</li>
     * <li>Updates {@code Shipment.shippingFee} with GHN's {@code total_fee}</li>
     * <li>Advances {@code Shipment.status} to {@code DELIVERING}</li>
     * </ul>
     *
     * @param shipmentId internal shipment UUID
     * @param request    client shipping options; backend maps full GHN payload from
     *                   shipment
     * @return updated ShipmentDto
     */
    ShipmentDto createShipmentOrder(UUID shipmentId, GhnCreateShipmentOrderClientRequest request);

    /**
     * Apply GHN create-order business logic onto the provided Shipment entity
     * without persisting it. Caller is responsible for saving.
     */
    Shipment prepareShipmentOrder(Shipment shipment, GhnCreateShipmentOrderClientRequest request);

    /**
     * Cancel the GHN order linked to the given Shipment (by tracking number).
     * On success updates Shipment status to CANCELLED.
     *
     * @param shipmentId internal shipment UUID
     * @param reason     human-readable cancellation reason
     * @return updated ShipmentDto
     */
    ShipmentDto cancelShipmentOrder(UUID shipmentId, String reason);

    /**
     * Cancel GHN order directly from in-memory shipment state when local
     * persistence fails after GHN order creation.
     */
    void cancelShipmentOrderCompensation(Shipment shipment, String reason);

    // ── Tracking ──────────────────────────────────────────────────────────────

    /**
     * Fetch live tracking detail from GHN for the given Shipment.
     *
     * @param shipmentId internal shipment UUID
     * @return GHN order detail including status and log entries
     */
    GhnOrderDetailResponse getOrderTracking(UUID shipmentId);

    // ── Master data (address lookup) ──────────────────────────────────────────

    /** Retrieve all Vietnamese provinces from GHN. */
    List<GhnProvinceDto> getProvinces();

    /** Retrieve all districts in a given province. */
    List<GhnDistrictDto> getDistricts(int provinceId);

    /** Retrieve all wards in a given district. */
    List<GhnWardDto> getWards(int districtId);

    /**
     * Retrieve available shipping services between two districts.
     *
     * @param vendorId       vendor whose GHN shop ID to use
     * @param fromDistrictId sender's district ID
     * @param toDistrictId   recipient's district ID
     */
    List<GhnAvailableServiceDto> getAvailableServices(UUID vendorId, int fromDistrictId, int toDistrictId);

    // ── Webhook ───────────────────────────────────────────────────────────────

    /**
     * Handle an inbound GHN status-update push notification.
     * Maps GHN status strings to our
     * {@link fpt.edu.sep490.pilahub.enums.ShipmentStatus}
     * and persists changes to the matching Shipment and its OrderDetails.
     *
     * @param payload raw webhook body from GHN
     * @return updated ShipmentDto
     */
    ShipmentDto processWebhook(GhnWebhookPayload payload);
}
