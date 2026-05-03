package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.OrderDto;
import fpt.edu.sep490.pilahub.dto.request.order.CancelOrderRequest;
import fpt.edu.sep490.pilahub.dto.request.order.CreateOrderRequest;
import fpt.edu.sep490.pilahub.dto.request.order.OrderItemRequest;
import fpt.edu.sep490.pilahub.dto.request.order.SearchOrderRequest;
import fpt.edu.sep490.pilahub.dto.request.order.VendorShippingRequest;
import fpt.edu.sep490.pilahub.enums.*;
import fpt.edu.sep490.pilahub.event.NotificationEvent;
import fpt.edu.sep490.pilahub.exception.InvalidRequestException;
import fpt.edu.sep490.pilahub.exception.InsufficientBalanceException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.OrderMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.Order;
import fpt.edu.sep490.pilahub.pojo.OrderDetail;
import fpt.edu.sep490.pilahub.pojo.Product;
import fpt.edu.sep490.pilahub.pojo.Shipment;
import fpt.edu.sep490.pilahub.pojo.Transaction;
import fpt.edu.sep490.pilahub.pojo.Vendor;
import fpt.edu.sep490.pilahub.pojo.Wallet;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.OrderDetailRepository;
import fpt.edu.sep490.pilahub.repository.OrderRepository;
import fpt.edu.sep490.pilahub.repository.ProductRepository;
import fpt.edu.sep490.pilahub.repository.ShipmentRepository;
import fpt.edu.sep490.pilahub.repository.TransactionRepository;
import fpt.edu.sep490.pilahub.repository.VendorRepository;
import fpt.edu.sep490.pilahub.repository.WalletRepository;
import fpt.edu.sep490.pilahub.repository.AddressRepository;
import fpt.edu.sep490.pilahub.pojo.Address;
import fpt.edu.sep490.pilahub.service.OrderService;
import fpt.edu.sep490.pilahub.service.ShipmentService;
import fpt.edu.sep490.pilahub.service.SystemConfigService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import fpt.edu.sep490.pilahub.enums.OrderStatus;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.enums.ShippingProvider;
import fpt.edu.sep490.pilahub.enums.ShipmentStatus;
import fpt.edu.sep490.pilahub.enums.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final EntityManager entityManager;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final VendorRepository vendorRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentService shipmentService;
    private final SystemConfigService systemConfigService;
    private final SecurityUtil securityUtil;
    private final ApplicationEventPublisher eventPublisher;
    private final AddressRepository addressRepository;

    // ========== 1. CORE CRUD OPERATIONS ==========

    @Override
    @Transactional
    public List<OrderDto> createOrder(UUID accountId, CreateOrderRequest request) {
        log.info("Placing order for account: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        // ── 1. Validate all items and group by vendor ─────────────────────────
        record ItemWithProduct(OrderItemRequest item, Product product) {
        }
        Map<UUID, List<ItemWithProduct>> byVendor = new LinkedHashMap<>();
        Map<UUID, Integer> requestedQtyByProduct = new HashMap<>();

        for (OrderItemRequest item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Không tìm thấy sản phẩm có ID: " + item.productId()));

            validateProductExpiryForOrder(product);

            validateInstallationRequest(product, item.installationRequest(), request.shippingAddress());

            if (product.getVendor() == null) {
                throw new InvalidRequestException("Không tìm thấy nhà cung cấp cho " + product.getName());
            }

            int totalRequestedQty = requestedQtyByProduct.merge(
                    product.getProductId(),
                    item.quantity(),
                    Integer::sum);

            if (product.getStockQuantity() != null && product.getStockQuantity() < totalRequestedQty) {
                throw new InvalidRequestException("Không đủ hàng cho sản phẩm: " + product.getName());
            }

            byVendor.computeIfAbsent(product.getVendor().getVendorId(), k -> new ArrayList<>())
                    .add(new ItemWithProduct(item, product));
        }

        // ── 2. Compute per-vendor subtotals and grand total ───────────────────
        Map<UUID, BigDecimal> vendorSubtotals = new LinkedHashMap<>();
        BigDecimal grandSubtotal = BigDecimal.ZERO;
        for (Map.Entry<UUID, List<ItemWithProduct>> entry : byVendor.entrySet()) {
            BigDecimal vendorSubtotal = entry.getValue().stream()
                    .map(iwp -> {
                        BigDecimal up = BigDecimal.valueOf(iwp.product().getPrice());
                        BigDecimal sub = up.multiply(BigDecimal.valueOf(iwp.item().quantity()));
                        BigDecimal disc = iwp.item().discountAmount() != null ? iwp.item().discountAmount()
                                : BigDecimal.ZERO;
                        return sub.subtract(disc);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vendorSubtotals.put(entry.getKey(), vendorSubtotal);
            grandSubtotal = grandSubtotal.add(vendorSubtotal);
        }

        Map<UUID, BigDecimal> vendorShippingFees = request.vendorShippings().stream()
                .collect(Collectors.toMap(
                        VendorShippingRequest::vendorId,
                        vs -> vs.shippingFee() != null ? vs.shippingFee() : BigDecimal.ZERO,
                        (existing, replacement) -> {
                            throw new IllegalArgumentException("Duplicate vendorId in vendorShippings");
                        },
                        LinkedHashMap::new));

        for (UUID vendorId : byVendor.keySet()) {
            if (!vendorShippingFees.containsKey(vendorId)) {
                throw new IllegalArgumentException("Missing shipping fee for vendor: " + vendorId);
            }
        }

        for (UUID vendorId : vendorShippingFees.keySet()) {
            if (!byVendor.containsKey(vendorId)) {
                throw new IllegalArgumentException("Shipping fee provided for vendor not in order items: " + vendorId);
            }
        }

        BigDecimal totalShippingFee = vendorShippingFees.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal orderLevelDiscount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
        BigDecimal grandTotal = grandSubtotal.add(totalShippingFee).subtract(orderLevelDiscount);

        String paymentMethod = request.paymentMethod();
        boolean isCodPayment = "COD".equalsIgnoreCase(paymentMethod);

        // Resolve recipient ward/district from addressId (if provided)
        String resolvedRecipientWard = null;
        String resolvedRecipientDistrict = null;
        if (request.addressId() != null) {
            Address address = addressRepository.findById(request.addressId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Address not found with ID: " + request.addressId()));
            resolvedRecipientWard = address.getWard();
            resolvedRecipientDistrict = address.getDistrict();
        }

        // ── 4. Create one Order per vendor ────────────────────────────────────
        record OrderAndVendor(Order order, Vendor vendor) {
        }
        List<OrderAndVendor> created = new ArrayList<>();

        for (Map.Entry<UUID, List<ItemWithProduct>> entry : byVendor.entrySet()) {
            List<ItemWithProduct> items = entry.getValue();
            Vendor vendor = items.get(0).product().getVendor();

            // Distribute order-level discount proportionally across vendor orders
            BigDecimal vendorSubtotal = vendorSubtotals.get(entry.getKey());
            BigDecimal vendorDiscount = grandSubtotal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : orderLevelDiscount.multiply(vendorSubtotal).divide(grandSubtotal, 2, RoundingMode.HALF_UP);
            BigDecimal vendorShippingFee = vendorShippingFees.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            BigDecimal vendorTotal = vendorSubtotal.add(vendorShippingFee).subtract(vendorDiscount);

            List<OrderDetail> orderDetails = new ArrayList<>();
            for (ItemWithProduct iwp : items) {
                BigDecimal unitPrice = BigDecimal.valueOf(iwp.product().getPrice());
                BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(iwp.item().quantity()));
                BigDecimal itemDiscount = iwp.item().discountAmount() != null ? iwp.item().discountAmount()
                        : BigDecimal.ZERO;
                itemSubtotal = itemSubtotal.subtract(itemDiscount);

                OrderDetail detail = OrderDetail.builder()
                        .product(iwp.product())
                        .status(OrderDetailStatus.PENDING)
                        .quantity(iwp.item().quantity())
                        .unitPrice(unitPrice)
                        .subtotal(itemSubtotal)
                        .discountAmount(itemDiscount)
                        .installationRequest(Boolean.TRUE.equals(iwp.item().installationRequest()))
                        .productName(iwp.product().getName())
                        .productImageUrl(iwp.product().getImageUrl())
                        .build();
                orderDetails.add(detail);
            }

            Order order = Order.builder()
                    .account(account)
                    .status(OrderStatus.PENDING)
                    .totalAmount(vendorTotal)
                    .discountAmount(vendorDiscount)
                    .shippingFee(vendorShippingFee)
                    .recipientName(request.recipientName())
                    .recipientPhone(request.recipientPhone())
                    .shippingAddress(request.shippingAddress())
                    .recipientWard(resolvedRecipientWard)
                    .recipientDistrict(resolvedRecipientDistrict)
                    .notes(request.notes())
                    .orderNumber(generateOrderNumber())
                    .paymentMethod(request.paymentMethod())
                    .paid(false)
                    .build();

            for (OrderDetail detail : orderDetails) {
                detail.setOrder(order);
                order.getOrderDetails().add(detail);
            }

            Order saved = orderRepository.save(order);
            created.add(new OrderAndVendor(saved, vendor));
            log.info("Vendor order created: orderId={}, orderNumber={}, vendor={}",
                    saved.getOrderId(), saved.getOrderNumber(), vendor.getVendorId());
        }

        // ── 3. Validate wallet balance upfront ────────────────────────────────
        Wallet customerWallet = null;
        if (!isCodPayment) {
            customerWallet = walletRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for account ID: " + accountId));
            if (!customerWallet.isActive()) {
                throw new IllegalStateException("Your wallet is not active. Please contact support.");
            }
            if (customerWallet.getAvailableVND().compareTo(grandTotal) < 0) {
                throw new InsufficientBalanceException(
                        String.format("Insufficient balance. Required: %s VND, Available: %s VND",
                                grandTotal, customerWallet.getAvailableVND()));
            }
        }

        // ── 5. Deduct wallet balance once for the combined grand total ─────────
        if (!isCodPayment) {
            customerWallet.setAvailableVND(customerWallet.getAvailableVND().subtract(grandTotal));
            customerWallet.setBalanceVND(customerWallet.getBalanceVND().subtract(grandTotal));
            walletRepository.save(customerWallet);
        }

        deductProductStocks(requestedQtyByProduct);

        for (OrderAndVendor oav : created) {
            Order saved = oav.order();
            if (!isCodPayment) {
                saved.setPaid(true);
                saved.setPaidAt(Instant.now());
                orderRepository.save(saved);
            }

            transactionRepository.save(Transaction.builder()
                    .transactionType(TransactionType.ORDER)
                    .amount(saved.getTotalAmount())
                    .accountId(accountId)
                    .referenceId(saved.getOrderId())
                    .description("Thanh toán đơn hàng: " + saved.getOrderNumber())
                    .build());

            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    saved.getAccount().getAccountId(),
                    NotificationType.ORDER,
                    "Đặt Hàng Thành Công",
                    "Đơn hàng của bạn đã được đặt thành công. Mã đơn: " + saved.getOrderNumber(),
                    saved.getOrderId(),
                    "ORDER"));

            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    oav.vendor().getVendorId(),
                    NotificationType.ORDER,
                    "Đơn Hàng Mới",
                    "Vui lòng xác nhận đơn hàng trong vòng 24 giờ. Mã đơn: " + saved.getOrderNumber(),
                    saved.getOrderId(),
                    "ORDER"));
        }

        log.info("Placed {} order(s) for account: {}", created.size(), accountId);

        return orderMapper.toDtoList(created.stream().map(OrderAndVendor::order).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        return orderMapper.toDtoList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByAccountId(UUID accountId) {
        List<Order> orders = orderRepository.findByAccount_AccountIdOrderByCreatedAtDesc(accountId);
        return orderMapper.toDtoList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getUnpaidNonCodOrdersByAccountId(UUID accountId) {
        List<Order> orders = orderRepository.findUnpaidNonCodOrdersByAccountId(accountId);
        return orderMapper.toDtoList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByAccountIdAndStatus(UUID accountId, OrderStatus status) {
        List<Order> orders = orderRepository.findByAccount_AccountIdAndStatusOrderByCreatedAtDesc(accountId, status);
        return orderMapper.toDtoList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByVendorId(UUID vendorId) {
        List<Order> orders = orderRepository.findDistinctByVendorResponsibility(vendorId);
        return orderMapper.toDtoList(orders);
    }

    @Override
    @Transactional
    public void deleteOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        orderRepository.delete(order);
        log.info("Order deleted successfully: {}", orderId);
    }

    // ========== 1.1. SEARCH AND PAGINATION ==========

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> searchOrders(SearchOrderRequest searchRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> query = cb.createQuery(Order.class);
        Root<Order> root = query.from(Order.class);

        List<Predicate> predicates = new ArrayList<>();

        // Keyword search (order number, recipient name, phone, address)
        if (searchRequest.keyword() != null && !searchRequest.keyword().isBlank()) {
            String keyword = "%" + searchRequest.keyword().toLowerCase() + "%";
            Predicate keywordPredicate = cb.or(
                    cb.like(cb.lower(root.get("orderNumber")), keyword),
                    cb.like(cb.lower(root.get("recipientName")), keyword),
                    cb.like(cb.lower(root.get("recipientPhone")), keyword),
                    cb.like(cb.lower(root.get("shippingAddress")), keyword));
            predicates.add(keywordPredicate);
        }

        // Filter by account ID
        if (searchRequest.accountId() != null) {
            predicates.add(cb.equal(root.get("account").get("accountId"), searchRequest.accountId()));
        }

        // Filter by status
        if (searchRequest.status() != null) {
            predicates.add(cb.equal(root.get("status"), searchRequest.status()));
        }

        // Filter by payment status
        if (searchRequest.isPaid() != null) {
            predicates.add(cb.equal(root.get("paid"), searchRequest.isPaid()));
        }

        // Filter by payment method
        if (searchRequest.paymentMethod() != null && !searchRequest.paymentMethod().isBlank()) {
            predicates.add(cb.equal(root.get("paymentMethod"), searchRequest.paymentMethod()));
        }

        // Filter by date range
        if (searchRequest.startDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), searchRequest.startDate()));
        }
        if (searchRequest.endDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), searchRequest.endDate()));
        }

        // Filter by amount range
        if (searchRequest.minAmount() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("totalAmount"), searchRequest.minAmount()));
        }
        if (searchRequest.maxAmount() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("totalAmount"), searchRequest.maxAmount()));
        }

        query.where(predicates.toArray(new Predicate[0]));

        // Count total results
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Order> countRoot = countQuery.from(Order.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(predicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        // Apply pagination and sorting
        List<Order> results = entityManager.createQuery(query)
                .setFirstResult((int) searchRequest.getPageable().getOffset())
                .setMaxResults(searchRequest.getPageable().getPageSize())
                .getResultList();

        List<OrderDto> dtos = orderMapper.toDtoList(results);
        return new PageImpl<>(dtos, searchRequest.getPageable(), total);
    }

    // ========== 2. STATUS MANAGEMENT ==========

    @Override
    @Transactional
    public OrderDto updateOrderStatus(UUID orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // validate
        if (status == OrderStatus.CONFIRMED
                && securityUtil.getCurrentUserRole() == Role.VENDOR
                && !"COD".equalsIgnoreCase(order.getPaymentMethod())
                && !order.isPaid()) {
            throw new IllegalStateException("Cannot confirm unpaid non-COD order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update a cancelled order");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update a completed order");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);

        switch (status) {

            case DELIVERED -> {
                order.setPaid(true);
                if (order.getPaidAt() == null) {
                    order.setPaidAt(Instant.now());
                }
            }

            case CANCELLED -> {
                order.setCancelledAt(Instant.now());
                updateAllOrderDetailsStatus(order, OrderDetailStatus.CANCELLED);
            }
        }

        Order updatedOrder = orderRepository.save(order);

        if (oldStatus != OrderStatus.CONFIRMED && status == OrderStatus.CONFIRMED) {
            shipmentService.createShipmentForOrder(orderId);
        }

        log.info("Order {} status changed: {} -> {}", orderId, oldStatus, status);

        return orderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(UUID orderId, CancelOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!(order.getStatus() == OrderStatus.PENDING)) {
            throw new IllegalStateException("Order can not cancel");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setCancellationReason(request.cancellationReason());

        // Cancel all order items
        updateAllOrderDetailsStatus(order, OrderDetailStatus.CANCELLED);

        // If payment method is not COD, return order amount to customer wallet.
        boolean isCodPayment = "COD".equalsIgnoreCase(order.getPaymentMethod());
        UUID accountId = order.getAccount().getAccountId();

        if (!isCodPayment) {
            Wallet customerWallet = walletRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for account: " + accountId));

            BigDecimal refundAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0
                    && !transactionRepository.existsByReferenceIdAndTransactionType(order.getOrderId(),
                            TransactionType.REFUND)) {
                customerWallet.setAvailableVND(customerWallet.getAvailableVND().add(refundAmount));
                customerWallet.setBalanceVND(customerWallet.getBalanceVND().add(refundAmount));
                walletRepository.save(customerWallet);
            }
        }

        transactionRepository.save(Transaction.builder()
                .transactionType(TransactionType.REFUND)
                .amount(order.getTotalAmount())
                .accountId(accountId)
                .referenceId(order.getOrderId())
                .description("Refund for cancelled order: " + order.getOrderNumber())
                .build());

        Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled: {}, Reason: {}. All items cancelled.", orderId, request.cancellationReason());

        return orderMapper.toDto(cancelledOrder);
    }

    @Override
    @Transactional
    public OrderDto returnOrder(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot return a cancelled order");
        }

        if (order.getStatus() == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Order is already refunded");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Can only return DELIVERED orders");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("You cannot return a completed order");
        }

        Instant now = Instant.now();
        for (Shipment shipment : order.getShipments()) {
            if ((shipment.getStatus() == ShipmentStatus.DELIVERED
                    || shipment.getStatus() == ShipmentStatus.RETURN
                    || shipment.getStatus() == ShipmentStatus.RETURNING)
                    && shipment.getReturnDeadline() != null
                    && now.isAfter(shipment.getReturnDeadline())) {
                throw new IllegalStateException("Return deadline has passed for shipment: " + shipment.getShipmentId());
            }
        }

        order.setStatus(OrderStatus.RETURNED);
        order.setCancellationReason(reason);

        for (OrderDetail detail : order.getOrderDetails()) {
            if (detail.getStatus() == OrderDetailStatus.DELIVERED
                    || detail.getStatus() == OrderDetailStatus.COMPLETED) {
                detail.setStatus(OrderDetailStatus.RETURNED);
            }
        }

        for (Shipment shipment : order.getShipments()) {
            if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
                shipment.setStatus(ShipmentStatus.RETURN);
            }
        }

        Order returnedOrder = orderRepository.save(order);
        log.info("Order returned: {}, Reason: {}", orderId, reason);
        return orderMapper.toDto(returnedOrder);
    }

    @Override
    @Transactional
    public OrderDto completeOrderForTrainee(UUID orderId) {
        UUID accountId = securityUtil.getCurrentUserId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getAccount().getAccountId().equals(accountId)) {
            throw new IllegalStateException("You can only complete your own order");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            return orderMapper.toDto(order);
        }

        if (order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.RETURNED
                || order.getStatus() == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Cannot complete order in status: " + order.getStatus());
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Only DELIVERED orders can be completed by trainee");
        }

        for (OrderDetail detail : order.getOrderDetails()) {
            if (detail.getStatus() == OrderDetailStatus.DELIVERED) {
                detail.setStatus(OrderDetailStatus.COMPLETED);
            }
        }

        order.setStatus(OrderStatus.COMPLETED);

        Vendor vendor = order.getShipments().get(0).getVendor();
        if (vendor == null) {
            throw new IllegalStateException("Order shipment has no vendor");
        }

        double feeRate = vendor.getPlatformFeePercentage() != null
                ? vendor.getPlatformFeePercentage()
                : systemConfigService.getDefaultPlatformFeePercentage();

        BigDecimal orderGross = order.getTotalAmount().subtract(order.getShippingFee());
        BigDecimal orderFee = orderGross.multiply(BigDecimal.valueOf(feeRate / 100.0));
        BigDecimal orderNet = orderGross.subtract(orderFee);

        String emailAdmin = systemConfigService.getEmailAdmin();
        UUID adminId = accountRepository.findByEmail(emailAdmin)
                .map(Account::getAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        transactionRepository.save(Transaction.builder()
                .transactionType(TransactionType.VENDOR_EARNING)
                .amount(orderNet)
                .accountId(adminId)
                .referenceId(order.getOrderId())
                .description(String.format(
                        "Shop được hưởng từ đơn hàng | Đơn hàng: %s | %s VND",
                        order.getOrderNumber(),
                        orderNet))
                .build());

        transactionRepository.save(Transaction.builder()
                .transactionType(TransactionType.PLATFORM_FEE)
                .amount(orderFee)
                .accountId(adminId)
                .referenceId(order.getOrderId())
                .description(String.format(
                        "Phí nền tảng | Đơn hàng: %s | %s VND",
                        order.getOrderNumber(),
                        orderFee))
                .build());

        TransactionType type = TransactionType.SHIPPING_FEE_VENDOR;

        if (order.getShipments().get(0).getShippingProvider() != ShippingProvider.SELF) {
            type = TransactionType.SHIPPING_FEE_THIRD_PARTY;
        }

        transactionRepository.save(Transaction.builder()
                .transactionType(type)
                .amount(order.getShippingFee())
                .accountId(adminId)
                .referenceId(order.getOrderId())
                .description(String.format(
                        "Phí giao hàng | Đơn hàng: %s | %s VND",
                        order.getOrderNumber(),
                        order.getShippingFee()))
                .build());

        Instant returnDeadline = Instant.now();
        for (Shipment shipment : order.getShipments()) {
            if (shipment.getReturnDeadline() == null) {
                shipment.setReturnDeadline(returnDeadline);
            }
        }

        Order saved = orderRepository.save(order);
        log.info("Order {} completed by trainee {}", orderId, accountId);
        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrderDto processRefund(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (transactionRepository.existsByReferenceIdAndTransactionType(orderId, TransactionType.REFUND)) {
            throw new IllegalStateException("Refund has already been processed for this order");
        }

        if (order.getStatus() == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Order is already refunded");
        }

        if (order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.RETURNED) {
            throw new IllegalStateException("Refund can only be processed for CANCELLED or RETURNED orders");
        }

        UUID accountId = order.getAccount().getAccountId();
        Wallet customerWallet = walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for account: " + accountId));

        BigDecimal refundAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Refund amount must be greater than 0");
        }

        customerWallet.setAvailableVND(customerWallet.getAvailableVND().add(refundAmount));
        customerWallet.setBalanceVND(customerWallet.getBalanceVND().add(refundAmount));
        walletRepository.save(customerWallet);

        transactionRepository.save(Transaction.builder()
                .transactionType(TransactionType.REFUND)
                .amount(refundAmount)
                .accountId(accountId)
                .referenceId(order.getOrderId())
                .description("Refund for order: " + order.getOrderNumber())
                .build());

        for (OrderDetail detail : order.getOrderDetails()) {
            if (detail.getStatus() != OrderDetailStatus.REFUNDED) {
                detail.setStatus(OrderDetailStatus.REFUNDED);
            }
        }
        orderDetailRepository.saveAll(order.getOrderDetails());

        for (Shipment shipment : order.getShipments()) {
            if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
                shipment.setStatus(ShipmentStatus.CANCELLED);
            } else {
                shipment.setStatus(ShipmentStatus.RETURNED);
            }
        }
        shipmentRepository.saveAll(order.getShipments());

        order.setStatus(OrderStatus.REFUNDED);
        Order refundedOrder = orderRepository.save(order);

        log.info("Refund of {} VND processed for order {} (account {})", refundAmount, orderId, accountId);
        return orderMapper.toDto(refundedOrder);
    }

    // ========== 3. ORDER ITEMS MANAGEMENT ==========

    @Override
    @Transactional
    public OrderDto addItemToOrder(UUID orderId, OrderItemRequest itemRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only add items to orders with PENDING status");
        }

        Product product = productRepository.findById(itemRequest.productId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product not found with ID: " + itemRequest.productId()));

        validateInstallationRequest(product, itemRequest.installationRequest(), order.getShippingAddress());

        // Check if product already exists in order
        boolean productExists = order.getOrderDetails().stream()
                .anyMatch(detail -> detail.getProduct().getProductId().equals(itemRequest.productId()));

        if (productExists) {
            throw new IllegalStateException("Product already exists in order. Use update quantity instead.");
        }

        BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
        BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));
        BigDecimal itemDiscount = itemRequest.discountAmount() != null ? itemRequest.discountAmount() : BigDecimal.ZERO;
        itemSubtotal = itemSubtotal.subtract(itemDiscount);

        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(product)
                .quantity(itemRequest.quantity())
                .unitPrice(unitPrice)
                .subtotal(itemSubtotal)
                .discountAmount(itemDiscount)
                .installationRequest(Boolean.TRUE.equals(itemRequest.installationRequest()))
                .productName(product.getName())
                .productImageUrl(product.getImageUrl())
                .build();

        order.getOrderDetails().add(orderDetail);

        // Recalculate total
        recalculateTotals(order);

        Order updatedOrder = orderRepository.save(order);
        log.info("Item added to order: {}", orderId);

        return orderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto removeItemFromOrder(UUID orderId, UUID orderDetailId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only remove items from orders with PENDING status");
        }

        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Order detail not found with ID: " + orderDetailId));

        if (!orderDetail.getOrder().getOrderId().equals(orderId)) {
            throw new IllegalStateException("Order detail does not belong to this order");
        }

        if (order.getOrderDetails().size() == 1) {
            throw new IllegalStateException("Cannot remove the last item from order. Cancel the order instead.");
        }

        order.getOrderDetails().remove(orderDetail);
        orderDetailRepository.delete(orderDetail);

        // Recalculate total
        recalculateTotals(order);

        Order updatedOrder = orderRepository.save(order);
        log.info("Item removed from order: {}", orderId);

        return orderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto updateOrderItemQuantity(UUID orderId, UUID orderDetailId, Integer newQuantity) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only update items in orders with PENDING status");
        }

        if (newQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Order detail not found with ID: " + orderDetailId));

        if (!orderDetail.getOrder().getOrderId().equals(orderId)) {
            throw new IllegalStateException("Order detail does not belong to this order");
        }

        orderDetail.setQuantity(newQuantity);
        BigDecimal newSubtotal = orderDetail.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity))
                .subtract(orderDetail.getDiscountAmount());
        orderDetail.setSubtotal(newSubtotal);

        // Recalculate total
        recalculateTotals(order);

        Order updatedOrder = orderRepository.save(order);
        log.info("Item quantity updated in order: {}", orderId);

        return orderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderTracking(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return orderMapper.toDto(order);
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Generate a unique order number in format: ORD-YYYYMMDD-XXXX
     */
    // ← payWithWallet removed: wallet deduction is now done inline in createOrder
    private String generateOrderNumber() {
        String datePrefix = "ORD-" + LocalDate.now().toString().replace("-", "");
        String orderNumber;
        int counter = 1;

        do {
            orderNumber = String.format("%s-%04d", datePrefix, counter);
            counter++;
        } while (orderRepository.existsByOrderNumber(orderNumber));

        return orderNumber;
    }

    /**
     * Recalculate order totals based on order details
     */
    private void recalculateTotals(Order order) {
        BigDecimal subtotal = order.getOrderDetails().stream()
                .map(OrderDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        order.setTotalAmount(subtotal.add(shippingFee).subtract(discount));
    }

    private void deductProductStocks(Map<UUID, Integer> requestedQtyByProduct) {
        for (Map.Entry<UUID, Integer> entry : requestedQtyByProduct.entrySet()) {
            UUID productId = entry.getKey();
            int quantityToDeduct = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

            if (product.getStockQuantity() == null) {
                continue;
            }

            int remainingStock = product.getStockQuantity() - quantityToDeduct;
            if (remainingStock < 0) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(remainingStock);
            productRepository.save(product);
        }
    }

    private void validateInstallationRequest(Product product, Boolean installationRequest, String shippingAddress) {
        if (!Boolean.TRUE.equals(installationRequest)) {
            return;
        }

        if (!product.isInstallationSupported()) {
            throw new InvalidRequestException(
                    "Installation request is not valid because product does not support installation");
        }

        if (!isShippingAddressInSupportedRegion(product.getRegionSupported(), shippingAddress)) {
            throw new InvalidRequestException(
                    "Installation request is not valid because shipping address is outside supported regions");
        }
    }

    private boolean isShippingAddressInSupportedRegion(String[] supportedRegions, String shippingAddress) {
        if (supportedRegions == null || supportedRegions.length == 0 || shippingAddress == null) {
            return false;
        }

        String normalizedAddress = shippingAddress.trim().toLowerCase();
        if (normalizedAddress.isEmpty()) {
            return false;
        }

        for (String region : supportedRegions) {
            if (region == null) {
                continue;
            }

            String normalizedRegion = region.trim().toLowerCase();
            if (!normalizedRegion.isEmpty() && normalizedAddress.contains(normalizedRegion)) {
                return true;
            }
        }

        return false;
    }

    private void validateProductExpiryForOrder(Product product) {
        Instant expiredDate = product.getExpiredDate();
        if (expiredDate == null) {
            if (product.getCategoryType() == CategoryType.SUPPLEMENT) {
                throw new InvalidRequestException("Product " + product.getName() + " has no expired date");
            }
            return;
        }

        int minRequiredMonths = systemConfigService.getActiveProductMinRequiredMonths();
        Instant minAllowedExpiredDate = Instant.now()
                .atOffset(ZoneOffset.UTC)
                .plusMonths(minRequiredMonths)
                .toInstant();

        if (expiredDate.isBefore(minAllowedExpiredDate)) {
            throw new InvalidRequestException(
                    "Product " + product.getName() + " does not meet minimum remaining shelf life of "
                            + minRequiredMonths + " month(s)");
        }
    }

    /**
     * Update all order detail statuses to the given status
     */
    private void updateAllOrderDetailsStatus(Order order, OrderDetailStatus status) {
        Instant now = Instant.now();
        for (OrderDetail detail : order.getOrderDetails()) {
            detail.setStatus(status);
        }
        // Timestamp transitions (SHIPPED / DELIVERED) are managed at the Shipment
        // level
        if (status == OrderDetailStatus.SHIPPED || status == OrderDetailStatus.DELIVERED) {
            for (Shipment shipment : order.getShipments()) {
                if (status == OrderDetailStatus.SHIPPED) {
                    shipment.setShippedAt(now);
                } else {
                    shipment.setDeliveredAt(now);
                    int holdingDays = shipment.getVendor() != null
                            && shipment.getVendor().getHoldingDays() != null
                                    ? shipment.getVendor().getHoldingDays()
                                    : systemConfigService.getDefaultHoldingDays();
                    shipment.setPayoutReleaseDate(now.plus(holdingDays, ChronoUnit.DAYS));
                    shipment.setReturnDeadline(now.plus(7, ChronoUnit.DAYS));
                }
            }
        }
        log.info("Updated all order details to status {} for order: {}", status, order.getOrderNumber());
    }

    /**
     * Check if all order items are completed and auto-update order to COMPLETED
     */
    public void checkAndCompleteOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() == OrderStatus.COMPLETED
                || order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.RETURNED
                || order.getStatus() == OrderStatus.REFUNDED) {
            return;
        }

        boolean allCompleted = order.getOrderDetails().stream()
                .allMatch(detail -> detail.getStatus() == OrderDetailStatus.COMPLETED);

        if (allCompleted && !order.getOrderDetails().isEmpty()) {
            order.setStatus(OrderStatus.COMPLETED);

            // Set return deadline on all shipments (7 days after completion)
            Instant returnDeadline = Instant.now().plus(7, ChronoUnit.DAYS);
            for (Shipment shipment : order.getShipments()) {
                if (shipment.getReturnDeadline() == null) {
                    shipment.setReturnDeadline(returnDeadline);
                }
            }

            orderRepository.save(order);
        }
    }

    /**
     * Update individual order detail status and check if order should be completed
     */
    @Transactional
    public void updateOrderDetailStatus(UUID orderDetailId, OrderDetailStatus newStatus) {
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Order detail not found with ID: " + orderDetailId));

        Order order = orderDetail.getOrder();

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update items of a cancelled order");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            // Allow status updates for returns/refunds
            if (newStatus != OrderDetailStatus.RETURNED && newStatus != OrderDetailStatus.REFUNDED) {
                throw new IllegalStateException("Can only process returns/refunds for completed orders");
            }
        }

        OrderDetailStatus oldStatus = orderDetail.getStatus();
        orderDetail.setStatus(newStatus);

        // Set timestamps at Shipment level, not OrderDetail level
        Shipment shipment = orderDetail.getShipment();
        if (shipment != null) {
            switch (newStatus) {
                case SHIPPED -> shipment.setShippedAt(Instant.now());
                case DELIVERED -> {
                    Instant deliveredAt = Instant.now();
                    shipment.setDeliveredAt(deliveredAt);
                    Vendor vendor = shipment.getVendor();
                    int holdingDays = (vendor != null && vendor.getHoldingDays() != null)
                            ? vendor.getHoldingDays()
                            : systemConfigService.getDefaultHoldingDays();
                    shipment.setPayoutReleaseDate(deliveredAt.plus(holdingDays, ChronoUnit.DAYS));
                    shipment.setReturnDeadline(deliveredAt.plus(7, ChronoUnit.DAYS));
                    log.info("Shipment {} delivered. Payout release date set.", shipment.getShipmentId());
                }
            }
        }

        orderDetailRepository.save(orderDetail);

        log.info("OrderDetail {} status updated from {} to {}\"", orderDetailId, oldStatus, newStatus);

        // If item is completed, check if all items are completed to complete the order
        if (newStatus == OrderDetailStatus.COMPLETED) {
            checkAndCompleteOrder(order.getOrderId());
        }
    }

    /**
     * Process vendor payouts for order details that have reached their payout
     * release date
     * This method should be called by a scheduled job or manually
     */
    // @Transactional
    // public void processVendorPayouts(UUID orderId) {
    // log.info("Processing vendor payouts for order: {}", orderId);
    //
    // Order order = orderRepository.findById(orderId)
    // .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: "
    // + orderId));
    //
    // // Verify order is paid
    // if (!order.isPaid()) {
    // log.warn("Order {} is not paid yet. Skipping vendor payout.",
    // order.getOrderNumber());
    // return;
    // }
    //
    // Instant now = Instant.now();
    // Map<Vendor, BigDecimal> vendorPayouts = new HashMap<>();
    // List<OrderDetail> eligibleItems = new ArrayList<>();
    //
    // // Find items eligible for payout
    // for (OrderDetail detail : order.getOrderDetails()) {
    // // Skip if not delivered
    // if (detail.getStatus() != OrderDetailStatus.DELIVERED) {
    // log.debug("OrderDetail {} status is {}. Skipping payout.",
    // detail.getOrderDetailId(), detail.getStatus());
    // continue;
    // }
    //
    // // Skip if already paid out
    // if (detail.isPaidOut()) {
    // log.debug("OrderDetail {} already paid out. Skipping.",
    // detail.getOrderDetailId());
    // continue;
    // }
    //
    // // Check if payout release date has passed
    // Instant releaseDate = detail.getPayoutReleaseDate();
    // if (releaseDate == null || now.isBefore(releaseDate)) {
    // log.debug("OrderDetail {} is still in holding period. Release date: {}",
    // detail.getOrderDetailId(), releaseDate);
    // continue;
    // }
    //
    // // Item is eligible for payout
    // Product product = detail.getProduct();
    // Vendor vendor = product.getVendor();
    //
    // if (vendor == null) {
    // log.error("Product {} has no vendor. Skipping payout.",
    // product.getProductId());
    // continue;
    // }
    //
    // eligibleItems.add(detail);
    // vendorPayouts.merge(vendor, detail.getSubtotal(), BigDecimal::add);
    // }
    //
    // if (eligibleItems.isEmpty()) {
    // log.info("No items eligible for payout in order: {}",
    // order.getOrderNumber());
    // return;
    // }
    //
    // // Get system wallet for platform fees
    // Wallet systemWallet = walletRepository.findByAccountId(getSystemAccountId())
    // .orElseGet(this::createSystemWallet);
    //
    // // Process payouts for each vendor
    // for (Map.Entry<Vendor, BigDecimal> entry : vendorPayouts.entrySet()) {
    // Vendor vendor = entry.getKey();
    // BigDecimal grossAmount = entry.getValue();
    //
    // // Calculate platform fee
    // Double platformFeePercentage = vendor.getPlatformFeePercentage();
    // if (platformFeePercentage == null) {
    // platformFeePercentage = 20.0; // default
    // }
    //
    // BigDecimal platformFee =
    // grossAmount.multiply(BigDecimal.valueOf(platformFeePercentage / 100));
    // BigDecimal netAmount = grossAmount.subtract(platformFee);
    //
    // // Get vendor's account wallet
    // Wallet vendorWallet =
    // walletRepository.findByAccountId(vendor.getAccount().getAccountId())
    // .orElseThrow(() -> new ResourceNotFoundException(
    // "Wallet not found for vendor: " + vendor.getVendorId()));
    //
    // // Credit vendor wallet
    // vendorWallet.setAvailableVND(vendorWallet.getAvailableVND().add(netAmount));
    // vendorWallet.setBalanceVND(vendorWallet.getBalanceVND().add(netAmount));
    // walletRepository.save(vendorWallet);
    //
    // // Create vendor payout transaction
    // Transaction vendorTransaction = Transaction.builder()
    // .transactionType(TransactionType.VENDOR_PAYOUT)
    // .amount(netAmount)
    // .accountId(vendor.getAccount().getAccountId())
    // .referenceId(order.getOrderId())
    // .description(String.format("Payout for order %s (Vendor: %s, Gross: %s VND,
    // Fee: %.2f%%, Net: %s VND)",
    // order.getOrderNumber(), vendor.getBusinessName(), grossAmount,
    // platformFeePercentage, netAmount))
    // .build();
    // transactionRepository.save(vendorTransaction);
    //
    // // Credit system wallet with platform fee
    // systemWallet.setAvailableVND(systemWallet.getAvailableVND().add(platformFee));
    // systemWallet.setBalanceVND(systemWallet.getBalanceVND().add(platformFee));
    //
    // // Create platform fee transaction
    // Transaction platformFeeTransaction = Transaction.builder()
    // .transactionType(TransactionType.PLATFORM_FEE)
    // .amount(platformFee)
    // .accountId(getSystemAccountId())
    // .referenceId(order.getOrderId())
    // .description(String.format("Platform fee from order %s (Vendor: %s, Rate:
    // %.2f%%, Amount: %s VND)",
    // order.getOrderNumber(), vendor.getBusinessName(), platformFeePercentage,
    // platformFee))
    // .build();
    // transactionRepository.save(platformFeeTransaction);
    //
    // log.info("Processed payout for vendor {}: Gross={} VND, Fee={} VND ({}%),
    // Net={} VND",
    // vendor.getBusinessName(), grossAmount, platformFee, platformFeePercentage,
    // netAmount);
    // }
    //
    // walletRepository.save(systemWallet);
    //
    // // Mark items as paid out
    // for (OrderDetail detail : eligibleItems) {
    // detail.setPaidOut(true);
    // }
    // orderDetailRepository.saveAll(eligibleItems);
    //
    // log.info("Vendor payouts completed for order: {}. Processed {} items.",
    // order.getOrderNumber(), eligibleItems.size());
    // }
    //
    // /**
    // * Get system account ID for platform fees
    // */
    // private UUID getSystemAccountId() {
    // return accountRepository.findByEmail("system@pilahub.com")
    // .map(Account::getAccountId)
    // .orElseThrow(() -> new ResourceNotFoundException(
    // "System account not found. Please create system@pilahub.com account."));
    // }
    //
    // /**
    // * Create system wallet if it doesn't exist
    // */
    // private Wallet createSystemWallet() {
    // log.info("Creating system wallet");
    // UUID systemAccountId = getSystemAccountId();
    // Wallet systemWallet = Wallet.builder()
    // .accountId(systemAccountId)
    // .balanceVND(BigDecimal.ZERO)
    // .availableVND(BigDecimal.ZERO)
    // .active(true)
    // .build();
    // return walletRepository.save(systemWallet);
    // }
}