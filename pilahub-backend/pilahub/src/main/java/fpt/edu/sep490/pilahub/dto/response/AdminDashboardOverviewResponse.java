package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Admin dashboard overview metrics")
public record AdminDashboardOverviewResponse(
                @Schema(description = "Total number of trainees", example = "120") Long totalTrainees,

                @Schema(description = "Total number of vendors", example = "35") Long totalVendors,

                @Schema(description = "Total number of coaches", example = "54") Long totalCoaches,

                @Schema(description = "Number of transactions created today", example = "18") Long transactionsToday,

                @Schema(description = "Total gross transaction amount of current month", example = "15200000.00") BigDecimal totalGrossMonthly,

                @Schema(description = "Monthly gross transaction amounts of the current year (January to December)") List<AdminMonthlyGrossResponse> grossMonthlyOfYear,

                @Schema(description = "List of coaches sorted by average rating in descending order") List<AdminCoachRatingResponse> coachesByAvgRating) {
}
