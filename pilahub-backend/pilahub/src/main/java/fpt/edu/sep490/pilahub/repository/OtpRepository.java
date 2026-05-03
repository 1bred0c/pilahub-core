package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {

    Optional<Otp> findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(
            String email,
            String otpCode,
            Instant currentTime
    );

    void deleteByEmailAndUsedFalse(String email);

    void deleteByExpiryTimeBefore(Instant currentTime);
}
