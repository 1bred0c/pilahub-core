package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.AccountDto;
import fpt.edu.sep490.pilahub.dto.request.*;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.dto.response.AuthResponse;
import fpt.edu.sep490.pilahub.dto.response.GoogleAuthResponse;
import fpt.edu.sep490.pilahub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new account", description = "Create new user account. OTP will be sent to email for verification.")
    @ApiResponse(responseCode = "201", description = "Registration successful")
    @ApiResponse(responseCode = "409", description = "Email or phone already exists")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Registration successful. Please check your email for OTP verification.", null));
    }

    @PostMapping("/register-vendor")
    @Operation(
            summary = "Register new vendor account",
            description = "Create new vendor account. OTP will be sent to email for verification. " +
                    "Use verify-email/resend-otp endpoints to complete verification."
    )
    @ApiResponse(responseCode = "201", description = "Vendor registration successful")
    @ApiResponse(responseCode = "409", description = "Email or phone already exists")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<Void>> registerVendor(@Valid @RequestBody RegisterRequest request) {
        authService.registerVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Vendor registration successful. Please check your email for OTP verification.", null));
    }

    @PostMapping("/admin/create-coach")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create coach account (Admin)",
            description = "Admin creates a coach account directly without OTP verification. Coach can complete profile after first login."
    )
    @ApiResponse(responseCode = "201", description = "Coach account created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    @ApiResponse(responseCode = "409", description = "Email or phone already exists")
    public ResponseEntity<APIResponse<AccountDto>> createCoachAccountByAdmin(
            @Valid @RequestBody CreateCoachAccountRequest request) {
        AccountDto createdCoach = authService.createCoachAccountByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Coach account created successfully.", createdCoach));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and get JWT token. Valid for 24 hours.")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "403", description = "Email not verified")
    public ResponseEntity<APIResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(APIResponse.success("Login successful", authResponse));
    }

    @PostMapping("/google-login")
    @Operation(
            summary = "Google Login",
            description = "Login or register using Google account. " +
                    "For new users, password and phone number are required. " +
                    "Email is automatically verified by Google."
    )
    @ApiResponse(responseCode = "200", description = "Login/Registration successful")
    @ApiResponse(responseCode = "400", description = "Invalid input or missing required fields for new user")
    @ApiResponse(responseCode = "409", description = "Phone number already exists")
    public ResponseEntity<APIResponse<GoogleAuthResponse>> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {
        GoogleAuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(
                APIResponse.success(
                        response.requiresRegistration()
                                ? "Please complete registration with password and phone number"
                                : "Google login successful",
                        response
                )
        );
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with OTP", description = "Verify account using 6-digit OTP code. OTP expires after 5 minutes.")
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<APIResponse<Void>> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyEmail(request.email(), request.otpCode());
        return ResponseEntity.ok(APIResponse.success("Email verified successfully. You can now login.", null));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP", description = "Request new OTP code if previous one expired.")
    @ApiResponse(responseCode = "200", description = "OTP resent successfully")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "400", description = "Email already verified")
    public ResponseEntity<APIResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request.email());
        return ResponseEntity.ok(APIResponse.success("OTP has been resent to your email.", null));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token. Updates last seen timestamp.")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    public ResponseEntity<APIResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(APIResponse.success("Token refreshed successfully", authResponse));
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Forgot Password",
            description = "Request password reset OTP. OTP will be sent to registered email address. " +
                    "Only registered and active accounts can request password reset."
    )
    @ApiResponse(responseCode = "200", description = "Password reset OTP sent successfully")
    @ApiResponse(responseCode = "404", description = "Email not found or account not active")
    public ResponseEntity<APIResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.ok(
                APIResponse.success("Password reset code has been sent to your email. Please check your inbox.", null)
        );
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset Password",
            description = "Reset password using OTP code. OTP must be valid and not expired (5 minutes). " +
                    "After successful reset, user can login with new password."
    )
    @ApiResponse(responseCode = "200", description = "Password reset successful")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<APIResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.email(), request.otpCode(), request.newPassword());
        return ResponseEntity.ok(
                APIResponse.success("Password has been reset successfully. You can now login with your new password.", null)
        );
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Current User",
            description = "Get current authenticated user's account information. Requires valid JWT token."
    )
    @ApiResponse(responseCode = "200", description = "User information retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    public ResponseEntity<APIResponse<AccountDto>> getCurrentUser() {
        AccountDto currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(APIResponse.success("User information retrieved successfully", currentUser));
    }
}
