package pilahub.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import pilahub.enums.MetricSource;
import pilahub.enums.WorkoutFrequency;
import pilahub.enums.WorkoutLevel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class HealthProfileRequest {

    @NotNull(message = "Age must not be null")
    @Min(value = 10, message = "Age must be at least 10")
    @Max(value = 120, message = "Age must not exceed 120")
    private Integer age;

    @NotBlank(message = "Gender must not be blank")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;

    @NotNull(message = "Workout level must not be null")
    private WorkoutLevel workoutLevel;

    @NotNull(message = "Workout frequency must not be null")
    private WorkoutFrequency workoutFrequency;

    @Valid
    private List<InjuryDTO> injuries;

    @NotNull(message = "Height must not be null")
    @DecimalMin(value = "50.0", message = "Height must be at least 50 cm")
    @DecimalMax(value = "300.0", message = "Height must not exceed 300 cm")
    private BigDecimal heightCm;

    @NotNull(message = "Weight must not be null")
    @DecimalMin(value = "20.0", message = "Weight must be at least 20 kg")
    @DecimalMax(value = "500.0", message = "Weight must not exceed 500 kg")
    private BigDecimal weightKg;

    @DecimalMin(value = "10.0", message = "BMI must be at least 10")
    @DecimalMax(value = "60.0", message = "BMI must not exceed 60")
    private BigDecimal bmi;

    @DecimalMin(value = "0.0", message = "Body fat percentage must not be negative")
    @DecimalMax(value = "100.0", message = "Body fat percentage must not exceed 100")
    private BigDecimal bodyFatPercentage;

    @DecimalMin(value = "0.0", message = "Muscle mass must not be negative")
    private BigDecimal muscleMassKg;

    @DecimalMin(value = "0.0", message = "Waist must not be negative")
    private BigDecimal waistCm;

    @DecimalMin(value = "0.0", message = "Hip must not be negative")
    private BigDecimal hipCm;

    @NotNull(message = "Source must not be null")
    private MetricSource source;

    private Map<String, Object> metaData;
}
