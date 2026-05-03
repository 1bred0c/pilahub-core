package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Collection of health metrics over time")
public record HealthMetrics(
        @Schema(description = "Weight measurements over time (kg)")
        List<MetricDataPoint> weightKg,

        @Schema(description = "BMI measurements over time")
        List<MetricDataPoint> bmi,

        @Schema(description = "Body fat percentage measurements over time")
        List<MetricDataPoint> bodyFatPercentage,

        @Schema(description = "Muscle mass measurements over time (kg)")
        List<MetricDataPoint> muscleMassKg,

        @Schema(description = "Waist measurements over time (cm)")
        List<MetricDataPoint> waistCm,

        @Schema(description = "Hip measurements over time (cm)")
        List<MetricDataPoint> hipCm
) {
}

