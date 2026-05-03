package fpt.edu.sep490.pilahub.scheduler;

import fpt.edu.sep490.pilahub.enums.BookingStatus;
import fpt.edu.sep490.pilahub.enums.LiveSessionStatus;
import fpt.edu.sep490.pilahub.enums.TransactionType;
import fpt.edu.sep490.pilahub.pojo.CoachBooking;
import fpt.edu.sep490.pilahub.pojo.Transaction;
import fpt.edu.sep490.pilahub.pojo.Wallet;
import fpt.edu.sep490.pilahub.repository.CoachBookingRepository;
import fpt.edu.sep490.pilahub.repository.LiveSessionRepository;
import fpt.edu.sep490.pilahub.repository.TransactionRepository;
import fpt.edu.sep490.pilahub.repository.WalletRepository;
import fpt.edu.sep490.pilahub.service.LiveSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoachBookingScheduler {

    private final CoachBookingRepository coachBookingRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LiveSessionService liveSessionService;

    /**
     * Runs every minute to move SCHEDULED bookings to READY 5 minutes before start time.
     * Example: booking at 14:00 becomes READY at 13:55.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void transitionScheduledToReady() {
        Instant readyThreshold = Instant.now().plus(Duration.ofMinutes(5));

        List<CoachBooking> readyBookings = coachBookingRepository.findScheduledBookingsReadyToStart(readyThreshold);

        if (!readyBookings.isEmpty()) {
            log.info("Found {} bookings ready to start", readyBookings.size());

            for (CoachBooking booking : readyBookings) {
                booking.setStatus(BookingStatus.READY);
                coachBookingRepository.save(booking);
                liveSessionService.activateSessionForReadyBooking(booking.getId());
                log.info("Booking {} transitioned to READY (start time: {})", booking.getId(), booking.getStartTime());
            }
        }
    }

    /**
     * Runs every 5 minutes to check if READY bookings have been waiting for more than 15 minutes
     * without both parties joining.
     * - If coach doesn't show: NO_SHOW_BY_COACH -> REFUNDED (auto refund to trainee)
     * - If trainee doesn't show: NO_SHOW_BY_TRAINEE (no refund)
     *
     * Note: Current implementation marks as NO_SHOW_BY_COACH (assumes coach responsible if no one joins)
     * In real scenario, you should track who joined and mark accordingly
     */
    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
    @Transactional
    public void checkReadyBookingsForNoShow() {
        Instant fifteenMinutesAgo = Instant.now().minus(Duration.ofMinutes(15));

        List<CoachBooking> overdueBookings = coachBookingRepository.findReadyBookingsOverdue(fifteenMinutesAgo);

        if (!overdueBookings.isEmpty()) {
            log.info("Found {} READY bookings overdue (no-show)", overdueBookings.size());

            for (CoachBooking booking : overdueBookings) {
                // Mark as NO_SHOW_BY_COACH (assuming coach is responsible if session doesn't start)
                // In a real implementation, you should track who actually joined
                booking.setStatus(BookingStatus.NO_SHOW_BY_COACH);
                coachBookingRepository.save(booking);
                log.info("Booking {} marked as NO_SHOW_BY_COACH", booking.getId());

                // Process refund for coach no-show
                try {
                    processRefundForCoachNoShow(booking);

                    // Update to REFUNDED status
                    booking.setStatus(BookingStatus.REFUNDED);
                    coachBookingRepository.save(booking);
                    log.info("Booking {} refunded and marked as REFUNDED", booking.getId());
                } catch (Exception e) {
                    log.error("Failed to refund booking {}: {}", booking.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * Runs every minute to auto-complete IN_PROGRESS bookings when end time is reached.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void completeEndedInProgressBookings() {
        Instant now = Instant.now();
        List<CoachBooking> endedBookings = coachBookingRepository.findInProgressBookingsEndingBefore(now);

        if (!endedBookings.isEmpty()) {
            log.info("Found {} IN_PROGRESS bookings that reached end time", endedBookings.size());
            for (CoachBooking booking : endedBookings) {
                booking.setStatus(BookingStatus.COMPLETED);
                coachBookingRepository.save(booking);
                log.info("Booking {} auto-completed at end time", booking.getId());
            }
        }
    }

    /**
     * Process refund when coach doesn't show up
     */
    private void processRefundForCoachNoShow(CoachBooking booking) {
        Wallet wallet = walletRepository.findByAccountId(booking.getTrainee().getTraineeId())
                .orElseThrow(() -> new RuntimeException("Wallet not found for trainee: " + booking.getTrainee().getTraineeId()));

        // Refund amount back to trainee's wallet
        wallet.setAvailableVND(wallet.getAvailableVND().add(booking.getTotalAmount()));
        wallet.setBalanceVND(wallet.getBalanceVND().add(booking.getTotalAmount()));
        walletRepository.save(wallet);

        // Create refund transaction
        Transaction refundTransaction = Transaction.builder()
                .transactionType(TransactionType.BOOKING_COACH_REFUND)
                .amount(booking.getTotalAmount())
                .accountId(booking.getTrainee().getTraineeId())
                .referenceId(booking.getId())
                .description("Refund for coach no-show")
                .build();

        transactionRepository.save(refundTransaction);

        log.info("Refunded {} VND to trainee {} for booking {}",
                booking.getTotalAmount(),
                booking.getTrainee().getTraineeId(),
                booking.getId());
    }
}




