package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.request.shipment.CreateShipmentRequest;
import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import fpt.edu.sep490.pilahub.enums.ShippingProvider;
import fpt.edu.sep490.pilahub.enums.ShipmentStatus;
import fpt.edu.sep490.pilahub.pojo.Shipment;
import fpt.edu.sep490.pilahub.service.ShippingProviderService;
import fpt.edu.sep490.pilahub.service.helper.ShipmentDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class SelfShippingServiceImpl implements ShippingProviderService {

    private final ShipmentDomainService shipmentDomainService;

    @Override
    public Shipment createProviderForShipment(Shipment shipment, CreateShipmentRequest request) {
        return createSelfShipment(shipment, request);
    }

    private Shipment createSelfShipment(Shipment shipment, CreateShipmentRequest request) {
        if (request.selfRequest() == null) {
            throw new IllegalArgumentException("selfRequest is required when shippingProvider = SELF");
        }

        shipment.setShippingProvider(ShippingProvider.SELF);
        shipment.setTrackingNumber(request.selfRequest().trackingNumber());
        shipment.setStatus(ShipmentStatus.PICKED);
        shipment.setShippedAt(Instant.now());
        shipmentDomainService.propagateStatusToDetails(shipment, OrderDetailStatus.SHIPPED);
        shipmentDomainService.syncOrderStatusWithShipment(shipment);

        return shipment;
    }
}
