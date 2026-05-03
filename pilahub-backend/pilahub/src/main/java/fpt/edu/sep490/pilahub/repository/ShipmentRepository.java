package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.ShipmentStatus;
import fpt.edu.sep490.pilahub.pojo.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

       List<Shipment> findByOrder_OrderId(UUID orderId);

       List<Shipment> findByVendor_VendorId(UUID vendorId);

       List<Shipment> findByOrder_OrderIdAndVendor_VendorId(UUID orderId, UUID vendorId);

       List<Shipment> findByStatus(ShipmentStatus status);

       /** Look up a shipment by its carrier tracking number (GHN order code). */
       Optional<Shipment> findByTrackingNumber(String trackingNumber);

}
