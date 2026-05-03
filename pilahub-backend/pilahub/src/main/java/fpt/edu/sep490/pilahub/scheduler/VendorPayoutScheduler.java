package fpt.edu.sep490.pilahub.scheduler;

import fpt.edu.sep490.pilahub.service.VendorPayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Triggers vendor payout processing every day at 02:00 AM.
 * All business logic lives in {@link VendorPayoutService}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VendorPayoutScheduler {

    private final VendorPayoutService vendorPayoutService;

    /**
     * cron = "second minute hour day month weekday"
     * Runs daily at 02:00 AM server time.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void processVendorPayouts() {
        log.info("=== [VendorPayoutScheduler] Starting at {} ===", Instant.now());
        try {
            vendorPayoutService.releaseEligiblePayouts();
            log.info("=== [VendorPayoutScheduler] Completed successfully ===");
        } catch (Exception e) {
            log.error("=== [VendorPayoutScheduler] Fatal error during payout job ===", e);
        }
    }
}

