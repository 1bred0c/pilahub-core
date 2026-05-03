package fpt.edu.sep490.pilahub.scheduler;

import fpt.edu.sep490.pilahub.service.OrderDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Auto-completes delivered order details whose return deadline has passed.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderDetailAutoCompleteScheduler {

    private final OrderDetailService orderDetailService;

    /**
     * Runs every hour and marks eligible DELIVERED items as COMPLETED.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void autoCompleteDeliveredOrderDetails() {
        log.info("=== [OrderDetailAutoCompleteScheduler] Starting at {} ===", Instant.now());
        try {
            orderDetailService.autoCompleteDeliveredOrderDetailsPastDeadline();
            log.info("=== [OrderDetailAutoCompleteScheduler] Completed successfully ===");
        } catch (Exception e) {
            log.error("=== [OrderDetailAutoCompleteScheduler] Fatal error during auto-complete job ===", e);
        }
    }
}
