package fpt.edu.sep490.pilahub.service;

import java.util.UUID;

/**
 * Service responsible for releasing vendor payouts for eligible
 * {@link fpt.edu.sep490.pilahub.pojo.OrderDetail} records.
 */
public interface VendorPayoutService {

    /**
     * Scans all {@code Order} rows that are:
     * <ul>
     * <li>all order details have status = COMPLETED</li>
     * <li>paidOut = false</li>
     * </ul>
     * Groups them by vendor, credits the vendor wallet with the summed net amount
     * (gross revenue minus platform fee) in one wallet operation, and creates one
     * {@code VENDOR_PAYOUT} transaction per eligible {@code Order} for a full
     * audit trail. Finally marks every processed {@code Order} as
     * {@code paidOut = true}.
     */
    void releaseEligiblePayouts();

    /**
     * Admin-triggered payout for a single order.
     * Only checks {@code paidOut = false}; does not enforce
     * {@code returnDeadline} or {@code payoutReleaseDate}.
     */
    void payoutOrderForVendor(UUID orderId);
}
