package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts", uniqueConstraints = {
                @UniqueConstraint(name = "uk_account_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_account_phone", columnNames = "phone_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

        @Id
        @GeneratedValue
        @Column(name = "account_id", nullable = false, updatable = false)
        private UUID accountId;

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email format is invalid")
        @Size(max = 255)
        @Column(name = "email", nullable = false, length = 255)
        private String email;

        @NotNull(message = "Role must not be null")
        @Enumerated(EnumType.STRING)
        @Column(name = "role", nullable = false, length = 30)
        private Role role;

    @Pattern(
            regexp = "^\\+?[0-9]{9,15}$",
            message = "Phone number format is invalid"
    )
    @Column(name = "phone_number", length = 20) // Nullable - can be updated later
    private String phoneNumber;

    @Size(min = 60, max = 255)
    @Column(name = "password_hash", length = 255) // Nullable - for Google login users
    private String passwordHash;

        @Column(name = "is_active", nullable = false)
        @Builder.Default
        private boolean active = true;

        @Column(name = "is_email_verified", nullable = false)
        @Builder.Default
        private boolean emailVerified = false;

        @Column(name = "last_seen_at")
        private Instant lastSeenAt;

        @Size(max = 500)
        @Column(name = "fcm_token", length = 500)
        private String fcmToken;

        @Column(name = "is_reminded")
        private Boolean isReminded;

        @NotNull
        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt;

        @PrePersist
        protected void onCreate() {
                this.createdAt = Instant.now();
        }
}
