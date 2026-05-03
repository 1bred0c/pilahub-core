package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.CoachBookingDto;
import fpt.edu.sep490.pilahub.dto.request.booking.CreateBatchBookingRequest;
import fpt.edu.sep490.pilahub.dto.request.booking.CreateSingleBookingRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.dto.response.BatchBookingResponse;
import fpt.edu.sep490.pilahub.dto.response.BusyTimeSlot;
import fpt.edu.sep490.pilahub.enums.BookingStatus;
import fpt.edu.sep490.pilahub.service.CoachBookingService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/coach-bookings")
@RequiredArgsConstructor
@  SecurityRequirement(name = "bearerAuth")
@Tag(name = "Coach Booking", description = "Manage coach booking sessions")
public class CoachBookingController {

    private final CoachBookingService coachBookingService;
    private final SecurityUtil securityUtil;

    @PostMapping("/single")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Create single booking", description = "Trainee creates a single coach booking session with payment")
    @ApiResponse(responseCode = "201", description = "Booking created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or time conflict")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<CoachBookingDto>> createSingleBooking(
            @Valid @RequestBody CreateSingleBookingRequest request) {
        UUID traineeId = securityUtil.getCurrentUserId();
        CoachBookingDto booking = coachBookingService.createSingleBooking(traineeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Booking created successfully", booking));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Create multiple bookings", description = "Trainee creates multiple coach bookings at once. Returns conflicts for user to reschedule.")
    @ApiResponse(responseCode = "201", description = "Batch booking processed")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<BatchBookingResponse>> createBatchBooking(
            @Valid @RequestBody CreateBatchBookingRequest request) {
        UUID traineeId = securityUtil.getCurrentUserId();
        BatchBookingResponse response = coachBookingService.createBatchBooking(traineeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Batch booking processed", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get booking by ID", description = "Retrieve a booking by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    public ResponseEntity<APIResponse<CoachBookingDto>> getBookingById(@PathVariable("id") UUID bookingId) {
        CoachBookingDto booking = coachBookingService.getBookingById(bookingId);
        return ResponseEntity.ok(APIResponse.success("Booking retrieved successfully", booking));
    }

    @GetMapping("/coach/{coachId}")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    @Operation(summary = "Get bookings for a coach", description = "Retrieve all bookings for a specific coach")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachBookingDto>>> getBookingsByCoach(
            @PathVariable("coachId") UUID coachId) {
        List<CoachBookingDto> bookings = coachBookingService.getBookingsByCoach(coachId);
        return ResponseEntity.ok(APIResponse.success("Bookings retrieved successfully", bookings));
    }

    @GetMapping("/trainee/{traineeId}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'ADMIN')")
    @Operation(summary = "Get bookings for a trainee", description = "Retrieve all bookings for a specific trainee")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachBookingDto>>> getBookingsByTrainee(
            @PathVariable("traineeId") UUID traineeId) {
        List<CoachBookingDto> bookings = coachBookingService.getBookingsByTrainee(traineeId);
        return ResponseEntity.ok(APIResponse.success("Bookings retrieved successfully", bookings));
    }

    @GetMapping("/coach/{coachId}/status/{status}")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    @Operation(summary = "Get bookings by coach and status", description = "Retrieve bookings for a coach filtered by status")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachBookingDto>>> getBookingsByCoachAndStatus(
            @PathVariable("coachId") UUID coachId,
            @PathVariable("status") BookingStatus status) {
        List<CoachBookingDto> bookings = coachBookingService.getBookingsByCoachAndStatus(coachId, status);
        return ResponseEntity.ok(APIResponse.success("Bookings retrieved successfully", bookings));
    }

    @GetMapping("/trainee/{traineeId}/status/{status}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'ADMIN')")
    @Operation(summary = "Get bookings by trainee and status", description = "Retrieve bookings for a trainee filtered by status")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachBookingDto>>> getBookingsByTraineeAndStatus(
            @PathVariable("traineeId") UUID traineeId,
            @PathVariable("status") BookingStatus status) {
        List<CoachBookingDto> bookings = coachBookingService.getBookingsByTraineeAndStatus(traineeId, status);
        return ResponseEntity.ok(APIResponse.success("Bookings retrieved successfully", bookings));
    }

    @GetMapping("/my-bookings/status/{status}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get my bookings by status", description = "Trainee retrieves their own bookings filtered by status")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachBookingDto>>> getMyBookingsByStatus(
            @PathVariable("status") BookingStatus status) {
        UUID traineeId = securityUtil.getCurrentUserId();
        List<CoachBookingDto> bookings = coachBookingService.getBookingsByTraineeAndStatus(traineeId, status);
        return ResponseEntity.ok(APIResponse.success("Your bookings retrieved successfully", bookings));
    }

    @GetMapping("/coach/{coachId}/time-range")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get coach bookings in time range", description = "Retrieve bookings for a coach within a time range")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachBookingDto>>> getBookingsByCoachAndTimeRange(
            @PathVariable("coachId") UUID coachId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        List<CoachBookingDto> bookings = coachBookingService.getBookingsByCoachAndTimeRange(coachId, startTime, endTime);
        return ResponseEntity.ok(APIResponse.success("Bookings retrieved successfully", bookings));
    }

    @GetMapping("/trainee/{traineeId}/time-range")
    @PreAuthorize("hasAnyRole('TRAINEE', 'ADMIN')")
    @Operation(summary = "Get trainee bookings in time range", description = "Retrieve bookings for a trainee within a time range")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachBookingDto>>> getBookingsByTraineeAndTimeRange(
            @PathVariable("traineeId") UUID traineeId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        List<CoachBookingDto> bookings = coachBookingService.getBookingsByTraineeAndTimeRange(traineeId, startTime, endTime);
        return ResponseEntity.ok(APIResponse.success("Bookings retrieved successfully", bookings));
    }

    @GetMapping("/recurring-group/{recurringGroupId}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get bookings by recurring group ID", description = "Retrieve all bookings in the same recurring group (batch booking)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachBookingDto>>> getBookingsByRecurringGroup(
            @PathVariable("recurringGroupId") UUID recurringGroupId) {
        List<CoachBookingDto> bookings = coachBookingService.getBookingsByRecurringGroup(recurringGroupId);
        return ResponseEntity.ok(APIResponse.success("Recurring group bookings retrieved successfully", bookings));
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get my bookings", description = "Trainee retrieves their own bookings")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachBookingDto>>> getMyBookings() {
        UUID traineeId = securityUtil.getCurrentUserId();
        List<CoachBookingDto> bookings = coachBookingService.getBookingsByTrainee(traineeId);
        return ResponseEntity.ok(APIResponse.success("Your bookings retrieved successfully", bookings));
    }

    @GetMapping("/my-coaching-sessions")
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Get my coaching sessions", description = "Coach retrieves their coaching sessions")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachBookingDto>>> getMyCoachingSessions() {
        UUID coachId = securityUtil.getCurrentUserId();
        List<CoachBookingDto> bookings = coachBookingService.getBookingsByCoach(coachId);
        return ResponseEntity.ok(APIResponse.success("Your coaching sessions retrieved successfully", bookings));
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH')")
    @Operation(summary = "Cancel booking", description = "Cancel a booking (must be at least 24 hours in advance)")
    @ApiResponse(responseCode = "200", description = "Booking cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Cannot cancel booking")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    public ResponseEntity<APIResponse<Void>> cancelBooking(@PathVariable("id") UUID bookingId) {
        UUID userId = securityUtil.getCurrentUserId();
        coachBookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.ok(APIResponse.success("Booking cancelled successfully", null));
    }

    @PostMapping("/{id}/coach-join")
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Coach joins session", description = "Coach marks join intent. Booking moves to IN_PROGRESS only when both users join in live session.")
    @ApiResponse(responseCode = "200", description = "Coach joined successfully")
    @ApiResponse(responseCode = "400", description = "Session not ready")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    public ResponseEntity<APIResponse<Void>> coachJoinSession(@PathVariable("id") UUID bookingId) {
        UUID coachId = securityUtil.getCurrentUserId();
        coachBookingService.coachJoinSession(bookingId, coachId);
        return ResponseEntity.ok(APIResponse.success("Coach joined session successfully", null));
    }

    @PostMapping("/{id}/trainee-join")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Trainee joins session", description = "Trainee marks join intent. Booking moves to IN_PROGRESS only when both users join in live session.")
    @ApiResponse(responseCode = "200", description = "Trainee joined successfully")
    @ApiResponse(responseCode = "400", description = "Session not ready")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    public ResponseEntity<APIResponse<Void>> traineeJoinSession(@PathVariable("id") UUID bookingId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        coachBookingService.traineeJoinSession(bookingId, traineeId);
        return ResponseEntity.ok(APIResponse.success("Trainee joined session successfully", null));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH')")
    @Operation(summary = "Complete booking", description = "Mark booking as completed when booking end time is reached.")
    @ApiResponse(responseCode = "200", description = "Booking completed successfully")
    @ApiResponse(responseCode = "400", description = "Booking not in progress")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    public ResponseEntity<APIResponse<Void>> completeBooking(@PathVariable("id") UUID bookingId) {
        UUID userId = securityUtil.getCurrentUserId();
        coachBookingService.completeBooking(bookingId, userId);
        return ResponseEntity.ok(APIResponse.success("Booking completed successfully", null));
    }

    @GetMapping("/trainee/schedule-view")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get busy time slots for trainee",
            description = "Get a simple list of all busy time slots (startTime, endTime) for a specific coach. " +
                    "Includes coach's time offs, coach's active bookings, and trainee's own bookings. " +
                    "Excludes cancelled and refunded bookings. Optional time range filter."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved busy time slots")
    @ApiResponse(responseCode = "404", description = "Trainee or Coach not found")
    public ResponseEntity<APIResponse<List<BusyTimeSlot>>> getTraineeScheduleView(
            @RequestParam("coachId") UUID coachId,
            @RequestParam(value = "startTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(value = "endTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        UUID traineeId = securityUtil.getCurrentUserId();
        List<BusyTimeSlot> busySlots = coachBookingService.getTraineeScheduleView(
                traineeId, coachId, startTime, endTime);
        return ResponseEntity.ok(APIResponse.success("Busy time slots retrieved successfully", busySlots));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all bookings (Admin)", description = "Admin retrieves all bookings")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachBookingDto>>> getAllBookings() {
        List<CoachBookingDto> bookings = coachBookingService.getAllBookings();
        return ResponseEntity.ok(APIResponse.success("All bookings retrieved successfully", bookings));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update booking status (Admin)", description = "Admin updates booking status")
    @ApiResponse(responseCode = "200", description = "Status updated successfully")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    public ResponseEntity<APIResponse<Void>> updateBookingStatus(
            @PathVariable("id") UUID bookingId,
            @RequestParam("status") BookingStatus status) {
        coachBookingService.updateBookingStatus(bookingId, status);
        return ResponseEntity.ok(APIResponse.success("Booking status updated successfully", null));
    }
}


