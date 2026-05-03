package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.SubscriptionDto;
import fpt.edu.sep490.pilahub.dto.request.SubscribePackageRequest;
import fpt.edu.sep490.pilahub.dto.request.UpgradePackageRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.dto.response.UpgradePackageResponse;
import fpt.edu.sep490.pilahub.dto.response.UpgradeablePackageDto;
import fpt.edu.sep490.pilahub.service.SubscriptionService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription Management", description = "APIs for managing user subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SecurityUtil securityUtil;

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Subscribe to a package (Trainee only)",
            description = "Subscribe to a package. Trainee access required. Deducts package price from wallet."
    )
    @ApiResponse(responseCode = "201", description = "Subscription created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or already has active subscription")
    @ApiResponse(responseCode = "403", description = "Forbidden - Trainee access required")
    @ApiResponse(responseCode = "404", description = "Package or Account not found")
    public ResponseEntity<APIResponse<SubscriptionDto>> subscribePackage(
            @Valid @RequestBody SubscribePackageRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();
        SubscriptionDto subscriptionDto = subscriptionService.subscribePackage(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Subscription created successfully", subscriptionDto));
    }

    @PostMapping("/upgrade")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Upgrade to a new package (Trainee only)",
            description = "Upgrade current active subscription to a new package. Calculates proration refund and charges difference."
    )
    @ApiResponse(responseCode = "200", description = "Subscription upgraded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input, no active subscription, or package price is lower")
    @ApiResponse(responseCode = "403", description = "Forbidden - Trainee access required")
    @ApiResponse(responseCode = "404", description = "Package or Account not found")
    public ResponseEntity<APIResponse<UpgradePackageResponse>> upgradePackage(
            @Valid @RequestBody UpgradePackageRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();
        UpgradePackageResponse response = subscriptionService.upgradePackage(accountId, request);
        return ResponseEntity.ok(APIResponse.success("Subscription upgraded successfully", response));
    }

    @GetMapping("/{subscriptionId}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'ADMIN')")
    @Operation(
            summary = "Get subscription by ID",
            description = "Retrieve subscription details by subscription ID."
    )
    @ApiResponse(responseCode = "200", description = "Subscription found")
    @ApiResponse(responseCode = "404", description = "Subscription not found")
    public ResponseEntity<APIResponse<SubscriptionDto>> getSubscriptionById(
            @PathVariable UUID subscriptionId) {
        SubscriptionDto subscriptionDto = subscriptionService.getSubscriptionById(subscriptionId);
        return ResponseEntity.ok(APIResponse.success("Subscription retrieved successfully", subscriptionDto));
    }

    @GetMapping("/my-subscriptions")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get my subscriptions (Trainee only)",
            description = "Retrieve all subscriptions for the authenticated trainee."
    )
    @ApiResponse(responseCode = "200", description = "Subscriptions retrieved successfully")
    public ResponseEntity<APIResponse<List<SubscriptionDto>>> getMySubscriptions() {
        UUID accountId = securityUtil.getCurrentUserId();
        List<SubscriptionDto> subscriptions = subscriptionService.getSubscriptionsByAccountId(accountId);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d subscription(s)", subscriptions.size()),
                subscriptions
        ));
    }

    @GetMapping("/my-active-subscription")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get my active subscription (Trainee only)",
            description = "Retrieve the active subscription for the authenticated trainee."
    )
    @ApiResponse(responseCode = "200", description = "Active subscription found")
    @ApiResponse(responseCode = "404", description = "No active subscription found")
    public ResponseEntity<APIResponse<SubscriptionDto>> getMyActiveSubscription() {
        UUID accountId = securityUtil.getCurrentUserId();
        SubscriptionDto subscriptionDto = subscriptionService.getActiveSubscriptionByAccountId(accountId);
        return ResponseEntity.ok(APIResponse.success("Active subscription retrieved successfully", subscriptionDto));
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get subscriptions by account ID (Admin only)",
            description = "Retrieve all subscriptions for a specific account. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Subscriptions retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<APIResponse<List<SubscriptionDto>>> getSubscriptionsByAccountId(
            @PathVariable UUID accountId) {
        List<SubscriptionDto> subscriptions = subscriptionService.getSubscriptionsByAccountId(accountId);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d subscription(s) for account", subscriptions.size()),
                subscriptions
        ));
    }

    @GetMapping("/account/{accountId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get active subscription by account ID (Admin only)",
            description = "Retrieve the active subscription for a specific account. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Active subscription found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Account or active subscription not found")
    public ResponseEntity<APIResponse<SubscriptionDto>> getActiveSubscriptionByAccountId(
            @PathVariable UUID accountId) {
        SubscriptionDto subscriptionDto = subscriptionService.getActiveSubscriptionByAccountId(accountId);
        return ResponseEntity.ok(APIResponse.success("Active subscription retrieved successfully", subscriptionDto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all subscriptions with pagination (Admin only)",
            description = "Retrieve a paginated list of all subscriptions. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Subscriptions retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<Page<SubscriptionDto>>> getAllSubscriptions(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SubscriptionDto> subscriptions = subscriptionService.getAllSubscriptions(pageable);

        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d subscription(s) from page %d of %d",
                        subscriptions.getNumberOfElements(),
                        subscriptions.getNumber() + 1,
                        subscriptions.getTotalPages()),
                subscriptions
        ));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get subscriptions by status (Admin only)",
            description = "Retrieve all subscriptions with a specific status (ACTIVE, EXPIRED, UPGRADED). Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Subscriptions retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<List<SubscriptionDto>>> getSubscriptionsByStatus(
            @PathVariable String status) {
        List<SubscriptionDto> subscriptions = subscriptionService.getSubscriptionsByStatus(status);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d subscription(s) with status: %s", subscriptions.size(), status),
                subscriptions
        ));
    }

    @PatchMapping("/{subscriptionId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cancel subscription by ID (Admin only)",
            description = "Cancel an active subscription by changing its status to EXPIRED. Only ACTIVE subscriptions can be cancelled. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Subscription cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Subscription is not in ACTIVE status")
    @ApiResponse(responseCode = "404", description = "Subscription not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<SubscriptionDto>> cancelSubscription(
            @PathVariable UUID subscriptionId) {
        SubscriptionDto subscriptionDto = subscriptionService.cancelSubscription(subscriptionId);
        return ResponseEntity.ok(APIResponse.success("Subscription cancelled successfully", subscriptionDto));
    }

    @GetMapping("/upgradeable-packages")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get available packages for upgrade with discounted prices (Trainee only)",
            description = "Retrieve all packages that can be upgraded to, with final prices after applying proration credit from current subscription. Only packages with price >= current package price are shown."
    )
    @ApiResponse(responseCode = "200", description = "Upgradeable packages retrieved successfully")
    @ApiResponse(responseCode = "400", description = "No active subscription found")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Trainee access required")
    public ResponseEntity<APIResponse<List<UpgradeablePackageDto>>> getUpgradeablePackages() {
        UUID accountId = securityUtil.getCurrentUserId();
        List<UpgradeablePackageDto> packages = subscriptionService.getUpgradeablePackages(accountId);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Found %d upgradeable package(s) with discounted prices", packages.size()),
                packages
        ));
    }
}
