package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.CoachBookingDto;
import fpt.edu.sep490.pilahub.enums.NotificationType;
import fpt.edu.sep490.pilahub.event.NotificationEvent;
import fpt.edu.sep490.pilahub.dto.CoachBusyScheduleDto;
import fpt.edu.sep490.pilahub.dto.PersonalScheduleDto;
import fpt.edu.sep490.pilahub.dto.request.LockFundsRequest;
import fpt.edu.sep490.pilahub.dto.request.UnlockFundsRequest;
import fpt.edu.sep490.pilahub.dto.request.booking.BookingSlotRequest;
import fpt.edu.sep490.pilahub.dto.request.booking.CreateBatchBookingRequest;
import fpt.edu.sep490.pilahub.dto.request.booking.CreateSingleBookingRequest;
import fpt.edu.sep490.pilahub.dto.response.BatchBookingResponse;
import fpt.edu.sep490.pilahub.dto.response.BusyTimeSlot;
import fpt.edu.sep490.pilahub.enums.BookingStatus;
import fpt.edu.sep490.pilahub.enums.BookingType;
import fpt.edu.sep490.pilahub.enums.BusyScheduleType;
import fpt.edu.sep490.pilahub.enums.TransactionType;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.CoachBookingMapper;
import fpt.edu.sep490.pilahub.mapper.PersonalScheduleMapper;
import fpt.edu.sep490.pilahub.pojo.*;
import fpt.edu.sep490.pilahub.repository.*;
import fpt.edu.sep490.pilahub.service.CoachBookingService;
import fpt.edu.sep490.pilahub.service.LiveSessionService;
import fpt.edu.sep490.pilahub.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CoachBookingServiceImpl implements CoachBookingService {

    private final CoachBookingRepository coachBookingRepository;
    private final CoachRepository coachRepository;
    private final TraineeRepository traineeRepository;
    private final CoachTimeOffRepository coachTimeOffRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final CoachBookingMapper coachBookingMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PersonalScheduleMapper personalScheduleMapper;
    private final PersonalScheduleRepository personalScheduleRepository;
    private final WalletService walletService;
    private final LiveSessionService liveSessionService;

    private static final int MIN_BOOKING_HOURS = 1;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private static final int MAX_BOOKING_HOURS = 4;
    private static final int WORKING_START_HOUR = 6;
    private static final int WORKING_END_HOUR = 20;

    @Override
    public CoachBookingDto createSingleBooking(UUID traineeId, CreateSingleBookingRequest request) {
        // Validate trainee
        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", traineeId));

        // Validate coach
        Coach coach = coachRepository.findById(request.coachId())
                .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", request.coachId()));

        if (!coach.isActive()) {
            throw new IllegalStateException("Coach is not active");
        }

        // Validate booking time
        validateBookingTime(request.startTime(), request.endTime());

        // Check for conflicts
        List<BookingStatus> validStatuses = Arrays.asList(
                BookingStatus.SCHEDULED,
                BookingStatus.READY,
                BookingStatus.IN_PROGRESS);

        List<CoachBooking> conflicts = coachBookingRepository.findConflictingBookings(
                request.coachId(),
                request.startTime(),
                request.endTime(),
                validStatuses);

        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Time slot conflicts with existing booking");
        }

        // Check coach time off
        List<CoachTimeOff> timeOffs = coachTimeOffRepository.findConflictingTimeOffs(
                request.coachId(),
                request.startTime(),
                request.endTime());

        if (!timeOffs.isEmpty()) {
            throw new IllegalStateException("Time slot conflicts with coach's time off");
        }

        // Calculate pricing
        BigDecimal pricePerHour = coach.getPricePerHour();
        long hours = Duration.between(request.startTime(), request.endTime()).toHours();
        BigDecimal totalAmount = pricePerHour.multiply(BigDecimal.valueOf(hours));

        // Process payment - lock funds in trainee's wallet
        LockFundsRequest lockRequest = new LockFundsRequest(totalAmount, "Coach booking payment");
        walletService.lockFunds(traineeId, lockRequest);
        log.info("Locked {} VND from trainee {} for booking", totalAmount, traineeId);

        // Create booking
        CoachBooking booking = CoachBooking.builder()
                .coach(coach)
                .trainee(trainee)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .pricePerHour(pricePerHour)
                .totalAmount(totalAmount)
                .status(BookingStatus.SCHEDULED)
                .bookingType(request.bookingType())
                .recurringGroupId(null) // Single booking always has null recurringGroupId
                .build();

        CoachBooking savedBooking = coachBookingRepository.save(booking);

        // Create transaction record
        createTransactionRecord(traineeId, totalAmount, savedBooking.getId());

        // Notify both parties
        String sessionTime = DATE_FMT.format(savedBooking.getStartTime());
        eventPublisher.publishEvent(new NotificationEvent(
                this,
                trainee.getTraineeId(),
                NotificationType.BOOKING_CONFIRMED,
                "Xác nhận đặt chỗ",
                "Buổi huấn luyện của bạn vào " + sessionTime + " đã được xác nhận.",
                savedBooking.getId(), "COACH_BOOKING"));
        eventPublisher.publishEvent(new NotificationEvent(
                this,
                coach.getCoachId(),
                NotificationType.BOOKING_CONFIRMED,
                "Buổi Huấn Luyện Mới",
                "Bạn có một buổi huấn luyện mới được lên lịch vào " + sessionTime + ".",
                savedBooking.getId(), "COACH_BOOKING"));

        // Create live session for this booking
        try {
            liveSessionService.createLiveSession(savedBooking.getId());
            log.info("Live session created for booking: {}", savedBooking.getId());
        } catch (Exception e) {
            log.error("Failed to create live session for booking {}: {}", savedBooking.getId(), e.getMessage());
        }

        return toDto(savedBooking);
    }

    @Override
    public BatchBookingResponse createBatchBooking(UUID traineeId, CreateBatchBookingRequest request) {
        // Validate trainee
        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", traineeId));

        // Validate coach
        Coach coach = coachRepository.findById(request.coachId())
                .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", request.coachId()));

        if (!coach.isActive()) {
            throw new IllegalStateException("Coach is not active");
        }

        List<CoachBookingDto> successfulBookings = new ArrayList<>();
        List<BookingSlotRequest> conflictingSlots = new ArrayList<>();

        BigDecimal pricePerHour = coach.getPricePerHour();
        BigDecimal totalPayment = BigDecimal.ZERO;

        // Prepare valid statuses for conflict checking
        List<BookingStatus> validStatuses = Arrays.asList(
                BookingStatus.SCHEDULED,
                BookingStatus.READY,
                BookingStatus.IN_PROGRESS);

        // First pass: validate all slots and identify conflicts
        for (BookingSlotRequest slot : request.bookingSlots()) {
            try {
                // Validate booking time
                validateBookingTime(slot.startTime(), slot.endTime());

                // Check for conflicts with existing bookings
                List<CoachBooking> bookingConflicts = coachBookingRepository.findConflictingBookings(
                        request.coachId(),
                        slot.startTime(),
                        slot.endTime(),
                        validStatuses);

                // Check coach time off
                List<CoachTimeOff> timeOffs = coachTimeOffRepository.findConflictingTimeOffs(
                        request.coachId(),
                        slot.startTime(),
                        slot.endTime());

                if (!bookingConflicts.isEmpty() || !timeOffs.isEmpty()) {
                    conflictingSlots.add(slot);
                } else {
                    // Calculate cost for this slot
                    long hours = Duration.between(slot.startTime(), slot.endTime()).toHours();
                    totalPayment = totalPayment.add(pricePerHour.multiply(BigDecimal.valueOf(hours)));
                }
            } catch (IllegalArgumentException e) {
                conflictingSlots.add(slot);
            }
        }

        // If there are conflicts, return immediately without creating any bookings
        if (!conflictingSlots.isEmpty()) {
            return new BatchBookingResponse(
                    successfulBookings, // empty list
                    conflictingSlots,
                    0,
                    conflictingSlots.size());
        }

        // No conflicts - process payment and create all bookings with shared
        // recurringGroupId
        if (totalPayment.compareTo(BigDecimal.ZERO) > 0) {
            // Process payment for all successful bookings - lock funds
            LockFundsRequest lockRequest = new LockFundsRequest(totalPayment, "Batch coach booking payment");
            walletService.lockFunds(traineeId, lockRequest);
            log.info("Locked {} VND from trainee {} for batch booking", totalPayment, traineeId);

            // Generate a new recurringGroupId for this batch
            // Use provided recurringGroupId if exists, otherwise generate new one
            UUID recurringGroupId = request.recurringGroupId() != null
                    ? request.recurringGroupId()
                    : UUID.randomUUID();

            // Create bookings for all slots (no conflicts at this point)
            for (BookingSlotRequest slot : request.bookingSlots()) {
                long hours = Duration.between(slot.startTime(), slot.endTime()).toHours();
                BigDecimal slotAmount = pricePerHour.multiply(BigDecimal.valueOf(hours));

                CoachBooking booking = CoachBooking.builder()
                        .coach(coach)
                        .trainee(trainee)
                        .startTime(slot.startTime())
                        .endTime(slot.endTime())
                        .pricePerHour(pricePerHour)
                        .totalAmount(slotAmount)
                        .status(BookingStatus.SCHEDULED)
                        .bookingType(request.bookingType())
                        .recurringGroupId(recurringGroupId) // All bookings share same recurringGroupId
                        .build();

                CoachBooking savedBooking = coachBookingRepository.save(booking);

                // Create transaction record for each booking
                createTransactionRecord(traineeId, slotAmount, savedBooking.getId());

                // Create live session for each booking
                try {
                    liveSessionService.createLiveSession(savedBooking.getId());
                    log.info("Live session created for booking: {}", savedBooking.getId());
                } catch (Exception e) {
                    log.error("Failed to create live session for booking {}: {}", savedBooking.getId(), e.getMessage());
                }

                successfulBookings.add(toDto(savedBooking));
            }

            if (!successfulBookings.isEmpty()) {
                int count = successfulBookings.size();
                eventPublisher.publishEvent(new NotificationEvent(
                        this,
                        trainee.getTraineeId(),
                        NotificationType.BOOKING_CONFIRMED,
                        "Xác Nhận Đặt Chỗ Hàng Loạt",
                        count + " buổi huấn luyện đã được đặt thành công.",
                        null, null));
                eventPublisher.publishEvent(new NotificationEvent(
                        this,
                        coach.getCoachId(),
                        NotificationType.BOOKING_CONFIRMED,
                        "Các Buổi Huấn Luyện Mới Hàng Loạt",
                        "Bạn có " + count + " buổi huấn luyện mới được lên lịch.",
                        null, null));
            }
        }

        return new BatchBookingResponse(
                successfulBookings,
                conflictingSlots, // empty list at this point
                successfulBookings.size(),
                0);
    }

    @Override
    public CoachBookingDto getBookingById(UUID bookingId) {
        CoachBooking booking = coachBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachBooking", "id", bookingId));
        return toDto(booking);
    }

    @Override
    public List<CoachBookingDto> getBookingsByCoach(UUID coachId) {
        List<CoachBooking> bookings = coachBookingRepository.findByCoach_CoachId(coachId);
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachBookingDto> getBookingsByTrainee(UUID traineeId) {
        List<CoachBooking> bookings = coachBookingRepository.findByTrainee_TraineeId(traineeId);
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachBookingDto> getBookingsByCoachAndStatus(UUID coachId, BookingStatus status) {
        List<CoachBooking> bookings = coachBookingRepository.findByCoach_CoachIdAndStatus(coachId, status);
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachBookingDto> getBookingsByTraineeAndStatus(UUID traineeId, BookingStatus status) {
        List<CoachBooking> bookings = coachBookingRepository.findByTrainee_TraineeIdAndStatus(traineeId, status);
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachBookingDto> getBookingsByCoachAndTimeRange(UUID coachId, Instant startTime, Instant endTime) {
        List<CoachBooking> bookings = coachBookingRepository.findByCoachIdAndTimeRange(coachId, startTime, endTime);
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachBookingDto> getBookingsByTraineeAndTimeRange(UUID traineeId, Instant startTime, Instant endTime) {
        List<CoachBooking> bookings = coachBookingRepository.findByTraineeIdAndTimeRange(traineeId, startTime, endTime);
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachBookingDto> getBookingsByRecurringGroup(UUID recurringGroupId) {
        List<CoachBooking> bookings = coachBookingRepository.findByRecurringGroupId(recurringGroupId);
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelBooking(UUID bookingId, UUID userId) {
        CoachBooking booking = coachBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachBooking", "id", bookingId));

        // Verify user is either coach or trainee
        boolean isCoach = booking.getCoach().getCoachId().equals(userId);
        boolean isTrainee = booking.getTrainee().getTraineeId().equals(userId);

        if (!isCoach && !isTrainee) {
            throw new IllegalStateException("You can only cancel your own bookings");
        }

        // Can only cancel SCHEDULED or READY bookings
        if (booking.getStatus() != BookingStatus.SCHEDULED &&
                booking.getStatus() != BookingStatus.READY) {
            throw new IllegalStateException("Can only cancel scheduled or ready bookings");
        }

        // Check if cancellation is at least 24 hours before start time
        Instant now = Instant.now();
        long hoursUntilStart = Duration.between(now, booking.getStartTime()).toHours();

        if (hoursUntilStart < 24) {
            throw new IllegalStateException("Bookings must be cancelled at least 24 hours in advance");
        }

        // Set status based on who cancels
        if (isCoach) {
            // Coach cancels - always refund to trainee
            booking.setStatus(BookingStatus.CANCELLED_BY_COACH);
            coachBookingRepository.save(booking);

            // Refund using unlock funds and update to REFUNDED status
            refundPayment(booking.getTrainee().getTraineeId(), booking.getTotalAmount(), bookingId);
            booking.setStatus(BookingStatus.REFUNDED);
            coachBookingRepository.save(booking);

            String sessionTime = DATE_FMT.format(booking.getStartTime());
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    booking.getTrainee().getTraineeId(),
                    NotificationType.BOOKING_REFUNDED,
                    "Đã Hủy Đặt Chỗ – Hoàn Tiền Đã Phát Hành",
                    "Buổi học của bạn vào " + sessionTime + " đã được huấn luyện viên hủy. " +
                            booking.getTotalAmount().toPlainString() + " VND đã được hoàn lại vào ví của bạn.",
                    bookingId, "COACH_BOOKING"));
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    booking.getCoach().getCoachId(),
                    NotificationType.BOOKING_CANCELLED,
                    "Đã Hủy Đặt Chỗ",
                    "Bạn đã hủy buổi học vào " + sessionTime + ". Học viên đã được hoàn tiền.",
                    bookingId, "COACH_BOOKING"));

            log.info("Booking {} cancelled by coach and refunded to trainee", bookingId);

        } else {
            // Trainee cancels - no refund by business rule
            booking.setStatus(BookingStatus.CANCELLED_BY_TRAINEE);
            coachBookingRepository.save(booking);

            String sessionTime = DATE_FMT.format(booking.getStartTime());
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    booking.getCoach().getCoachId(),
                    NotificationType.BOOKING_CANCELLED,
                    "Đã Hủy Đặt Chỗ",
                    "Một học viên đã hủy buổi học được lên lịch vào " + sessionTime + ".",
                    bookingId, "COACH_BOOKING"));

            log.info("Booking {} cancelled by trainee (no refund)", bookingId);

        }
    }

    @Override
    public void coachJoinSession(UUID bookingId, UUID coachId) {
        CoachBooking booking = coachBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachBooking", "id", bookingId));

        if (!booking.getCoach().getCoachId().equals(coachId)) {
            throw new IllegalStateException("You can only join your own sessions");
        }

        if (booking.getStatus() != BookingStatus.READY) {
            throw new IllegalStateException("Session is not ready to start");
        }

        // Keep booking in READY. IN_PROGRESS is set by LiveSessionService when both
        // participants are present.

        eventPublisher.publishEvent(new NotificationEvent(
                this,
                booking.getTrainee().getTraineeId(),
                NotificationType.BOOKING_IN_PROGRESS,
                "Huấn Luyện Viên Đã Tham Gia",
                "Huấn luyện viên đã tham gia. Buổi học sẽ bắt đầu khi cả hai người tham gia đều trong phòng.",
                bookingId, "COACH_BOOKING"));
    }

    @Override
    public void traineeJoinSession(UUID bookingId, UUID traineeId) {
        CoachBooking booking = coachBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachBooking", "id", bookingId));

        if (!booking.getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only join your own sessions");
        }

        if (booking.getStatus() != BookingStatus.READY) {
            throw new IllegalStateException("Session is not ready to start");
        }

        // Keep booking in READY. IN_PROGRESS is set by LiveSessionService when both
        // participants are present.

        eventPublisher.publishEvent(new NotificationEvent(
                this,
                booking.getCoach().getCoachId(),
                NotificationType.BOOKING_IN_PROGRESS,
                "Học Viên Đã Tham Gia",
                "Học viên đã tham gia. Buổi học sẽ bắt đầu khi cả hai người tham gia đều trong phòng.",
                bookingId, "COACH_BOOKING"));
    }

    @Override
    public void completeBooking(UUID bookingId, UUID userId) {
        CoachBooking booking = coachBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachBooking", "id", bookingId));

        // Verify user is either coach or trainee
        if (!booking.getCoach().getCoachId().equals(userId) &&
                !booking.getTrainee().getTraineeId().equals(userId)) {
            throw new IllegalStateException("You can only complete your own bookings");
        }

        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only complete in-progress bookings");
        }

        if (Instant.now().isBefore(booking.getEndTime())) {
            throw new IllegalStateException(
                    "Booking can only be completed after end time or when both users leave the live room");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        coachBookingRepository.save(booking);

        String sessionTime = DATE_FMT.format(booking.getStartTime());
        eventPublisher.publishEvent(new NotificationEvent(
                this,
                booking.getCoach().getCoachId(),
                NotificationType.BOOKING_COMPLETED,
                "Buổi Học Đã Hoàn Thành",
                "Buổi huấn luyện vào " + sessionTime + " đã được hoàn thành.",
                bookingId, "COACH_BOOKING"));
        eventPublisher.publishEvent(new NotificationEvent(
                this,
                booking.getTrainee().getTraineeId(),
                NotificationType.BOOKING_COMPLETED,
                "Buổi Học Đã Hoàn Thành",
                "Buổi huấn luyện của bạn vào " + sessionTime
                        + " đã được hoàn thành. Đừng quên để lại đánh giá của bạn!",
                bookingId, "COACH_BOOKING"));
    }

    @Override
    public List<CoachBookingDto> getAllBookings() {
        List<CoachBooking> bookings = coachBookingRepository.findAll();
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateBookingStatus(UUID bookingId, BookingStatus newStatus) {
        CoachBooking booking = coachBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachBooking", "id", bookingId));

        booking.setStatus(newStatus);
        coachBookingRepository.save(booking);

        if (newStatus == BookingStatus.READY) {
            liveSessionService.activateSessionForReadyBooking(bookingId);
        }
    }

    // Helper methods

    private CoachBookingDto toDto(CoachBooking booking) {
        CoachBookingDto dto = coachBookingMapper.toDto(booking);
        if (booking.getBookingType() == BookingType.PERSONAL_TRAINING_PACKAGE
                && booking.getRecurringGroupId() != null) {
            Instant startOfDay = booking.getStartTime().truncatedTo(ChronoUnit.DAYS);
            Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);
            PersonalScheduleDto scheduleDto = personalScheduleRepository
                    .findByRoadmapIdAndDate(booking.getRecurringGroupId(), startOfDay, endOfDay)
                    .map(personalScheduleMapper::toDto)
                    .orElse(null);
            return new CoachBookingDto(
                    dto.id(), dto.coach(), dto.trainee(), dto.startTime(), dto.endTime(),
                    dto.pricePerHour(), dto.totalAmount(), dto.status(), dto.bookingType(),
                    dto.recurringGroupId(), dto.createdAt(), scheduleDto);
        }
        return dto;
    }

    private void validateBookingTime(Instant startTime, Instant endTime) {
        Instant now = Instant.now();

        // Must book at least 1 hour in advance
        if (startTime.isBefore(now.plus(Duration.ofHours(1)))) {
            throw new IllegalArgumentException("Bookings must be made at least 1 hour in advance");
        }

        // Start time must be before end time
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // Calculate duration in hours
        long hours = Duration.between(startTime, endTime).toHours();

        // Check minimum and maximum hours
        if (hours < MIN_BOOKING_HOURS) {
            throw new IllegalArgumentException("Minimum booking duration is " + MIN_BOOKING_HOURS + " hour(s)");
        }

        if (hours > MAX_BOOKING_HOURS) {
            throw new IllegalArgumentException("Maximum booking duration is " + MAX_BOOKING_HOURS + " hours");
        }

        // Check if booking is within working hours (6:00 - 20:00)
        ZonedDateTime startZoned = startTime.atZone(ZoneId.systemDefault());
        ZonedDateTime endZoned = endTime.atZone(ZoneId.systemDefault());

        if (startZoned.getHour() < WORKING_START_HOUR || endZoned.getHour() > WORKING_END_HOUR) {
            throw new IllegalArgumentException(String.format(
                    "Bookings must be within working hours (%d:00 - %d:00)",
                    WORKING_START_HOUR,
                    WORKING_END_HOUR));
        }

        // If end time hour is exactly 20, check minutes
        if (endZoned.getHour() == WORKING_END_HOUR && endZoned.getMinute() > 0) {
            throw new IllegalArgumentException(String.format(
                    "Bookings must end by %d:00",
                    WORKING_END_HOUR));
        }
    }

    private void refundPayment(UUID traineeId, BigDecimal amount, UUID bookingId) {
        // Unlock funds to refund to trainee
        UnlockFundsRequest unlockRequest = new UnlockFundsRequest(amount, "Refund for cancelled coach booking");
        walletService.unlockFunds(traineeId, unlockRequest);

        // Create refund transaction with BOOKING_COACH_REFUND type
        Transaction refundTransaction = Transaction.builder()
                .transactionType(TransactionType.BOOKING_COACH_REFUND)
                .amount(amount)
                .accountId(traineeId)
                .referenceId(bookingId)
                .description("Refund for cancelled coach booking")
                .build();

        transactionRepository.save(refundTransaction);
        log.info("Refunded {} VND to trainee {} for booking {}", amount, traineeId, bookingId);
    }

    @Override
    public List<BusyTimeSlot> getTraineeScheduleView(UUID traineeId, UUID coachId, Instant startTime, Instant endTime) {
        log.info("Getting busy time slots for trainee {} and coach {}", traineeId, coachId);

        // Validate trainee exists
        if (!traineeRepository.existsById(traineeId)) {
            throw new ResourceNotFoundException("Trainee", "id", traineeId);
        }

        // Validate coach exists
        if (!coachRepository.existsById(coachId)) {
            throw new ResourceNotFoundException("Coach", "id", coachId);
        }

        List<BusyTimeSlot> allBusySlots = new ArrayList<>();

        // 1. Get coach's time offs
        List<CoachTimeOff> timeOffs;
        if (startTime != null && endTime != null) {
            timeOffs = coachTimeOffRepository.findByCoachIdAndTimeRange(coachId, startTime, endTime);
        } else {
            timeOffs = coachTimeOffRepository.findByCoach_CoachId(coachId);
        }

        // Add time offs to busy slots
        for (CoachTimeOff timeOff : timeOffs) {
            allBusySlots.add(new BusyTimeSlot(timeOff.getStartTime(), timeOff.getEndTime()));
        }

        // 2. Get all active bookings for this coach (from all trainees)
        List<BookingStatus> activeBusyStatuses = Arrays.asList(
                BookingStatus.SCHEDULED,
                BookingStatus.READY,
                BookingStatus.IN_PROGRESS,
                BookingStatus.COMPLETED,
                BookingStatus.NO_SHOW_BY_TRAINEE);

        List<CoachBooking> coachBookings;
        if (startTime != null && endTime != null) {
            coachBookings = coachBookingRepository.findByCoachIdAndTimeRange(coachId, startTime, endTime);
        } else {
            coachBookings = coachBookingRepository.findByCoach_CoachId(coachId);
        }

        // Filter and add coach's active bookings
        coachBookings.stream()
                .filter(booking -> activeBusyStatuses.contains(booking.getStatus()))
                .forEach(booking -> allBusySlots.add(
                        new BusyTimeSlot(booking.getStartTime(), booking.getEndTime())));

        // 3. Get trainee's own bookings with this coach (including NO_SHOW_BY_COACH to
        // show trainee)
        List<BookingStatus> traineeActiveStatuses = Arrays.asList(
                BookingStatus.SCHEDULED,
                BookingStatus.READY,
                BookingStatus.IN_PROGRESS,
                BookingStatus.COMPLETED,
                BookingStatus.NO_SHOW_BY_TRAINEE,
                BookingStatus.NO_SHOW_BY_COACH);

        List<CoachBooking> traineeBookings;
        if (startTime != null && endTime != null) {
            traineeBookings = coachBookingRepository.findByTraineeIdAndTimeRange(traineeId, startTime, endTime);
        } else {
            traineeBookings = coachBookingRepository.findByTrainee_TraineeId(traineeId);
        }

        // Add trainee's bookings (that match the coach)
        traineeBookings.stream()
                .filter(booking -> booking.getCoach().getCoachId().equals(coachId))
                .filter(booking -> traineeActiveStatuses.contains(booking.getStatus()))
                .forEach(booking -> allBusySlots.add(
                        new BusyTimeSlot(booking.getStartTime(), booking.getEndTime())));

        // Remove duplicates (same time slot might appear in both coach and trainee
        // bookings)
        List<BusyTimeSlot> uniqueSlots = allBusySlots.stream()
                .distinct()
                .sorted(Comparator.comparing(BusyTimeSlot::startTime))
                .collect(Collectors.toList());

        log.info("Found {} unique busy time slots", uniqueSlots.size());

        return uniqueSlots;
    }

    // Helper method to build coach busy schedule without circular dependency
    private List<CoachBusyScheduleDto> buildCoachBusySchedule(UUID coachId, Instant startTime, Instant endTime) {
        List<CoachBusyScheduleDto> busySchedules = new ArrayList<>();

        // Get time offs
        List<CoachTimeOff> timeOffs;
        if (startTime != null && endTime != null) {
            timeOffs = coachTimeOffRepository.findByCoachIdAndTimeRange(coachId, startTime, endTime);
        } else {
            timeOffs = coachTimeOffRepository.findByCoach_CoachId(coachId);
        }

        // Convert time offs to busy schedule items
        for (CoachTimeOff timeOff : timeOffs) {
            busySchedules.add(new CoachBusyScheduleDto(
                    timeOff.getId(),
                    BusyScheduleType.TIME_OFF,
                    timeOff.getStartTime(),
                    timeOff.getEndTime(),
                    "Time Off",
                    timeOff.getReason() != null ? timeOff.getReason() : "No reason provided"));
        }

        // Get active bookings (exclude cancelled and refunded)
        List<BookingStatus> activeBusyStatuses = Arrays.asList(
                BookingStatus.SCHEDULED,
                BookingStatus.READY,
                BookingStatus.IN_PROGRESS,
                BookingStatus.COMPLETED,
                BookingStatus.NO_SHOW_BY_TRAINEE);

        List<CoachBooking> bookings;
        if (startTime != null && endTime != null) {
            bookings = coachBookingRepository.findByCoachIdAndTimeRange(coachId, startTime, endTime);
        } else {
            bookings = coachBookingRepository.findByCoach_CoachId(coachId);
        }

        // Filter bookings by active status
        bookings = bookings.stream()
                .filter(booking -> activeBusyStatuses.contains(booking.getStatus()))
                .collect(Collectors.toList());

        // Convert bookings to busy schedule items
        for (CoachBooking booking : bookings) {
            String traineeName = booking.getTrainee().getFullName();
            String title = "Training Session";
            String details = String.format("With %s - Status: %s", traineeName, booking.getStatus());

            busySchedules.add(new CoachBusyScheduleDto(
                    booking.getId(),
                    BusyScheduleType.BOOKING,
                    booking.getStartTime(),
                    booking.getEndTime(),
                    title,
                    details));
        }

        // Sort by start time
        busySchedules.sort(Comparator.comparing(CoachBusyScheduleDto::startTime));

        return busySchedules;
    }

    private void createTransactionRecord(UUID traineeId, BigDecimal amount, UUID bookingId) {
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.BOOKING_COACH)
                .amount(amount)
                .accountId(traineeId)
                .referenceId(bookingId)
                .description("Payment for coach booking")
                .build();

        transactionRepository.save(transaction);
    }
}
