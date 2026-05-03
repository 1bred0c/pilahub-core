package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.WalletDto;
import fpt.edu.sep490.pilahub.dto.request.AdjustBalanceRequest;
import fpt.edu.sep490.pilahub.dto.request.DeductLockedFundsRequest;
import fpt.edu.sep490.pilahub.dto.request.LockFundsRequest;
import fpt.edu.sep490.pilahub.dto.request.UnlockFundsRequest;
import fpt.edu.sep490.pilahub.exception.*;
import fpt.edu.sep490.pilahub.mapper.WalletMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.Wallet;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.WalletRepository;
import fpt.edu.sep490.pilahub.service.WalletService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final AccountRepository accountRepository;
    private final WalletMapper walletMapper;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional(readOnly = true)
    public WalletDto getMyWallet() {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Fetching wallet for user ID: {}", currentUserId);

        Wallet wallet = walletRepository.findByAccountId(currentUserId)
                .orElseThrow(() -> {
                    log.error("Wallet not found for account ID: {}", currentUserId);
                    return new WalletNotFoundException("Wallet not found for your account. Please open a wallet first.");
                });

        log.info("Successfully retrieved wallet for user ID: {}. Balance: {}, Available: {}, Locked: {}",
                currentUserId, wallet.getBalanceVND(), wallet.getAvailableVND(), wallet.getLockedVND());

        return walletMapper.toDto(wallet);
    }

    @Override
    public WalletDto openMyWallet() {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Opening wallet for user ID: {}", currentUserId);

        // Check if wallet already exists
        if (walletRepository.findByAccountId(currentUserId).isPresent()) {
            log.error("Wallet already exists for account ID: {}", currentUserId);
            throw new WalletAlreadyExistsException("You already have a wallet");
        }

        Account account = accountRepository.findById(currentUserId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        // Build wallet - DO NOT set accountId manually, @MapsId will handle it
        Wallet wallet = new Wallet();
        wallet.setAccount(account);
        wallet.setBalanceVND(BigDecimal.ZERO);
        wallet.setAvailableVND(BigDecimal.ZERO);
        wallet.setLockedVND(BigDecimal.ZERO);
        wallet.setActive(true);
        wallet.setOpenAt(Instant.now());

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet opened successfully for user ID: {}", savedWallet.getAccountId());

        return walletMapper.toDto(savedWallet);
    }

    // ============= ADMIN OPERATIONS =============

    @Override
    @Transactional(readOnly = true)
    public WalletDto getWalletByAccountId(UUID accountId) {
        log.info("Admin fetching wallet for account ID: {}", accountId);

        Wallet wallet = walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> {
                    log.error("Wallet not found for account ID: {}", accountId);
                    return new WalletNotFoundException("Wallet not found for account ID: " + accountId);
                });

        return walletMapper.toDto(wallet);
    }

    @Override
    public WalletDto createWalletForAccount(UUID accountId) {
        log.info("Admin creating wallet for account ID: {}", accountId);

        // Check if wallet already exists
        if (walletRepository.findByAccountId(accountId).isPresent()) {
            log.error("Wallet already exists for account ID: {}", accountId);
            throw new WalletAlreadyExistsException("Wallet already exists for this account");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

        // Build wallet - DO NOT set accountId manually, @MapsId will handle it
        Wallet wallet = new Wallet();
        wallet.setAccount(account);
        wallet.setBalanceVND(BigDecimal.ZERO);
        wallet.setAvailableVND(BigDecimal.ZERO);
        wallet.setLockedVND(BigDecimal.ZERO);
        wallet.setActive(true);
        wallet.setOpenAt(Instant.now());

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created successfully by admin for account ID: {}", savedWallet.getAccountId());

        return walletMapper.toDto(savedWallet);
    }

    @Override
    public WalletDto activateWallet(UUID accountId) {
        log.info("Admin activating wallet for account ID: {}", accountId);

        Wallet wallet = walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for account ID: " + accountId));

        if (wallet.isActive()) {
            log.warn("Wallet is already active for account ID: {}", accountId);
            throw new IllegalArgumentException("Wallet is already active");
        }

        wallet.setActive(true);
        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet activated successfully for account ID: {}", accountId);

        return walletMapper.toDto(savedWallet);
    }

    @Override
    public WalletDto deactivateWallet(UUID accountId) {
        log.info("Admin deactivating wallet for account ID: {}", accountId);

        Wallet wallet = walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for account ID: " + accountId));

        if (!wallet.isActive()) {
            log.warn("Wallet is already inactive for account ID: {}", accountId);
            throw new IllegalArgumentException("Wallet is already inactive");
        }

        wallet.setActive(false);
        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet deactivated successfully for account ID: {}", accountId);

        return walletMapper.toDto(savedWallet);
    }

    @Override
    public WalletDto lockFunds(UUID accountId, LockFundsRequest request) {
        log.info("Admin locking {} VND for account ID: {}. Reason: {}",
                request.amount(), accountId, request.reason());

        Wallet wallet = walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for account ID: " + accountId));

        if (!wallet.isActive()) {
            throw new WalletNotActiveException("Cannot lock funds in inactive wallet");
        }

        // Check if available balance is sufficient
        if (wallet.getAvailableVND().compareTo(request.amount()) < 0) {
            log.error("Insufficient available balance for account ID: {}. Available: {}, Requested: {}",
                    accountId, wallet.getAvailableVND(), request.amount());
            throw new InsufficientBalanceException(
                    "Insufficient available balance. Available: " + wallet.getAvailableVND() + " VND");
        }

        // Lock funds: decrease available, increase locked
        wallet.setAvailableVND(wallet.getAvailableVND().subtract(request.amount()));
        wallet.setLockedVND(wallet.getLockedVND().add(request.amount()));

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Funds locked successfully. Account ID: {}, Amount: {}, New Available: {}, New Locked: {}",
                accountId, request.amount(), savedWallet.getAvailableVND(), savedWallet.getLockedVND());

        return walletMapper.toDto(savedWallet);
    }

    @Override
    public WalletDto unlockFunds(UUID accountId, UnlockFundsRequest request) {
        log.info("Admin unlocking {} VND for account ID: {}. Reason: {}",
                request.amount(), accountId, request.reason());

        Wallet wallet = walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for account ID: " + accountId));

        // Check if locked balance is sufficient
        if (wallet.getLockedVND().compareTo(request.amount()) < 0) {
            log.error("Insufficient locked balance for account ID: {}. Locked: {}, Requested: {}",
                    accountId, wallet.getLockedVND(), request.amount());
            throw new InsufficientBalanceException(
                    "Insufficient locked balance. Locked: " + wallet.getLockedVND() + " VND");
        }

        // Unlock funds: decrease locked, increase available
        wallet.setLockedVND(wallet.getLockedVND().subtract(request.amount()));
        wallet.setAvailableVND(wallet.getAvailableVND().add(request.amount()));

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Funds unlocked successfully. Account ID: {}, Amount: {}, New Available: {}, New Locked: {}",
                accountId, request.amount(), savedWallet.getAvailableVND(), savedWallet.getLockedVND());

        return walletMapper.toDto(savedWallet);
    }

    @Override
    public WalletDto deductLockedFunds(UUID accountId, DeductLockedFundsRequest request) {
        log.info("Admin deducting {} VND from locked funds for account ID: {}. Reason: {}",
                request.amount(), accountId, request.reason());

        Wallet wallet = walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for account ID: " + accountId));

        // Check if locked balance is sufficient
        if (wallet.getLockedVND().compareTo(request.amount()) < 0) {
            log.error("Insufficient locked balance for account ID: {}. Locked: {}, Requested: {}",
                    accountId, wallet.getLockedVND(), request.amount());
            throw new InsufficientBalanceException(
                    "Insufficient locked balance. Locked: " + wallet.getLockedVND() + " VND");
        }

        // Deduct from locked and total balance
        wallet.setLockedVND(wallet.getLockedVND().subtract(request.amount()));
        wallet.setBalanceVND(wallet.getBalanceVND().subtract(request.amount()));

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Locked funds deducted successfully. Account ID: {}, Amount: {}, New Balance: {}, New Locked: {}",
                accountId, request.amount(), savedWallet.getBalanceVND(), savedWallet.getLockedVND());

        return walletMapper.toDto(savedWallet);
    }

    @Override
    public WalletDto adjustBalance(UUID accountId, AdjustBalanceRequest request) {
        log.info("Admin adjusting balance by {} VND for account ID: {}. Reason: {}",
                request.amount(), accountId, request.reason());

        Wallet wallet = walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for account ID: " + accountId));

        BigDecimal newBalance = wallet.getBalanceVND().add(request.amount());
        BigDecimal newAvailable = wallet.getAvailableVND().add(request.amount());

        // Validate that balance doesn't go negative
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            log.error("Adjustment would result in negative balance for account ID: {}. Current: {}, Adjustment: {}",
                    accountId, wallet.getBalanceVND(), request.amount());
            throw new InsufficientBalanceException(
                    "Insufficient balance for this adjustment. Current balance: " + wallet.getBalanceVND() + " VND");
        }

        if (newAvailable.compareTo(BigDecimal.ZERO) < 0) {
            log.error("Adjustment would result in negative available balance for account ID: {}. Current: {}, Adjustment: {}",
                    accountId, wallet.getAvailableVND(), request.amount());
            throw new InsufficientBalanceException(
                    "Insufficient available balance for this adjustment. Current available: " + wallet.getAvailableVND() + " VND");
        }

        wallet.setBalanceVND(newBalance);
        wallet.setAvailableVND(newAvailable);

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Balance adjusted successfully. Account ID: {}, Adjustment: {}, New Balance: {}, New Available: {}",
                accountId, request.amount(), savedWallet.getBalanceVND(), savedWallet.getAvailableVND());

        return walletMapper.toDto(savedWallet);
    }
}
