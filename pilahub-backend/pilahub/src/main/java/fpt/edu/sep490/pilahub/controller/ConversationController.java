package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.ConversationDetailDto;
import fpt.edu.sep490.pilahub.dto.ConversationInboxDto;
import fpt.edu.sep490.pilahub.dto.MessageDto;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.ConversationService;
import fpt.edu.sep490.pilahub.service.MessageService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "1-to-1 conversation management")
@SecurityRequirement(name = "bearerAuth")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final SecurityUtil securityUtil;

    @GetMapping
    @Operation(summary = "Get inbox", description = "Get paginated conversations of current user sorted by latest message time")
    @ApiResponse(responseCode = "200", description = "Inbox retrieved")
    public ResponseEntity<APIResponse<Page<ConversationInboxDto>>> getInbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID currentUserId = securityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"));
        Page<ConversationInboxDto> result = conversationService.getInbox(currentUserId, pageable);
        return ResponseEntity.ok(APIResponse.success("Inbox retrieved", result));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread message count", description = "Get unread messages across all conversations")
    @ApiResponse(responseCode = "200", description = "Unread count retrieved")
    public ResponseEntity<APIResponse<Long>> getUnreadCount() {
        UUID currentUserId = securityUtil.getCurrentUserId();
        long count = conversationService.getTotalUnreadCount(currentUserId);
        return ResponseEntity.ok(APIResponse.success("Unread count retrieved", count));
    }

    @GetMapping("/user/{receiverId}")
    @Operation(summary = "Get conversation by user", description = "Get existing conversation with a specific user without auto-create")
    @ApiResponse(responseCode = "200", description = "Conversation lookup completed")
    public ResponseEntity<APIResponse<ConversationDetailDto>> getConversationByUser(@PathVariable UUID receiverId) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        ConversationDetailDto data = conversationService.getConversationByUser(currentUserId, receiverId).orElse(null);
        return ResponseEntity.ok(APIResponse.success("Conversation lookup completed", data));
    }

    @PutMapping("/{conversationId}/read")
    @Operation(summary = "Mark conversation read", description = "Mark all messages in a conversation as read for current user")
    @ApiResponse(responseCode = "200", description = "Conversation marked as read")
    public ResponseEntity<APIResponse<Void>> markConversationAsRead(@PathVariable UUID conversationId) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        conversationService.markConversationAsRead(conversationId, currentUserId);
        return ResponseEntity.ok(APIResponse.success("Conversation marked as read", null));
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Get messages by conversation", description = "Get paginated messages sorted by latest first")
    @ApiResponse(responseCode = "200", description = "Messages retrieved")
    public ResponseEntity<APIResponse<Page<MessageDto>>> getConversationMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID currentUserId = securityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
        Page<MessageDto> result = messageService.getConversationMessages(conversationId, currentUserId, pageable);
        return ResponseEntity.ok(APIResponse.success("Messages retrieved", result));
    }
}

