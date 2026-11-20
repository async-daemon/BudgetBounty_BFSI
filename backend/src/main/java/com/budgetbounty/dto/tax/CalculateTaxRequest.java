package com.budgetbounty.dto.tax;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalculateTaxRequest {
    @NotNull
    @PositiveOrZero
    private Double annualIncome;

    @PositiveOrZero
    private Double deductions = 0.0;

    private String regime = "new";
}
