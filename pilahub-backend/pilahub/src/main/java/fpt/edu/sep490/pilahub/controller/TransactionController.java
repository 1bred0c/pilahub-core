package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.TransactionDto;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.enums.TransactionType;
import fpt.edu.sep490.pilahub.service.TransactionService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing transactions")
public class TransactionController {

        private final TransactionService transactionService;
        private final SecurityUtil securityUtil;

        // ==================== USER ENDPOINTS (using own token) ====================

        @GetMapping("/my-transactions")
        @Operation(summary = "Get own transactions", description = "Retrieve all transactions for the authenticated user. Uses account ID from JWT token.")
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
        @ApiResponse(responseCode = "401", description = "Unauthorized")
        public ResponseEntity<APIResponse<List<TransactionDto>>> getOwnTransactions() {
                UUID accountId = securityUtil.getCurrentUserId();
                List<TransactionDto> transactions = transactionService.getTransactionsByAccountId(accountId);
                return ResponseEntity.ok(APIResponse.success(
                                String.format("Retrieved %d transaction(s) successfully", transactions.size()),
                                transactions));
        }

        @GetMapping("/my-transactions/by-type")
        @Operation(summary = "Get own transactions by type", description = "Retrieve transactions for the authenticated user filtered by transaction type. Uses account ID from JWT token.")
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
        @ApiResponse(responseCode = "401", description = "Unauthorized")
        @ApiResponse(responseCode = "400", description = "Invalid transaction type")
        public ResponseEntity<APIResponse<List<TransactionDto>>> getOwnTransactionsByType(
                        @RequestParam TransactionType type) {
                UUID accountId = securityUtil.getCurrentUserId();
                List<TransactionDto> transactions = transactionService.getTransactionsByAccountIdAndType(accountId,
                                type);
                return ResponseEntity.ok(APIResponse.success(
                                String.format("Retrieved %d transaction(s) of type %s successfully",
                                                transactions.size(), type),
                                transactions));
        }

        @GetMapping("/my-transactions/{transactionId}")
        @Operation(summary = "Get own transaction by ID", description = "Retrieve a specific transaction by ID for the authenticated user.")
        @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully")
        @ApiResponse(responseCode = "404", description = "Transaction not found")
        @ApiResponse(responseCode = "401", description = "Unauthorized")
        @ApiResponse(responseCode = "403", description = "Forbidden - Not your transaction")
        public ResponseEntity<APIResponse<TransactionDto>> getOwnTransactionById(@PathVariable UUID transactionId) {
                UUID accountId = securityUtil.getCurrentUserId();
                TransactionDto transaction = transactionService.getTransactionById(transactionId);

                // Verify the transaction belongs to the current user
                if (!transaction.accountId().equals(accountId)) {
                        return ResponseEntity.status(403)
                                        .body(APIResponse.error("You don't have permission to view this transaction"));
                }

                return ResponseEntity.ok(APIResponse.success("Transaction retrieved successfully", transaction));
        }

        // ==================== ADMIN ENDPOINTS ====================

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get all transactions (Admin only)", description = "Retrieve all transactions in the system. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        public ResponseEntity<APIResponse<List<TransactionDto>>> getAllTransactions() {
                List<TransactionDto> transactions = transactionService.getAllTransactions();
                return ResponseEntity.ok(APIResponse.success(
                                String.format("Retrieved %d transaction(s) successfully", transactions.size()),
                                transactions));
        }

        @GetMapping("/{transactionId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get transaction by ID (Admin only)", description = "Retrieve a specific transaction by ID. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Transaction found")
        @ApiResponse(responseCode = "404", description = "Transaction not found")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        public ResponseEntity<APIResponse<TransactionDto>> getTransactionById(@PathVariable UUID transactionId) {
                TransactionDto transaction = transactionService.getTransactionById(transactionId);
                return ResponseEntity.ok(APIResponse.success("Transaction retrieved successfully", transaction));
        }

        @GetMapping("/by-account/{accountId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get transactions by account ID (Admin only)", description = "Retrieve all transactions for a specific account. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        public ResponseEntity<APIResponse<List<TransactionDto>>> getTransactionsByAccountId(
                        @PathVariable UUID accountId) {
                List<TransactionDto> transactions = transactionService.getTransactionsByAccountId(accountId);
                return ResponseEntity.ok(APIResponse.success(
                                String.format("Retrieved %d transaction(s) for account %s", transactions.size(),
                                                accountId),
                                transactions));
        }

        @GetMapping("/by-type")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get transactions by type (Admin only)", description = "Retrieve all transactions of a specific type. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        @ApiResponse(responseCode = "400", description = "Invalid transaction type")
        public ResponseEntity<APIResponse<List<TransactionDto>>> getTransactionsByType(
                        @RequestParam TransactionType type) {
                List<TransactionDto> transactions = transactionService.getTransactionsByType(type);
                return ResponseEntity.ok(APIResponse.success(
                                String.format("Retrieved %d transaction(s) of type %s", transactions.size(), type),
                                transactions));
        }

        @GetMapping("/by-reference/{referenceId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get transactions by reference ID (Admin only)", description = "Retrieve all transactions for a specific reference ID. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        public ResponseEntity<APIResponse<List<TransactionDto>>> getTransactionsByReferenceId(
                        @PathVariable UUID referenceId) {
                List<TransactionDto> transactions = transactionService.getTransactionsByReferenceId(referenceId);
                return ResponseEntity.ok(APIResponse.success(
                                String.format("Retrieved %d transaction(s) for reference ID %s", transactions.size(),
                                                referenceId),
                                transactions));
        }
}
