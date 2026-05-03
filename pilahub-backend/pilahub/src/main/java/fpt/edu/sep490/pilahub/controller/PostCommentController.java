package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.PostCommentDto;
import fpt.edu.sep490.pilahub.dto.PostCommentReplyDto;
import fpt.edu.sep490.pilahub.dto.request.post.CreatePostCommentRequest;
import fpt.edu.sep490.pilahub.dto.request.post.UpdatePostCommentRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.PostCommentService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/post-comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Post Comment", description = "Manage comments and replies for posts")
public class PostCommentController {

    private final PostCommentService postCommentService;
    private final SecurityUtil securityUtil;

    @PostMapping("/post/{postId}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Create comment/reply", description = "Create a top-level comment or reply. Reply depth is capped at 1.")
    @ApiResponse(responseCode = "201", description = "Comment created successfully")
    public ResponseEntity<APIResponse<PostCommentDto>> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CreatePostCommentRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();
        PostCommentDto comment = postCommentService.createComment(accountId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Comment created successfully", comment));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get comment by ID", description = "Retrieve a comment by ID")
    @ApiResponse(responseCode = "200", description = "Comment retrieved successfully")
    public ResponseEntity<APIResponse<PostCommentDto>> getById(@PathVariable("id") UUID commentId) {
        return ResponseEntity.ok(APIResponse.success("Comment retrieved successfully", postCommentService.getById(commentId)));
    }

    @GetMapping("/post/{postId}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get latest root comments", description = "Get latest root comments of post. Default 5 roots, each has max 3 replies and hasMoreReplies flag.")
    @ApiResponse(responseCode = "200", description = "Comments retrieved successfully")
    public ResponseEntity<APIResponse<List<PostCommentDto>>> getLatestRootComments(
            @PathVariable UUID postId,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        List<PostCommentDto> comments = postCommentService.getPostLatestRootComments(postId, limit);
        return ResponseEntity.ok(APIResponse.success("Comments retrieved successfully", comments));
    }

    @GetMapping("/post/{postId}/all")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get all root comments", description = "Get all root comments of post. Each root contains max 3 reply preview and hasMoreReplies flag.")
    @ApiResponse(responseCode = "200", description = "All comments retrieved successfully")
    public ResponseEntity<APIResponse<List<PostCommentDto>>> getAllRootComments(@PathVariable UUID postId) {
        List<PostCommentDto> comments = postCommentService.getAllPostRootComments(postId);
        return ResponseEntity.ok(APIResponse.success("All comments retrieved successfully", comments));
    }

    @GetMapping("/{id}/replies")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get all replies of root comment", description = "Get full reply list for one root comment")
    @ApiResponse(responseCode = "200", description = "Replies retrieved successfully")
    public ResponseEntity<APIResponse<List<PostCommentReplyDto>>> getReplies(@PathVariable("id") UUID rootCommentId) {
        List<PostCommentReplyDto> replies = postCommentService.getAllRepliesOfRootComment(rootCommentId);
        return ResponseEntity.ok(APIResponse.success("Replies retrieved successfully", replies));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get my comments", description = "Get all comments created by current user")
    @ApiResponse(responseCode = "200", description = "Comments retrieved successfully")
    public ResponseEntity<APIResponse<List<PostCommentDto>>> getMyComments() {
        UUID accountId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(APIResponse.success("Your comments retrieved successfully", postCommentService.getMyComments(accountId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Update my comment", description = "Update content of current user's own comment")
    @ApiResponse(responseCode = "200", description = "Comment updated successfully")
    public ResponseEntity<APIResponse<PostCommentDto>> updateComment(
            @PathVariable("id") UUID commentId,
            @Valid @RequestBody UpdatePostCommentRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();
        PostCommentDto updated = postCommentService.updateComment(accountId, commentId, request);
        return ResponseEntity.ok(APIResponse.success("Comment updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Delete my comment", description = "Delete current user's own comment")
    @ApiResponse(responseCode = "200", description = "Comment deleted successfully")
    public ResponseEntity<APIResponse<Void>> deleteComment(@PathVariable("id") UUID commentId) {
        UUID accountId = securityUtil.getCurrentUserId();
        postCommentService.deleteComment(accountId, commentId);
        return ResponseEntity.ok(APIResponse.success("Comment deleted successfully", null));
    }
}

