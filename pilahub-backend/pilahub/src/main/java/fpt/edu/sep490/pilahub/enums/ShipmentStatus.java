package fpt.edu.sep490.pilahub.enums;

public enum ShipmentStatus {
    DRAFT, // Shipment draft created before pickup workflow starts
    READY_TO_PICK, // Shipment created and ready for pickup
    PICKING, // Carrier is picking up the shipment
    PICKED, // Shipment has been picked from sender
    STORING, // Shipment is stored at a hub/warehouse
    TRANSPORTING, // Shipment is being transported between hubs
    DELIVERING, // Shipment handed to carrier — tracking number assigned
    DELIVERED, // Shipment delivered to customer
    DELIVERY_FAIL, // Delivery attempt failed
    RETURN, // Return process has started
    RETURNING, // Shipment is being returned
    RETURNED, // Shipment returned by customer
    CANCELLED, // Shipment cancelled (by customer or vendor)
}
