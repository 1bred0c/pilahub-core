package fpt.edu.sep490.pilahub.scheduler;

import fpt.edu.sep490.pilahub.dto.request.order.CancelOrderRequest;
import fpt.edu.sep490.pilahub.enums.OrderStatus;
import fpt.edu.sep490.pilahub.pojo.Order;
import fpt.edu.sep490.pilahub.repository.OrderRepository;
import fpt.edu.sep490.pilahub.service.OrderService;
import fpt.edu.sep490.pilahub.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PendingOrderAutoCancelScheduler {

    private static final String CANCELLATION_REASON =
            "Order automatically cancelled due to vendor inactivity after 24 hours";

    private final SystemConfigService systemConfigService;

    private final OrderRepository orderRepository;
    private final OrderService orderService;


    @Scheduled(cron = "0 0 1 * * *")
    public void autoCancelStalePendingOrders() {

        Instant cutoff = Instant.now().minus(systemConfigService.getVendorConfirmOrderHours(), ChronoUnit.HOURS);

        List<Order> orders =
                orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff);

        if (orders.isEmpty()) {
            log.debug("No stale pending orders to cancel");
            return;
        }

        int cancelled = 0;

        for (Order order : orders) {
            try {
                orderService.cancelOrder(
                        order.getOrderId(),
                        new CancelOrderRequest(CANCELLATION_REASON)
                );
                cancelled++;
            } catch (Exception e) {
                log.error("Failed to auto-cancel order {}", order.getOrderId(), e);
            }
        }

        log.info("Auto-cancel completed: {}/{}", cancelled, orders.size());
    }
}
