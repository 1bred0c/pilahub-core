package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.BookingStatus;
import fpt.edu.sep490.pilahub.enums.BookingType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "coach_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachBooking {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull(message = "Coach must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @NotNull(message = "Trainee must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    // Booking time đơn vị sẽ là giờ, và mỗi booking sẽ có startTime và endTime,
    // thời gian tối thiểu là 1 giờ, tối đa là 4 giờ
    @NotNull(message = "Start time must not be null")
    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @NotNull(message = "End time must not be null")
    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @NotNull(message = "Price per hour must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price per hour must be greater than 0")
    @Column(name = "price_per_hour", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePerHour;

    @NotNull(message = "Total amount must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @NotNull(message = "Booking status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private BookingStatus status;

    @NotNull(message = "Booking type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false, length = 50)
    private BookingType bookingType;
    // Đọc trong ENUM để hiểu nghiệp vụ

    @Column(name = "recurring_group_id")
    private UUID recurringGroupId; // nếu thuộc 1 chuỗi lặp, thường sẽ là id của booking coach để follow theo
                                   // roadmap, chọn các buổi tập ngay lúc booking.

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
