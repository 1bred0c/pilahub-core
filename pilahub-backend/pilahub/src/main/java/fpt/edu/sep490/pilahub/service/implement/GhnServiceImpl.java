package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.config.properties.GhnProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.edu.sep490.pilahub.dto.ShipmentDto;
import fpt.edu.sep490.pilahub.dto.ghn.*;
import fpt.edu.sep490.pilahub.dto.request.shipment.CreateShipmentRequest;
import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import fpt.edu.sep490.pilahub.enums.OrderStatus;
import fpt.edu.sep490.pilahub.enums.ShippingProvider;
import fpt.edu.sep490.pilahub.enums.ShipmentStatus;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.ShipmentMapper;
import fpt.edu.sep490.pilahub.pojo.Order;
import fpt.edu.sep490.pilahub.pojo.OrderDetail;
import fpt.edu.sep490.pilahub.pojo.Shipment;
import fpt.edu.sep490.pilahub.pojo.Address;
import fpt.edu.sep490.pilahub.pojo.Vendor;
import fpt.edu.sep490.pilahub.repository.AddressRepository;
import fpt.edu.sep490.pilahub.repository.OrderDetailRepository;
import fpt.edu.sep490.pilahub.repository.OrderRepository;
import fpt.edu.sep490.pilahub.repository.ShipmentRepository;
import fpt.edu.sep490.pilahub.repository.VendorRepository;
import fpt.edu.sep490.pilahub.service.GhnService;
import fpt.edu.sep490.pilahub.service.ShippingProviderService;
import fpt.edu.sep490.pilahub.service.SystemConfigService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GhnServiceImpl implements GhnService, ShippingProviderService {

    private static final ShippingProvider PROVIDER_NAME = ShippingProvider.GHN;

    // GHN API path constants
    private static final String PATH_FEE = "/shiip/public-api/v2/shipping-order/fee";
    private static final String PATH_CREATE_ORDER = "/shiip/public-api/v2/shipping-order/create";
    private static final String PATH_ORDER_DETAIL = "/shiip/public-api/v2/shipping-order/detail";
    private static final String PATH_CANCEL = "/shiip/public-api/v2/switch-status/cancel";
    private static final String PATH_PROVINCES = "/shiip/public-api/master-data/province";
    private static final String PATH_DISTRICTS = "/shiip/public-api/master-data/district";
    private static final String PATH_WARDS = "/shiip/public-api/master-data/ward";
    private static final String PATH_AVAIL_SERVICES = "/shiip/public-api/v2/shipping-order/available-services";
    private static final String PATH_CREATE_STORE = "/shiip/public-api/v2/shop/register";
    private static final String PATH_GET_ALL_STORES = "/shiip/public-api/v2/shop/all";
    private static final ObjectMapper GHN_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final RestTemplate restTemplate;
    private final GhnProperties ghnProperties;
    private final ShipmentRepository shipmentRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final VendorRepository vendorRepository;
    private final AddressRepository addressRepository;
    private final ShipmentMapper shipmentMapper;
    private final OrderRepository orderRepository;
    private final SystemConfigService systemConfigService;

    // ── Store management ──────────────────────────────────────────────────────

    @Override
    public GhnStoreResponse registerStore(UUID vendorId, GhnCreateStoreRequest request) {
        Vendor vendor = loadVendor(vendorId);

        ResponseEntity<GhnApiResponse<Map<String, Object>>> response = restTemplate.exchange(
                url(PATH_CREATE_STORE),
                HttpMethod.POST,
                buildEntity(request, null),
                new ParameterizedTypeReference<>() {
                });
        Map<String, Object> data = extractData(response, "Register GHN store");
        GhnStoreResponse store = toStoreResponse(data);

        // Persist the GHN shop ID onto the vendor so future order calls use it
        // automatically
        if (store == null || store.id() == null) {
            throw new IllegalStateException("GHN register store succeeded but response does not contain shop id");
        }
        vendor.setGhnShopId(store.id());
        vendorRepository.save(vendor);
        log.info("GHN store registered: shopId={} for vendorId={}", store.id(), vendorId);
        return store;
    }

    @Override
    public GhnStoreResponse getStoreInfo(UUID vendorId) {
        Vendor vendor = loadVendor(vendorId);
        if (vendor.getGhnShopId() == null) {
            throw new IllegalStateException(
                    "Vendor " + vendorId + " has not registered a GHN store yet");
        }
        // GHN does not have a single-store GET by ID publicly, so list all and filter
        // by id
        List<GhnStoreResponse> all = getAllStores(0);
        return all.stream()
                .filter(s -> vendor.getGhnShopId().equals(s.id()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GHN Store", "shopId", vendor.getGhnShopId()));
    }

    @Override
    public List<GhnStoreResponse> getAllStores(int offset) {
        // GHN wraps the list inside { shops: [...], total: N, last_offset: N }
        // Use a raw Map to extract the "shops" array without a dedicated wrapper DTO
        ResponseEntity<GhnApiResponse<java.util.Map<String, Object>>> response = restTemplate.exchange(
                url(PATH_GET_ALL_STORES) + "?offset=" + offset,
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(null)),
                new ParameterizedTypeReference<>() {
                });
        java.util.Map<String, Object> data = extractData(response, "Get all GHN stores");
        if (data == null || !data.containsKey("shops"))
            return List.of();

        // Jackson deserialises List<LinkedHashMap> — convert each map back to
        // GhnStoreResponse
        @SuppressWarnings("unchecked")
        List<Object> rawShops = (List<Object>) data.get("shops");
        return rawShops.stream()
                .map(this::toStoreResponse)
                .filter(Objects::nonNull)
                .toList();
    }

    // ── Fee calculation ───────────────────────────────────────────────────────

    @Override
    public GhnFeeResponse calculateFee(UUID vendorId, GhnFeeRequest request) {
        Integer shopId = resolveShopId(vendorId);
        ResponseEntity<GhnApiResponse<GhnFeeResponse>> response = restTemplate.exchange(
                url(PATH_FEE),
                HttpMethod.POST,
                buildEntity(request, shopId),
                new ParameterizedTypeReference<>() {
                });
        return extractData(response, "Calculate fee");
    }

    @Override
    public GhnFeeResponse calculateFee(GhnCalculateFeeByProductRequest request) {
        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", request.addressId()));

        UUID vendorId = request.vendorId();
        GhnStoreResponse store = getStoreInfo(vendorId);
        if (store.id() == null || store.districtId() == null) {
            throw new IllegalStateException("Vendor GHN store information is incomplete");
        }

        Integer toDistrictId = resolveToDistrictId(address);
        String toWardCode = resolveToWardCode(address, toDistrictId);

        int quantity = request.quantity();
        int totalWeight = Math.max(1, request.weight() * quantity);
        int insuranceValue = 0;

        GhnFeeRequest ghnRequest = new GhnFeeRequest(
                null,
                request.serviceTypeId(),
                store.districtId(),
                store.wardCode(),
                toDistrictId,
                toWardCode,
                request.height(),
                request.length(),
                request.width(),
                totalWeight,
                insuranceValue,
                0,
                null,
                null);

        return calculateFee(vendorId, ghnRequest);
    }

    // ── Order lifecycle ───────────────────────────────────────────────────────

    @Override
    public Shipment createProviderForShipment(Shipment shipment, CreateShipmentRequest request) {
        if (request == null || request.ghnRequest() == null) {
            throw new IllegalArgumentException("ghnRequest is required");
        }
        return prepareShipmentOrder(shipment, request.ghnRequest());
    }

    @Override
    public ShipmentDto createShipmentOrder(UUID shipmentId, GhnCreateShipmentOrderClientRequest request) {
        Shipment shipment = loadShipment(shipmentId);

        Shipment prepared = prepareShipmentOrder(shipment, request);
        Shipment saved = shipmentRepository.save(prepared);
        log.info("GHN order created: trackingNumber={} for shipmentId={}", saved.getTrackingNumber(), shipmentId);
        return shipmentMapper.toDto(saved);
    }

    @Override
    public Shipment prepareShipmentOrder(Shipment shipment, GhnCreateShipmentOrderClientRequest request) {

        Integer shopId = vendorShopId(shipment);
        GhnCreateShipmentOrderRequest mappedRequest = mapClientToCreateOrderRequest(shipment, request);
        Map<String, Object> body = buildCreateOrderBody(shipment, mappedRequest);

        ResponseEntity<GhnApiResponse<GhnCreateOrderResponse>> response = restTemplate.exchange(
                url(PATH_CREATE_ORDER),
                HttpMethod.POST,
                buildEntity(body, shopId),
                new ParameterizedTypeReference<>() {
                });
        GhnCreateOrderResponse ghnResponse = extractData(response, "Create GHN order");

        // ── Persist GHN response back onto the Shipment ──────────────────────
        shipment.setTrackingNumber(ghnResponse.orderCode());
        shipment.setShippingProvider(PROVIDER_NAME);

        if (ghnResponse.totalFee() != null) {
            try {
                BigDecimal fee = new BigDecimal(ghnResponse.totalFee());
                // Shipping fee belongs to the Order; recalculate total accordingly
                Order parentOrder = shipment.getOrder();
                parentOrder.setShippingFee(fee);
                recalculateOrderTotal(parentOrder);
            } catch (NumberFormatException e) {
                log.warn("Could not parse GHN totalFee '{}' as BigDecimal — skipping fee update",
                        ghnResponse.totalFee());
            }
        }

        if (ghnResponse.expectedDeliveryTime() != null) {
            try {
                shipment.setEstimatedDeliveryAt(
                        OffsetDateTime.parse(ghnResponse.expectedDeliveryTime()).toInstant());
            } catch (Exception e) {
                log.warn("Could not parse GHN expectedDeliveryTime '{}' — skipping",
                        ghnResponse.expectedDeliveryTime());
            }
        }

        // Advance status to pickup-ready and propagate to items
        shipment.setStatus(ShipmentStatus.READY_TO_PICK);
        propagateStatusToDetails(shipment, OrderDetailStatus.READY);
        syncOrderStatusWithShipment(shipment);

        return shipment;
    }

    @Override
    public ShipmentDto cancelShipmentOrder(UUID shipmentId, String reason) {
        Shipment shipment = loadShipment(shipmentId);
        String orderCode = shipment.getTrackingNumber();

        if (orderCode == null || orderCode.isBlank()) {
            throw new IllegalStateException(
                    "Shipment " + shipmentId + " has no GHN order code — cannot cancel via GHN");
        }

        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a shipment that has already been delivered");
        }

        Integer shopId = vendorShopId(shipment);
        Map<String, Object> body = Map.of("order_codes", List.of(orderCode));
        ResponseEntity<GhnApiResponse<Object>> response = restTemplate.exchange(
                url(PATH_CANCEL),
                HttpMethod.POST,
                buildEntity(body, shopId),
                new ParameterizedTypeReference<>() {
                });
        extractData(response, "Cancel GHN order");

        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipment.setCancelledAt(Instant.now());
        if (reason != null && !reason.isBlank()) {
            shipment.setCancellationReason(reason);
        }
        propagateStatusToDetails(shipment, OrderDetailStatus.CANCELLED);
        syncOrderStatusWithShipment(shipment);

        Shipment saved = shipmentRepository.save(shipment);
        log.info("GHN order cancelled: orderCode={} for shipmentId={}", orderCode, shipmentId);
        return shipmentMapper.toDto(saved);
    }

    @Override
    public void cancelShipmentOrderCompensation(Shipment shipment, String reason) {
        if (shipment == null || shipment.getTrackingNumber() == null || shipment.getTrackingNumber().isBlank()) {
            throw new IllegalArgumentException("trackingNumber is required for GHN compensation cancel");
        }

        Integer shopId = vendorShopId(shipment);
        Map<String, Object> body = Map.of("order_codes", List.of(shipment.getTrackingNumber()));

        ResponseEntity<GhnApiResponse<Object>> response = restTemplate.exchange(
                url(PATH_CANCEL),
                HttpMethod.POST,
                buildEntity(body, shopId),
                new ParameterizedTypeReference<>() {
                });
        extractData(response, "Compensation cancel GHN order");

        log.warn("GHN compensation cancel sent: orderCode={}, shipmentId={}, reason={}",
                shipment.getTrackingNumber(), shipment.getShipmentId(), reason);
    }

    // ── Tracking ──────────────────────────────────────────────────────────────

    @Override
    public GhnOrderDetailResponse getOrderTracking(UUID shipmentId) {
        Shipment shipment = loadShipment(shipmentId);
        String orderCode = shipment.getTrackingNumber();

        if (orderCode == null || orderCode.isBlank()) {
            throw new IllegalStateException(
                    "Shipment " + shipmentId + " does not have a GHN order code yet");
        }

        ResponseEntity<GhnApiResponse<GhnOrderDetailResponse>> response = restTemplate.exchange(
                url(PATH_ORDER_DETAIL) + "?order_code=" + orderCode,
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(vendorShopId(shipment))),
                new ParameterizedTypeReference<>() {
                });
        return extractData(response, "Get GHN order tracking");
    }

    // ── Master data ───────────────────────────────────────────────────────────

    @Override
    public List<GhnProvinceDto> getProvinces() {
        ResponseEntity<GhnApiResponse<List<GhnProvinceDto>>> response = restTemplate.exchange(
                url(PATH_PROVINCES),
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(null)),
                new ParameterizedTypeReference<>() {
                });
        List<GhnProvinceDto> provinces = extractData(response, "Get provinces");
        return provinces != null ? provinces : List.of();
    }

    @Override
    public List<GhnDistrictDto> getDistricts(int provinceId) {
        ResponseEntity<GhnApiResponse<List<GhnDistrictDto>>> response = restTemplate.exchange(
                url(PATH_DISTRICTS) + "?province_id=" + provinceId,
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(null)),
                new ParameterizedTypeReference<>() {
                });
        List<GhnDistrictDto> districts = extractData(response, "Get districts");
        return districts != null ? districts : List.of();
    }

    @Override
    public List<GhnWardDto> getWards(int districtId) {
        ResponseEntity<GhnApiResponse<List<GhnWardDto>>> response = restTemplate.exchange(
                url(PATH_WARDS) + "?district_id=" + districtId,
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(null)),
                new ParameterizedTypeReference<>() {
                });
        List<GhnWardDto> wards = extractData(response, "Get wards");
        return wards != null ? wards : List.of();
    }

    @Override
    public List<GhnAvailableServiceDto> getAvailableServices(UUID vendorId, int fromDistrictId, int toDistrictId) {
        Integer shopId = resolveShopId(vendorId);
        String endpoint = url(PATH_AVAIL_SERVICES)
                + "?shop_id=" + shopId
                + "&from_district=" + fromDistrictId
                + "&to_district=" + toDistrictId;

        ResponseEntity<GhnApiResponse<List<GhnAvailableServiceDto>>> response = restTemplate.exchange(
                endpoint,
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(null)),
                new ParameterizedTypeReference<>() {
                });
        return extractData(response, "Get available services");
    }

    // ── Webhook processing ────────────────────────────────────────────────────

    /**
     * Handle an inbound GHN status-update webhook.
     * Maps GHN status strings to our {@link ShipmentStatus} and updates the entity.
     */
    public ShipmentDto processWebhook(GhnWebhookPayload payload) {
        String orderCode = payload.orderCode();
        Shipment shipment = shipmentRepository.findByTrackingNumber(orderCode)
                .orElseThrow(() -> {
                    log.warn("GHN webhook received for unknown orderCode={}", orderCode);
                    return new ResourceNotFoundException("Shipment", "trackingNumber", orderCode);
                });

        ShipmentStatus newStatus = mapGhnStatus(payload.status());
        if (newStatus == null) {
            log.info("Ignoring unmapped GHN status '{}' for orderCode={}", payload.status(), orderCode);
            return shipmentMapper.toDto(shipment);
        }

        // Avoid redundant updates
        if (shipment.getStatus() == newStatus) {
            return shipmentMapper.toDto(shipment);
        }

        Instant now = Instant.now();
        shipment.setStatus(newStatus);

        switch (newStatus) {
            case DELIVERING -> shipment.setShippedAt(now);
            case DELIVERED -> {
                shipment.setDeliveredAt(now);
                shipment.setReturnDeadline(now.plus(7, ChronoUnit.DAYS));
                int holdingDays = (shipment.getVendor() != null
                        && shipment.getVendor().getHoldingDays() != null)
                                ? shipment.getVendor().getHoldingDays()
                                : systemConfigService.getDefaultHoldingDays();
                shipment.setPayoutReleaseDate(now.plus(holdingDays, ChronoUnit.DAYS));
                propagateStatusToDetails(shipment, OrderDetailStatus.DELIVERED);
            }
            case CANCELLED -> {
                shipment.setCancelledAt(now);
                if (payload.reason() != null && !payload.reason().isBlank()) {
                    shipment.setCancellationReason(payload.reason());
                }
                propagateStatusToDetails(shipment, OrderDetailStatus.CANCELLED);
            }
            case RETURN, RETURNING, RETURNED -> propagateStatusToDetails(shipment, OrderDetailStatus.RETURNED);
            default -> {
                /*
                 * DRAFT, READY_TO_PICK, PICKING, PICKED, STORING, TRANSPORTING,
                 * DELIVERY_FAIL — no
                 * extra logic
                 */ }
        }

        syncOrderStatusWithShipment(shipment);

        Shipment saved = shipmentRepository.save(shipment);
        log.info("GHN webhook processed: orderCode={}, status→{}", orderCode, newStatus);
        return shipmentMapper.toDto(saved);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Shipment loadShipment(UUID shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "shipmentId", shipmentId));
    }

    private Vendor loadVendor(UUID vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "vendorId", vendorId));
    }

    /**
     * Return the GHN shop ID for a vendor by their internal ID.
     * Falls back to the global {@code ghn.shop-id} if the vendor has not yet
     * registered their own store.
     */
    private Integer resolveShopId(UUID vendorId) {
        if (vendorId == null)
            return ghnProperties.getShopId();
        Vendor vendor = loadVendor(vendorId);
        return (vendor.getGhnShopId() != null) ? vendor.getGhnShopId() : ghnProperties.getShopId();
    }

    /** Convenience: extract the GHN shop ID from a Shipment's owning vendor. */
    private Integer vendorShopId(Shipment shipment) {
        Vendor vendor = shipment.getVendor();
        if (vendor != null && vendor.getGhnShopId() != null) {
            return vendor.getGhnShopId();
        }
        log.warn("Shipment {} vendor has no ghnShopId — falling back to global shopId",
                shipment.getShipmentId());
        return ghnProperties.getShopId();
    }

    private String url(String path) {
        return ghnProperties.getBaseUrl() + path;
    }

    /**
     * Build HTTP headers for a GHN request.
     *
     * @param shopId value to set in the {@code ShopId} header; pass {@code null} to
     *               omit it
     */
    private HttpHeaders buildHeaders(Integer shopId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnProperties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (shopId != null && shopId > 0) {
            headers.set("ShopId", String.valueOf(shopId));
        }
        return headers;
    }

    private <T> HttpEntity<T> buildEntity(T body, Integer shopId) {
        return new HttpEntity<>(body, buildHeaders(shopId));
    }

    private <T> T extractData(ResponseEntity<GhnApiResponse<T>> response, String operation) {
        GhnApiResponse<T> body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("GHN API returned empty response for: " + operation);
        }
        if (!Integer.valueOf(200).equals(body.code())) {
            log.error("GHN API error [{}]: code={}, message={}", operation, body.code(), body.message());
            throw new IllegalStateException("GHN API error (" + operation + "): " + body.message());
        }
        return body.data();
    }

    private GhnStoreResponse toStoreResponse(Object rawData) {
        if (rawData == null) {
            return null;
        }

        GhnStoreResponse mapped = GHN_MAPPER.convertValue(rawData, GhnStoreResponse.class);
        if (mapped != null && mapped.id() != null) {
            return mapped;
        }

        if (rawData instanceof Map<?, ?> rawMap) {
            Integer shopId = extractInteger(rawMap.get("shop_id"));
            if (shopId == null) {
                shopId = extractInteger(rawMap.get("id"));
            }
            if (shopId == null) {
                Object nested = rawMap.get("shop");
                if (nested instanceof Map<?, ?> nestedMap) {
                    shopId = extractInteger(nestedMap.get("_id"));
                    if (shopId == null) {
                        shopId = extractInteger(nestedMap.get("shop_id"));
                    }
                }
            }

            return new GhnStoreResponse(
                    shopId,
                    stringValue(rawMap.get("name")),
                    stringValue(rawMap.get("phone")),
                    stringValue(rawMap.get("address")),
                    stringValue(rawMap.get("ward_code")),
                    extractInteger(rawMap.get("district_id")),
                    extractInteger(rawMap.get("client_id")),
                    extractInteger(rawMap.get("status")),
                    stringValue(rawMap.get("version_no")));
        }

        return mapped;
    }

    private Integer extractInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer resolveToDistrictId(Address address) {
        String provinceName = (address.getCity() != null && !address.getCity().isBlank())
                ? address.getCity()
                : address.getProvince();
        if (provinceName == null || provinceName.isBlank()) {
            throw new IllegalStateException("Address city/province is required to resolve GHN district");
        }

        GhnProvinceDto province = getProvinces().stream()
                .filter(p -> normalizedEquals(p.provinceName(), provinceName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot map address province/city to GHN province: " + provinceName));

        return getDistricts(province.provinceId()).stream()
                .filter(d -> normalizedEquals(d.districtName(), address.getDistrict()))
                .map(GhnDistrictDto::districtId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot map address district to GHN district: " + address.getDistrict()));
    }

    private String resolveToWardCode(Address address, Integer districtId) {
        return getWards(districtId).stream()
                .filter(w -> normalizedEquals(w.wardName(), address.getWard()))
                .map(GhnWardDto::wardCode)
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException("Cannot map address ward to GHN ward: " + address.getWard()));
    }

    private GhnCreateShipmentOrderRequest mapClientToCreateOrderRequest(
            Shipment shipment,
            GhnCreateShipmentOrderClientRequest clientRequest) {

        if (clientRequest == null) {
            throw new IllegalArgumentException("ghnRequest is required");
        }

        Vendor vendor = shipment.getVendor();
        if (vendor == null || vendor.getVendorId() == null) {
            throw new IllegalStateException("Shipment has no vendor information");
        }

        Order order = shipment.getOrder();
        if (order == null) {
            throw new IllegalStateException("Shipment has no order information");
        }

        GhnStoreResponse store = getStoreInfo(vendor.getVendorId());
        if (store == null || store.districtId() == null || store.wardCode() == null) {
            throw new IllegalStateException("Vendor GHN store information is incomplete");
        }

        GhnDistrictDto senderDistrict = resolveDistrictById(store.districtId());
        String senderWardName = getWards(store.districtId()).stream()
                .filter(w -> normalizedEquals(w.wardCode(), store.wardCode()))
                .map(GhnWardDto::wardName)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot map vendor GHN ward code to ward name: " + store.wardCode()));

        List<OrderDetail> shipmentDetails = shipment.getOrderDetails() == null ? List.of() : shipment.getOrderDetails();
        if (shipmentDetails.isEmpty()) {
            throw new IllegalStateException("Shipment has no order details to create GHN order");
        }

        List<GhnItem> items = shipmentDetails.stream()
                .map(this::toGhnItem)
                .toList();

        int totalWeight = shipmentDetails.stream()
                .mapToInt(od -> safePositive(getProductWeight(od), 200) * safePositive(od.getQuantity(), 1))
                .sum();

        int length = shipmentDetails.stream()
                .mapToInt(od -> safePositive(getProductLength(od), 10))
                .max()
                .orElse(10);

        int width = shipmentDetails.stream()
                .mapToInt(od -> safePositive(getProductWidth(od), 10))
                .max()
                .orElse(10);

        int height = shipmentDetails.stream()
                .mapToInt(od -> safePositive(getProductHeight(od), 10))
                .max()
                .orElse(10);

        int insuranceValue = shipmentDetails.stream()
                .map(OrderDetail::getSubtotal)
                .filter(Objects::nonNull)
                .map(BigDecimal::intValue)
                .reduce(0, Integer::sum);

        String content = shipmentDetails.stream()
                .map(od -> {
                    if (od.getProductName() != null && !od.getProductName().isBlank()) {
                        return od.getProductName();
                    }
                    return od.getProduct() != null ? od.getProduct().getName() : "item";
                })
                .filter(Objects::nonNull)
                .distinct()
                .limit(5)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Product items");

        Integer toDistrictId = resolveToDistrictIdFromOrder(order);
        String toWardCode = resolveToWardCodeFromOrder(order, toDistrictId);
        boolean isCodPayment = "COD".equalsIgnoreCase(order.getPaymentMethod());
        int paymentTypeId = isCodPayment ? 2 : 1;
        int serviceTypeId = 2;
        int codAmount = isCodPayment ? toVndInt(order.getTotalAmount()) : 0;

        return new GhnCreateShipmentOrderRequest(
                firstNonBlankOrThrow(store.name(), vendor.getBusinessName(), "Sender name is missing"),
                firstNonBlankOrThrow(store.phone(), vendor.getPhoneNumber(), "Sender phone is missing"),
                firstNonBlankOrThrow(store.address(), vendor.getAddress(), "Sender address is missing"),
                senderWardName,
                senderDistrict.districtName(),
                senderDistrict.provinceId() == null
                        ? firstNonBlankOrThrow(vendor.getCity(), null, "Sender province/city is missing")
                        : resolveProvinceName(senderDistrict.provinceId()),
                firstNonBlankOrThrow(order.getRecipientName(), null, "Recipient name is missing"),
                firstNonBlankOrThrow(order.getRecipientPhone(), null, "Recipient phone is missing"),
                firstNonBlankOrThrow(order.getShippingAddress(), null, "Recipient address is missing"),
                toWardCode,
                toDistrictId,
                paymentTypeId,
                clientRequest.requiredNote() != null ? clientRequest.requiredNote() : "CHOTHUHANG",
                serviceTypeId,
                null,
                codAmount,
                Math.max(0, insuranceValue),
                Math.max(1, totalWeight),
                Math.max(1, length),
                Math.max(1, width),
                Math.max(1, height),
                shipment.getShipmentId().toString(),
                content,
                clientRequest.note(),
                items);
    }

    private GhnDistrictDto resolveDistrictById(Integer districtId) {
        if (districtId == null) {
            throw new IllegalStateException("District id is required");
        }

        for (GhnProvinceDto province : getProvinces()) {
            Optional<GhnDistrictDto> district = getDistricts(province.provinceId()).stream()
                    .filter(d -> Objects.equals(d.districtId(), districtId))
                    .findFirst();
            if (district.isPresent()) {
                return district.get();
            }
        }

        throw new IllegalStateException("Cannot map GHN district id: " + districtId);
    }

    private String resolveProvinceName(Integer provinceId) {
        return getProvinces().stream()
                .filter(p -> Objects.equals(p.provinceId(), provinceId))
                .map(GhnProvinceDto::provinceName)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot map GHN province id: " + provinceId));
    }

    private Integer resolveToDistrictIdFromOrder(Order order) {
        String districtName = firstNonBlankOrThrow(
                order.getRecipientDistrict(),
                null,
                "Recipient district is missing on order");
        String wardName = firstNonBlankOrThrow(
                order.getRecipientWard(),
                null,
                "Recipient ward is missing on order");

        List<Integer> districtCandidates = new ArrayList<>();
        for (GhnProvinceDto province : getProvinces()) {
            getDistricts(province.provinceId()).stream()
                    .filter(d -> normalizedEquals(d.districtName(), districtName))
                    .map(GhnDistrictDto::districtId)
                    .filter(Objects::nonNull)
                    .forEach(districtCandidates::add);
        }

        if (districtCandidates.isEmpty()) {
            throw new IllegalStateException("Cannot map order recipient district to GHN district: " + districtName);
        }

        if (districtCandidates.size() == 1) {
            return districtCandidates.get(0);
        }

        List<Integer> wardMatchedDistricts = districtCandidates.stream()
                .filter(districtId -> getWards(districtId).stream()
                        .anyMatch(w -> normalizedEquals(w.wardName(), wardName)))
                .toList();

        if (wardMatchedDistricts.size() == 1) {
            return wardMatchedDistricts.get(0);
        }

        throw new IllegalStateException(
                "Ambiguous GHN district mapping for order district '" + districtName
                        + "' and ward '" + wardName + "'");
    }

    private String resolveToWardCodeFromOrder(Order order, Integer districtId) {
        String wardName = firstNonBlankOrThrow(
                order.getRecipientWard(),
                null,
                "Recipient ward is missing on order");

        return getWards(districtId).stream()
                .filter(w -> normalizedEquals(w.wardName(), wardName))
                .map(GhnWardDto::wardCode)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot map order recipient ward to GHN ward: " + wardName));
    }

    private Address resolveRecipientAddress(Shipment shipment) {
        UUID accountId = shipment.getOrder() != null && shipment.getOrder().getAccount() != null
                ? shipment.getOrder().getAccount().getAccountId()
                : null;
        if (accountId == null) {
            throw new IllegalStateException("Shipment order account is missing; cannot resolve recipient address");
        }

        return addressRepository.findByTrainee_TraineeIdAndDefaultTrue(accountId)
                .or(() -> addressRepository.findByTrainee_TraineeId(accountId).stream().findFirst())
                .orElseThrow(() -> new IllegalStateException(
                        "No recipient address found for account " + accountId + "; cannot create GHN order"));
    }

    private GhnItem toGhnItem(OrderDetail orderDetail) {
        String name = defaultIfBlank(orderDetail.getProductName(),
                orderDetail.getProduct() != null ? orderDetail.getProduct().getName() : null);
        String code = orderDetail.getProduct() != null && orderDetail.getProduct().getProductId() != null
                ? orderDetail.getProduct().getProductId().toString()
                : orderDetail.getOrderDetailId().toString();

        return new GhnItem(
                name,
                code,
                safePositive(orderDetail.getQuantity(), 1),
                toVndInt(orderDetail.getUnitPrice()),
                safePositive(getProductLength(orderDetail), 10),
                safePositive(getProductWidth(orderDetail), 10),
                safePositive(getProductHeight(orderDetail), 10),
                safePositive(getProductWeight(orderDetail), 200));
    }

    private Integer getProductLength(OrderDetail orderDetail) {
        return orderDetail.getProduct() != null ? orderDetail.getProduct().getLength() : null;
    }

    private Integer getProductWidth(OrderDetail orderDetail) {
        return orderDetail.getProduct() != null ? orderDetail.getProduct().getWidth() : null;
    }

    private Integer getProductHeight(OrderDetail orderDetail) {
        return orderDetail.getProduct() != null ? orderDetail.getProduct().getHeight() : null;
    }

    private Integer getProductWeight(OrderDetail orderDetail) {
        return orderDetail.getProduct() != null ? orderDetail.getProduct().getWeight() : null;
    }

    private int toVndInt(BigDecimal value) {
        return value == null ? 0 : Math.max(0, value.intValue());
    }

    private int safePositive(Integer value, int fallback) {
        if (value == null || value <= 0) {
            return fallback;
        }
        return value;
    }

    private String defaultIfBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "";
    }

    private String firstNonBlankOrThrow(String primary, String fallback, String errorMessage) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        throw new IllegalStateException(errorMessage);
    }

    private boolean normalizedEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return normalizeLocation(a).equals(normalizeLocation(b));
    }

    private String normalizeLocation(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        normalized = normalized
                .replace("tp.", "")
                .replace("thanh pho", "")
                .replace("quan", "")
                .replace("huyen", "")
                .replace("thi xa", "")
                .replace("phuong", "")
                .replace("xa", "")
                .replace("thi tran", "");
        return normalized.replaceAll("\\s+", " ").trim();
    }

    private void propagateStatusToDetails(Shipment shipment, OrderDetailStatus status) {
        if (shipment.getOrderDetails() == null || shipment.getOrderDetails().isEmpty())
            return;
        shipment.getOrderDetails().forEach(od -> od.setStatus(status));
        orderDetailRepository.saveAll(shipment.getOrderDetails());
    }

    /**
     * Build the snake_case Map sent to GHN's create-order endpoint.
     * We use a Map rather than a separate DTO class to avoid yet another
     * mapping layer; Jackson serialises it as a standard JSON object.
     */
    private Map<String, Object> buildCreateOrderBody(Shipment shipment, GhnCreateShipmentOrderRequest req) {
        Map<String, Object> body = new LinkedHashMap<>();

        // Our reference code so GHN can echo it back
        body.put("client_order_code", shipment.getShipmentId().toString());

        // Sender (vendor store)
        body.put("from_name", req.fromName());
        body.put("from_phone", req.fromPhone());
        body.put("from_address", req.fromAddress());
        body.put("from_ward_name", req.fromWardName());
        body.put("from_district_name", req.fromDistrictName());
        body.put("from_province_name", req.fromProvinceName());

        // Recipient (customer)
        body.put("to_name", req.toName());
        body.put("to_phone", req.toPhone());
        body.put("to_address", req.toAddress());
        body.put("to_ward_code", req.toWardCode());
        body.put("to_district_id", req.toDistrictId());

        // Shipment parameters
        body.put("payment_type_id", req.paymentTypeId() != null ? req.paymentTypeId() : 1);
        body.put("required_note", req.requiredNote() != null ? req.requiredNote() : "CHOTHUHANG");
        body.put("service_type_id", req.serviceTypeId() != null ? req.serviceTypeId() : 2);
        body.put("cod_amount", req.codAmount() != null ? req.codAmount() : 0);
        body.put("insurance_value", req.insuranceValue() != null ? req.insuranceValue() : 0);
        body.put("weight", req.weight());
        body.put("length", req.length());
        body.put("width", req.width());
        body.put("height", req.height());

        if (req.note() != null && !req.note().isBlank()) {
            body.put("note", req.note());
        }
        if (req.items() != null && !req.items().isEmpty()) {
            body.put("items", req.items());
        }

        return body;
    }

    /**
     * Map a GHN status string to our internal {@link ShipmentStatus}.
     * Returns {@code null} for statuses we intentionally ignore.
     */
    private ShipmentStatus mapGhnStatus(String ghnStatus) {
        if (ghnStatus == null)
            return null;
        return switch (ghnStatus.toLowerCase()) {
            case "ready_to_pick" -> ShipmentStatus.READY_TO_PICK;
            case "picking" -> ShipmentStatus.PICKING;
            case "picked" -> ShipmentStatus.PICKED;
            case "storing" -> ShipmentStatus.STORING;
            case "transporting", "sorting" -> ShipmentStatus.TRANSPORTING;
            case "delivering" -> ShipmentStatus.DELIVERING;
            case "delivered" -> ShipmentStatus.DELIVERED;
            case "delivery_fail" -> ShipmentStatus.DELIVERY_FAIL;
            case "waiting_to_return" -> ShipmentStatus.RETURNING;
            case "return" -> ShipmentStatus.RETURN;
            case "returned" -> ShipmentStatus.RETURNED;
            case "cancel" -> ShipmentStatus.CANCELLED;
            default -> null;
        };
    }

    private void syncOrderStatusWithShipment(Shipment shipment) {
        Order order = shipment.getOrder();
        if (order == null) {
            return;
        }

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.REFUNDED) {
            return;
        }

        OrderStatus targetStatus = switch (shipment.getStatus()) {
            case DRAFT -> OrderStatus.CONFIRMED;
            case READY_TO_PICK, PICKING, STORING, TRANSPORTING -> OrderStatus.READY;
            case PICKED, DELIVERING -> OrderStatus.SHIPPED;
            case DELIVERED -> OrderStatus.DELIVERED;
            case DELIVERY_FAIL -> OrderStatus.FAILED_DELIVERY;
            case RETURN, RETURNING, RETURNED -> OrderStatus.RETURNED;
            case CANCELLED -> OrderStatus.CANCELLED;
        };

        if (order.getStatus() != targetStatus) {
            order.setStatus(targetStatus);
            orderRepository.save(order);
            log.info("Order {} status synced to {} from shipment {}", order.getOrderId(), targetStatus,
                    shipment.getShipmentId());
        }
    }

    /**
     * Recalculate and persist the parent order's {@code totalAmount}.
     * total = sum(orderDetail.subtotal) + order.shippingFee - order.discountAmount.
     */
    private void recalculateOrderTotal(Order order) {
        if (order == null)
            return;
        BigDecimal itemsSubtotal = order.getOrderDetails() == null ? BigDecimal.ZERO
                : order.getOrderDetails().stream()
                        .map(OrderDetail::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        order.setTotalAmount(itemsSubtotal.add(shippingFee).subtract(discount));
        orderRepository.save(order);
        log.info("Order {} total recalculated to {} (items={}, shippingFee={}, discount={})",
                order.getOrderId(), order.getTotalAmount(), itemsSubtotal, shippingFee, discount);
    }
}
