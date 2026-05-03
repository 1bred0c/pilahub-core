 package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.MessageDto;
import fpt.edu.sep490.pilahub.dto.request.message.SendMessageRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.MessageService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "1-to-1 message management")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @Operation(summary = "Send a message", description = "Create a new message and auto create conversation if needed")
    @ApiResponse(responseCode = "200", description = "Message sent")
    public ResponseEntity<APIResponse<MessageDto>> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        UUID senderId = securityUtil.getCurrentUserId();
        MessageDto result = messageService.sendMessage(senderId, request);
        return ResponseEntity.ok(APIResponse.success("Message sent", result));
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "Revoke message", description = "Soft delete a sent message. Only sender can revoke")
    @ApiResponse(responseCode = "200", description = "Message revoked")
    public ResponseEntity<APIResponse<Void>> deleteMessage(@PathVariable UUID messageId) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        messageService.deleteMessage(messageId, currentUserId);
        return ResponseEntity.ok(APIResponse.success("Message revoked", null));
    }
}

