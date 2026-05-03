package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.PostDto;
import fpt.edu.sep490.pilahub.dto.request.post.CreatePostRequest;
import fpt.edu.sep490.pilahub.dto.request.post.UpdatePostRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.PostService;
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
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Post", description = "Manage coach posts")
public class PostController {

    private final PostService postService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Create post", description = "Coach creates a new post with optional media list")
    @ApiResponse(responseCode = "201", description = "Post created successfully")
    public ResponseEntity<APIResponse<PostDto>> createPost(@Valid @RequestBody CreatePostRequest request) {
        UUID coachId = securityUtil.getCurrentUserId();
        PostDto post = postService.createPost(coachId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Post created successfully", post));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get post by ID", description = "Retrieve a post by ID")
    @ApiResponse(responseCode = "200", description = "Post retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Post not found")
    public ResponseEntity<APIResponse<PostDto>> getById(@PathVariable("id") UUID postId) {
        PostDto post = postService.getById(postId);
        return ResponseEntity.ok(APIResponse.success("Post retrieved successfully", post));
    }

    @GetMapping("/coach/{coachId}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get posts by coach", description = "Retrieve all posts of one coach")
    @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
    public ResponseEntity<APIResponse<List<PostDto>>> getByCoachId(@PathVariable UUID coachId) {
        List<PostDto> posts = postService.getByCoachId(coachId);
        return ResponseEntity.ok(APIResponse.success("Posts retrieved successfully", posts));
    }

    @GetMapping("/my-posts")
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Get my posts", description = "Coach retrieves all of their own posts")
    @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
    public ResponseEntity<APIResponse<List<PostDto>>> getMyPosts() {
        UUID coachId = securityUtil.getCurrentUserId();
        List<PostDto> posts = postService.getMine(coachId);
        return ResponseEntity.ok(APIResponse.success("Your posts retrieved successfully", posts));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Update post", description = "Coach updates their own post and media list")
    @ApiResponse(responseCode = "200", description = "Post updated successfully")
    @ApiResponse(responseCode = "404", description = "Post not found")
    public ResponseEntity<APIResponse<PostDto>> updatePost(
            @PathVariable("id") UUID postId,
            @Valid @RequestBody UpdatePostRequest request) {
        UUID coachId = securityUtil.getCurrentUserId();
        PostDto post = postService.updatePost(postId, coachId, request);
        return ResponseEntity.ok(APIResponse.success("Post updated successfully", post));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Delete post", description = "Coach deletes their own post")
    @ApiResponse(responseCode = "200", description = "Post deleted successfully")
    @ApiResponse(responseCode = "404", description = "Post not found")
    public ResponseEntity<APIResponse<Void>> deletePost(@PathVariable("id") UUID postId) {
        UUID coachId = securityUtil.getCurrentUserId();
        postService.deletePost(postId, coachId);
        return ResponseEntity.ok(APIResponse.success("Post deleted successfully", null));
    }
}

