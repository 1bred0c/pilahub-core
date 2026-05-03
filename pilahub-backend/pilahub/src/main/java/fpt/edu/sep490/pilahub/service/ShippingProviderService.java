package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.request.shipment.CreateShipmentRequest;
import fpt.edu.sep490.pilahub.pojo.Shipment;

public interface ShippingProviderService {
    Shipment createProviderForShipment(Shipment shipment, CreateShipmentRequest request);
}
