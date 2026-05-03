package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        String roleWithPrefix = "ROLE_" + account.getRole().name();
        log.info("=== Loading UserDetails for {} ===", email);
        log.info("Account ID: {}", account.getAccountId());
        log.info("Role from DB: {}", account.getRole().name());
        log.info("Authority being assigned: {}", roleWithPrefix);
        log.info("Is Active: {}", account.isActive());
        log.info("Is Email Verified: {}", account.isEmailVerified());

        UserDetails userDetails = User.builder()
                .username(account.getEmail())
                .password(account.getPasswordHash())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority(roleWithPrefix)))
                .accountExpired(false)
                .accountLocked(!account.isActive())
                .credentialsExpired(false)
                .disabled(!account.isEmailVerified())
                .build();

        log.info("UserDetails created with authorities: {}", userDetails.getAuthorities());

        return userDetails;
    }
}
