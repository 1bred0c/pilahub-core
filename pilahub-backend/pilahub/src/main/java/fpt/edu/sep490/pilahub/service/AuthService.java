package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.AccountDto;
import fpt.edu.sep490.pilahub.dto.request.CreateCoachAccountRequest;
import fpt.edu.sep490.pilahub.dto.response.AuthResponse;
import fpt.edu.sep490.pilahub.dto.response.GoogleAuthResponse;
import fpt.edu.sep490.pilahub.dto.request.GoogleLoginRequest;
import fpt.edu.sep490.pilahub.dto.request.LoginRequest;
import fpt.edu.sep490.pilahub.dto.request.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    void registerVendor(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    GoogleAuthResponse googleLogin(GoogleLoginRequest request);

    void verifyEmail(String email, String otpCode);

    void resendOtp(String email);

    AuthResponse refreshToken(String refreshToken);

    void forgotPassword(String email);

    void resetPassword(String email, String otpCode, String newPassword);

    AccountDto createCoachAccountByAdmin(CreateCoachAccountRequest request);

    AccountDto getCurrentUser();
}
