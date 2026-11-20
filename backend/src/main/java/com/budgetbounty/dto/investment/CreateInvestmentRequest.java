package com.budgetbounty.dto.investment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInvestmentRequest {
    @NotBlank
    private String name;

    private String assetType;

    @Positive
    private Double investedAmount;

    @Positive
    private Double currentValue;

    private Long goalId;
}
