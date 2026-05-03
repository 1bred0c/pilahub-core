package fpt.edu.sep490.pilahub.scheduler;

import fpt.edu.sep490.pilahub.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupScheduler {

    private final OtpService otpService;

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void cleanupExpiredOtps() {
        log.info("Running scheduled OTP cleanup task");
        otpService.cleanupExpiredOtps();
        log.info("OTP cleanup task completed");
    }
}
