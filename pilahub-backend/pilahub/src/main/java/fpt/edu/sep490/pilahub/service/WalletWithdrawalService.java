package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.WalletWithdrawalDto;
import fpt.edu.sep490.pilahub.dto.request.ApproveWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.request.CompleteWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.request.CreateWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.request.RejectWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.response.BankInfoDto;
import fpt.edu.sep490.pilahub.enums.WalletWithdrawalStatus;

import java.util.List;
import java.util.UUID;

public interface WalletWithdrawalService {

    // ============= USER OPERATIONS =============

    /**
     * Get list of banks from VietQR API
     * @return List of bank information
     */
    List<BankInfoDto> getBankList();

    /**
     * Create a withdrawal request
     * @param request Withdrawal request details
     * @return Created withdrawal DTO
     */
    WalletWithdrawalDto createWithdrawal(CreateWithdrawalRequest request);

    /**
     * Get own withdrawals
     * @return List of user's withdrawals
     */
    List<WalletWithdrawalDto> getMyWithdrawals();

    /**
     * Get own withdrawal by ID
     * @param withdrawalId Withdrawal ID
     * @return Withdrawal DTO
     */
    WalletWithdrawalDto getMyWithdrawalById(UUID withdrawalId);

    /**
     * Get own withdrawals by status
     * @param status Withdrawal status
     * @return List of withdrawals with the specified status
     */
    List<WalletWithdrawalDto> getMyWithdrawalsByStatus(WalletWithdrawalStatus status);

    /**
     * Update a pending withdrawal
     * @param withdrawalId Withdrawal ID
     * @param request Update request
     * @return Updated withdrawal DTO
     */
    WalletWithdrawalDto updateWithdrawal(UUID withdrawalId, UpdateWithdrawalRequest request);

    /**
     * Cancel a pending withdrawal
     * @param withdrawalId Withdrawal ID
     * @return Cancelled withdrawal DTO
     */
    WalletWithdrawalDto cancelWithdrawal(UUID withdrawalId);

    // ============= ADMIN OPERATIONS =============

    /**
     * Get all withdrawals (Admin only)
     * @return List of all withdrawals
     */
    List<WalletWithdrawalDto> getAllWithdrawals();

    /**
     * Get withdrawals by status (Admin only)
     * @param status Withdrawal status
     * @return List of withdrawals with the specified status
     */
    List<WalletWithdrawalDto> getWithdrawalsByStatus(WalletWithdrawalStatus status);

    /**
     * Get withdrawal by ID (Admin only)
     * @param withdrawalId Withdrawal ID
     * @return Withdrawal DTO
     */
    WalletWithdrawalDto getWithdrawalById(UUID withdrawalId);

    /**
     * Approve a pending withdrawal (Admin only)
     * @param withdrawalId Withdrawal ID
     * @param request Approval request with optional admin note
     * @return Approved withdrawal DTO
     */
    WalletWithdrawalDto approveWithdrawal(UUID withdrawalId, ApproveWithdrawalRequest request);

    /**
     * Reject a pending withdrawal (Admin only)
     * @param withdrawalId Withdrawal ID
     * @param request Rejection request with optional admin note
     * @return Rejected withdrawal DTO
     */
    WalletWithdrawalDto rejectWithdrawal(UUID withdrawalId, RejectWithdrawalRequest request);

    /**
     * Complete an approved withdrawal (Admin only)
     * @param withdrawalId Withdrawal ID
     * @param request Completion request with required receipt URL
     * @return Completed withdrawal DTO
     */
    WalletWithdrawalDto completeWithdrawal(UUID withdrawalId, CompleteWithdrawalRequest request);
}
