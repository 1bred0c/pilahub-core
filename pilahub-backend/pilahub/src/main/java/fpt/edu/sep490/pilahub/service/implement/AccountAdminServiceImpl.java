package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.AccountDto;
import fpt.edu.sep490.pilahub.enums.NotificationType;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.event.NotificationEvent;
import fpt.edu.sep490.pilahub.dto.request.UpdateAccountRequest;
import fpt.edu.sep490.pilahub.exception.AccountNotFoundException;
import fpt.edu.sep490.pilahub.mapper.AccountMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.CoachRepository;
import fpt.edu.sep490.pilahub.service.AccountAdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountAdminServiceImpl implements AccountAdminService {

    private final AccountRepository accountRepository;
    private final CoachRepository coachRepository;
    private final AccountMapper accountMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Page<AccountDto> getAllAccounts(Pageable pageable) {
        log.info("Fetching all accounts with pagination - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Account> accountPage = accountRepository.findAll(pageable);
        log.info("Found {} total accounts, returning page {} with {} elements",
                accountPage.getTotalElements(),
                accountPage.getNumber(),
                accountPage.getNumberOfElements());

        return accountPage.map(accountMapper::toDto);
    }

    @Override
    public AccountDto getAccountById(UUID accountId) {
        log.info("Fetching account with ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Account not found with ID: {}", accountId);
                    return new AccountNotFoundException("Account not found with ID: " + accountId);
                });

        return accountMapper.toDto(account);
    }

    @Override
    public AccountDto updateAccount(UUID accountId, UpdateAccountRequest request) {
        log.info("Updating account with ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Account not found with ID: {}", accountId);
                    return new AccountNotFoundException("Account not found with ID: " + accountId);
                });

        // Update only non-null fields
        if (request.email() != null) {
            // Check if email is already taken by another account
            if (!request.email().equals(account.getEmail()) &&
                    accountRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("Email already exists");
            }
            account.setEmail(request.email());
        }

        if (request.phoneNumber() != null) {
            // Check if phone number is already taken by another account
            if (!request.phoneNumber().equals(account.getPhoneNumber()) &&
                    accountRepository.existsByPhoneNumber(request.phoneNumber())) {
                throw new IllegalArgumentException("Phone number already exists");
            }
            account.setPhoneNumber(request.phoneNumber());
        }

        if (request.role() != null) {
            account.setRole(request.role());
        }

        if (request.active() != null) {
            account.setActive(request.active());
            if (!request.active()) {
                deactivateCoachProfileIfExists(account);
            }
        }

        if (request.emailVerified() != null) {
            account.setEmailVerified(request.emailVerified());
        }

        Account updatedAccount = accountRepository.save(account);
        log.info("Account updated successfully with ID: {}", accountId);

        return accountMapper.toDto(updatedAccount);
    }

    @Override
    public void deleteAccount(UUID accountId) {
        log.info("Deactivating account with ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Account not found with ID: {}", accountId);
                    return new AccountNotFoundException("Account not found with ID: " + accountId);
                });

        account.setActive(false);
        deactivateCoachProfileIfExists(account);
        accountRepository.save(account);
        log.info("Account deactivated successfully with ID: {}", accountId);

        eventPublisher.publishEvent(new NotificationEvent(
                this,
                accountId,
                NotificationType.ACCOUNT_DEACTIVATED,
                "Tài khoản đã bị vô hiệu hóa",
                "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ hỗ trợ để được hỗ trợ.",
                accountId, "ACCOUNT"));
    }

    @Override
    public AccountDto activateAccount(UUID accountId) {
        log.info("Activating account with ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Account not found with ID: {}", accountId);
                    return new AccountNotFoundException("Account not found with ID: " + accountId);
                });

        account.setActive(true);
        Account updatedAccount = accountRepository.save(account);
        log.info("Account activated successfully with ID: {}", accountId);

        eventPublisher.publishEvent(new NotificationEvent(
                this,
                accountId,
                NotificationType.ACCOUNT_REACTIVATED,
                "Tài khoản đã được kích hoạt lại",
                "Tài khoản của bạn đã được kích hoạt lại. Chào mừng trở lại!",
                accountId, "ACCOUNT"));

        return accountMapper.toDto(updatedAccount);
    }

    private void deactivateCoachProfileIfExists(Account account) {
        if (account.getRole() != Role.COACH) {
            return;
        }

        coachRepository.findById(account.getAccountId()).ifPresent(coach -> {
            if (coach.isActive()) {
                coach.setActive(false);
                coachRepository.save(coach);
                log.info("Coach profile also deactivated for account ID: {}", account.getAccountId());
            }
        });
    }
}
