//package fpt.edu.sep490.pilahub.scheduler;
//
//import fpt.edu.sep490.pilahub.service.LiveSessionService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class LiveSessionScheduler {
//
//    private final LiveSessionService liveSessionService;
//
//    /**
//     * Run every minute at the 55th second
//     * Updates session status from PENDING to ACTIVE when booking becomes READY
//     */
//    @Scheduled(cron = "55 * * * * *")
//    public void updateSessionStatus() {
//        try {
//            log.info("Running scheduled task: Update live session status from booking status");
//            liveSessionService.updateSessionStatusFromBooking();
//            log.info("Completed scheduled task: Update live session status");
//        } catch (Exception e) {
//            log.error("Error in updateSessionStatus scheduler: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Run every minute at the 0th second
//     * Generate tokens for sessions starting soon (within 10 minutes)
//     */
//    @Scheduled(cron = "0 * * * * *")
//    public void generateTokensForUpcomingSessions() {
//        try {
//            log.info("Running scheduled task: Generate tokens for upcoming sessions");
//            liveSessionService.generateTokensForUpcomingSessions();
//            log.info("Completed scheduled task: Generate tokens");
//        } catch (Exception e) {
//            log.error("Error in generateTokensForUpcomingSessions scheduler: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Run every 5 minutes
//     * Check for no-show situations (15 minutes after session start)
//     */
//    @Scheduled(cron = "0 */5 * * * *")
//    public void checkForNoShow() {
//        try {
//            log.info("Running scheduled task: Check for no-show sessions");
//            liveSessionService.checkAndHandleNoShow();
//            log.info("Completed scheduled task: Check for no-show");
//        } catch (Exception e) {
//            log.error("Error in checkForNoShow scheduler: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Run every minute
//     * Complete sessions that have ended
//     */
//    @Scheduled(cron = "0 * * * * *")
//    public void completeEndedSessions() {
//        try {
//            log.info("Running scheduled task: Complete ended sessions");
//            liveSessionService.completeEndedSessions();
//            log.info("Completed scheduled task: Complete ended sessions");
//        } catch (Exception e) {
//            log.error("Error in completeEndedSessions scheduler: {}", e.getMessage(), e);
//        }
//    }
//}
//
