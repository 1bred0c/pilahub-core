package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.TraineeCourseDto;
import fpt.edu.sep490.pilahub.enums.NotificationType;
import fpt.edu.sep490.pilahub.event.NotificationEvent;
import fpt.edu.sep490.pilahub.dto.request.course.CreateTraineeCourseRequest;
import fpt.edu.sep490.pilahub.enums.TransactionType;
import fpt.edu.sep490.pilahub.exception.InsufficientBalanceException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.TraineeCourseMapper;
import fpt.edu.sep490.pilahub.pojo.Course;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.pojo.TraineeCourse;
import fpt.edu.sep490.pilahub.pojo.Transaction;
import fpt.edu.sep490.pilahub.pojo.Wallet;
import fpt.edu.sep490.pilahub.repository.CourseRepository;
import fpt.edu.sep490.pilahub.repository.TraineeCourseRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.repository.TransactionRepository;
import fpt.edu.sep490.pilahub.repository.WalletRepository;
import fpt.edu.sep490.pilahub.service.TraineeCourseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TraineeCourseServiceImpl implements TraineeCourseService {

    private final TraineeCourseRepository traineeCourseRepository;
    private final TraineeRepository traineeRepository;
    private final CourseRepository courseRepository;
    private final TraineeCourseMapper traineeCourseMapper;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public TraineeCourseDto enrollCourse(CreateTraineeCourseRequest request) {
        log.info("Processing course enrollment for trainee ID: {} to course ID: {}", request.traineeId(),
                request.courseId());

        // Validate trainee exists
        Trainee trainee = traineeRepository.findById(request.traineeId())
                .orElseThrow(() -> {
                    log.error("Trainee not found with ID: {}", request.traineeId());
                    return new ResourceNotFoundException("Trainee", "id", request.traineeId());
                });

        // Validate course exists
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", request.courseId());
                    return new ResourceNotFoundException("Course", "id", request.courseId());
                });

        // Check if already enrolled
        if (traineeCourseRepository.existsByTrainee_TraineeIdAndCourse_CourseId(
                request.traineeId(), request.courseId())) {
            log.error("Trainee ID: {} is already enrolled in course ID: {}", request.traineeId(), request.courseId());
            throw new IllegalStateException("Trainee is already enrolled in this course");
        }

        // Calculate course amount
        BigDecimal courseAmount = course.getPrice() != null ? BigDecimal.valueOf(course.getPrice()) : BigDecimal.ZERO;

        // Get wallet and check balance if course has a price
        if (courseAmount.compareTo(BigDecimal.ZERO) > 0) {
            Wallet wallet = walletRepository.findByAccountId(trainee.getTraineeId())
                    .orElseThrow(() -> {
                        log.error("Wallet not found for account ID: {}", trainee.getTraineeId());
                        return new ResourceNotFoundException("Wallet", "accountId", trainee.getTraineeId());
                    });

            // Check available balance
            if (wallet.getAvailableVND().compareTo(courseAmount) < 0) {
                log.error("Insufficient balance for account ID: {}. Required: {}, Available: {}",
                        trainee.getTraineeId(), courseAmount, wallet.getAvailableVND());
                throw new InsufficientBalanceException("Insufficient balance to enroll in this course");
            }

            // Deduct balance
            wallet.setAvailableVND(wallet.getAvailableVND().subtract(courseAmount));
            wallet.setBalanceVND(wallet.getBalanceVND().subtract(courseAmount));
            walletRepository.save(wallet);
            log.info("Deducted {} VND from wallet for account ID: {}", courseAmount, trainee.getTraineeId());
        }

        // Create new enrollment
        TraineeCourse traineeCourse = TraineeCourse.builder()
                .trainee(trainee)
                .course(course)
                .enrolledAt(Instant.now())
                .active(true)
                .progressPercentage(0)
                .build();

        TraineeCourse saved = traineeCourseRepository.save(traineeCourse);
        log.info("Course enrollment created successfully with ID: {}", saved.getTraineeCourseId());

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.COURSE)
                .amount(courseAmount)
                .accountId(trainee.getTraineeId()) // Trainee ID is same as Account ID
                .referenceId(saved.getTraineeCourseId())
                .description("Enrolled in course: " + course.getName())
                .build();

        transactionRepository.save(transaction);
        log.info("Transaction created for course enrollment ID: {}", saved.getTraineeCourseId());

        eventPublisher.publishEvent(new NotificationEvent(
                this,
                trainee.getTraineeId(),
                NotificationType.COURSE_ENROLLED,
                "Đã Đăng Ký Khóa Học",
                "Bạn đã đăng ký thành công khóa \"" + course.getName() + "\". Bắt đầu hành trình học tập của bạn nhé!",
                saved.getTraineeCourseId(), "TRAINEE_COURSE"));

        return traineeCourseMapper.toDto(saved);
    }

    @Override
    public TraineeCourseDto getById(UUID traineeCourseId) {
        TraineeCourse traineeCourse = traineeCourseRepository.findById(traineeCourseId)
                .orElseThrow(() -> new ResourceNotFoundException("TraineeCourse", "id", traineeCourseId));
        return traineeCourseMapper.toDto(traineeCourse);
    }

    @Override
    public List<TraineeCourseDto> getByTraineeId(UUID traineeId) {
        // Validate trainee exists
        if (!traineeRepository.existsById(traineeId)) {
            throw new ResourceNotFoundException("Trainee", "id", traineeId);
        }

        return traineeCourseRepository.findByTrainee_TraineeId(traineeId).stream()
                .map(traineeCourseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TraineeCourseDto updateProgress(UUID traineeCourseId, Integer progressPercentage) {
        TraineeCourse traineeCourse = traineeCourseRepository.findById(traineeCourseId)
                .orElseThrow(() -> new ResourceNotFoundException("TraineeCourse", "id", traineeCourseId));

        if (progressPercentage < 0 || progressPercentage > 100) {
            throw new IllegalArgumentException("Progress percentage must be between 0 and 100");
        }

        traineeCourse.setProgressPercentage(progressPercentage);

        TraineeCourse updated = traineeCourseRepository.save(traineeCourse);
        return traineeCourseMapper.toDto(updated);
    }

    @Override
    public TraineeCourseDto activateTraineeCourse(UUID traineeCourseId) {
        TraineeCourse traineeCourse = traineeCourseRepository.findById(traineeCourseId)
                .orElseThrow(() -> new ResourceNotFoundException("TraineeCourse", "id", traineeCourseId));

        traineeCourse.activate();

        TraineeCourse activated = traineeCourseRepository.save(traineeCourse);
        return traineeCourseMapper.toDto(activated);
    }

    @Override
    public void deleteTraineeCourse(UUID traineeCourseId) {
        if (!traineeCourseRepository.existsById(traineeCourseId)) {
            throw new ResourceNotFoundException("TraineeCourse", "id", traineeCourseId);
        }
        traineeCourseRepository.deleteById(traineeCourseId);
    }

    @Override
    public boolean isEnrolled(UUID traineeId, UUID courseId) {
        return traineeCourseRepository.existsByTrainee_TraineeIdAndCourse_CourseId(traineeId, courseId);
    }
}
