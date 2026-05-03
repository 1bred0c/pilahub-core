package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.SubscriptionDto;
import fpt.edu.sep490.pilahub.dto.request.SubscribePackageRequest;
import fpt.edu.sep490.pilahub.dto.request.UpgradePackageRequest;
import fpt.edu.sep490.pilahub.dto.response.UpgradePackageResponse;
import fpt.edu.sep490.pilahub.dto.response.UpgradeablePackageDto;
import fpt.edu.sep490.pilahub.enums.SubscriptionStatus;
import fpt.edu.sep490.pilahub.enums.TransactionType;
import fpt.edu.sep490.pilahub.exception.InsufficientBalanceException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.PackageMapper;
import fpt.edu.sep490.pilahub.mapper.SubscriptionMapper;
import fpt.edu.sep490.pilahub.pojo.*;
import fpt.edu.sep490.pilahub.pojo.Package;
import fpt.edu.sep490.pilahub.repository.*;
import fpt.edu.sep490.pilahub.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final AccountRepository accountRepository;
    private final TraineeRepository traineeRepository;
    private final PackageRepository packageRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final PackageMapper packageMapper;

    @Override
    public SubscriptionDto subscribePackage(UUID accountId, SubscribePackageRequest request) {
        log.info("Processing subscription for account ID: {} to package ID: {}", accountId, request.packageId());

        // Validate account exists
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Account not found with ID: {}", accountId);
                    return new ResourceNotFoundException("Account", "id", accountId);
                });

        // Get trainee
        Trainee trainee = traineeRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Trainee not found for account ID: {}", accountId);
                    return new ResourceNotFoundException("Trainee", "accountId", accountId);
                });

        // Validate package exists and is active
        Package pkg = packageRepository.findById(request.packageId())
                .orElseThrow(() -> {
                    log.error("Package not found with ID: {}", request.packageId());
                    return new ResourceNotFoundException("Package", "id", request.packageId());
                });

        if (!pkg.getIsActive()) {
            log.error("Package is not active: {}", request.packageId());
            throw new IllegalArgumentException("Package is not active");
        }

        // Check if user already has an active subscription
        Optional<Subscription> existingActive = subscriptionRepository.findActiveSubscriptionByTraineeId(trainee.getTraineeId());
        if (existingActive.isPresent()) {
            log.error("Trainee already has an active subscription: {}", trainee.getTraineeId());
            throw new IllegalArgumentException("You already have an active subscription. Please upgrade instead.");
        }

        // Get wallet
        Wallet wallet = walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> {
                    log.error("Wallet not found for account ID: {}", accountId);
                    return new ResourceNotFoundException("Wallet", "accountId", accountId);
                });

        // Check available balance
        if (wallet.getAvailableVND().compareTo(pkg.getPrice()) < 0) {
            log.error("Insufficient balance for account ID: {}. Required: {}, Available: {}",
                    accountId, pkg.getPrice(), wallet.getAvailableVND());
            throw new InsufficientBalanceException("Insufficient balance to subscribe to this package");
        }

        // Deduct balance
        wallet.setAvailableVND(wallet.getAvailableVND().subtract(pkg.getPrice()));
        wallet.setBalanceVND(wallet.getBalanceVND().subtract(pkg.getPrice()));
        walletRepository.save(wallet);
        log.info("Deducted {} VND from wallet for account ID: {}", pkg.getPrice(), accountId);

        // Create subscription
        Instant startDate = Instant.now();
        Instant endDate = startDate.plus(pkg.getDurationInDays(), ChronoUnit.DAYS);

        Subscription subscription = Subscription.builder()
                .trainee(trainee)
                .subscribedPackage(pkg)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription created successfully with ID: {}", savedSubscription.getSubscriptionId());

        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.SUBSCRIPTION_PACKAGE)
                .amount(pkg.getPrice())
                .accountId(accountId)
                .referenceId(savedSubscription.getSubscriptionId())
                .description("Subscribed to package: " + pkg.getPackageName())
                .build();

        transactionRepository.save(transaction);
        log.info("Transaction created for subscription ID: {}", savedSubscription.getSubscriptionId());

        return subscriptionMapper.toDto(savedSubscription);
    }

    @Override
    public UpgradePackageResponse upgradePackage(UUID accountId, UpgradePackageRequest request) {
        log.info("Processing package upgrade for account ID: {} to new package ID: {}", accountId, request.newPackageId());

        // Validate account exists
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Account not found with ID: {}", accountId);
                    return new ResourceNotFoundException("Account", "id", accountId);
                });

        // Get trainee
        Trainee trainee = traineeRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Trainee not found for account ID: {}", accountId);
                    return new ResourceNotFoundException("Trainee", "accountId", accountId);
                });

        // Get current active subscription
        Subscription currentSubscription = subscriptionRepository.findActiveSubscriptionByTraineeId(trainee.getTraineeId())
                .orElseThrow(() -> {
                    log.error("No active subscription found for trainee ID: {}", trainee.getTraineeId());
                    return new IllegalArgumentException("You don't have an active subscription to upgrade");
                });

        // Validate new package exists and is active
        Package newPackage = packageRepository.findById(request.newPackageId())
                .orElseThrow(() -> {
                    log.error("Package not found with ID: {}", request.newPackageId());
                    return new ResourceNotFoundException("Package", "id", request.newPackageId());
                });

        if (!newPackage.getIsActive()) {
            log.error("New package is not active: {}", request.newPackageId());
            throw new IllegalArgumentException("Package is not active");
        }

        // Validate new package price is greater than or equal to current package price
        Package currentPackage = currentSubscription.getSubscribedPackage();
        if (newPackage.getPrice().compareTo(currentPackage.getPrice()) < 0) {
            log.error("Cannot upgrade to a cheaper package. Current: {}, New: {}",
                    currentPackage.getPrice(), newPackage.getPrice());
            throw new IllegalArgumentException("Cannot upgrade to a package with lower price than current package");
        }

        // Calculate proration refund
        Instant now = Instant.now();
        long totalDays = currentPackage.getDurationInDays();
        long daysRemaining = Duration.between(now, currentSubscription.getEndDate()).toDays();

        if (daysRemaining < 0) {
            daysRemaining = 0;
        }

        // Calculate refund amount: (remaining days / total days) * current package price
        BigDecimal refundAmount = currentPackage.getPrice()
                .multiply(BigDecimal.valueOf(daysRemaining))
                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);

        log.info("Calculated proration refund: {} VND for {} days remaining out of {} total days",
                refundAmount, daysRemaining, totalDays);

        // Get wallet
        Wallet wallet = walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> {
                    log.error("Wallet not found for account ID: {}", accountId);
                    return new ResourceNotFoundException("Wallet", "accountId", accountId);
                });

        // Calculate final price after applying refund
        // User only needs to pay: New Package Price - Refund Amount
        BigDecimal finalPrice = newPackage.getPrice().subtract(refundAmount);

        // Check if wallet has enough balance for the upgrade
        if (wallet.getAvailableVND().compareTo(finalPrice) < 0) {
            log.error("Insufficient balance for account ID: {}. Required: {}, Available: {}",
                    accountId, finalPrice, wallet.getAvailableVND());
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance to upgrade. Need %s VND but only have %s VND available",
                            finalPrice, wallet.getAvailableVND())
            );
        }

        // Deduct final price from wallet
        wallet.setAvailableVND(wallet.getAvailableVND().subtract(finalPrice));
        wallet.setBalanceVND(wallet.getBalanceVND().subtract(finalPrice));
        walletRepository.save(wallet);
        log.info("Deducted {} VND from wallet for account ID: {}. Refund {} VND was applied to reduce the price.",
                finalPrice, accountId, refundAmount);

        // Create refund transaction for record keeping (NOT actual money transfer)
        Transaction refundTransaction = Transaction.builder()
                .transactionType(TransactionType.SUBSCRIPTION_PRORATION_REFUND)
                .amount(refundAmount)
                .accountId(accountId)
                .referenceId(currentSubscription.getSubscriptionId())
                .description("Proration credit for " + daysRemaining + " days remaining of package: " + currentPackage.getPackageName())
                .build();

        transactionRepository.save(refundTransaction);
        log.info("Refund transaction created for record keeping. Old subscription ID: {}", currentSubscription.getSubscriptionId());

        // Update current subscription to UPGRADED and flush to database immediately
        // This is CRITICAL to satisfy the unique constraint before creating new ACTIVE subscription
        currentSubscription.setStatus(SubscriptionStatus.UPGRADED);
        subscriptionRepository.saveAndFlush(currentSubscription);
        log.info("Updated old subscription ID: {} to UPGRADED status", currentSubscription.getSubscriptionId());

        // Create new subscription
        Instant startDate = Instant.now();
        Instant endDate = startDate.plus(newPackage.getDurationInDays(), ChronoUnit.DAYS);

        Subscription newSubscription = Subscription.builder()
                .trainee(trainee)
                .subscribedPackage(newPackage)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        Subscription savedNewSubscription = subscriptionRepository.save(newSubscription);
        log.info("New subscription created successfully with ID: {}", savedNewSubscription.getSubscriptionId());

        // Create upgrade transaction
        Transaction upgradeTransaction = Transaction.builder()
                .transactionType(TransactionType.SUBSCRIPTION_UPGRADE)
                .amount(newPackage.getPrice())
                .accountId(accountId)
                .referenceId(savedNewSubscription.getSubscriptionId())
                .description("Upgraded to package: " + newPackage.getPackageName() + " (Final price after refund: " + finalPrice + " VND)")
                .build();

        transactionRepository.save(upgradeTransaction);
        log.info("Upgrade transaction created for new subscription ID: {}", savedNewSubscription.getSubscriptionId());

        // Build detailed response
        UpgradePackageResponse response = new UpgradePackageResponse(
                subscriptionMapper.toDto(savedNewSubscription),
                refundAmount,
                daysRemaining,
                newPackage.getPrice(),
                finalPrice,
                String.format("Successfully upgraded from %s to %s. Refunded %s VND for %d remaining days. Final price: %s VND",
                        currentPackage.getPackageName(),
                        newPackage.getPackageName(),
                        refundAmount,
                        daysRemaining,
                        finalPrice)
        );

        return response;
    }

    @Override
    public SubscriptionDto getSubscriptionById(UUID subscriptionId) {
        log.info("Fetching subscription with ID: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> {
                    log.error("Subscription not found with ID: {}", subscriptionId);
                    return new ResourceNotFoundException("Subscription", "id", subscriptionId);
                });

        return subscriptionMapper.toDto(subscription);
    }

    @Override
    public List<SubscriptionDto> getSubscriptionsByAccountId(UUID accountId) {
        log.info("Fetching subscriptions for account ID: {}", accountId);

        // Validate account exists and get trainee
        Trainee trainee = traineeRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Trainee not found for account ID: {}", accountId);
                    return new ResourceNotFoundException("Trainee", "accountId", accountId);
                });

        List<Subscription> subscriptions = subscriptionRepository.findByTrainee_TraineeId(trainee.getTraineeId());
        log.info("Found {} subscription(s) for trainee ID: {}", subscriptions.size(), trainee.getTraineeId());

        return subscriptions.stream()
                .map(subscriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionDto getActiveSubscriptionByAccountId(UUID accountId) {
        log.info("Fetching active subscription for account ID: {}", accountId);

        // Validate account exists and get trainee
        Trainee trainee = traineeRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Trainee not found for account ID: {}", accountId);
                    return new ResourceNotFoundException("Trainee", "accountId", accountId);
                });

        Subscription subscription = subscriptionRepository.findActiveSubscriptionByTraineeId(trainee.getTraineeId())
                .orElseThrow(() -> {
                    log.error("No active subscription found for trainee ID: {}", trainee.getTraineeId());
                    return new ResourceNotFoundException("Active Subscription", "traineeId", trainee.getTraineeId());
                });

        return subscriptionMapper.toDto(subscription);
    }

    @Override
    public Page<SubscriptionDto> getAllSubscriptions(Pageable pageable) {
        log.info("Fetching all subscriptions with pagination - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Subscription> subscriptionPage = subscriptionRepository.findAll(pageable);
        log.info("Found {} total subscriptions, returning page {} with {} elements",
                subscriptionPage.getTotalElements(),
                subscriptionPage.getNumber(),
                subscriptionPage.getNumberOfElements());

        return subscriptionPage.map(subscriptionMapper::toDto);
    }

    @Override
    public List<SubscriptionDto> getSubscriptionsByStatus(String status) {
        log.info("Fetching subscriptions with status: {}", status);

        SubscriptionStatus subscriptionStatus;
        try {
            subscriptionStatus = SubscriptionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid subscription status: {}", status);
            throw new IllegalArgumentException("Invalid subscription status: " + status);
        }

        List<Subscription> subscriptions = subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == subscriptionStatus)
                .toList();

        log.info("Found {} subscription(s) with status: {}", subscriptions.size(), status);

        return subscriptions.stream()
                .map(subscriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void expireSubscriptions() {
        log.info("Running scheduled task to expire subscriptions");

        Instant now = Instant.now();
        List<Subscription> expiredSubscriptions = subscriptionRepository.findExpiredActiveSubscriptions(now);

        if (expiredSubscriptions.isEmpty()) {
            log.info("No subscriptions to expire");
            return;
        }

        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            log.info("Expired subscription ID: {} for trainee ID: {}",
                    subscription.getSubscriptionId(),
                    subscription.getTrainee().getTraineeId());
        }

        log.info("Successfully expired {} subscription(s)", expiredSubscriptions.size());
    }

    @Override
    public SubscriptionDto cancelSubscription(UUID subscriptionId) {
        log.info("Cancelling subscription with ID: {} by admin", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> {
                    log.error("Subscription not found with ID: {}", subscriptionId);
                    return new ResourceNotFoundException("Subscription", "id", subscriptionId);
                });

        // Only ACTIVE subscriptions can be cancelled
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            log.error("Cannot cancel subscription with status: {}", subscription.getStatus());
            throw new IllegalArgumentException("Can only cancel ACTIVE subscriptions. Current status: " + subscription.getStatus());
        }

        // Change status to EXPIRED
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription ID: {} has been cancelled by admin for trainee ID: {}",
                subscriptionId, subscription.getTrainee().getTraineeId());

        return subscriptionMapper.toDto(updatedSubscription);
    }

    @Override
    public List<UpgradeablePackageDto> getUpgradeablePackages(UUID accountId) {
        log.info("Fetching upgradeable packages for account ID: {}", accountId);

        // Get trainee
        Trainee trainee = traineeRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Trainee not found for account ID: {}", accountId);
                    return new ResourceNotFoundException("Trainee", "accountId", accountId);
                });

        // Get current active subscription
        Subscription currentSubscription = subscriptionRepository.findActiveSubscriptionByTraineeId(trainee.getTraineeId())
                .orElseThrow(() -> {
                    log.error("No active subscription found for trainee ID: {}", trainee.getTraineeId());
                    return new IllegalArgumentException("You don't have an active subscription. Please subscribe first.");
                });

        Package currentPackage = currentSubscription.getSubscribedPackage();

        // Calculate proration credit
        Instant now = Instant.now();
        long totalDays = currentPackage.getDurationInDays();
        long daysRemaining = Duration.between(now, currentSubscription.getEndDate()).toDays();

        if (daysRemaining < 0) {
            daysRemaining = 0;
        }

        // Create final copy for use in lambda
        final long finalDaysRemaining = daysRemaining;

        // Calculate refund amount (proration credit)
        BigDecimal prorationCredit = currentPackage.getPrice()
                .multiply(BigDecimal.valueOf(finalDaysRemaining))
                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);

        log.info("Calculated proration credit: {} VND for {} days remaining out of {} total days",
                prorationCredit, finalDaysRemaining, totalDays);

        // Get all active packages
        List<Package> allActivePackages = packageRepository.findByIsActiveTrue();

        // Filter packages that can be upgraded to (price >= current price) and calculate final price
        List<UpgradeablePackageDto> upgradeablePackages = allActivePackages.stream()
                .filter(pkg -> pkg.getPrice().compareTo(currentPackage.getPrice()) >= 0)
                .filter(pkg -> !pkg.getPackageId().equals(currentPackage.getPackageId())) // Exclude current package
                .map(pkg -> {
                    BigDecimal finalPrice = pkg.getPrice().subtract(prorationCredit);

                    // Ensure final price is not negative
                    if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                        finalPrice = BigDecimal.ZERO;
                    }

                    // Calculate discount percentage
                    double discountPercentage = 0.0;
                    if (pkg.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                        discountPercentage = prorationCredit.divide(pkg.getPrice(), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue();
                    }

                    String discountDescription = String.format(
                            "You have %d days remaining from your current package worth %s VND. " +
                                    "This credit will be applied to reduce the upgrade cost.",
                            finalDaysRemaining,
                            prorationCredit
                    );

                    return new UpgradeablePackageDto(
                            packageMapper.toDto(pkg),
                            pkg.getPrice(),
                            prorationCredit,
                            finalPrice,
                            discountPercentage,
                            discountDescription
                    );
                })
                .sorted((a, b) -> a.finalPrice().compareTo(b.finalPrice())) // Sort by final price ascending
                .collect(Collectors.toList());

        log.info("Found {} upgradeable package(s) for trainee ID: {}", upgradeablePackages.size(), trainee.getTraineeId());

        return upgradeablePackages;
    }
}
