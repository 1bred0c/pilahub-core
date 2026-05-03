package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.NotificationDto;
import fpt.edu.sep490.pilahub.dto.request.AdminBroadcastNotificationRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.NotificationService;
import fpt.edu.sep490.pilahub.service.PushNotificationService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management and retrieval")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;
    private final PushNotificationService pushService;

    // ─── REST endpoints ───────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get my notifications", description = "Returns a paginated list of the authenticated user's notifications, newest first.")
    @ApiResponse(responseCode = "200", description = "Notifications retrieved")
    public ResponseEntity<APIResponse<Page<NotificationDto>>> getNotifications(
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "20") @RequestParam(defaultValue = "20") int size) {
        UUID accountId = securityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotificationDto> result = notificationService.getNotifications(accountId, pageable);
        return ResponseEntity.ok(APIResponse.success("Notifications retrieved", result));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count", description = "Returns the number of unread notifications for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Unread count retrieved")
    public ResponseEntity<APIResponse<Long>> getUnreadCount() {
        UUID accountId = securityUtil.getCurrentUserId();
        long count = notificationService.getUnreadCount(accountId);
        return ResponseEntity.ok(APIResponse.success("Unread count retrieved", count));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a single notification as read. Only the owner can perform this action.")
    @ApiResponse(responseCode = "200", description = "Notification marked as read")
    @ApiResponse(responseCode = "404", description = "Notification not found or not owned by user")
    public ResponseEntity<APIResponse<NotificationDto>> markAsRead(
            @PathVariable UUID notificationId) {
        UUID accountId = securityUtil.getCurrentUserId();
        NotificationDto dto = notificationService.markAsRead(notificationId, accountId);
        return ResponseEntity.ok(APIResponse.success("Notification marked as read", dto));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks every unread notification of the authenticated user as read.")
    @ApiResponse(responseCode = "200", description = "All notifications marked as read")
    public ResponseEntity<APIResponse<Void>> markAllAsRead() {
        UUID accountId = securityUtil.getCurrentUserId();
        notificationService.markAllAsRead(accountId);
        return ResponseEntity.ok(APIResponse.success("All notifications marked as read", null));
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a notification", description = "Permanently deletes a notification. Only the owner can perform this action.")
    @ApiResponse(responseCode = "200", description = "Notification deleted")
    @ApiResponse(responseCode = "404", description = "Notification not found or not owned by user")
    public ResponseEntity<APIResponse<Void>> deleteNotification(
            @PathVariable UUID notificationId) {
        UUID accountId = securityUtil.getCurrentUserId();
        notificationService.deleteNotification(notificationId, accountId);
        return ResponseEntity.ok(APIResponse.success("Notification deleted", null));
    }

    @PostMapping("/admin/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Broadcast admin notification", description = "Creates and sends an admin notification to all active users; pushes to each user's FCM token if present.")
    @ApiResponse(responseCode = "200", description = "Notification broadcast successfully")
    public ResponseEntity<APIResponse<Integer>> broadcastAdminNotification(
            @Valid @RequestBody AdminBroadcastNotificationRequest request) {
        int totalSent = notificationService.broadcastAdminNotification(
                request.title(),
                request.message(),
                null,
                null);
        return ResponseEntity.ok(APIResponse.success("Broadcast notification sent", totalSent));
    }

    @PostMapping("/push")
    public String push(@RequestParam String token) throws Exception {
        pushService.sendPush(token, "Hello", "Test từ Spring Boot 🚀",
                "abc");
        return "Sent!";
    }
}
