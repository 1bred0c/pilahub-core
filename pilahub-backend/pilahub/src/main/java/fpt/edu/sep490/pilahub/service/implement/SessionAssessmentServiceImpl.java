package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.SessionAssessmentDto;
import fpt.edu.sep490.pilahub.dto.request.assessment.SubmitAssessmentResultRequest;
import fpt.edu.sep490.pilahub.dto.request.assessment.SubmitSessionAssessmentRequest;
import fpt.edu.sep490.pilahub.enums.LiveSessionStatus;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.exception.AccessDeniedException;
import fpt.edu.sep490.pilahub.exception.InvalidRequestException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.SessionAssessmentMapper;
import fpt.edu.sep490.pilahub.pojo.AssessmentCriterion;
import fpt.edu.sep490.pilahub.pojo.AssessmentResult;
import fpt.edu.sep490.pilahub.pojo.LiveSession;
import fpt.edu.sep490.pilahub.pojo.SessionAssessment;
import fpt.edu.sep490.pilahub.repository.AssessmentCriterionRepository;
import fpt.edu.sep490.pilahub.repository.LiveSessionRepository;
import fpt.edu.sep490.pilahub.repository.SessionAssessmentRepository;
import fpt.edu.sep490.pilahub.service.SessionAssessmentService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SessionAssessmentServiceImpl implements SessionAssessmentService {

    private final SessionAssessmentRepository sessionAssessmentRepository;
    private final AssessmentCriterionRepository criterionRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final SessionAssessmentMapper sessionAssessmentMapper;
    private final SecurityUtil securityUtil;

    @Override
    public SessionAssessmentDto submitAssessment(UUID liveSessionId, SubmitSessionAssessmentRequest request) {
        LiveSession liveSession = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("LiveSession", "id", liveSessionId));

        UUID currentUserId = securityUtil.getCurrentUserId();
        Role currentRole = securityUtil.getCurrentUserRole();
        UUID coachId = liveSession.getCoachBooking().getCoach().getCoachId();
        UUID traineeId = liveSession.getCoachBooking().getTrainee().getTraineeId();

        if (currentRole != Role.COACH || !currentUserId.equals(coachId)) {
            throw new AccessDeniedException("Only the coach of this session can submit assessment");
        }

        if (liveSession.getStatus() != LiveSessionStatus.COMPLETED) {
            throw new InvalidRequestException("Assessment can only be submitted for COMPLETED sessions");
        }

        if (sessionAssessmentRepository.existsByLiveSessionId(liveSessionId)) {
            throw new InvalidRequestException("Assessment has already been submitted for this session");
        }

        List<AssessmentCriterion> activeCriteria = criterionRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        if (activeCriteria.isEmpty()) {
            throw new InvalidRequestException("No active assessment criteria configured");
        }

        validateSubmission(request.results(), activeCriteria);
        Map<UUID, AssessmentCriterion> criterionMap = activeCriteria.stream()
                .collect(Collectors.toMap(AssessmentCriterion::getAssessmentCriterionId, criterion -> criterion));

        SessionAssessment sessionAssessment = new SessionAssessment();
        sessionAssessment.setLiveSession(liveSession);
        sessionAssessment.setCoachId(coachId);
        sessionAssessment.setTraineeId(traineeId);
        sessionAssessment.setSubmittedAt(Instant.now());

        List<AssessmentResult> results = request.results().stream()
                .map(item -> AssessmentResult.builder()
                        .sessionAssessment(sessionAssessment)
                        .criterion(criterionMap.get(item.criterionId()))
                        .score(item.score())
                        .build())
                .toList();

        sessionAssessment.setResults(results);
        SessionAssessment saved = sessionAssessmentRepository.save(sessionAssessment);
        log.info("Coach {} submitted assessment for session {}", currentUserId, liveSessionId);
        return sessionAssessmentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public SessionAssessmentDto getSessionAssessment(UUID liveSessionId) {
        SessionAssessment assessment = sessionAssessmentRepository.findWithResultsByLiveSessionId(liveSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("SessionAssessment", "liveSessionId", liveSessionId));

        UUID currentUserId = securityUtil.getCurrentUserId();
        Role currentRole = securityUtil.getCurrentUserRole();

        if (currentRole == Role.ADMIN) {
            return sessionAssessmentMapper.toDto(assessment);
        }

        boolean isOwnerCoach = currentRole == Role.COACH && currentUserId.equals(assessment.getCoachId());
        boolean isOwnerTrainee = currentRole == Role.TRAINEE && currentUserId.equals(assessment.getTraineeId());

        if (!isOwnerCoach && !isOwnerTrainee) {
            throw new AccessDeniedException("You are not authorized to view this assessment");
        }

        return sessionAssessmentMapper.toDto(assessment);
    }

    @Override
    @Transactional
    public List<SessionAssessmentDto> getMyAssessmentHistory() {
        UUID currentUserId = securityUtil.getCurrentUserId();
        Role currentRole = securityUtil.getCurrentUserRole();

        if (currentRole != Role.TRAINEE) {
            throw new AccessDeniedException("Only trainee can view assessment history");
        }

        return sessionAssessmentRepository.findByTraineeIdOrderBySubmittedAtAsc(currentUserId).stream()
                .map(sessionAssessmentMapper::toDto)
                .collect(Collectors.toList());
    }

    private void validateSubmission(List<SubmitAssessmentResultRequest> submittedResults,
                                    List<AssessmentCriterion> activeCriteria) {
        Set<UUID> submittedCriterionIds = new HashSet<>();
        for (SubmitAssessmentResultRequest submittedResult : submittedResults) {
            if (!submittedCriterionIds.add(submittedResult.criterionId())) {
                throw new InvalidRequestException("Duplicate criterion in assessment submission");
            }
        }

        Set<UUID> activeCriterionIds = activeCriteria.stream()
                .map(AssessmentCriterion::getAssessmentCriterionId)
                .collect(Collectors.toSet());

        if (!submittedCriterionIds.equals(activeCriterionIds)) {
            throw new InvalidRequestException("Assessment must include all active criteria exactly once");
        }
    }
}


