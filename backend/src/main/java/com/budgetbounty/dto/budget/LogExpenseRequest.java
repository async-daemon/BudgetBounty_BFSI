package com.budgetbounty.dto.budget;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogExpenseRequest {
    @Positive
    private Double amount;

    private String description;
}
