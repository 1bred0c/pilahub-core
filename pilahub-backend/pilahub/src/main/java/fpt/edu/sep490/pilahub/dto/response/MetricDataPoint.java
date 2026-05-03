package fpt.edu.sep490.pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "A single data point for a metric over time")
public record MetricDataPoint(
        @Schema(description = "Date of the measurement", example = "2026-02-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @Schema(description = "Value of the metric", example = "82.5")
        BigDecimal value
) {
}

