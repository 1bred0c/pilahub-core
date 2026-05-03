package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Gross transaction amount for a specific month")
public record AdminMonthlyGrossResponse(
        @Schema(description = "Month number in the current year", example = "1") int month,

        @Schema(description = "Total gross transaction amount for this month", example = "15200000.00") BigDecimal totalGross) {
}