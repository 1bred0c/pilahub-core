package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import fpt.edu.sep490.pilahub.enums.TransactionType;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.pojo.Order;
import fpt.edu.sep490.pilahub.pojo.OrderDetail;
import fpt.edu.sep490.pilahub.pojo.Transaction;
import fpt.edu.sep490.pilahub.pojo.Vendor;
import fpt.edu.sep490.pilahub.pojo.Wallet;
import fpt.edu.sep490.pilahub.repository.OrderRepository;
import fpt.edu.sep490.pilahub.repository.TransactionRepository;
import fpt.edu.sep490.pilahub.repository.WalletRepository;
import fpt.edu.sep490.pilahub.service.SystemConfigService;
import fpt.edu.sep490.pilahub.service.VendorPayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorPayoutServiceImpl implements VendorPayoutService {

    private final OrderRepository orderRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final SystemConfigService systemConfigService;

    @Override
    @Transactional
    public void releaseEligiblePayouts() {
        List<Order> eligible = orderRepository
                .findEligibleForVendorPayoutByOrder(OrderDetailStatus.COMPLETED);

        if (eligible.isEmpty()) {
            log.info("[VendorPayout] No eligible orders for payout.");
            return;
        }

        log.info("[VendorPayout] Processing {} eligible order(s).", eligible.size());

        Map<UUID, VendorBucket> buckets = new LinkedHashMap<>();

        for (Order order : eligible) {
            if (order.getShipments() == null || order.getShipments().isEmpty()) {
                log.warn("[VendorPayout] Order {} has no shipment - skipped.", order.getOrderId());
                continue;
            }

            Vendor vendor = order.getShipments().get(0).getVendor();
            if (vendor == null) {
                log.warn("[VendorPayout] Order {} has no vendor - skipped.", order.getOrderId());
                continue;
            }

            double feeRate = vendor.getPlatformFeePercentage() != null
                    ? vendor.getPlatformFeePercentage()
                    : systemConfigService.getDefaultPlatformFeePercentage();

            BigDecimal gross = order.getOrderDetails().stream()
                    .map(OrderDetail::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal platformFee = gross.multiply(BigDecimal.valueOf(feeRate / 100.0));
            BigDecimal net = gross.subtract(platformFee);

            buckets.computeIfAbsent(vendor.getVendorId(), id -> new VendorBucket(vendor))
                    .add(order, gross, net, feeRate);
        }

        List<Order> processedOrders = new ArrayList<>();

        for (VendorBucket bucket : buckets.values()) {
            try {
                creditVendor(bucket, processedOrders);
            } catch (Exception e) {
                log.error("[VendorPayout] Failed for vendor {} — skipped. Reason: {}",
                        bucket.vendor.getVendorId(), e.getMessage(), e);
            }
        }

        processedOrders.forEach(order -> order.setPaidOut(true));
        orderRepository.saveAll(processedOrders);

        log.info("[VendorPayout] Done. Marked {} order(s) as paid out.", processedOrders.size());
    }

    @Override
    @Transactional
    public void payoutOrderForVendor(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.isPaidOut()) {
            throw new IllegalStateException("Order is already paid out");
        }

        if (order.getShipments() == null || order.getShipments().isEmpty()) {
            throw new IllegalStateException("Order has no shipment");
        }

        Vendor vendor = order.getShipments().get(0).getVendor();
        if (vendor == null) {
            throw new IllegalStateException("Order shipment has no vendor");
        }

        double feeRate = vendor.getPlatformFeePercentage() != null
                ? vendor.getPlatformFeePercentage()
                : systemConfigService.getDefaultPlatformFeePercentage();

        BigDecimal gross = order.getOrderDetails().stream()
                .map(OrderDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal platformFee = gross.multiply(BigDecimal.valueOf(feeRate / 100.0));
        BigDecimal net = gross.subtract(platformFee);

        VendorBucket bucket = new VendorBucket(vendor);
        bucket.add(order, gross, net, feeRate);

        List<Order> processedOrders = new ArrayList<>();
        creditVendor(bucket, processedOrders);

        order.setPaidOut(true);
        orderRepository.save(order);

        log.info("[VendorPayout] Admin payout completed for order {}", orderId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Credits the vendor wallet with the total net amount from all eligible
     * orders in the bucket (one wallet save), then creates one
     * {@code VENDOR_PAYOUT} transaction per Order for full audit trail.
     */
    private void creditVendor(VendorBucket bucket, List<Order> processedOrders) {
        Vendor vendor = bucket.vendor;
        UUID vendorAccountId = vendor.getAccount().getAccountId();

        BigDecimal netShippingFee = BigDecimal.ZERO;

        for (DetailEntry entry : bucket.entries) {
            Order order = entry.order;
            if (transactionRepository.existsByReferenceIdAndTransactionType(
                    order.getOrderId(), TransactionType.SHIPPING_FEE_VENDOR)) {

                netShippingFee = netShippingFee.add(
                        order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO
                );
            }
        }

        // One wallet credit per vendor
        Wallet vendorWallet = walletRepository.findByAccountId(vendorAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found for vendor account: " + vendorAccountId));

        vendorWallet.setAvailableVND(vendorWallet.getAvailableVND().add(bucket.totalNet).add(netShippingFee));
        vendorWallet.setBalanceVND(vendorWallet.getBalanceVND().add(bucket.totalNet).add(netShippingFee));
        walletRepository.save(vendorWallet);

        // One transaction per Order for full audit trail
        for (DetailEntry entry : bucket.entries) {
            Order order = entry.order;
            BigDecimal orderGross = entry.gross;
            BigDecimal orderFee = orderGross.multiply(BigDecimal.valueOf(entry.feeRate / 100.0));
            BigDecimal orderNet = orderGross.subtract(orderFee);

            if (transactionRepository.existsByReferenceIdAndTransactionType(order.getOrderId(), TransactionType.SHIPPING_FEE_VENDOR)){
                transactionRepository.save(Transaction.builder()
                        .transactionType(TransactionType.VENDOR_PAYOUT)
                        .amount(order.getShippingFee())
                        .accountId(vendorAccountId)
                        .referenceId(order.getOrderId())
                        .description(String.format(
                                "Thanh toán phí vận chuyển (tự giao hàng) | Đơn hàng: %s | %s VND",
                                order.getOrderNumber(),
                                order.getShippingFee()))
                        .build());
            }

            transactionRepository.save(Transaction.builder()
                    .transactionType(TransactionType.VENDOR_PAYOUT)
                    .amount(orderNet)
                    .accountId(vendorAccountId)
                    .referenceId(order.getOrderId())
                    .description(String.format(
                            "Thanh toán công nợ | Đơn hàng: %s | Tổng: %s VND | Phí: %.2f%% | Thực nhận: %s VND",
                            order.getOrderNumber(),
                            orderGross,
                            entry.feeRate,
                            orderNet))
                    .build());

            processedOrders.add(order);
        }

        log.info("[VendorPayout] Vendor '{}' credited {} VND ({} order(s)).",
                vendor.getBusinessName(), bucket.totalNet, bucket.entries.size());
    }

    // -------------------------------------------------------------------------
    // Inner data helpers
    // -------------------------------------------------------------------------

    private static class DetailEntry {
        final Order order;
        final BigDecimal gross;
        final BigDecimal net;
        final double feeRate;

        DetailEntry(Order order, BigDecimal gross, BigDecimal net, double feeRate) {
            this.order = order;
            this.gross = gross;
            this.net = net;
            this.feeRate = feeRate;
        }
    }

    private static class VendorBucket {
        final Vendor vendor;
        final List<DetailEntry> entries = new ArrayList<>();
        BigDecimal totalNet = BigDecimal.ZERO;

        VendorBucket(Vendor vendor) {
            this.vendor = vendor;
        }

        void add(Order order, BigDecimal gross, BigDecimal net, double feeRate) {
            entries.add(new DetailEntry(order, gross, net, feeRate));
            totalNet = totalNet.add(net);
        }
    }
}
