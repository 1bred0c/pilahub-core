package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.RuleAction;
import fpt.edu.sep490.pilahub.enums.RuleOperator;
import fpt.edu.sep490.pilahub.enums.RuleSeverity;
import fpt.edu.sep490.pilahub.enums.RuleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ingredient_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientRule {

    @Id
    @GeneratedValue
    @Column(name = "ingredient_rule_id", nullable = false, updatable = false)
    private UUID ingredientRuleId;

    @NotNull(message = "Ingredient must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @NotNull(message = "Rule type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 50)
    private RuleType ruleType;

    @NotBlank(message = "Rule description must not be blank")
    @Size(max = 1000)
    @Column(name = "rule_description", nullable = false, length = 1000)
    private String ruleDescription;

    @NotNull(message = "Operator must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false, length = 50)
    private RuleOperator operator;

    @Size(max = 500)
    @Column(name = "value", length = 500)
    private String value;

    @NotNull(message = "Severity must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 50)
    private RuleSeverity severity;

    @NotNull(message = "Action must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private RuleAction action;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
