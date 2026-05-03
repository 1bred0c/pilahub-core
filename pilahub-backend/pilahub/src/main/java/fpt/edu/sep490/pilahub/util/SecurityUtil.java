package fpt.edu.sep490.pilahub.util;

import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.exception.AccountNotFoundException;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtil {

    private final AccountRepository accountRepository;

    /**
     * Get the currently authenticated user's email
     * @return email of the authenticated user
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authenticated user found in SecurityContext");
            throw new IllegalStateException("No authenticated user found");
        }
        String email = authentication.getName();
        log.debug("Current authenticated user email: {}", email);
        return email;
    }

    /**
     * Get the currently authenticated user's Account
     * @return Account of the authenticated user
     */
    public Account getCurrentUser() {
        String email = getCurrentUserEmail();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Account not found for email: {}", email);
                    return new AccountNotFoundException("Account not found for email: " + email);
                });
        log.debug("Found account for email {}: accountId={}, role={}", email, account.getAccountId(), account.getRole());
        return account;
    }

    /**
     * Get the currently authenticated user's account ID
     * @return UUID of the authenticated user's account
     */
    public UUID getCurrentUserId() {
        UUID userId = getCurrentUser().getAccountId();
        log.debug("Current user ID: {}", userId);
        return userId;
    }

    /**
     * Get the currently authenticated user's role
     * @return Role of the authenticated user
     */
    public Role getCurrentUserRole() {
        Role role = getCurrentUser().getRole();
        log.debug("Current user role: {}", role);
        return role;
    }
}
