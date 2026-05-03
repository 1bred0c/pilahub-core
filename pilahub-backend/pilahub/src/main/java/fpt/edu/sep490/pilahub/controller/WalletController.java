package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.WalletDto;
import fpt.edu.sep490.pilahub.dto.TransactionDto;
import fpt.edu.sep490.pilahub.dto.request.AdjustBalanceRequest;
import fpt.edu.sep490.pilahub.dto.request.CreateDepositRequest;
import fpt.edu.sep490.pilahub.dto.request.DeductLockedFundsRequest;
import fpt.edu.sep490.pilahub.dto.request.LockFundsRequest;
import fpt.edu.sep490.pilahub.dto.request.UnlockFundsRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.dto.response.MoMoDepositResponse;
import fpt.edu.sep490.pilahub.dto.response.VNPayDepositResponse;
import fpt.edu.sep490.pilahub.service.MoMoService;
import fpt.edu.sep490.pilahub.service.VNPayService;
import fpt.edu.sep490.pilahub.service.WalletService;
import fpt.edu.sep490.pilahub.service.TransactionService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Wallet management APIs for users and admins")
public class WalletController {

    private final WalletService walletService;
    private final VNPayService vnPayService;
    private final MoMoService moMoService;
    private final TransactionService transactionService;
    private final SecurityUtil securityUtil;

    // ============= USER ENDPOINTS =============

    @GetMapping("/my-wallet")
    @Operation(
            summary = "Get my wallet balance",
            description = "Retrieve wallet balance information for the currently authenticated user. " +
                    "Returns total balance, available balance, and locked balance in VND."
    )
    @ApiResponse(responseCode = "200", description = "Wallet balance retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Wallet not found for the current user")
    public ResponseEntity<APIResponse<WalletDto>> getMyWallet() {
        WalletDto walletDto = walletService.getMyWallet();
        return ResponseEntity.ok(APIResponse.success("Wallet balance retrieved successfully", walletDto));
    }

    @PostMapping("/my-wallet/open")
    @Operation(
            summary = "Open a new wallet",
            description = "Create and activate a new wallet for the currently authenticated user. " +
                    "Each user can only have one wallet."
    )
    @ApiResponse(responseCode = "201", description = "Wallet created successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "409", description = "Wallet already exists for this user")
    public ResponseEntity<APIResponse<WalletDto>> openMyWallet() {
        WalletDto walletDto = walletService.openMyWallet();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Wallet opened successfully", walletDto));
    }

    @PostMapping("/deposit/create")
    @Operation(
            summary = "Create deposit payment URL",
            description = "Create a VNPay payment URL for depositing funds into wallet. " +
                    "User will be redirected to VNPay payment gateway. Minimum deposit: 10,000 VND."
    )
    @ApiResponse(responseCode = "200", description = "Payment URL created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid deposit amount")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Wallet not found - Please open a wallet first")
    public ResponseEntity<APIResponse<VNPayDepositResponse>> createDepositPaymentUrl(
            @Valid @RequestBody CreateDepositRequest request,
            HttpServletRequest httpRequest) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        VNPayDepositResponse response = vnPayService.createDepositPaymentUrl(currentUserId, request, httpRequest);
        return ResponseEntity.ok(APIResponse.success("Payment URL created successfully", response));
    }

