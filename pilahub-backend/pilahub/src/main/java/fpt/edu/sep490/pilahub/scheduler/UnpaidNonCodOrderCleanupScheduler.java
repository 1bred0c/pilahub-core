package fpt.edu.sep490.pilahub.scheduler;

import fpt.edu.sep490.pilahub.pojo.Order;
import fpt.edu.sep490.pilahub.repository.OrderDetailRepository;
import fpt.edu.sep490.pilahub.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnpaidNonCodOrderCleanupScheduler {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    // Run every 1 hour 30 minutes.
    @Scheduled(fixedRate = 5_400_000)
    @Transactional
    public void cleanupUnpaidNonCodOrders() {
        List<Order> ordersToDelete = orderRepository.findAllUnpaidNonCodOrders();

        if (ordersToDelete.isEmpty()) {
            log.debug("No unpaid non-COD orders to clean up");
            return;
        }

        int deletedCount = 0;
        for (Order order : ordersToDelete) {
            UUID orderId = order.getOrderId();
            try {
                deleteOrderWithDetails(orderId);
                deletedCount++;
            } catch (Exception e) {
                log.error("Failed to delete unpaid non-COD order {}", orderId, e);
            }
        }

        log.info("Unpaid non-COD cleanup completed. Deleted {}/{} order(s)", deletedCount, ordersToDelete.size());
    }

    private void deleteOrderWithDetails(UUID orderId) {
        long detailCount = orderDetailRepository.deleteByOrder_OrderId(orderId);
        orderRepository.deleteById(orderId);
        log.info("Deleted unpaid non-COD order {} with {} order detail(s)", orderId, detailCount);
    }
}
