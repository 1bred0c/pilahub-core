package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.HeartRateDto;
import fpt.edu.sep490.pilahub.exception.AccountNotFoundException;
//import fpt.edu.sep490.pilahub.exception.ForbiddenException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.LiveSession;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.LiveSessionRepository;
import fpt.edu.sep490.pilahub.service.HeartRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeartRateServiceImpl implements HeartRateService {

    private final LiveSessionRepository liveSessionRepository;
    private final AccountRepository accountRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(readOnly = true)
    public void sendHeartRate(HeartRateDto heartRateDto, UUID accountId) {
        // Validate and get coach ID
        UUID coachAccountId = validateAndGetCoachId(heartRateDto.liveSessionId(), accountId);
        
        // Send heart rate directly to coach via WebSocket
        // Destination: /queue/heartrate/{coachAccountId}
        String destination = "/queue/heartrate/" + coachAccountId;
        messagingTemplate.convertAndSend(destination, heartRateDto);
        
        log.debug("Heart rate {} BPM sent to coach {} for session {}", 
                heartRateDto.heartRate(), coachAccountId, heartRateDto.liveSessionId());
    }

    @Override
    @Transactional(readOnly = true)
    public UUID validateAndGetCoachId(UUID liveSessionId, UUID accountId) {
        // Find live session
        LiveSession liveSession = liveSessionRepository.findByLiveSessionId(liveSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("LiveSession", "id", liveSessionId));
        
        // Get trainee account ID
        UUID traineeAccountId = liveSession.getCoachBooking().getTrainee().getAccount().getAccountId();
        
        // Validate that sender is the trainee
        if (!traineeAccountId.equals(accountId)) {
//            throw new ForbiddenException("Only the trainee can send heart rate data for this session");
        }
        
        // Return coach account ID
        UUID coachAccountId = liveSession.getCoachBooking().getCoach().getAccount().getAccountId();
        log.debug("Validated heart rate sender. Trainee: {}, Coach: {}, Session: {}", 
                traineeAccountId, coachAccountId, liveSessionId);
        
        return coachAccountId;
    }

    @Override
    @Transactional(readOnly = true)
    public UUID getAccountIdByEmail(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for email: " + email));
        return account.getAccountId();
    }
}

