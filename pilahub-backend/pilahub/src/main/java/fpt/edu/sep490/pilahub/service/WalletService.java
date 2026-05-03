package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.WalletDto;
import fpt.edu.sep490.pilahub.dto.request.AdjustBalanceRequest;
import fpt.edu.sep490.pilahub.dto.request.DeductLockedFundsRequest;
import fpt.edu.sep490.pilahub.dto.request.LockFundsRequest;
import fpt.edu.sep490.pilahub.dto.request.UnlockFundsRequest;

import java.util.UUID;

public interface WalletService {

    /**
     * Get wallet balance for the currently authenticated user
     * @return WalletDto containing balance information
     */
    WalletDto getMyWallet();

    /**
     * Open/create a new wallet for the currently authenticated user
     * @return WalletDto of the newly created wallet
     */
    WalletDto openMyWallet();

    // ============= ADMIN OPERATIONS =============

    /**
     * Get wallet by account ID (Admin only)
     * @param accountId Account ID
     * @return WalletDto containing balance information
     */
    WalletDto getWalletByAccountId(UUID accountId);

    /**
     * Create wallet for a specific account (Admin only)
     * @param accountId Account ID
     * @return WalletDto of the newly created wallet
     */
    WalletDto createWalletForAccount(UUID accountId);

    /**
     * Activate wallet (Admin only)
     * @param accountId Account ID
     * @return WalletDto of the activated wallet
     */
    WalletDto activateWallet(UUID accountId);

    /**
     * Deactivate/lock wallet (Admin only)
     * @param accountId Account ID
     * @return WalletDto of the deactivated wallet
     */
    WalletDto deactivateWallet(UUID accountId);

    /**
     * Lock funds in wallet (Admin only)
     * @param accountId Account ID
     * @param request Lock funds request
     * @return WalletDto with updated balance
     */
    WalletDto lockFunds(UUID accountId, LockFundsRequest request);

    /**
     * Unlock funds in wallet (Admin only)
     * @param accountId Account ID
     * @param request Unlock funds request
     * @return WalletDto with updated balance
     */
    WalletDto unlockFunds(UUID accountId, UnlockFundsRequest request);

    /**
     * Deduct locked funds (complete transaction) (Admin only)
     * @param accountId Account ID
     * @param request Deduct locked funds request
     * @return WalletDto with updated balance
     */
    WalletDto deductLockedFunds(UUID accountId, DeductLockedFundsRequest request);

    /**
     * Adjust wallet balance - add or deduct funds (Admin only)
     * @param accountId Account ID
     * @param request Adjust balance request
     * @return WalletDto with updated balance
     */
    WalletDto adjustBalance(UUID accountId, AdjustBalanceRequest request);
}
