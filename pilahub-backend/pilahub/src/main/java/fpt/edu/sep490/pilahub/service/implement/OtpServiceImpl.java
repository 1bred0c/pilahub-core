package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.pojo.Otp;
import fpt.edu.sep490.pilahub.repository.OtpRepository;
import fpt.edu.sep490.pilahub.service.OtpService;
import fpt.edu.sep490.pilahub.util.OtpGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final OtpGenerator otpGenerator;

    @Value("${otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Override
    public String generateAndSaveOtp(String email) {
        // Invalidate any existing OTPs for this email
        invalidateOtps(email);

        // Generate new OTP
        String otpCode = otpGenerator.generateOtp();
        Instant expiryTime = Instant.now().plusSeconds(otpExpirationMinutes * 60L);

        Otp otp = Otp.builder()
                .otpCode(otpCode)
                .email(email)
                .expiryTime(expiryTime)
                .used(false)
                .build();

        otpRepository.save(otp);
        log.info("OTP generated for email: {}", email);

        return otpCode;
    }

    @Override
    public boolean verifyOtp(String email, String otpCode) {
        return otpRepository.findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(
                        email, otpCode, Instant.now())
                .map(otp -> {
                    otp.setUsed(true);
                    otpRepository.save(otp);
                    log.info("OTP verified successfully for email: {}", email);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("Invalid or expired OTP for email: {}", email);
                    return false;
                });
    }

    @Override
    public void invalidateOtps(String email) {
        otpRepository.deleteByEmailAndUsedFalse(email);
        log.info("Invalidated existing OTPs for email: {}", email);
    }

    @Override
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiryTimeBefore(Instant.now());
        log.info("Cleaned up expired OTPs");
    }
}
