package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.AccountDto;
import fpt.edu.sep490.pilahub.dto.request.CreateCoachAccountRequest;
import fpt.edu.sep490.pilahub.dto.response.AuthResponse;
import fpt.edu.sep490.pilahub.dto.response.GoogleAuthResponse;
import fpt.edu.sep490.pilahub.dto.request.GoogleLoginRequest;
import fpt.edu.sep490.pilahub.dto.request.LoginRequest;
import fpt.edu.sep490.pilahub.dto.request.RegisterRequest;
import fpt.edu.sep490.pilahub.enums.PackageType;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.exception.*;
import fpt.edu.sep490.pilahub.mapper.AccountMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.Subscription;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.SubscriptionRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.service.AuthService;
import fpt.edu.sep490.pilahub.service.EmailService;
import fpt.edu.sep490.pilahub.service.OtpService;
import fpt.edu.sep490.pilahub.util.JwtUtil;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import fpt.edu.sep490.pilahub.util.GoogleTokenVerifier;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AccountMapper accountMapper;
    private final SecurityUtil securityUtil;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final SubscriptionRepository subscriptionRepository;
    private final TraineeRepository traineeRepository;

    @Override
    public void register(RegisterRequest request) {
        registerWithRole(request, Role.TRAINEE);
    }

    @Override
    public void registerVendor(RegisterRequest request) {
        registerWithRole(request, Role.VENDOR);
    }

    private void registerWithRole(RegisterRequest request, Role role) {
        log.info("Registering new {} account with email: {}", role, request.email());

        // Check if email already exists
        if (accountRepository.existsByEmail(request.email())) {
            throw new DuplicateAccountException("Email already registered");
        }

        // Check if phone number already exists
        if (accountRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateAccountException("Phone number already registered");
        }

        // Hash password using BCrypt
        String hashedPassword = passwordEncoder.encode(request.password());

        // Create account (not active initially, will be activated after OTP verification)
        Account account = Account.builder()
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .passwordHash(hashedPassword)
                .role(role)
                .active(false) // Account is not active until email is verified
                .emailVerified(false)
                .build();

        accountRepository.save(account);
        log.info("{} account created successfully for email: {}", role, request.email());

        // Generate and send OTP
        String otpCode = otpService.generateAndSaveOtp(request.email());
        emailService.sendOtpEmail(request.email(), otpCode);

        log.info("{} registration completed, OTP sent to: {}", role, request.email());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        // Find account by email
        Account account = accountRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Check if email is verified
        if (!account.isEmailVerified()) {
            throw new AccountNotVerifiedException("Please verify your email before logging in");
        }

        // Check if account is active
        if (!account.isActive()) {
            throw new AccountNotFoundException("Account is deactivated");
        }

        // Update last seen timestamp
        account.setLastSeenAt(java.time.Instant.now());
        accountRepository.save(account);

        // Generate JWT tokens
        String accessToken = jwtUtil.generateToken(account.getAccountId(), account.getEmail(), account.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(account.getAccountId(), account.getEmail());

        log.info("Login successful for email: {}", request.email());

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtUtil.getExpirationTime(),
                jwtUtil.getRefreshExpirationTime(),
                accountMapper.toDto(account)
        );
    }

    @Override
    public GoogleAuthResponse googleLogin(GoogleLoginRequest request) {
        log.info("Google login attempt with ID token");

        // Step 1: Verify Google ID token and extract user info
        GoogleTokenVerifier.GoogleUserInfo googleUserInfo;
        try {
            googleUserInfo = googleTokenVerifier.verifyToken(request.googleIdToken());
        } catch (IllegalArgumentException e) {
            log.error("Invalid Google ID token: {}", e.getMessage());
            throw new InvalidCredentialsException("Invalid Google ID token: " + e.getMessage());
        }

        String email = googleUserInfo.email();
        log.info("Google token verified successfully for email: {}", email);

        // Step 2: Check if account exists
        var accountOpt = accountRepository.findByEmail(email);

        if (accountOpt.isEmpty()) {
            // New user - create account with minimal info
            log.info("New Google user detected: {}. Creating account...", email);

            // Create new account for Google user
            Account newAccount = Account.builder()
                    .email(email)
                    .phoneNumber(null) // Will be updated later via account endpoint
                    .passwordHash(null) // Will be set later via reset password endpoint
                    .role(Role.TRAINEE) // Default role
                    .active(true) // Active immediately since Google verified the email
                    .emailVerified(true) // Email is verified by Google
                    .build();

            accountRepository.save(newAccount);
            log.info("New Google account created for email: {} (phoneNumber: null, password: null)", email);

            // Generate JWT tokens
            String accessToken = jwtUtil.generateToken(newAccount.getAccountId(), newAccount.getEmail(), newAccount.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(newAccount.getAccountId(), newAccount.getEmail());

            // Send welcome email
            String displayName = googleUserInfo.givenName() != null 
                    ? googleUserInfo.givenName() 
                    : newAccount.getEmail().split("@")[0];
            emailService.sendWelcomeEmail(newAccount.getEmail(), displayName);

            AuthResponse authResponse = new AuthResponse(
                    accessToken,
                    refreshToken,
                    jwtUtil.getExpirationTime(),
                    jwtUtil.getRefreshExpirationTime(),
                    accountMapper.toDto(newAccount)
            );

            log.info("Google login successful for new user: {}", email);
            return GoogleAuthResponse.success(authResponse);
        }

        // Existing user - login directly
        Account account = accountOpt.get();

        // Check if account is active
        if (!account.isActive()) {
            throw new AccountNotFoundException("Account is deactivated");
        }

        // Update last seen timestamp
        account.setLastSeenAt(java.time.Instant.now());
        accountRepository.save(account);

        // Generate JWT tokens
        String accessToken = jwtUtil.generateToken(account.getAccountId(), account.getEmail(), account.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(account.getAccountId(), account.getEmail());

        AuthResponse authResponse = new AuthResponse(
                accessToken,
                refreshToken,
                jwtUtil.getExpirationTime(),
                jwtUtil.getRefreshExpirationTime(),
                accountMapper.toDto(account)
            );

        log.info("Google login successful for existing user: {}", email);
        return GoogleAuthResponse.success(authResponse);
    }

    @Override
    public void verifyEmail(String email, String otpCode) {
        log.info("Email verification attempt for: {}", email);

        // Find account
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        // Check if already verified
        if (account.isEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        // Verify OTP
        if (!otpService.verifyOtp(email, otpCode)) {
            throw new InvalidOtpException("Invalid or expired OTP code");
        }

        // Update account
        account.setEmailVerified(true);
        account.setActive(true); // Activate account after email verification
        accountRepository.save(account);

        // Send welcome email
        emailService.sendWelcomeEmail(email, email.split("@")[0]);

        log.info("Email verified successfully for: {}", email);
    }

    @Override
    public void resendOtp(String email) {
        log.info("Resending OTP for email: {}", email);

        // Find account
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        // Check if already verified
        if (account.isEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        // Generate and send new OTP
        String otpCode = otpService.generateAndSaveOtp(email);
        emailService.sendOtpEmail(email, otpCode);

        log.info("OTP resent successfully to: {}", email);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refresh token request");

        try {
            // Extract email from refresh token
            String email = jwtUtil.extractUsername(refreshToken);

            // Find account
            Account account = accountRepository.findByEmail(email)
                    .orElseThrow(() -> new AccountNotFoundException("Account not found"));

            // Check if account is active and verified
            if (!account.isActive() || !account.isEmailVerified()) {
                throw new InvalidCredentialsException("Account is not active or verified");
            }

            // Update last seen timestamp
            account.setLastSeenAt(java.time.Instant.now());
            accountRepository.save(account);

            // Generate new tokens
            String newAccessToken = jwtUtil.generateToken(account.getAccountId(), account.getEmail(), account.getRole().name());
            String newRefreshToken = jwtUtil.generateRefreshToken(account.getAccountId(), account.getEmail());

            log.info("Tokens refreshed successfully for email: {}", email);

            return new AuthResponse(
                    newAccessToken,
                    newRefreshToken,
                    jwtUtil.getExpirationTime(),
                    jwtUtil.getRefreshExpirationTime(),
                    accountMapper.toDto(account)
            );
        } catch (Exception e) {
            log.error("Failed to refresh token: {}", e.getMessage());
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Password reset request for email: {}", email);

        // Check if account exists
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("No account found with email: " + email));

        // Check if account is active
        if (!account.isActive()) {
            throw new AccountNotFoundException("Account is deactivated");
        }

        // Generate and send OTP for password reset
        String otpCode = otpService.generateAndSaveOtp(email);
        emailService.sendPasswordResetOtp(email, otpCode);

        log.info("Password reset OTP sent to: {}", email);
    }

    @Override
    public void resetPassword(String email, String otpCode, String newPassword) {
        log.info("Attempting to reset password for email: {}", email);

        // Find account
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        // Verify OTP
        if (!otpService.verifyOtp(email, otpCode)) {
            throw new InvalidOtpException("Invalid or expired OTP code");
        }

        // Hash new password
        String hashedPassword = passwordEncoder.encode(newPassword);

        // Update password
        account.setPasswordHash(hashedPassword);
        accountRepository.save(account);

        log.info("Password reset successful for email: {}", email);

        // Send confirmation email
        emailService.sendPasswordChangedNotification(email);
    }

    @Override
    public AccountDto createCoachAccountByAdmin(CreateCoachAccountRequest request) {
        log.info("Admin creating coach account with email: {}", request.email());

        if (accountRepository.existsByEmail(request.email())) {
            throw new DuplicateAccountException("Email already registered");
        }

        if (accountRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateAccountException("Phone number already registered");
        }

        Account account = Account.builder()
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.COACH)
                .active(true)
                .emailVerified(true)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Coach account created successfully by admin for email: {}", request.email());

        return accountMapper.toDto(savedAccount);
    }

    @Override
    public AccountDto getCurrentUser() {
        log.info("Fetching current authenticated user");
        Account account = securityUtil.getCurrentUser();
        
        // Get active package type if user is a trainee with active subscription
        PackageType activePackageType = null;
        if (account.getRole() == Role.TRAINEE) {
            try {
                Trainee trainee = traineeRepository.findById(account.getAccountId()).orElse(null);
                if (trainee != null) {
                    Subscription activeSubscription = subscriptionRepository
                            .findActiveSubscriptionByTraineeId(trainee.getTraineeId())
                            .orElse(null);
                    if (activeSubscription != null && activeSubscription.getSubscribedPackage() != null) {
                        activePackageType = activeSubscription.getSubscribedPackage().getPackageType();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch active subscription for user {}: {}", account.getAccountId(), e.getMessage());
            }
        }
        
        return accountMapper.toDto(account, activePackageType);
    }
}
