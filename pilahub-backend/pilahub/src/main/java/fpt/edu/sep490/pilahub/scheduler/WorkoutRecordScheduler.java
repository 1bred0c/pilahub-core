package fpt.edu.sep490.pilahub.scheduler;

import fpt.edu.sep490.pilahub.pojo.WorkoutSession;
import fpt.edu.sep490.pilahub.repository.WorkoutSessionRepository;
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
public class WorkoutRecordScheduler {

    private final WorkoutSessionRepository workoutSessionRepository;

    /**
     * Marks workout recordings as unavailable after 7 days
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void expireOldRecordings() {
        log.info("Running scheduled workout recording expiration task");

        // Calculate the cutoff date (7 days ago)
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        // Find all completed sessions with available recordings that ended more than 7 days ago
        List<WorkoutSession> expiredSessions = workoutSessionRepository
                .findByCompletedTrueAndRecordAvailableTrueAndEndTimeBefore(sevenDaysAgo);

        if (!expiredSessions.isEmpty()) {
            log.info("Found {} workout recordings to expire", expiredSessions.size());

            // Mark recordings as unavailable
            expiredSessions.forEach(session -> {
                session.setRecordAvailable(false);
                log.debug("Expired recording for workout session ID: {}", session.getWorkoutSessionId());
            });

            workoutSessionRepository.saveAll(expiredSessions);
            log.info("Successfully expired {} workout recordings", expiredSessions.size());
        } else {
            log.info("No workout recordings to expire");
        }
    }
}

