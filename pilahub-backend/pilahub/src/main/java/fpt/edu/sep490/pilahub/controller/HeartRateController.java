package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.HeartRateDto;
import fpt.edu.sep490.pilahub.service.HeartRateService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

/**
 * WebSocket controller for real-time heart rate streaming
 * Handles 1-1 communication between Trainee and Coach in a Live Session
 *
 * Flow:
 * 1. Trainee connects to WebSocket: /ws/heartrate
 * 2. Coach connects to WebSocket: /ws/heartrate
 * 3. Coach subscribes to: /queue/heartrate/{coachAccountId}
 * 4. Trainee sends heart rate to: /app/heartrate/send
 * 5. Backend validates and forwards to coach's queue
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@Hidden // Hide from Swagger since WebSocket is not REST
public class HeartRateController {

    private final HeartRateService heartRateService;

    /**
     * Receive heart rate from trainee and forward to coach
     * Uses Principal from message header (not SecurityContext) to avoid thread issues
     *
     * @param heartRateDto Heart rate data
     * @param headerAccessor Message header accessor to get authenticated user
     */
    @MessageMapping("/heartrate/send")
    public void receiveHeartRate(@Payload HeartRateDto heartRateDto,
                                  SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Get user from message header (thread-safe)
            Principal user = headerAccessor.getUser();

            if (user == null) {
                log.error("No user found in message header");
                return;
            }

            // Extract account ID from UserDetails
            UUID accountId = extractAccountIdFromPrincipal(user);

            if (accountId == null) {
                log.error("Cannot extract account ID from user: {}", user.getName());
                return;
            }

            log.debug("Received heart rate {} BPM from account {} for session {}",
                    heartRateDto.heartRate(), accountId, heartRateDto.liveSessionId());

            // Validate and send to coach
            heartRateService.sendHeartRate(
                    new HeartRateDto(
                            heartRateDto.liveSessionId(),
                            heartRateDto.heartRate(),
                            Instant.now() // Use server timestamp
                    ),
                    accountId
            );

        } catch (Exception e) {
            log.error("Error processing heart rate: {}", e.getMessage(), e);
        }
    }

    /**
     * Extract account ID from Principal (which is Authentication object)
     */
    private UUID extractAccountIdFromPrincipal(Principal principal) {
        if (principal instanceof Authentication) {
            Authentication auth = (Authentication) principal;
            Object principalObj = auth.getPrincipal();

            if (principalObj instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principalObj;
                String email = userDetails.getUsername();

                // Account ID is stored in authorities or we need to query
                // For now, we'll query via service
                log.debug("Extracted email from principal: {}", email);
                return heartRateService.getAccountIdByEmail(email);
            }
        }
        return null;
    }
}


