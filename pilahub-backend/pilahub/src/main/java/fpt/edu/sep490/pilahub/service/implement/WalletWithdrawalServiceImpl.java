package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.WalletWithdrawalDto;
import fpt.edu.sep490.pilahub.enums.NotificationType;
import fpt.edu.sep490.pilahub.event.NotificationEvent;
import fpt.edu.sep490.pilahub.dto.request.ApproveWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.request.CompleteWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.request.CreateWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.request.RejectWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateWithdrawalRequest;
import fpt.edu.sep490.pilahub.dto.response.BankInfoDto;
import fpt.edu.sep490.pilahub.enums.TransactionType;
import fpt.edu.sep490.pilahub.enums.WalletWithdrawalStatus;
import fpt.edu.sep490.pilahub.exception.*;
import fpt.edu.sep490.pilahub.mapper.WalletWithdrawalMapper;
import fpt.edu.sep490.pilahub.pojo.Wallet;
import fpt.edu.sep490.pilahub.pojo.WalletWithdrawal;
import fpt.edu.sep490.pilahub.repository.WalletRepository;
import fpt.edu.sep490.pilahub.repository.WalletWithdrawalRepository;
import fpt.edu.sep490.pilahub.service.TransactionService;
import fpt.edu.sep490.pilahub.service.WalletWithdrawalService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WalletWithdrawalServiceImpl implements WalletWithdrawalService {

    private final WalletWithdrawalRepository withdrawalRepository;
    private final WalletRepository walletRepository;
    private final WalletWithdrawalMapper withdrawalMapper;
    private final SecurityUtil securityUtil;
    private final TransactionService transactionService;
    private final RestTemplate restTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${vietqr.api.url:https://api.vietqr.io/v2/ios-app-deeplinks}")
    private String vietQrApiUrl;

    // ============= USER OPERATIONS =============

    @Override
    @Transactional(readOnly = true)
    public List<BankInfoDto> getBankList() {
        log.info("Fetching bank list from VietQR API");

        try {
            Map<String, Object> response = restTemplate.getForObject(vietQrApiUrl, Map.class);

            if (response != null && response.containsKey("apps")) {
                List<Map<String, Object>> apps = (List<Map<String, Object>>) response.get("apps");

                return apps.stream()
                        .map(app -> new BankInfoDto(
                                (String) app.get("appId"),
                                (String) app.get("bankName"),
                                (String) app.get("appLogo")))
                        .collect(Collectors.toList());
            }

            log.warn("VietQR API returned empty or invalid response");
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error fetching bank list from VietQR API: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to fetch bank list. Please try again later.");
        }
    }

    @Override
    public WalletWithdrawalDto createWithdrawal(CreateWithdrawalRequest request) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Creating withdrawal request for user ID: {}", currentUserId);

        // Get user's wallet
        Wallet wallet = walletRepository.findByAccountId(currentUserId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found. Please open a wallet first."));

        // Validate wallet is active
        if (!wallet.isActive()) {
            throw new WalletNotActiveException("Your wallet is inactive. Please contact support.");
        }

        // Validate sufficient available balance
        if (wallet.getAvailableVND().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient available balance. Available: %s VND, Requested: %s VND",
                            wallet.getAvailableVND(), request.amount()));
        }

        // Create withdrawal
        WalletWithdrawal withdrawal = WalletWithdrawal.builder()
                .wallet(wallet)
                .recipientName(request.recipientName())
                .bankAccountNumber(request.bankAccountNumber())
                .bankCode(request.bankCode())
                .bankName(request.bankName())
                .bankLogo(request.bankLogo())
                .amount(request.amount())
                .note(request.note())
                .status(WalletWithdrawalStatus.PENDING)
                .requestedAt(Instant.now())
                .build();

        // Lock the funds: reduce available balance, increase locked balance
        BigDecimal amount = withdrawal.getAmount();
        wallet.setAvailableVND(wallet.getAvailableVND().subtract(amount));
        wallet.setLockedVND(wallet.getLockedVND().add(amount));
        walletRepository.save(wallet);

        WalletWithdrawal saved = withdrawalRepository.save(withdrawal);
        log.info("Withdrawal request created successfully. ID: {}, Amount: {}",
                saved.getWalletWithdrawalId(), saved.getAmount());

        eventPublisher.publishEvent(new NotificationEvent(
                this,
                currentUserId,
                NotificationType.WALLET_WITHDRAWAL_REQUESTED,
                "Yêu Cầu Rút Tiền Đã Được Gửi",
                "Yêu cầu rút " + saved.getAmount().toPlainString() + " VND của bạn đang chờ xử lý.",
                saved.getWalletWithdrawalId(), "WALLET_WITHDRAWAL"));

        return withdrawalMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletWithdrawalDto> getMyWithdrawals() {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Fetching withdrawals for user ID: {}", currentUserId);

        return withdrawalRepository.findByWallet_AccountIdOrderByRequestedAtDesc(currentUserId)
                .stream()
                .map(withdrawalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WalletWithdrawalDto getMyWithdrawalById(UUID withdrawalId) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Fetching withdrawal ID: {} for user ID: {}", withdrawalId, currentUserId);

        WalletWithdrawal withdrawal = withdrawalRepository
                .findByWalletWithdrawalIdAndWallet_AccountId(withdrawalId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Withdrawal", "id", withdrawalId));

        return withdrawalMapper.toDto(withdrawal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletWithdrawalDto> getMyWithdrawalsByStatus(WalletWithdrawalStatus status) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Fetching withdrawals with status {} for user ID: {}", status, currentUserId);

        return withdrawalRepository.findByWallet_AccountIdAndStatusOrderByRequestedAtDesc(currentUserId, status)
                .stream()
                .map(withdrawalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public WalletWithdrawalDto updateWithdrawal(UUID withdrawalId, UpdateWithdrawalRequest request) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Updating withdrawal ID: {} for user ID: {}", withdrawalId, currentUserId);

        WalletWithdrawal withdrawal = withdrawalRepository
                .findByWalletWithdrawalIdAndWallet_AccountId(withdrawalId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Withdrawal", "id", withdrawalId));

        // Can only update PENDING withdrawals
        if (withdrawal.getStatus() != WalletWithdrawalStatus.PENDING) {
            throw new IllegalArgumentException("Can only update withdrawals with PENDING status");
        }

        // Update fields if provided
        if (request.recipientName() != null) {
            withdrawal.setRecipientName(request.recipientName());
        }
        if (request.bankAccountNumber() != null) {
            withdrawal.setBankAccountNumber(request.bankAccountNumber());
        }
        if (request.bankCode() != null) {
            withdrawal.setBankCode(request.bankCode());
        }
        if (request.bankName() != null) {
            withdrawal.setBankName(request.bankName());
        }
        if (request.bankLogo() != null) {
            withdrawal.setBankLogo(request.bankLogo());
        }
        if (request.amount() != null) {
            // Validate sufficient balance for new amount
            Wallet wallet = withdrawal.getWallet();
            if (wallet.getAvailableVND().compareTo(request.amount()) < 0) {
                throw new InsufficientBalanceException(
                        String.format("Insufficient available balance. Available: %s VND, Requested: %s VND",
                                wallet.getAvailableVND(), request.amount()));
            }
            withdrawal.setAmount(request.amount());
        }
        if (request.note() != null) {
            withdrawal.setNote(request.note());
        }

        WalletWithdrawal updated = withdrawalRepository.save(withdrawal);
        log.info("Withdrawal updated successfully. ID: {}", withdrawalId);

        return withdrawalMapper.toDto(updated);
    }

    @Override
    public WalletWithdrawalDto cancelWithdrawal(UUID withdrawalId) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Cancelling withdrawal ID: {} by user ID: {}", withdrawalId, currentUserId);

        WalletWithdrawal withdrawal = withdrawalRepository
                .findByWalletWithdrawalIdAndWallet_AccountId(withdrawalId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Withdrawal", "id", withdrawalId));

        // Can only cancel PENDING withdrawals
        if (withdrawal.getStatus() != WalletWithdrawalStatus.PENDING) {
            throw new IllegalArgumentException("Can only cancel withdrawals with PENDING status");
        }

        Wallet wallet = withdrawal.getWallet();
        BigDecimal amount = withdrawal.getAmount();
        releaseLockedFunds(wallet, amount);
        walletRepository.save(wallet);

        withdrawal.setStatus(WalletWithdrawalStatus.CANCELLED);
        withdrawal.setProcessedAt(Instant.now());

        WalletWithdrawal cancelled = withdrawalRepository.save(withdrawal);
        log.info("Withdrawal cancelled successfully. ID: {}", withdrawalId);

        return withdrawalMapper.toDto(cancelled);
    }

    // ============= ADMIN OPERATIONS =============

    @Override
    @Transactional(readOnly = true)
    public List<WalletWithdrawalDto> getAllWithdrawals() {
        log.info("Admin fetching all withdrawals");

        return withdrawalRepository.findAllByOrderByRequestedAtDesc()
                .stream()
                .map(withdrawalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletWithdrawalDto> getWithdrawalsByStatus(WalletWithdrawalStatus status) {
        log.info("Admin fetching withdrawals with status: {}", status);

        return withdrawalRepository.findByStatusOrderByRequestedAtDesc(status)
                .stream()
                .map(withdrawalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WalletWithdrawalDto getWithdrawalById(UUID withdrawalId) {
        log.info("Admin fetching withdrawal ID: {}", withdrawalId);

        WalletWithdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new ResourceNotFoundException("Withdrawal", "id", withdrawalId));

        return withdrawalMapper.toDto(withdrawal);
    }

    @Override
    public WalletWithdrawalDto approveWithdrawal(UUID withdrawalId, ApproveWithdrawalRequest request) {
        UUID adminId = securityUtil.getCurrentUserId();
        log.info("Admin ID: {} approving withdrawal ID: {}", adminId, withdrawalId);

        WalletWithdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new ResourceNotFoundException("Withdrawal", "id", withdrawalId));

        // Can only approve PENDING withdrawals
        if (withdrawal.getStatus() != WalletWithdrawalStatus.PENDING) {
            throw new IllegalArgumentException("Can only approve withdrawals with PENDING status");
        }

        Wallet wallet = withdrawal.getWallet();
        BigDecimal amount = withdrawal.getAmount();
        validateSufficientLockedFunds(wallet, amount);

        // Update withdrawal status
        withdrawal.setStatus(WalletWithdrawalStatus.APPROVED);
        withdrawal.setAdminNote(request.adminNote());
        withdrawal.setProcessedBy(adminId);
        withdrawal.setProcessedAt(Instant.now());

        WalletWithdrawal approved = withdrawalRepository.save(withdrawal);

        eventPublisher.publishEvent(new NotificationEvent(
                this,
                withdrawal.getWallet().getAccountId(),
                NotificationType.WALLET_WITHDRAWAL_APPROVED,
                "Yêu Cầu Rút Tiền Được Phê Duyệt",
                "Yêu cầu rút " + withdrawal.getAmount().toPlainString()
                        + " VND của bạn đã được phê duyệt và đang được xử lý.",
                withdrawalId, "WALLET_WITHDRAWAL"));

        return withdrawalMapper.toDto(approved);
    }

    @Override
    public WalletWithdrawalDto rejectWithdrawal(UUID withdrawalId, RejectWithdrawalRequest request) {
        UUID adminId = securityUtil.getCurrentUserId();
        log.info("Admin ID: {} rejecting withdrawal ID: {}", adminId, withdrawalId);

        WalletWithdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new ResourceNotFoundException("Withdrawal", "id", withdrawalId));

        // Can only reject PENDING withdrawals
        if (withdrawal.getStatus() != WalletWithdrawalStatus.PENDING) {
            throw new IllegalArgumentException("Can only reject withdrawals with PENDING status");
        }

        Wallet wallet = withdrawal.getWallet();
        BigDecimal amount = withdrawal.getAmount();
        releaseLockedFunds(wallet, amount);
        walletRepository.save(wallet);

        withdrawal.setStatus(WalletWithdrawalStatus.REJECTED);
        withdrawal.setAdminNote(request.adminNote());
        withdrawal.setProcessedBy(adminId);
        withdrawal.setProcessedAt(Instant.now());

        WalletWithdrawal rejected = withdrawalRepository.save(withdrawal);
        log.info("Withdrawal rejected. ID: {}", withdrawalId);

        eventPublisher.publishEvent(new NotificationEvent(
                this,
                withdrawal.getWallet().getAccountId(),
                NotificationType.WALLET_WITHDRAWAL_REJECTED,
                "Yêu Cầu Rút Tiền Bị Từ Chối",
                "Yêu cầu rút " + withdrawal.getAmount().toPlainString() + " VND của bạn đã bị từ chối." +
                        (request.adminNote() != null ? " Lý do: " + request.adminNote() : ""),
                withdrawalId, "WALLET_WITHDRAWAL"));

        return withdrawalMapper.toDto(rejected);
    }

    @Override
    public WalletWithdrawalDto completeWithdrawal(UUID withdrawalId, CompleteWithdrawalRequest request) {
        UUID adminId = securityUtil.getCurrentUserId();
        log.info("Admin ID: {} completing withdrawal ID: {}", adminId, withdrawalId);

        if (request == null || !StringUtils.hasText(request.receiptUrl())) {
            throw new IllegalArgumentException("Receipt URL is required when completing withdrawal");
        }

        WalletWithdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new ResourceNotFoundException("Withdrawal", "id", withdrawalId));

        // Can only complete APPROVED withdrawals
        if (withdrawal.getStatus() != WalletWithdrawalStatus.APPROVED) {
            throw new IllegalArgumentException("Can only complete withdrawals with APPROVED status");
        }

        Wallet wallet = withdrawal.getWallet();
        BigDecimal amount = withdrawal.getAmount();

        // Deduct from locked balance and total balance
        wallet.setLockedVND(wallet.getLockedVND().subtract(amount));
        wallet.setBalanceVND(wallet.getBalanceVND().subtract(amount));
        walletRepository.save(wallet);

        // Update withdrawal status
        withdrawal.setStatus(WalletWithdrawalStatus.COMPLETED);
        withdrawal.setReceiptUrl(request.receiptUrl());
        withdrawal.setProcessedBy(adminId);
        withdrawal.setCompletedAt(Instant.now());

        WalletWithdrawal completed = withdrawalRepository.save(withdrawal);

        // Create transaction record
        transactionService.createTransaction(
                wallet.getAccountId(),
                TransactionType.WALLET_WITHDRAWAL,
                amount,
                withdrawalId,
                String.format("Withdrawal to %s - %s", withdrawal.getBankName(), withdrawal.getBankAccountNumber()));

        log.info("Withdrawal completed and transaction recorded. ID: {}, Amount: {}", withdrawalId, amount);

        eventPublisher.publishEvent(new NotificationEvent(
                this,
                wallet.getAccountId(),
                NotificationType.WALLET_WITHDRAWAL_COMPLETED,
                "Rút Tiền Đã Hoàn Thành",
                "Yêu cầu rút " + amount.toPlainString() + " VND tới " + withdrawal.getBankName() +
                        " (" + withdrawal.getBankAccountNumber() + ") đã hoàn thành.",
                withdrawalId, "WALLET_WITHDRAWAL"));

        return withdrawalMapper.toDto(completed);
    }

    private void releaseLockedFunds(Wallet wallet, BigDecimal amount) {
        validateSufficientLockedFunds(wallet, amount);

        wallet.setLockedVND(wallet.getLockedVND().subtract(amount));
        wallet.setAvailableVND(wallet.getAvailableVND().add(amount));
    }

    private void validateSufficientLockedFunds(Wallet wallet, BigDecimal amount) {
        if (wallet.getLockedVND().compareTo(amount) < 0) {
            throw new IllegalStateException(
                    String.format("Inconsistent wallet state. Locked: %s VND, Required: %s VND",
                            wallet.getLockedVND(), amount));
        }
    }
}
