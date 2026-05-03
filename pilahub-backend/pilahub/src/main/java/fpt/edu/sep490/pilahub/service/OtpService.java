package fpt.edu.sep490.pilahub.service;

public interface OtpService {

    String generateAndSaveOtp(String email);

    boolean verifyOtp(String email, String otpCode);

    void invalidateOtps(String email);

    void cleanupExpiredOtps();
}
