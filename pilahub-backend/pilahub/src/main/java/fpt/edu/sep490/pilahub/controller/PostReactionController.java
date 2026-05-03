package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.PostReactionDto;
import fpt.edu.sep490.pilahub.dto.PostReactionSummaryDto;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.PostReactionService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/post-reactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Post Reaction", description = "Manage reactions for posts")
public class PostReactionController {

    private final PostReactionService postReactionService;
    private final SecurityUtil securityUtil;

    @PostMapping("/post/{postId}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "React to post", description = "Create reaction for current user if not existed")
    @ApiResponse(responseCode = "201", description = "Reaction created successfully")
    public ResponseEntity<APIResponse<PostReactionDto>> react(@PathVariable UUID postId) {
        UUID accountId = securityUtil.getCurrentUserId();
        PostReactionDto reaction = postReactionService.react(accountId, postId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Reaction processed successfully", reaction));
    }

    @DeleteMapping("/post/{postId}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Remove reaction", description = "Remove current user's reaction from post")
    @ApiResponse(responseCode = "200", description = "Reaction removed successfully")
    public ResponseEntity<APIResponse<Void>> unreact(@PathVariable UUID postId) {
        UUID accountId = securityUtil.getCurrentUserId();
        postReactionService.unreact(accountId, postId);
        return ResponseEntity.ok(APIResponse.success("Reaction removed successfully", null));
    }

    @PostMapping("/post/{postId}/toggle")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Toggle reaction", description = "Toggle like/unlike and return latest summary")
    @ApiResponse(responseCode = "200", description = "Reaction toggled successfully")
    public ResponseEntity<APIResponse<PostReactionSummaryDto>> toggle(@PathVariable UUID postId) {
        UUID accountId = securityUtil.getCurrentUserId();
        PostReactionSummaryDto summary = postReactionService.toggleReaction(accountId, postId);
        return ResponseEntity.ok(APIResponse.success("Reaction toggled successfully", summary));
    }

    @GetMapping("/post/{postId}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get post reactions", description = "Get all reactions of a post")
    @ApiResponse(responseCode = "200", description = "Reactions retrieved successfully")
    public ResponseEntity<APIResponse<List<PostReactionDto>>> getPostReactions(@PathVariable UUID postId) {
        List<PostReactionDto> reactions = postReactionService.getPostReactions(postId);
        return ResponseEntity.ok(APIResponse.success("Reactions retrieved successfully", reactions));
    }

    @GetMapping("/post/{postId}/summary")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get reaction summary", description = "Get total reactions and current user reaction status")
    @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    public ResponseEntity<APIResponse<PostReactionSummaryDto>> getSummary(@PathVariable UUID postId) {
        UUID accountId = securityUtil.getCurrentUserId();
        PostReactionSummaryDto summary = postReactionService.getReactionSummary(accountId, postId);
        return ResponseEntity.ok(APIResponse.success("Reaction summary retrieved successfully", summary));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get my reactions", description = "Get all reactions created by current user")
    @ApiResponse(responseCode = "200", description = "Reactions retrieved successfully")
    public ResponseEntity<APIResponse<List<PostReactionDto>>> getMyReactions() {
        UUID accountId = securityUtil.getCurrentUserId();
        List<PostReactionDto> reactions = postReactionService.getMyReactions(accountId);
        return ResponseEntity.ok(APIResponse.success("Your reactions retrieved successfully", reactions));
    }
}

