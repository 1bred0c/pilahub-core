package fpt.edu.sep490.pilahub.enums;

public enum OrderStatus {
    PENDING, // Order is created and awaiting confirmation (default state)
    CONFIRMED, // Order confirmed and being fulfilled
    READY, // Order is packed/prepared and ready to ship
    SHIPPED, // Order has been handed to carrier
    DELIVERED, // Order has been delivered to customer
    FAILED_DELIVERY, // Delivery attempt failed
    COMPLETED, // Order completed - all items delivered
    CANCELLED, // Order cancelled
    RETURNED, // Order has been returned
    REFUNDED // Refund issued for the order
}
