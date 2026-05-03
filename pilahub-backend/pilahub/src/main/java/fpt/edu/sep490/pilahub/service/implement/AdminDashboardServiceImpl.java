package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.response.AdminCoachRatingResponse;
import fpt.edu.sep490.pilahub.dto.response.AdminDashboardOverviewResponse;
import fpt.edu.sep490.pilahub.dto.response.AdminMonthlyGrossResponse;
import fpt.edu.sep490.pilahub.pojo.Coach;
import fpt.edu.sep490.pilahub.repository.CoachRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.repository.TransactionRepository;
import fpt.edu.sep490.pilahub.repository.VendorRepository;
import fpt.edu.sep490.pilahub.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

        private final TraineeRepository traineeRepository;
        private final VendorRepository vendorRepository;
        private final CoachRepository coachRepository;
        private final TransactionRepository transactionRepository;

        @Override
        public AdminDashboardOverviewResponse getDashboardOverview() {
                Long totalTrainees = traineeRepository.count();
                Long totalVendors = vendorRepository.count();
                Long totalCoaches = coachRepository.count();

                ZoneId zoneId = ZoneId.systemDefault();
                LocalDate today = LocalDate.now(zoneId);

                Instant startOfToday = today.atStartOfDay(zoneId).toInstant();
                Instant startOfTomorrow = today.plusDays(1).atStartOfDay(zoneId).toInstant();
                Long transactionsToday = transactionRepository.countByTransactionDateBetween(startOfToday,
                                startOfTomorrow);

                LocalDate firstDayOfMonth = today.withDayOfMonth(1);
                Instant startOfMonth = firstDayOfMonth.atStartOfDay(zoneId).toInstant();
                Instant startOfNextMonth = firstDayOfMonth.plusMonths(1).atStartOfDay(zoneId).toInstant();
                BigDecimal totalGrossMonthly = transactionRepository.sumAmountByTransactionDateBetween(startOfMonth,
                                startOfNextMonth);

                LocalDate firstDayOfYear = today.withDayOfYear(1);
                List<AdminMonthlyGrossResponse> grossMonthlyOfYear = java.util.stream.IntStream.rangeClosed(1, 12)
                                .mapToObj(month -> {
                                        LocalDate monthStartDate = firstDayOfYear.withMonth(month).withDayOfMonth(1);
                                        Instant monthStart = monthStartDate.atStartOfDay(zoneId).toInstant();
                                        Instant nextMonthStart = monthStartDate.plusMonths(1).atStartOfDay(zoneId)
                                                        .toInstant();
                                        BigDecimal grossOfMonth = transactionRepository
                                                        .sumAmountByTransactionDateBetween(monthStart,
                                                                        nextMonthStart);
                                        return new AdminMonthlyGrossResponse(month, grossOfMonth);
                                })
                                .toList();

                List<AdminCoachRatingResponse> coachesByAvgRating = coachRepository.findAll().stream()
                                .sorted(Comparator.comparing(Coach::getAvgRating,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .map(coach -> new AdminCoachRatingResponse(coach.getFullName(), coach.getAvgRating()))
                                .toList();

                return new AdminDashboardOverviewResponse(
                                totalTrainees,
                                totalVendors,
                                totalCoaches,
                                transactionsToday,
                                totalGrossMonthly,
                                grossMonthlyOfYear,
                                coachesByAvgRating);
        }
}
