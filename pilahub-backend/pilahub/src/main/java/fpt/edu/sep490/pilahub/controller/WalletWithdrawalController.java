package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.WalletWithdrawalDto;
import fpt.edu.sep490.pilahub.dto.request.ApproveWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.request.CompleteWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.request.CreateWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.request.RejectWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.dto.response.BankInfoDto;
import fpt.edu.sep490.pilahub.enums.WalletWithdrawalStatus;
import fpt.edu.sep490.pilahub.service.WalletWithdrawalService;
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
@RequestMapping("/api/wallet-withdrawals")
@RequiredArgsConstructor
@Tag(name = "Wallet Withdrawal", description = "Wallet withdrawal management APIs for users and admins")
public class WalletWithdrawalController {

    private final WalletWithdrawalService withdrawalService;

    // ============= PUBLIC/USER ENDPOINTS =============

    @GetMapping("/banks")
    @Operation(
            summary = "Get list of banks",
            description = "Retrieve list of banks from VietQR API for withdrawal"
    )
    @ApiResponse(responseCode = "200", description = "Bank list retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    public ResponseEntity<APIResponse<List<BankInfoDto>>> getBankList() {
        List<BankInfoDto> banks = withdrawalService.getBankList();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d bank(s) successfully", banks.size()),
                banks
        ));
    }

    @PostMapping
    @Operation(
            summary = "Create withdrawal request",
            description = "Create a new withdrawal request. Status will be PENDING."
    )
    @ApiResponse(responseCode = "201", description = "Withdrawal request created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    public ResponseEntity<APIResponse<WalletWithdrawalDto>> createWithdrawal(
            @Valid @RequestBody CreateWithdrawalRequest request) {
        WalletWithdrawalDto withdrawal = withdrawalService.createWithdrawal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Withdrawal request created successfully", withdrawal));
    }

    @GetMapping("/my-withdrawals")
    @Operation(
            summary = "Get own withdrawals",
            description = "Retrieve all withdrawal requests for the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Withdrawals retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    public ResponseEntity<APIResponse<List<WalletWithdrawalDto>>> getMyWithdrawals() {
        List<WalletWithdrawalDto> withdrawals = withdrawalService.getMyWithdrawals();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d withdrawal(s) successfully", withdrawals.size()),
                withdrawals
        ));
    }

    @GetMapping("/my-withdrawals/{withdrawalId}")
    @Operation(
            summary = "Get own withdrawal by ID",
            description = "Retrieve a specific withdrawal request by ID for the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Withdrawal retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Withdrawal not found")
    public ResponseEntity<APIResponse<WalletWithdrawalDto>> getMyWithdrawalById(
            @PathVariable UUID withdrawalId) {
        WalletWithdrawalDto withdrawal = withdrawalService.getMyWithdrawalById(withdrawalId);
        return ResponseEntity.ok(APIResponse.success("Withdrawal retrieved successfully", withdrawal));
    }

    @GetMapping("/my-withdrawals/by-status")
    @Operation(
            summary = "Get own withdrawals by status",
            description = "Retrieve withdrawal requests filtered by status for the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Withdrawals retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    public ResponseEntity<APIResponse<List<WalletWithdrawalDto>>> getMyWithdrawalsByStatus(
            @RequestParam WalletWithdrawalStatus status) {
        List<WalletWithdrawalDto> withdrawals = withdrawalService.getMyWithdrawalsByStatus(status);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d withdrawal(s) with status %s", withdrawals.size(), status),
                withdrawals
        ));
    }

    @PutMapping("/my-withdrawals/{withdrawalId}")
    @Operation(
            summary = "Update pending withdrawal",
            description = "Update a withdrawal request. Only PENDING withdrawals can be updated."
    )
    @ApiResponse(responseCode = "200", description = "Withdrawal updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or withdrawal not in PENDING status")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Withdrawal not found")
    public ResponseEntity<APIResponse<WalletWithdrawalDto>> updateWithdrawal(
            @PathVariable UUID withdrawalId,
            @Valid @RequestBody UpdateWithdrawalRequest request) {
        WalletWithdrawalDto withdrawal = withdrawalService.updateWithdrawal(withdrawalId, request);
        return ResponseEntity.ok(APIResponse.success("Withdrawal updated successfully", withdrawal));
    }

    @PatchMapping("/my-withdrawals/{withdrawalId}/cancel")
    @Operation(
            summary = "Cancel pending withdrawal",
            description = "Cancel a withdrawal request. Only PENDING withdrawals can be cancelled. " +
                    "Status will change to CANCELLED and cannot be changed again."
    )
    @ApiResponse(responseCode = "200", description = "Withdrawal cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Withdrawal not in PENDING status")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Withdrawal not found")
    public ResponseEntity<APIResponse<WalletWithdrawalDto>> cancelWithdrawal(
            @PathVariable UUID withdrawalId) {
        WalletWithdrawalDto withdrawal = withdrawalService.cancelWithdrawal(withdrawalId);
        return ResponseEntity.ok(APIResponse.success("Withdrawal cancelled successfully", withdrawal));
    }

    // ============= ADMIN ENDPOINTS =============

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all withdrawals (Admin only)",
            description = "Retrieve all withdrawal requests in the system. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Withdrawals retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<List<WalletWithdrawalDto>>> getAllWithdrawals() {
        List<WalletWithdrawalDto> withdrawals = withdrawalService.getAllWithdrawals();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d withdrawal(s) successfully", withdrawals.size()),
                withdrawals
        ));
    }

    @GetMapping("/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get withdrawals by status (Admin only)",
            description = "Retrieve withdrawal requests filtered by status. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Withdrawals retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<List<WalletWithdrawalDto>>> getWithdrawalsByStatus(
            @RequestParam WalletWithdrawalStatus status) {
        List<WalletWithdrawalDto> withdrawals = withdrawalService.getWithdrawalsByStatus(status);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d withdrawal(s) with status %s", withdrawals.size(), status),
                withdrawals
        ));
    }

    @GetMapping("/{withdrawalId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get withdrawal by ID (Admin only)",
            description = "Retrieve a specific withdrawal request by ID. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Withdrawal retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Withdrawal not found")
    public ResponseEntity<APIResponse<WalletWithdrawalDto>> getWithdrawalById(
            @PathVariable UUID withdrawalId) {
        WalletWithdrawalDto withdrawal = withdrawalService.getWithdrawalById(withdrawalId);
        return ResponseEntity.ok(APIResponse.success("Withdrawal retrieved successfully", withdrawal));
    }

    @PatchMapping("/{withdrawalId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Approve withdrawal (Admin only)",
            description = "Approve a PENDING withdrawal. Locks the withdrawal amount from available balance. " +
                    "Status will change to APPROVED. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Withdrawal approved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or withdrawal not in PENDING status or insufficient balance")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Withdrawal not found")
    public ResponseEntity<APIResponse<WalletWithdrawalDto>> approveWithdrawal(
            @PathVariable UUID withdrawalId,
            @Valid @RequestBody(required = false) ApproveWithdrawalRequest request) {
        ApproveWithdrawalRequest approveRequest = request != null ? request : new ApproveWithdrawalRequest(null);
        WalletWithdrawalDto withdrawal = withdrawalService.approveWithdrawal(withdrawalId, approveRequest);
        return ResponseEntity.ok(APIResponse.success("Withdrawal approved successfully", withdrawal));
    }

    @PatchMapping("/{withdrawalId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Reject withdrawal (Admin only)",
            description = "Reject a PENDING withdrawal. Status will change to REJECTED and cannot be changed again. " +
                    "Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Withdrawal rejected successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or withdrawal not in PENDING status")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Withdrawal not found")
    public ResponseEntity<APIResponse<WalletWithdrawalDto>> rejectWithdrawal(
            @PathVariable UUID withdrawalId,
            @Valid @RequestBody(required = false) RejectWithdrawalRequest request) {
        RejectWithdrawalRequest rejectRequest = request != null ? request : new RejectWithdrawalRequest(null);
        WalletWithdrawalDto withdrawal = withdrawalService.rejectWithdrawal(withdrawalId, rejectRequest);
        return ResponseEntity.ok(APIResponse.success("Withdrawal rejected successfully", withdrawal));
    }

    @PatchMapping("/{withdrawalId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Complete withdrawal (Admin only)",
            description = "Complete an APPROVED withdrawal after successfully transferring funds externally. " +
                    "Deducts from locked balance and total balance. Creates transaction record. " +
                    "Status will change to COMPLETED and cannot be changed again. Receipt URL is required. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Withdrawal completed successfully")
    @ApiResponse(responseCode = "400", description = "Withdrawal not in APPROVED status")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Withdrawal not found")
    public ResponseEntity<APIResponse<WalletWithdrawalDto>> completeWithdrawal(
            @PathVariable UUID withdrawalId,
            @Valid @RequestBody CompleteWithdrawalRequest request) {
        WalletWithdrawalDto withdrawal = withdrawalService.completeWithdrawal(withdrawalId, request);
        return ResponseEntity.ok(APIResponse.success("Withdrawal completed successfully", withdrawal));
    }
}