    @PostMapping("/momo/deposit/create")
    @Operation(
            summary = "Create MoMo payment request",
            description = "Create a MoMo captureWallet payment request for wallet deposit and return deeplink/payUrl."
    )
    @ApiResponse(responseCode = "200", description = "MoMo payment request created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid deposit amount")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Wallet not found - Please open a wallet first")
    public ResponseEntity<APIResponse<MoMoDepositResponse>> createMoMoDepositPayment(
            @Valid @RequestBody CreateDepositRequest request) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        MoMoDepositResponse response = moMoService.createDepositPayment(currentUserId, request);
        return ResponseEntity.ok(APIResponse.success("MoMo payment request created successfully", response));
    }

    @GetMapping("/deposit/callback")
    @Operation(
            summary = "VNPay payment callback (IPN)",
            description = "Handle VNPay payment notification callback. This endpoint is called by VNPay server " +
                    "to notify payment result. User should not call this endpoint directly.",
            hidden = true
    )
    public ResponseEntity<Map<String, String>> handlePaymentCallback(@RequestParam Map<String, String> params) {
        String responseCode = vnPayService.handlePaymentCallback(params);
        Map<String, String> response = Map.of(
                "RspCode", responseCode,
                "Message", "00".equals(responseCode) ? "Success" : "Failed"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/momo/ipn")
    @Operation(
            summary = "MoMo payment callback (IPN)",
            description = "Handle MoMo payment notification callback.",
            hidden = true
    )
    public ResponseEntity<Map<String, Object>> handleMoMoPaymentCallback(@RequestBody Map<String, String> params) {
        Map<String, Object> response = moMoService.handlePaymentCallback(params);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/my-transactions")
    @Operation(
            summary = "Get my transaction history",
            description = "Retrieve all wallet transactions for the currently authenticated user. " +
                    "Includes deposits, withdrawals, and other wallet-related transactions."
    )
    @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    public ResponseEntity<APIResponse<List<TransactionDto>>> getMyTransactionHistory() {
        List<TransactionDto> transactions = transactionService.getMyTransactionHistory();
        return ResponseEntity.ok(APIResponse.success(
                "Transaction history retrieved successfully", transactions));
    }

    @GetMapping("/my-transactions/{transactionId}")
    @Operation(
            summary = "Get transaction by ID",
            description = "Retrieve a specific transaction details for the currently authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Transaction not found or does not belong to user")
    public ResponseEntity<APIResponse<TransactionDto>> getMyTransactionById(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable UUID transactionId) {
        TransactionDto transaction = transactionService.getMyTransactionById(transactionId);
        return ResponseEntity.ok(APIResponse.success("Transaction retrieved successfully", transaction));
    }

    // ============= ADMIN ENDPOINTS =============

    @GetMapping("/admin/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get wallet by account ID (Admin only)",
            description = "Retrieve wallet information for a specific account. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Wallet retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    public ResponseEntity<APIResponse<WalletDto>> getWalletByAccountId(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId) {
        WalletDto walletDto = walletService.getWalletByAccountId(accountId);
        return ResponseEntity.ok(APIResponse.success("Wallet retrieved successfully", walletDto));
    }

    @PostMapping("/admin/{accountId}/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create wallet for account (Admin only)",
            description = "Create a new wallet for a specific account. Admin access required."
    )
    @ApiResponse(responseCode = "201", description = "Wallet created successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "409", description = "Wallet already exists for this account")
    public ResponseEntity<APIResponse<WalletDto>> createWalletForAccount(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId) {
        WalletDto walletDto = walletService.createWalletForAccount(accountId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Wallet created successfully", walletDto));
    }

    @PatchMapping("/admin/{accountId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Activate wallet (Admin only)",
            description = "Activate a deactivated wallet. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Wallet activated successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    public ResponseEntity<APIResponse<WalletDto>> activateWallet(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId) {
        WalletDto walletDto = walletService.activateWallet(accountId);
        return ResponseEntity.ok(APIResponse.success("Wallet activated successfully", walletDto));
    }

    @PatchMapping("/admin/{accountId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Deactivate wallet (Admin only)",
            description = "Deactivate/lock a wallet to prevent transactions. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Wallet deactivated successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    public ResponseEntity<APIResponse<WalletDto>> deactivateWallet(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId) {
        WalletDto walletDto = walletService.deactivateWallet(accountId);
        return ResponseEntity.ok(APIResponse.success("Wallet deactivated successfully", walletDto));
    }

    @PostMapping("/admin/{accountId}/lock-funds")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Lock funds in wallet (Admin only)",
            description = "Lock a specific amount from available balance. " +
                    "Locked funds cannot be used until unlocked or deducted. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Funds locked successfully")
    @ApiResponse(responseCode = "400", description = "Insufficient available balance")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required or wallet not active")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    public ResponseEntity<APIResponse<WalletDto>> lockFunds(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId,
            @Valid @RequestBody LockFundsRequest request) {
        WalletDto walletDto = walletService.lockFunds(accountId, request);
        return ResponseEntity.ok(APIResponse.success("Funds locked successfully", walletDto));
    }

    @PostMapping("/admin/{accountId}/unlock-funds")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Unlock funds in wallet (Admin only)",
            description = "Unlock previously locked funds back to available balance. " +
                    "Use this when cancelling a transaction. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Funds unlocked successfully")
    @ApiResponse(responseCode = "400", description = "Insufficient locked balance")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    public ResponseEntity<APIResponse<WalletDto>> unlockFunds(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId,
            @Valid @RequestBody UnlockFundsRequest request) {
        WalletDto walletDto = walletService.unlockFunds(accountId, request);
        return ResponseEntity.ok(APIResponse.success("Funds unlocked successfully", walletDto));
    }

    @PostMapping("/admin/{accountId}/deduct-locked")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Deduct locked funds (Admin only)",
            description = "Deduct locked funds from wallet to complete a transaction. " +
                    "This reduces both locked balance and total balance. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Locked funds deducted successfully")
    @ApiResponse(responseCode = "400", description = "Insufficient locked balance")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    public ResponseEntity<APIResponse<WalletDto>> deductLockedFunds(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId,
            @Valid @RequestBody DeductLockedFundsRequest request) {
        WalletDto walletDto = walletService.deductLockedFunds(accountId, request);
        return ResponseEntity.ok(APIResponse.success("Locked funds deducted successfully", walletDto));
    }

    @PostMapping("/admin/{accountId}/adjust-balance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Adjust wallet balance (Admin only)",
            description = "Add or deduct funds from wallet balance. " +
                    "Use positive amount to add funds (refund, compensation), " +
                    "negative amount to deduct funds (penalty, correction). Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Balance adjusted successfully")
    @ApiResponse(responseCode = "400", description = "Insufficient balance for negative adjustment")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Wallet not found")
    public ResponseEntity<APIResponse<WalletDto>> adjustBalance(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId,
            @Valid @RequestBody AdjustBalanceRequest request) {
        WalletDto walletDto = walletService.adjustBalance(accountId, request);
        return ResponseEntity.ok(APIResponse.success("Balance adjusted successfully", walletDto));
    }
}
