package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Otp {

    @Id
    @GeneratedValue
    @Column(name = "otp_id", nullable = false, updatable = false)
    private UUID otpId;

    @NotBlank(message = "OTP code must not be blank")
    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    @NotBlank(message = "Email must not be blank")
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @NotNull(message = "Expiry time must not be null")
    @Column(name = "expiry_time", nullable = false)
    private Instant expiryTime;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private boolean used = false;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
