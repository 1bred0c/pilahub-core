package fpt.edu.sep490.pilahub.service.helper;

import fpt.edu.sep490.pilahub.enums.ShippingProvider;
import fpt.edu.sep490.pilahub.service.ShippingProviderService;
import fpt.edu.sep490.pilahub.service.implement.GhnServiceImpl;
import fpt.edu.sep490.pilahub.service.implement.SelfShippingServiceImpl;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ShippingServiceFactory {

    private final Map<ShippingProvider, ShippingProviderService> services;

    public ShippingServiceFactory(
            GhnServiceImpl ghnService,
            SelfShippingServiceImpl selfService
    ) {
        services = Map.of(
                ShippingProvider.GHN, ghnService,
                ShippingProvider.SELF, selfService
        );
    }

    public ShippingProviderService get(ShippingProvider provider) {
        ShippingProviderService service = services.get(provider);
        if (service == null) {
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
        return service;
    }
}
