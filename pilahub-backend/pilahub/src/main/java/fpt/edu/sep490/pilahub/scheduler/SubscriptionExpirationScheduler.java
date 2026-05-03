package fpt.edu.sep490.pilahub.scheduler;

import fpt.edu.sep490.pilahub.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpirationScheduler {

    private final SubscriptionService subscriptionService;

    /**
     * Run every day at midnight to check and expire subscriptions
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void expireSubscriptions() {
        log.info("Starting scheduled task to expire subscriptions");
        try {
            subscriptionService.expireSubscriptions();
            log.info("Completed scheduled task to expire subscriptions");
        } catch (Exception e) {
            log.error("Error occurred while expiring subscriptions", e);
        }
    }
}
