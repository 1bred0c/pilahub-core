package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.LiveSessionDto;
import fpt.edu.sep490.pilahub.dto.LiveSessionTokenDto;
import fpt.edu.sep490.pilahub.dto.request.SubmitTraineeRatingRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.AgoraRecordingService;
import fpt.edu.sep490.pilahub.service.LiveSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/live-sessions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Live Session", description = "Live coaching session management with Agora integration")
public class LiveSessionController {

    private final LiveSessionService liveSessionService;
    private final AgoraRecordingService agoraRecordingService;

    @GetMapping("/my-sessions")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE')")
    @Operation(
            summary = "Get my sessions",
            description = "Retrieve all live sessions for the authenticated user (coach or trainee)"
    )
    @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Only coaches and trainees can access")
    public ResponseEntity<APIResponse<List<LiveSessionDto>>> getMySessions() {
        List<LiveSessionDto> sessions = liveSessionService.getMySessions();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d session(s) successfully", sessions.size()),
                sessions
        ));
    }

    @GetMapping("/{liveSessionId}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(
            summary = "Get session by ID",
            description = "Retrieve live session details by ID. " +
                    "Returns recording URL if session is completed and recording is available."
    )
    @ApiResponse(responseCode = "200", description = "Session retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<APIResponse<LiveSessionDto>> getSessionById(
            @PathVariable UUID liveSessionId) {
        LiveSessionDto session = liveSessionService.getLiveSessionById(liveSessionId);

        String message = "Session retrieved successfully";
        if (session.recordingUrl() != null) {
            message += " (Recording available)";
        }

        return ResponseEntity.ok(APIResponse.success(message, session));
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(
            summary = "Get session by booking ID",
            description = "Retrieve live session details by coach booking ID"
    )
    @ApiResponse(responseCode = "200", description = "Session retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<APIResponse<LiveSessionDto>> getSessionByBookingId(
            @PathVariable UUID bookingId) {
        LiveSessionDto session = liveSessionService.getLiveSessionByBookingId(bookingId);
        return ResponseEntity.ok(APIResponse.success("Session retrieved successfully", session));
    }

    @GetMapping("/{liveSessionId}/token")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE')")
    @Operation(
            summary = "Get Agora token to join session",
            description = "Retrieve Agora access token to join the live session. " +
                    "Only available when session is ACTIVE (within 10 minutes before start time)"
    )
    @ApiResponse(responseCode = "200", description = "Token retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Session is not active or ready to join")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this session")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<APIResponse<LiveSessionTokenDto>> getSessionToken(
            @PathVariable UUID liveSessionId) {
        LiveSessionTokenDto token = liveSessionService.getMySessionToken(liveSessionId);
        return ResponseEntity.ok(APIResponse.success("Token retrieved successfully", token));
    }

    @PostMapping("/{liveSessionId}/join")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE')")
    @Operation(
            summary = "Mark as joined",
            description = "Mark that the user has successfully joined the Agora channel. " +
                    "When both coach and trainee join, booking status updates to IN_PROGRESS"
    )
    @ApiResponse(responseCode = "200", description = "Marked as joined successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this session")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<APIResponse<LiveSessionDto>> markAsJoined(
            @PathVariable UUID liveSessionId) {
        LiveSessionDto session = liveSessionService.markAsJoined(liveSessionId);
        return ResponseEntity.ok(APIResponse.success("Joined session successfully", session));
    }

    @PostMapping("/{liveSessionId}/leave")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE')")
    @Operation(
            summary = "Mark as left",
            description = "Mark that the user has left the Agora channel. " +
                    "When both users leave (room empty), recording auto-stops and session ends."
    )
    @ApiResponse(responseCode = "200", description = "Marked as left successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this session")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<APIResponse<LiveSessionDto>> markAsLeft(
            @PathVariable UUID liveSessionId) {
        LiveSessionDto session = liveSessionService.markAsLeft(liveSessionId);
        return ResponseEntity.ok(APIResponse.success("Left session successfully", session));
    }

    @PostMapping("/{liveSessionId}/end")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE')")
    @Operation(
            summary = "End session",
            description = "End an active session manually. Updates session and booking status to COMPLETED"
    )
    @ApiResponse(responseCode = "200", description = "Session ended successfully")
    @ApiResponse(responseCode = "400", description = "Session is not active")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this session")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<APIResponse<LiveSessionDto>> endSession(
            @PathVariable UUID liveSessionId) {
        LiveSessionDto session = liveSessionService.endSession(liveSessionId);
        return ResponseEntity.ok(APIResponse.success("Session ended successfully", session));
    }

    @GetMapping("/{liveSessionId}/recording-status")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(
            summary = "Get recording status",
            description = "Query Agora Cloud Recording status for this session. " +
                    "Returns status code: 0=Not started, 1=Initializing, 2=Start failed, " +
                    "5=Recording in progress, 6=Upload failed, 7=Upload success, " +
                    "NO_RECORDING=No recording found"
    )
    @ApiResponse(responseCode = "200", description = "Recording status retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<APIResponse<String>> getRecordingStatus(
            @PathVariable UUID liveSessionId) {
        String status = agoraRecordingService.getRecordingStatus(liveSessionId);

        String message;
        switch (status) {
            case "0" -> message = "Recording not started";
            case "1" -> message = "Recording initializing";
            case "2" -> message = "Recording start failed - check storage config";
            case "5" -> message = "Recording in progress";
            case "6" -> message = "Recording upload failed - check storage credentials";
            case "7" -> message = "Recording upload success - completed";
            case "NO_RECORDING" -> message = "No recording found for this session";
            default -> message = "Recording status: " + status;
        }

        return ResponseEntity.ok(APIResponse.success(message, status));
    }

    @GetMapping("/{liveSessionId}/recording-url")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(
            summary = "Get recording playback URL",
            description = "Get secure presigned URL for recording playback. " +
                    "URL is valid for 2 hours and can be used directly in <video> tag. " +
                    "Bucket can be private - presigned URL handles authentication."
    )
    @ApiResponse(responseCode = "200", description = "Presigned URL generated successfully")
    @ApiResponse(responseCode = "400", description = "Recording not available")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<APIResponse<String>> getRecordingUrl(
            @PathVariable UUID liveSessionId) {
        String presignedUrl = agoraRecordingService.getRecordingUrl(liveSessionId);

        return ResponseEntity.ok(APIResponse.success(
                "Presigned URL generated (valid for 2 hours)",
                presignedUrl
        ));
    }

    @PostMapping("/{liveSessionId}/rating")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Submit trainee rating",
            description = "Submit rating for a completed session (0.5-5.0 in 0.5 increments). Can only be done once."
    )
    @ApiResponse(responseCode = "200", description = "Rating submitted successfully")
    @ApiResponse(responseCode = "400", description = "Session not completed, rating already submitted, or invalid rating value")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Only the trainee of this session can submit rating")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<APIResponse<LiveSessionDto>> submitRating(
            @PathVariable UUID liveSessionId,
            @Valid @RequestBody SubmitTraineeRatingRequest request) {
        LiveSessionDto session = liveSessionService.submitTraineeRating(liveSessionId, request.rating());
        return ResponseEntity.ok(APIResponse.success("Rating submitted successfully", session));
    }


    // ============= ADMIN ENDPOINTS =============

    @GetMapping("/coach/{coachId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get coach sessions (Admin only)",
            description = "Retrieve all sessions for a specific coach. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<List<LiveSessionDto>>> getCoachSessions(
            @PathVariable UUID coachId) {
        List<LiveSessionDto> sessions = liveSessionService.getCoachSessions(coachId);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d session(s) for coach", sessions.size()),
                sessions
        ));
    }

    @GetMapping("/trainee/{traineeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get trainee sessions (Admin only)",
            description = "Retrieve all sessions for a specific trainee. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<List<LiveSessionDto>>> getTraineeSessions(
            @PathVariable UUID traineeId) {
        List<LiveSessionDto> sessions = liveSessionService.getTraineeSessions(traineeId);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d session(s) for trainee", sessions.size()),
                sessions
        ));
    }
}

