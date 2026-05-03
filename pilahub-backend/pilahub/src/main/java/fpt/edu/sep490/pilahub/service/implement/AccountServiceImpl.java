package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.CoachRepository;
import fpt.edu.sep490.pilahub.service.AccountService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CoachRepository coachRepository;
    private final SecurityUtil securityUtil;

    @Override
    public Account registerByEmail(
            String email,
            String phoneNumber,
            String passwordHash,
            Role role) {
        if (accountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (accountRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        Account account = Account.builder()
                .email(email)
                .phoneNumber(phoneNumber)
                .passwordHash(passwordHash)
                .role(role)
                .active(true)
                .emailVerified(false)
                .build();

        try {
            return accountRepository.save(account);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Duplicate account data", ex);
        }
    }

    @Override
    public Optional<Account> getById(UUID accountId) {
        return accountRepository.findById(accountId);
    }

    @Override
    public Optional<Account> getActiveByEmail(String email) {
        return accountRepository.findByEmailAndActiveTrue(email);
    }

    @Override
    public Optional<Account> getActiveByPhoneNumber(String phoneNumber) {
        return accountRepository.findByPhoneNumberAndActiveTrue(phoneNumber);
    }

    @Override
    public Account updateFcmToken(String fcmToken) {
        UUID accountId = securityUtil.getCurrentUserId();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.setFcmToken(fcmToken);
        return accountRepository.save(account);
    }

    @Override
    public Account updateIsReminded(Boolean isReminded) {
        UUID accountId = securityUtil.getCurrentUserId();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.setIsReminded(isReminded);
        return accountRepository.save(account);
    }

    @Override
    public void verifyEmail(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.isEmailVerified()) {
            return;
        }

        account.setEmailVerified(true);
        accountRepository.save(account);
    }

    @Override
    public void deactivateAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.isActive()) {
            return;
        }

        account.setActive(false);
        if (account.getRole() == Role.COACH) {
            coachRepository.findById(account.getAccountId()).ifPresent(coach -> {
                if (coach.isActive()) {
                    coach.setActive(false);
                    coachRepository.save(coach);
                }
            });
        }
        accountRepository.save(account);
    }
}
