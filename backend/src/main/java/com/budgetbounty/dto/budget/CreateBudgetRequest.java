package com.budgetbounty.dto.budget;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBudgetRequest {
    @NotBlank
    private String category;

    @Positive
    private Double monthlyLimit;
}
