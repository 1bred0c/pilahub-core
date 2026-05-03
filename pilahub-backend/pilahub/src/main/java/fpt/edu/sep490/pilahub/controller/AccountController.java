package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.AccountDto;
import fpt.edu.sep490.pilahub.dto.request.UpdateAccountRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.mapper.AccountMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.service.AccountAdminService;
import fpt.edu.sep490.pilahub.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "APIs for managing user accounts (Admin only)")
public class AccountController {

        private final AccountAdminService accountAdminService;
        private final AccountService accountService;
        private final AccountMapper accountMapper;

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get all accounts with pagination (Admin only)", description = "Retrieve a paginated list of all user accounts. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        public ResponseEntity<APIResponse<Page<AccountDto>>> getAllAccounts(
                        @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,

                        @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,

                        @Parameter(description = "Sort direction (asc or desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("asc")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();

                Pageable pageable = PageRequest.of(page, size, sort);
                Page<AccountDto> accounts = accountAdminService.getAllAccounts(pageable);

                return ResponseEntity.ok(APIResponse.success(
                                String.format("Retrieved %d account(s) from page %d of %d",
                                                accounts.getNumberOfElements(),
                                                accounts.getNumber() + 1,
                                                accounts.getTotalPages()),
                                accounts));
        }

        @GetMapping("/{accountId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get account by ID (Admin only)", description = "Retrieve account details by account ID. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Account found")
        @ApiResponse(responseCode = "404", description = "Account not found")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        public ResponseEntity<APIResponse<AccountDto>> getAccountById(
                        @PathVariable UUID accountId) {
                AccountDto accountDto = accountAdminService.getAccountById(accountId);
                return ResponseEntity.ok(APIResponse.success("Account retrieved successfully", accountDto));
        }

        @PutMapping("/{accountId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Update account by ID (Admin only)", description = "Update account details by account ID. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Account updated successfully")
        @ApiResponse(responseCode = "404", description = "Account not found")
        @ApiResponse(responseCode = "400", description = "Invalid input or duplicate data")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        public ResponseEntity<APIResponse<AccountDto>> updateAccount(
                        @PathVariable UUID accountId,
                        @Valid @RequestBody UpdateAccountRequest request) {
                AccountDto accountDto = accountAdminService.updateAccount(accountId, request);
                return ResponseEntity.ok(APIResponse.success("Account updated successfully", accountDto));
        }

        @DeleteMapping("/{accountId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Deactivate account by ID (Admin only)", description = "Deactivate account by setting active status to false. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Account deactivated successfully")
        @ApiResponse(responseCode = "404", description = "Account not found")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        public ResponseEntity<APIResponse<Void>> deleteAccount(
                        @PathVariable UUID accountId) {
                accountAdminService.deleteAccount(accountId);
                return ResponseEntity.ok(APIResponse.success("Account deactivated successfully", null));
        }

        @PatchMapping("/{accountId}/activate")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Activate account by ID (Admin only)", description = "Activate account by setting active status to true. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Account activated successfully")
        @ApiResponse(responseCode = "404", description = "Account not found")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        public ResponseEntity<APIResponse<AccountDto>> activateAccount(
                        @PathVariable UUID accountId) {
                AccountDto accountDto = accountAdminService.activateAccount(accountId);
                return ResponseEntity.ok(APIResponse.success("Account activated successfully", accountDto));
        }

        @PutMapping("/me/fcm-token")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Update current user FCM token", description = "Save or replace the FCM token for the currently authenticated account.")
        @ApiResponse(responseCode = "200", description = "FCM token updated successfully")
        @ApiResponse(responseCode = "401", description = "Unauthorized")
        public ResponseEntity<APIResponse<AccountDto>> updateMyFcmToken(
                        @RequestParam String fcmToken) {
                Account updated = accountService.updateFcmToken(fcmToken);
                return ResponseEntity.ok(
                                APIResponse.success("FCM token updated successfully", accountMapper.toDto(updated)));
        }

        @PutMapping("/me/reminded")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Update current user reminder preference", description = "Enable or disable reminder notifications for the currently authenticated account.")
        @ApiResponse(responseCode = "200", description = "Reminder preference updated successfully")
        @ApiResponse(responseCode = "401", description = "Unauthorized")
        public ResponseEntity<APIResponse<AccountDto>> updateMyIsReminded(
                        @RequestParam Boolean isReminded) {
                Account updated = accountService.updateIsReminded(isReminded);
                return ResponseEntity
                                .ok(APIResponse.success("Reminder preference updated successfully",
                                                accountMapper.toDto(updated)));
        }
}
