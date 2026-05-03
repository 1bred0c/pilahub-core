package fpt.edu.sep490.pilahub.enums;

public enum OrderDetailStatus {
    PENDING, // Item ordered, awaiting vendor confirmation
    CONFIRMED, // Vendor confirmed item availability
    READY, // Vendor has prepared/packed the item and it is ready to ship
    SHIPPED, // Item is being delivered to customer
    DELIVERED, // Item delivered successfully
    COMPLETED, // Item completed after customer confirmation or return deadline
    CANCELLED, // Item cancelled (by customer or vendor)
    RETURNED, // Item returned by customer
    REFUNDED, // Item refunded to customer
    OUT_OF_STOCK // Item unavailable from vendor
}
